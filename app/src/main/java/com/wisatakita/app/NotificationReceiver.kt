package com.wisatakita.app

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.wisatakita.app.data.DestinationRepository
import kotlinx.coroutines.runBlocking

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
            ACTION_REVIEW_NUDGE      -> showReviewNudge(context, intent)
        }
    }

    private fun showDailyDiscovery(context: Context) {
        val pendingResult = goAsync()
        Thread {
            try {
                val destinations = runCatching {
                    runBlocking { DestinationRepository(context.applicationContext).getAllDestinations() }
                }.getOrDefault(emptyList())
                val pick = destinations.randomOrNull()
                val title = pick?.name ?: context.getString(R.string.notif_daily_fallback_title)
                val location = pick?.location ?: "Indonesia"
                val image = pick?.imageUrl?.let { loadNotificationBitmap(context, it) }

                showNotification(
                    context,
                    id = 1001,
                    title = context.getString(R.string.notif_daily_fallback_title),
                    body = context.getString(R.string.notif_daily_body, title, location),
                    targetClass = MainActivity::class.java,
                    bigPicture = image
                )
                rescheduleDailyIfEnabled(context)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    private fun rescheduleDailyIfEnabled(context: Context) {
        val prefs = context.getSharedPreferences("wk_notif_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("daily_enabled", false)) {
            NotificationScheduler.scheduleDailyDiscovery(
                context,
                prefs.getInt("daily_hour", 8),
                prefs.getInt("daily_minute", 0)
            )
        }
    }

    private fun loadNotificationBitmap(context: Context, imageUrl: String): Bitmap? {
        return runCatching {
            Glide.with(context.applicationContext)
                .asBitmap()
                .load(imageUrl)
                .submit(960, 540)
                .get()
        }.getOrNull()
    }

    private fun showFavoriteReminder(context: Context) {
        showNotification(
            context,
            id = 1002,
            title = context.getString(R.string.koleksi_passport_title),
            body = context.getString(R.string.notif_favorite_body),
            targetClass = MainActivity::class.java
        )
    }

    private fun showReviewNudge(context: Context, intent: Intent) {
        val destinationId = intent.getStringExtra("DESTINATION_ID").orEmpty()
        showNotification(
            context,
            id = 1003,
            title = context.getString(R.string.notif_review_nudge_title),
            body = context.getString(R.string.notif_review_nudge_body),
            targetClass = DetailActivity::class.java,
            extras = mapOf(
                "DESTINATION_ID" to destinationId,
                DetailActivity.EXTRA_LANTERN_MESSAGE to context.getString(R.string.notif_review_lantern)
            )
        )
    }

    private fun showNotification(
        context: Context,
        id: Int,
        title: String,
        body: String,
        targetClass: Class<*>,
        extras: Map<String, String> = emptyMap(),
        bigPicture: Bitmap? = null
    ) {
        val openIntent = Intent(context, targetClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            extras.forEach { (key, value) -> putExtra(key, value) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val style = if (bigPicture != null) {
            NotificationCompat.BigPictureStyle()
                .bigPicture(bigPicture)
                .setSummaryText(body)
        } else {
            NotificationCompat.BigTextStyle().bigText(body)
        }

        val notification = NotificationCompat.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF4F8F35.toInt())
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, notification)
    }
}
