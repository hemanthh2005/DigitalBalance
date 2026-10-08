package com.digitalbalance.app.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.digitalbalance.app.data.local.entity.AppUsageEntity
import com.digitalbalance.app.data.local.entity.ProtectedAppEntity
import com.digitalbalance.app.data.local.entity.ReportEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndSavePdfReport(
        context: Context,
        report: ReportEntity,
        usageList: List<AppUsageEntity>,
        protectedApps: List<ProtectedAppEntity>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 Page (595 x 842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()
        val headerPaint = Paint()
        val boldPaint = Paint()

        // Configure Paints
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 20f
        titlePaint.color = Color.rgb(30, 136, 229) // Primary Blue

        headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        headerPaint.textSize = 14f
        headerPaint.color = Color.rgb(33, 33, 33)

        boldPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        boldPaint.textSize = 11f
        boldPaint.color = Color.BLACK

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.rgb(66, 66, 66)

        var y = 40f

        // Document Title Header
        canvas.drawText("DIGITALBALANCE", 40f, y, titlePaint)
        y += 20f
        canvas.drawText("Digital Wellbeing & Child Safety Academic Report", 40f, y, paint)
        y += 25f

        // Divider Line
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Report Metadata
        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(report.generatedAt))
        canvas.drawText("Report Date: ${report.reportDate}", 40f, y, boldPaint)
        canvas.drawText("Generated At: $timeStr", 300f, y, paint)
        y += 25f

        // Digital Wellbeing Score Card Section
        canvas.drawText("1. Digital Wellbeing & Risk Assessment", 40f, y, headerPaint)
        y += 18f
        canvas.drawText("Wellbeing Score: ${report.riskScore} / 100", 50f, y, boldPaint)
        canvas.drawText("Risk Classification: ${report.riskLevel}", 300f, y, boldPaint)
        y += 18f

        val totalHours = report.totalScreenTimeMs / (1000 * 60 * 60)
        val totalMins = (report.totalScreenTimeMs / (1000 * 60)) % 60
        val socialMins = report.socialMediaTimeMs / (1000 * 60)

        canvas.drawText("Total Screen Time: ${totalHours}h ${totalMins}m", 50f, y, paint)
        canvas.drawText("Social/Video Share: ${socialMins}m", 300f, y, paint)
        y += 18f
        canvas.drawText("Total App Open Sessions: ${report.totalSessions} opens", 50f, y, paint)
        y += 25f

        // Observed Behavioral Indicators Section
        canvas.drawText("2. Observed Behavioral Indicators", 40f, y, headerPaint)
        y += 18f
        val reasonsLines = report.riskReasons.split("\n")
        for (line in reasonsLines) {
            if (y > 780f) break
            canvas.drawText(line.trim(), 50f, y, paint)
            y += 16f
        }
        y += 10f

        // Tracked Applications Usage Breakdown Table
        canvas.drawText("3. Tracked Applications Usage Breakdown", 40f, y, headerPaint)
        y += 20f

        // Table Header
        canvas.drawText("Application Name", 50f, y, boldPaint)
        canvas.drawText("Category", 260f, y, boldPaint)
        canvas.drawText("Duration (Mins)", 420f, y, boldPaint)
        y += 6f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 16f

        val topApps = usageList.take(8)
        if (topApps.isEmpty()) {
            canvas.drawText("No application usage recorded for this date.", 50f, y, paint)
            y += 18f
        } else {
            for (app in topApps) {
                if (y > 780f) break
                val mins = app.foregroundTimeMs / (1000 * 60)
                canvas.drawText(app.appName, 50f, y, paint)
                canvas.drawText(app.category, 260f, y, paint)
                canvas.drawText("${mins}m (${app.sessionCount} opens)", 420f, y, paint)
                y += 16f
            }
        }
        y += 15f

        // Child Safety Status Section
        if (y < 780f) {
            canvas.drawText("4. Child Safety System Configuration", 40f, y, headerPaint)
            y += 18f
            canvas.drawText("Protected Applications Count: ${protectedApps.size}", 50f, y, paint)
            y += 16f
            val protectedNames = protectedApps.joinToString(", ") { it.appName }
            canvas.drawText("Protected Apps List: ${if (protectedNames.isBlank()) "None" else protectedNames}", 50f, y, paint)
            y += 25f
        }

        // Academic Footer Disclaimer
        canvas.drawLine(40f, 800f, 555f, 800f, linePaint)
        paint.textSize = 9f
        canvas.drawText("Academic Disclaimer: Behavioral digital-wellbeing risk estimation based on rule-based usage scoring. Not a clinical diagnosis.", 40f, 815f, paint)

        pdfDocument.finishPage(page)

        // Save PDF File
        val pdfFileName = "DigitalBalance_Report_${report.reportDate}.pdf"
        var outputFile: File? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Public Downloads via MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        pdfDocument.writeTo(os)
                    }
                }
            }

            // Also save to app's external files directory for immediate Intent previewing/sharing
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!docsDir.exists()) docsDir.mkdirs()
            outputFile = File(docsDir, pdfFileName)
            FileOutputStream(outputFile).use { fos ->
                pdfDocument.writeTo(fos)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return outputFile
    }

    fun openOrSharePdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share DigitalBalance Report PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
