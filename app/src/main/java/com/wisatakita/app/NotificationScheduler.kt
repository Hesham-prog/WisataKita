package com.wisatakita.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * NotificationScheduler — schedules and cancels all WisataKita push notifications.
 *
 * Notification types:
 * 1. Daily Discovery — fires daily at a user-chosen time
 * 2. Favorite Reminder — fires every Sunday at 10:00
 * 3. Review Nudge — fires 3 hours after an implicit "visited" flag is set
 */
object NotificationScheduler {

    const val CHANNEL_ID = "wisatakita_main"
    const val CHANNEL_NAME = "WisataKita Notifikasi"

    // Request codes for PendingIntent
    private const val RC_DAILY   = 1001
    private const val RC_FAVORITE = 1002
    private const val RC_REVIEW   = 1003

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Rekomendasi dan pengingat dari WisataKita"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Schedule daily discovery notification at [hourOfDay]:[minute].
     */
    fun scheduleDailyDiscovery(context: Context, hourOfDay: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_DAILY_DISCOVERY
        }
        val pending = PendingIntent.getBroadcast(
            context, RC_DAILY, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pending
            )
        } else {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pending
            )
        }

        // Persist settings
        context.getSharedPreferences("wk_notif_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("daily_enabled", true)
            .putInt("daily_hour", hourOfDay)
            .putInt("daily_minute", minute)
            .apply()
    }

    /**
     * Schedule weekly favorite reminder (every Sunday 10:00).
     */
    fun scheduleFavoriteReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_FAVORITE_REMINDER
        }
        val pending = PendingIntent.getBroadcast(
            context, RC_FAVORITE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.WEEK_OF_YEAR, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pending
        )

        context.getSharedPreferences("wk_notif_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("favorite_enabled", true).apply()
    }

    /**
     * Schedule a one-shot review nudge [delayMillis] from now.
     */
    fun scheduleReviewNudge(context: Context, delayMillis: Long = 3 * 60 * 60 * 1000L) {
        val prefs = context.getSharedPreferences("wk_notif_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("review_enabled", true)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_REVIEW_NUDGE
        }
        val pending = PendingIntent.getBroadcast(
            context, RC_REVIEW, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + delayMillis
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
    }

    /** Cancel all scheduled notifications. */
    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(
            Triple(RC_DAILY, NotificationReceiver.ACTION_DAILY_DISCOVERY, ""),
            Triple(RC_FAVORITE, NotificationReceiver.ACTION_FAVORITE_REMINDER, ""),
            Triple(RC_REVIEW, NotificationReceiver.ACTION_REVIEW_NUDGE, "")
        ).forEach { (rc, action, _) ->
            val intent = Intent(context, NotificationReceiver::class.java).apply { this.action = action }
            val pending = PendingIntent.getBroadcast(
                context, rc, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pending?.let { alarmManager.cancel(it) }
        }
    }

    /** Reschedule all enabled notifications (called from BootReceiver). */
    fun rescheduleAll(context: Context) {
        val prefs = context.getSharedPreferences("wk_notif_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("daily_enabled", false)) {
            scheduleDailyDiscovery(
                context,
                prefs.getInt("daily_hour", 8),
                prefs.getInt("daily_minute", 0)
            )
        }
        if (prefs.getBoolean("favorite_enabled", false)) {
            scheduleFavoriteReminder(context)
        }
    }
}
