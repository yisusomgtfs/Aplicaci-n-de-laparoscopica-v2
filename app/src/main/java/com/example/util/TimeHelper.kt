package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeHelper {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun getTodayDate(): String = dateFormat.format(Date())

    fun getCurrentTime(): String = timeFormat.format(Date())

    fun isHardwareClockValid(): Boolean {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return year >= 2026
    }

    /**
     * Calculates duration in minutes between HH:mm and HH:mm,
     * correctly handling midnight wraparound if surgery crosses midnight.
     */
    fun calculateMinutesBetween(startHHmm: String, endHHmm: String): Int {
        if (startHHmm.isBlank() || endHHmm.isBlank()) return 0
        try {
            val partsStart = startHHmm.split(":").map { it.trim().toInt() }
            val partsEnd = endHHmm.split(":").map { it.trim().toInt() }
            if (partsStart.size < 2 || partsEnd.size < 2) return 0

            val startMin = partsStart[0] * 60 + partsStart[1]
            var endMin = partsEnd[0] * 60 + partsEnd[1]

            if (endMin < startMin) {
                endMin += 1440 // crossed midnight
            }
            return (endMin - startMin).coerceAtLeast(0)
        } catch (_: Exception) {
            return 0
        }
    }

    fun formatDurationHms(seconds: Long): String {
        val s = seconds.coerceAtLeast(0)
        val hrs = s / 3600
        val mins = (s % 3600) / 60
        val secs = s % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    fun formatDurationHuman(seconds: Long): String {
        val s = seconds.coerceAtLeast(0)
        val hrs = s / 3600
        val mins = (s % 3600) / 60
        val secs = s % 60
        return if (hrs > 0) {
            "${hrs}h ${mins}m ${secs}s"
        } else if (mins > 0) {
            "${mins}m ${secs}s"
        } else {
            "${secs}s"
        }
    }

    fun formatMinutesHuman(totalMinutes: Double): String {
        val totalSecs = (totalMinutes * 60).toLong().coerceAtLeast(0)
        val hrs = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        return if (hrs > 0) {
            "${hrs}h ${mins}m"
        } else {
            "${mins} min"
        }
    }
}
