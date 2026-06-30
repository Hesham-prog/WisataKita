package com.wisatakita.app

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.wisatakita.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var networkReceiver: NetworkReceiver
    private var offlineSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set fragment awal
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        // Setup Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_team -> TeamFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }

        // Mulai musik
        if (!MusicService.isRunning) {
            startService(Intent(this, MusicService::class.java))
        }

        // Setup Network Receiver
        networkReceiver = NetworkReceiver { isConnected ->
            runOnUiThread {
                if (isConnected) {
                    offlineSnackbar?.dismiss()
                } else {
                    offlineSnackbar = Snackbar.make(
                        binding.root,
                        "Tidak ada koneksi internet",
                        Snackbar.LENGTH_INDEFINITE
                    )
                    offlineSnackbar?.show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        @Suppress("DEPRECATION")
        registerReceiver(networkReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(networkReceiver)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            stopService(Intent(this, MusicService::class.java))
        }
    }
}
