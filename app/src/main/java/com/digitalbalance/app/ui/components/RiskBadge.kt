package com.digitalbalance.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalbalance.app.domain.model.RiskLevel
import com.digitalbalance.app.ui.theme.RiskHighColor
import com.digitalbalance.app.ui.theme.RiskLowColor
import com.digitalbalance.app.ui.theme.RiskModerateColor

@Composable
fun RiskBadge(
    riskLevel: RiskLevel,
    score: Int,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, label) = when (riskLevel) {
        RiskLevel.LOW -> RiskLowColor to "LOW RISK ($score/100)"
        RiskLevel.MODERATE -> RiskModerateColor to "MODERATE RISK ($score/100)"
        RiskLevel.HIGH -> RiskHighColor to "HIGH RISK ($score/100)"
    }

    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
