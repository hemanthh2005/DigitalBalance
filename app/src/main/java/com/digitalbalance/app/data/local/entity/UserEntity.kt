package com.digitalbalance.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val displayName: String = "",
    val isGuardian: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
