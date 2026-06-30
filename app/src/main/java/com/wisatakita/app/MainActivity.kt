package com.wisatakita.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.FrameMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.wisatakita.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkReceiver: NetworkReceiver
    private lateinit var compassView: NusantaraCompassView

    private var offlineSnackbar: Snackbar? = null
    private var currentTabIndex = 0
    private val fragmentMap = mutableMapOf<Int, Fragment>()

    // Petal overlay views (added programmatically)
    private val petalViews = mutableListOf<View>()
    private var isMenuOpen = false

    companion object {
        private val TAB_LABELS = listOf("Beranda", "Jelajahi", "Koleksi", "Profil")
        private val TAB_ICONS = listOf(
            R.drawable.ic_nav_beranda,
            R.drawable.ic_nav_jelajahi,
            R.drawable.ic_nav_koleksi,
            R.drawable.ic_nav_profil
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCompass()
        setupFragments()
        setupMusicService()
        setupNetworkReceiver()
    }

    private fun setupCompass() {
        compassView = NusantaraCompassView(this)
        binding.compassContainer.addView(compassView)

        compassView.onToggleMenu = { open ->
            isMenuOpen = open
            if (open) showPetalMenu() else hidePetalMenu()
        }
    }

    private fun showPetalMenu() {
        val root = binding.root as ViewGroup
        petalViews.forEach { root.removeView(it) }
        petalViews.clear()

        val density = resources.displayMetrics.density
        val compassX = root.width / 2f
        val compassY = root.height - (32 + 36) * density // bottom margin + half button

        val angles = listOf(-128f, -96f, -64f, -32f)
        val radius = 118f * density

        for (i in 0..3) {
            val angleRad = Math.toRadians(angles[i].toDouble() - 90)
            val targetX = compassX + (radius * Math.cos(angleRad)).toFloat()
            val targetY = compassY + (radius * Math.sin(angleRad)).toFloat()

            val petalW = (116 * density).toInt()
            val petalH = (46 * density).toInt()
            val petal = LinearLayout(this).apply {
                layoutParams = ViewGroup.LayoutParams(petalW, petalH)
                background = getDrawable(R.drawable.bg_glassmorphism)
                elevation = 8 * density
                alpha = 0f
                scaleX = 0f
                scaleY = 0f
                translationX = compassX - petalW / 2f
                translationY = compassY - petalH / 2f
                gravity = android.view.Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL
                setPadding((12 * density).toInt(), 0, (12 * density).toInt(), 0)
                addView(ImageView(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams((18 * density).toInt(), (18 * density).toInt()).apply {
                        marginEnd = (8 * density).toInt()
                    }
                    setImageResource(TAB_ICONS[i])
                    setColorFilter(getColor(R.color.gold_primary))
                    contentDescription = TAB_LABELS[i]
                })
                addView(TextView(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    text = TAB_LABELS[i]
                    textSize = 12f
                    typeface = androidx.core.content.res.ResourcesCompat.getFont(this@MainActivity, R.font.plus_jakarta_sans_semibold)
                    setTextColor(getColor(R.color.cream_primary))
                    includeFontPadding = false
                })
                bounceClick()
                setOnClickListener { v ->
                    HapticUtil.click(v)
                    compassView.isMenuOpen = false
                    hidePetalMenu()
                    compassView.invalidate()
                    showFragment(i)
                }
            }

            root.addView(petal)
            petalViews.add(petal)

            // Animate petal out from compass center
            val delay = i * 60L
            val animX = ObjectAnimator.ofFloat(petal, View.TRANSLATION_X, compassX - petalW / 2f, targetX - petalW / 2f)
            val animY = ObjectAnimator.ofFloat(petal, View.TRANSLATION_Y, compassY - petalH / 2f, targetY - petalH / 2f)
            val animAlpha = ObjectAnimator.ofFloat(petal, View.ALPHA, 0f, 1f)
            val animScaleX = ObjectAnimator.ofFloat(petal, View.SCALE_X, 0f, 1f)
            val animScaleY = ObjectAnimator.ofFloat(petal, View.SCALE_Y, 0f, 1f)

            AnimatorSet().apply {
                playTogether(animX, animY, animAlpha, animScaleX, animScaleY)
                duration = 300
                startDelay = delay
                interpolator = OvershootInterpolator(1.8f)
                start()
            }
        }
    }

    private fun hidePetalMenu() {
        val root = binding.root as ViewGroup
        petalViews.forEachIndexed { i, petal ->
            val compassX = root.width / 2f
            val compassY = root.height.toFloat()
            petal.animate()
                .translationX(compassX - petal.width / 2f)
                .translationY(compassY)
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(200)
                .setStartDelay((petalViews.size - 1 - i) * 40L)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { root.removeView(petal) }
                .start()
        }
        petalViews.clear()
    }

    private fun createPetalDrawable(color: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(color)
            setStroke(2, 0x33FFFFFF)
        }
    }

    private fun setupFragments() {
        // Show HomeFragment by default
        showFragment(0)
    }

    fun showFragment(index: Int) {
        currentTabIndex = index
        val fm = supportFragmentManager

        val fragment = fragmentMap.getOrPut(index) {
            when (index) {
                0 -> HomeFragment()
                1 -> PenjelajahFragment()
                2 -> KoleksiFragment()
                3 -> ProfileFragment()
                else -> HomeFragment()
            }
        }

        val tx = fm.beginTransaction()
            .setCustomAnimations(R.anim.fade_in_slide_up, R.anim.fade_out)

        // Hide all existing fragments
        fragmentMap.values.forEach { if (it.isAdded && it != fragment) tx.hide(it) }

        if (!fragment.isAdded) {
            tx.add(R.id.fragment_container, fragment)
        } else {
            tx.show(fragment)
        }

        tx.commit()

        // Close compass menu
        if (isMenuOpen) {
            isMenuOpen = false
            compassView.isMenuOpen = false
            compassView.invalidate()
            hidePetalMenu()
        }
    }

    private fun setupMusicService() {
        if (!MusicService.isRunning) {
            startService(Intent(this, MusicService::class.java))
        }
    }

    private fun setupNetworkReceiver() {
        networkReceiver = NetworkReceiver { isConnected ->
            runOnUiThread {
                if (isConnected) {
                    offlineSnackbar?.dismiss()
                } else {
                    offlineSnackbar = Snackbar.make(
                        binding.root,
                        getString(R.string.error_no_internet),
                        Snackbar.LENGTH_INDEFINITE
                    ).apply { show() }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        @Suppress("DEPRECATION")
        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onPause() {
        super.onPause()
        try { unregisterReceiver(networkReceiver) } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) stopService(Intent(this, MusicService::class.java))
    }
}
