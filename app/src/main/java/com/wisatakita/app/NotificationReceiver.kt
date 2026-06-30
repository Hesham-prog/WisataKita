package com.wisatakita.app

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat

/**
 * NotificationReceiver — handles all WisataKita alarm events and shows notifications.
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_DISCOVERY    = "com.wisatakita.app.DAILY_DISCOVERY"
        const val ACTION_FAVORITE_REMINDER  = "com.wisatakita.app.FAVORITE_REMINDER"
        const val ACTION_REVIEW_NUDGE       = "com.wisatakita.app.REVIEW_NUDGE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        NotificationScheduler.createNotificationChannel(context)

        when (intent.action) {
            ACTION_DAILY_DISCOVERY   -> showDailyDiscovery(context)
            ACTION_FAVORITE_REMINDER -> showFavoriteReminder(context)
            ACTION_REVIEW_NUDGE      -> showReviewNudge(context)
        }
    }

    private fun showDailyDiscovery(context: Context) {
        val destinations = listOf(
            "Danau Toba" to "Sumatera Utara",
            "Taman Nasional Komodo" to "NTT",
            "Raja Ampat" to "Papua Barat",
            "Bromo" to "Jawa Timur",
            "Bali" to "Bali"
        )
        val pick = destinations.random()
        showNotification(
            context,
            id = 1001,
            title = "🌄 Destinasi Hari Ini",
            body = "Sudah pernah ke ${pick.first}, ${pick.second}? Yuk jelajahi sekarang!",
            targetClass = MainActivity::class.java
        )

        // Reschedule for tomorrow (repeating exact alarm)
        val prefs = context.getSharedPreferences("wk_notif_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("daily_enabled", false)) {
            NotificationScheduler.scheduleDailyDiscovery(
                context,
                prefs.getInt("daily_hour", 8),
                prefs.getInt("daily_minute", 0)
            )
        }
    }

    private fun showFavoriteReminder(context: Context) {
        showNotification(
            context,
            id = 1002,
            title = "🔖 Lihat Koleksimu",
            body = "Ada destinasi favorit yang belum kamu kunjungi. Yuk rencanakan liburanmu!",
            targetClass = MainActivity::class.java
        )
    }

    private fun showReviewNudge(context: Context) {
        showNotification(
            context,
            id = 1003,
            title = "✍️ Bagikan Pengalamanmu",
            body = "Gimana pengalaman wisatamu? Tulis ulasanmu dan bantu penjelajah lain!",
            targetClass = MainActivity::class.java
        )
    }

    private fun showNotification(
        context: Context,
        id: Int,
        title: String,
        body: String,
        targetClass: Class<*>
    ) {
        val openIntent = Intent(context, targetClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF4F8F35.toInt()) // green brand color
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, notification)
    }
}
