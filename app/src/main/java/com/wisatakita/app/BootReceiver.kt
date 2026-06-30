package com.wisatakita.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BootReceiver — re-schedules all WisataKita alarms after device reboot.
 * Registered in AndroidManifest with RECEIVE_BOOT_COMPLETED.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) {
            NotificationScheduler.rescheduleAll(context)
        }
    }
}
