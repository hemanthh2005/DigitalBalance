package com.digitalbalance.app.services.usage

import javax.inject.Inject
import javax.inject.Singleton

enum class AppCategory {
    SOCIAL,
    VIDEO,
    GAMING,
    PRODUCTIVITY,
    ENTERTAINMENT,
    COMMUNICATION,
    OTHER
}

@Singleton
class AppCategoryProvider @Inject constructor() {

    private val socialPackages = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.twitter.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.snapchat.android",
        "com.reddit.frontpage",
        "com.linkedin.android",
        "com.pinterest"
    )

    private val videoPackages = setOf(
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.amazon.avod.thirdpartyclient", // Prime Video
        "com.disney.disneyplus",
        "com.hbo.hbonow"
    )

    private val gamingPackages = setOf(
        "com.pubg.krmobile",
        "com.epicgames.fortnite",
        "com.mobile.legends",
        "com.mojang.minecraftpe",
        "com.roblox.client"
    )

    private val communicationPackages = setOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "com.discord",
        "com.google.android.talk" // Hangouts/Meet
    )

    private val productivityPackages = setOf(
        "com.google.android.apps.docs",
        "com.google.android.gm",
        "com.microsoft.office.word",
        "com.microsoft.office.excel",
        "com.slack"
    )

    fun getCategory(packageName: String): AppCategory {
        return when {
            socialPackages.contains(packageName) -> AppCategory.SOCIAL
            videoPackages.contains(packageName) -> AppCategory.VIDEO
            gamingPackages.contains(packageName) -> AppCategory.GAMING
            communicationPackages.contains(packageName) -> AppCategory.COMMUNICATION
            productivityPackages.contains(packageName) -> AppCategory.PRODUCTIVITY
            else -> AppCategory.OTHER
        }
    }

    fun isHighRiskCategory(category: AppCategory): Boolean {
        return category == AppCategory.SOCIAL || category == AppCategory.VIDEO || category == AppCategory.GAMING
    }

    fun isSocialMedia(packageName: String): Boolean {
        return getCategory(packageName) == AppCategory.SOCIAL
    }
}
