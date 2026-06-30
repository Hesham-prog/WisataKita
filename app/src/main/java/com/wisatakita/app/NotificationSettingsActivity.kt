package com.wisatakita.app

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.wisatakita.app.databinding.ActivityNotificationSettingsBinding

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var alarmManager: AlarmManager

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        bindPrefs()
        setupInteractions()
        updateExactAlarmRationale()
    }

    override fun onResume() {
        super.onResume()
        updateExactAlarmRationale()
        if (binding.switchDaily.isChecked && canScheduleExactAlarms()) {
            scheduleDaily()
        }
    }

    private fun bindPrefs() {
        binding.switchDaily.isChecked = prefs.getBoolean(KEY_DAILY_ENABLED, false)
        binding.switchFavorite.isChecked = prefs.getBoolean(KEY_FAVORITE_ENABLED, false)
        binding.switchReview.isChecked = prefs.getBoolean(KEY_REVIEW_ENABLED, true)
        renderDailyTime(
            prefs.getInt(KEY_DAILY_HOUR, DEFAULT_DAILY_HOUR),
            prefs.getInt(KEY_DAILY_MINUTE, DEFAULT_DAILY_MINUTE)
        )
        updateTimeSelectorState()
    }

    private fun setupInteractions() {
        binding.btnBack.setOnClickListener {
            HapticUtil.click(it)
            finish()
        }

        binding.rowDaily.setOnClickListener {
            binding.switchDaily.isChecked = !binding.switchDaily.isChecked
        }
        binding.rowFavorite.setOnClickListener {
            binding.switchFavorite.isChecked = !binding.switchFavorite.isChecked
        }
        binding.rowReview.setOnClickListener {
            binding.switchReview.isChecked = !binding.switchReview.isChecked
        }

        binding.switchDaily.setOnCheckedChangeListener { button, checked ->
            HapticUtil.click(button)
            prefs.edit().putBoolean(KEY_DAILY_ENABLED, checked).apply()
            updateTimeSelectorState()
            if (checked) {
                requestNotificationPermissionIfNeeded()
                scheduleDaily()
            } else {
                cancelAlarm(NotificationReceiver.ACTION_DAILY_DISCOVERY, RC_DAILY)
            }
            updateExactAlarmRationale()
        }

        binding.switchFavorite.setOnCheckedChangeListener { button, checked ->
            HapticUtil.click(button)
            prefs.edit().putBoolean(KEY_FAVORITE_ENABLED, checked).apply()
            if (checked) {
                requestNotificationPermissionIfNeeded()
                NotificationScheduler.scheduleFavoriteReminder(this)
            } else {
                cancelAlarm(NotificationReceiver.ACTION_FAVORITE_REMINDER, RC_FAVORITE)
            }
        }

        binding.switchReview.setOnCheckedChangeListener { button, checked ->
            HapticUtil.click(button)
            prefs.edit().putBoolean(KEY_REVIEW_ENABLED, checked).apply()
            if (checked) {
                requestNotificationPermissionIfNeeded()
            } else {
                cancelAlarm(NotificationReceiver.ACTION_REVIEW_NUDGE, RC_REVIEW)
            }
        }

        binding.cardTimeSelector.setOnClickListener {
            if (binding.switchDaily.isChecked) {
                HapticUtil.click(it)
                openTimePicker()
            }
        }
        binding.tvDailyTime.setOnClickListener {
            if (binding.switchDaily.isChecked) {
                HapticUtil.click(it)
                openTimePicker()
            }
        }
        binding.btnMinusTime.setOnClickListener {
            HapticUtil.click(it)
            adjustDailyTime(-15)
        }
        binding.btnPlusTime.setOnClickListener {
            HapticUtil.click(it)
            adjustDailyTime(15)
        }
        binding.btnGrantExactAlarm.setOnClickListener {
            HapticUtil.click(it)
            openExactAlarmSettings()
        }
        binding.btnSaveSettings.setOnClickListener {
            HapticUtil.click(it)
            if (binding.switchDaily.isChecked) scheduleDaily()
            if (binding.switchFavorite.isChecked) NotificationScheduler.scheduleFavoriteReminder(this)
            Toast.makeText(this, R.string.notif_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTimePicker() {
        val hour = prefs.getInt(KEY_DAILY_HOUR, DEFAULT_DAILY_HOUR)
        val minute = prefs.getInt(KEY_DAILY_MINUTE, DEFAULT_DAILY_MINUTE)
        TimePickerDialog(
            this,
            { _, pickedHour, pickedMinute -> setDailyTime(pickedHour, pickedMinute) },
            hour,
            minute,
            true
        ).show()
    }

    private fun adjustDailyTime(deltaMinutes: Int) {
        if (!binding.switchDaily.isChecked) return
        val dayMinutes = 24 * 60
        val current = prefs.getInt(KEY_DAILY_HOUR, DEFAULT_DAILY_HOUR) * 60 +
            prefs.getInt(KEY_DAILY_MINUTE, DEFAULT_DAILY_MINUTE)
        val next = ((current + deltaMinutes) % dayMinutes + dayMinutes) % dayMinutes
        setDailyTime(next / 60, next % 60)
    }

    private fun setDailyTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_DAILY_HOUR, hour)
            .putInt(KEY_DAILY_MINUTE, minute)
            .apply()
        renderDailyTime(hour, minute)
        if (binding.switchDaily.isChecked) scheduleDaily()
    }

    private fun renderDailyTime(hour: Int, minute: Int) {
        binding.tvDailyTime.text = "%02d:%02d".format(hour, minute)
    }

    private fun updateTimeSelectorState() {
        val enabled = binding.switchDaily.isChecked
        binding.cardTimeSelector.alpha = if (enabled) 1f else 0.45f
        binding.cardTimeSelector.isEnabled = enabled
        binding.tvDailyTime.isEnabled = enabled
        binding.btnMinusTime.isEnabled = enabled
        binding.btnPlusTime.isEnabled = enabled
    }

    private fun updateExactAlarmRationale() {
        binding.cardExactAlarmRationale.visibility =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun scheduleDaily() {
        NotificationScheduler.scheduleDailyDiscovery(
            this,
            prefs.getInt(KEY_DAILY_HOUR, DEFAULT_DAILY_HOUR),
            prefs.getInt(KEY_DAILY_MINUTE, DEFAULT_DAILY_MINUTE)
        )
    }

    private fun cancelAlarm(action: String, requestCode: Int) {
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            Intent(this, NotificationReceiver::class.java).apply { this.action = action },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
            }
            runCatching { startActivity(intent) }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        private const val PREFS_NAME = "wk_notif_prefs"
        private const val KEY_DAILY_ENABLED = "daily_enabled"
        private const val KEY_DAILY_HOUR = "daily_hour"
        private const val KEY_DAILY_MINUTE = "daily_minute"
        private const val KEY_FAVORITE_ENABLED = "favorite_enabled"
        private const val KEY_REVIEW_ENABLED = "review_enabled"
        private const val DEFAULT_DAILY_HOUR = 8
        private const val DEFAULT_DAILY_MINUTE = 0
        private const val RC_DAILY = 1001
        private const val RC_FAVORITE = 1002
        private const val RC_REVIEW = 1003
    }
}
