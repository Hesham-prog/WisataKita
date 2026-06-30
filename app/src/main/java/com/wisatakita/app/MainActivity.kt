package com.wisatakita.app

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
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

    // Compass overlay views (added programmatically)
    private val petalViews = mutableListOf<View>()
    private var isMenuOpen = false

    companion object {
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
        val scrim = View(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0x66000000)
            alpha = 0f
            setOnClickListener {
                compassView.isMenuOpen = false
                isMenuOpen = false
                compassView.invalidate()
                hidePetalMenu()
            }
        }

        val dock = LinearLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                (64 * density).toInt(),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = (94 * density).toInt()
            }
            background = getDrawable(R.drawable.bg_glassmorphism)
            elevation = 12 * density
            alpha = 0f
            translationY = 24 * density
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            setPadding((8 * density).toInt(), 0, (8 * density).toInt(), 0)
        }

        TAB_ICONS.forEachIndexed { index, icon ->
            dock.addView(ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams((52 * density).toInt(), (52 * density).toInt()).apply {
                    marginStart = (3 * density).toInt()
                    marginEnd = (3 * density).toInt()
                }
                background = getDrawable(R.drawable.bg_compass_dock_item)
                contentDescription = tabLabel(index)
                setImageResource(icon)
                setColorFilter(getColor(if (index == currentTabIndex) R.color.gold_primary else R.color.cream_primary))
                setPadding((15 * density).toInt(), (15 * density).toInt(), (15 * density).toInt(), (15 * density).toInt())
                bounceClick()
                setOnClickListener { v ->
                    HapticUtil.click(v)
                    compassView.isMenuOpen = false
                    isMenuOpen = false
                    hidePetalMenu()
                    compassView.invalidate()
                    showFragment(index)
                }
            })
        }

        root.addView(scrim)
        root.addView(dock)
        petalViews.add(scrim)
        petalViews.add(dock)

        scrim.animate().alpha(1f).setDuration(140).start()
        dock.animate().alpha(1f).translationY(0f).setDuration(220).start()
    }

    private fun hidePetalMenu() {
        val root = binding.root as ViewGroup
        val density = resources.displayMetrics.density
        petalViews.toList().forEach { petal ->
            val endTranslation = if (petal is LinearLayout) 20 * density else petal.translationY
            petal.animate()
                .alpha(0f)
                .translationY(endTranslation)
                .setDuration(160)
                .withEndAction { root.removeView(petal) }
                .start()
        }
        petalViews.clear()
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

    private fun tabLabel(index: Int): String = when (index) {
        0 -> getString(R.string.nav_beranda)
        1 -> getString(R.string.nav_jelajahi)
        2 -> getString(R.string.nav_koleksi)
        3 -> getString(R.string.nav_profil)
        else -> getString(R.string.app_name)
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
