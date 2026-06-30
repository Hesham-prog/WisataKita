package com.wisatakita.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.wisatakita.app.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private var splashPlayer: MediaPlayer? = null

    companion object {
        private const val PREF_NAME = "wk_app_prefs"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtil.applySavedLanguage(this)
        super.onCreate(savedInstanceState)

        // Edge-to-edge immersive
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupVideoBackground()
        startSplashSequence()
    }

    private fun setupVideoBackground() {
        val videoResId = resources.getIdentifier("splash_video", "raw", packageName)
        if (videoResId == 0) {
            binding.textureSplashVideo.visibility = View.GONE
            return
        }

        binding.textureSplashVideo.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                val surface = Surface(surfaceTexture)
                splashPlayer = MediaPlayer().apply {
                    setDataSource(
                        this@SplashActivity,
                        Uri.parse("android.resource://$packageName/$videoResId")
                    )
                    setSurface(surface)
                    isLooping = true
                    setVolume(0f, 0f)
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener { it.seekTo(0) }
                    prepareAsync()
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                releaseVideoBackground()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
        }
    }

    private fun startSplashSequence() {
        // Animate the premium brand mark in before the text sequence.
        try {
            binding.ivSplashLogo.apply {
                alpha = 0f
                scaleX = 0.84f
                scaleY = 0.84f
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(720L)
                    .setInterpolator(DecelerateInterpolator(1.6f))
                    .start()
            }
        } catch (e: Exception) {
            // Asset not yet added — still run the rest of the sequence
        }

        // Step 1 — after 500ms: fade in app name with overshoot scale
        handler.postDelayed({
            animateIn(binding.tvSplashAppName, translationY = 30f, delay = 0)
        }, 500)

        // Step 2 — after 900ms: fade in tagline
        handler.postDelayed({
            animateIn(binding.tvSplashTagline, translationY = 20f, delay = 0)
        }, 900)

        // Step 3 — after 2800ms: navigate
        handler.postDelayed({
            navigateNext()
        }, 2800)
    }

    private fun animateIn(view: View, translationY: Float, delay: Long) {
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
            duration = 600
            startDelay = delay
            interpolator = DecelerateInterpolator()
        }
        val slide = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, translationY, 0f).apply {
            duration = 600
            startDelay = delay
            interpolator = DecelerateInterpolator(1.5f)
        }
        AnimatorSet().apply {
            playTogether(alpha, slide)
            start()
        }
    }

    private fun navigateNext() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

        if (isFirstLaunch) {
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        releaseVideoBackground()
    }

    private fun releaseVideoBackground() {
        splashPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) player.stop()
                player.release()
            }
        }
        splashPlayer = null
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Intentionally swallowed — no back from splash
    }
}
