package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_ID_SURGICAL_ALERTS = "hgv_surgical_alerts"
    const val CHANNEL_ID_SURGICAL_ONGOING = "hgv_surgical_ongoing"

    const val NOTIFICATION_ID_ONGOING = 1001
    const val NOTIFICATION_ID_COMPLETED = 1002
    private const val NOTIFICATION_ID_ALERT_BASE = 2000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. High priority milestone alert channel (with sound and vibration)
            val alertChannel = NotificationChannel(
                CHANNEL_ID_SURGICAL_ALERTS,
                "Alertas de Tiempo Quirúrgico",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas cuando se cumplen tiempos o umbrales programados durante la cirugía activa"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 450, 200, 450, 200, 600)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // 2. Ongoing live chronometer channel (silent, persistent during surgery)
            val ongoingChannel = NotificationChannel(
                CHANNEL_ID_SURGICAL_ONGOING,
                "Monitoreo Quirúrgico en Vivo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra el cronómetro activo y datos del paciente en la barra de estado"
                enableVibration(false)
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(ongoingChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private fun getAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Updates or shows the persistent ongoing notification with real-time chronometer.
     */
    fun updateOngoingSurgeryNotification(
        context: Context,
        elapsedSeconds: Long,
        paciente: String,
        cirugiaRealizada: String,
        medicoTratante: String
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val durationFormatted = TimeHelper.formatDurationHms(elapsedSeconds)
        val patientText = paciente.ifBlank { "Paciente no especificado" }
        val procedureText = cirugiaRealizada.ifBlank { "Laparoscopia en proceso" }
        val doctorText = if (medicoTratante.isNotBlank()) " • $medicoTratante" else ""

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SURGICAL_ONGOING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏱️ Cirugía Activa: $durationFormatted")
            .setContentText("$patientText | $procedureText$doctorText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("⏱️ Tiempo en sala: $durationFormatted\n👤 Paciente: $patientText\n🩺 Cirugía: $procedureText$doctorText\n📍 Quirófano HGV Laparoscopia")
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(getAppPendingIntent(context))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ONGOING, notification)
        } catch (_: SecurityException) {
        }
    }

    /**
     * Triggers a milestone alert notification when a certain time threshold has passed (e.g., 30m, 45m, 60m).
     */
    fun triggerMilestoneAlert(
        context: Context,
        elapsedMinutes: Int,
        paciente: String,
        cirugiaRealizada: String,
        medicoTratante: String,
        isVibrationEnabled: Boolean = true
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val patientText = paciente.ifBlank { "Paciente" }
        val procedureText = cirugiaRealizada.ifBlank { "Procedimiento Quirúrgico" }
        val doctorText = if (medicoTratante.isNotBlank()) " ($medicoTratante)" else ""

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationId = NOTIFICATION_ID_ALERT_BASE + elapsedMinutes

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_SURGICAL_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ Alerta Quirúrgica: $elapsedMinutes min Cumplidos")
            .setContentText("La cirugía de $patientText ha alcanzado $elapsedMinutes min en sala.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "⚠️ ALERTA DE TIEMPO QUIRÚRGICO HGV\n" +
                    "Se han cumplido $elapsedMinutes minutos de cirugía activa.\n\n" +
                    "• Paciente: $patientText\n" +
                    "• Procedimiento: $procedureText$doctorText\n" +
                    "• Estado: En monitoreo continuo de laparoscopia"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setContentIntent(getAppPendingIntent(context))
            .addAction(
                R.drawable.ic_launcher_foreground,
                "VER BITÁCORA",
                getAppPendingIntent(context)
            )

        if (isVibrationEnabled) {
            builder.setVibrate(longArrayOf(0, 500, 250, 500, 250, 700))
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    /**
     * Cancels the ongoing surgery chronometer notification.
     */
    fun cancelOngoingSurgeryNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID_ONGOING)
        } catch (_: Exception) {
        }
    }

    /**
     * Posts a completion notification when the surgery ends.
     */
    fun postSurgeryCompletedNotification(
        context: Context,
        totalDurationSeconds: Long,
        paciente: String,
        cirugiaRealizada: String
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val durationHuman = TimeHelper.formatDurationHuman(totalDurationSeconds)
        val patientText = paciente.ifBlank { "Paciente" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SURGICAL_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✅ Cirugía Concluida ($durationHuman)")
            .setContentText("Procedimiento de $patientText finalizado exitosamente.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "✅ CIRUGÍA REGISTRADA EN BASE DE DATOS\n" +
                    "• Paciente: $patientText\n" +
                    "• Cirugía: ${cirugiaRealizada.ifBlank { "Laparoscopia" }}\n" +
                    "• Duración Concreta: $durationHuman\n" +
                    "Los sellos forenses han sido certificados en la bitácora local."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(getAppPendingIntent(context))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_COMPLETED, notification)
        } catch (_: SecurityException) {
        }
    }
}
