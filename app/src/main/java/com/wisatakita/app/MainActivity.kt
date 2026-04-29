package com.wisatakita.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.wisatakita.app.data.TeamMember
import com.wisatakita.app.data.UserPrefs
import com.wisatakita.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPrefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPrefs = UserPrefs(this)
        binding.tvGreeting.text = "Halo, ${userPrefs.getCurrentName()}!"

        setupCardAnimations()
        setupTeamSection()
        setupMusicToggle()

        if (!MusicService.isRunning) {
            startService(Intent(this, MusicService::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        syncMusicButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            stopService(Intent(this, MusicService::class.java))
        }
    }

    private fun setupMusicToggle() {
        binding.btnMusic.setOnClickListener {
            val intent = Intent(this, MusicService::class.java)
            if (MusicService.isPlaying) {
                intent.action = MusicService.ACTION_PAUSE
                MusicService.isPlaying = false
            } else {
                intent.action = MusicService.ACTION_RESUME
                MusicService.isPlaying = true
            }
            startService(intent)
            syncMusicButton()
        }
    }

    private fun syncMusicButton() {
        binding.btnMusic.setImageResource(
            if (MusicService.isPlaying) R.drawable.ic_music_on else R.drawable.ic_music_off
        )
    }

    private fun setupCardAnimations() {
        setupCardPress(binding.cardMenuDestinasi) {
            startActivity(Intent(this, ListActivity::class.java))
        }
        setupCardPress(binding.cardMenuFavorit) {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
    }

    private fun setupCardPress(card: View, onClick: () -> Unit) {
        card.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(120).start()
                }
                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).withEndAction {
                        onClick()
                    }.start()
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                }
            }
            true
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Keluar")
            .setMessage("Yakin mau keluar dari WisataKita?")
            .setPositiveButton("Keluar") { _, _ ->
                userPrefs.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupTeamSection() {
        val team = listOf(
            TeamMember(
                name = "Nathan Abigail Rahman",
                nim = "2410511036",
                role = "UI/UX & Logo",
                quote = "There is time to kill today",
                photoRes = R.drawable.team_nathan
            ),
            TeamMember(
                name = "Atalla Ahsan Indrayana",
                nim = "2410511039",
                role = "Frontend Developer",
                quote = "Ngopi skuy",
                photoRes = R.drawable.team_atalla
            ),
            TeamMember(
                name = "Athallah Abrar Duano",
                nim = "2410511046",
                role = "Feature Developer",
                quote = "KICAUMANIAAAGHHH!!!!",
                photoRes = R.drawable.team_athallah
            ),
            TeamMember(
                name = "Sulthon D. Arrafi",
                nim = "2410511061",
                role = "Content & Data",
                quote = "Saya doang yang kaya di grup ini semuanya miskin, btw follow ig aku ya guys @sulthdaffa!!!",
                photoRes = R.drawable.team_sulthon
            ),
            TeamMember(
                name = "Hesham Alsami",
                nim = "2410511066",
                role = "App Architect",
                quote = "HAI, SAYA AKAN LAWAN!",
                photoRes = R.drawable.team_hesham
            )
        )

        binding.recyclerTeam.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerTeam.adapter = TeamAdapter(team)
    }
}
