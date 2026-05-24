package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cloned_accounts")
data class ClonedAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val platform: String, // "Discord", "WhatsApp", "Telegram", "Facebook", "Reddit", "Custom"
    val loginNotes: String = "",
    val proxy: String = "",
    val banStatus: String = "ACTIVE", // "ACTIVE", "WARNING", "SHADOWBANNED", "BANNED"
    val banReason: String = "",
    val dailyActionLimit: Int = 50,
    val currentDailyActions: Int = 0,
    val lastUsedTimestamp: Long = 0L,
    val lastResetTimestamp: Long = 0L,
    val cooldownDurationMinutes: Int = 0,
    val cooldownEndTimestamp: Long = 0L
) {
    val isBanned: Boolean
        get() = banStatus == "BANNED"

    val isUnderCooldown: Boolean
        get() = cooldownEndTimestamp > System.currentTimeMillis()

    val cooldownMinutesRemaining: Int
        get() {
            val delta = cooldownEndTimestamp - System.currentTimeMillis()
            return if (delta > 0) (delta / 60000).toInt() + 1 else 0
        }

    val safetyPercentage: Int
        get() {
            when (banStatus) {
                "BANNED" -> return 5
                "SHADOWBANNED" -> return 25
                "WARNING" -> return 50
                "ACTIVE" -> {
                    val actionRatio = if (dailyActionLimit > 0) currentDailyActions.toDouble() / dailyActionLimit else 0.0
                    val penalty = (actionRatio * 40).toInt().coerceIn(0, 40)
                    return 100 - penalty
                }
                else -> return 100
            }
        }
}
