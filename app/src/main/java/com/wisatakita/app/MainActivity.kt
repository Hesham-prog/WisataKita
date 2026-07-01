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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import java.util.Stack
import com.google.android.material.snackbar.Snackbar
import com.wisatakita.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkReceiver: NetworkReceiver
    private lateinit var compassView: NusantaraCompassView

    private var offlineSnackbar: Snackbar? = null
    private var currentTabIndex = 0
    private val fragmentMap = mutableMapOf<Int, Fragment>()
    private val fragmentHistory = Stack<Int>()

    // Compass overlay views (added programmatically)
    private val petalViews = mutableListOf<View>()
    private var isMenuOpen = false
    private var arcMenuView: PetalArcMenuView? = null

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
        setupBackButton()
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
        if (arcMenuView == null) {
            arcMenuView = PetalArcMenuView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onTabSelected = { index ->
                    compassView.isMenuOpen = false
                    isMenuOpen = false
                    hidePetalMenu()
                    compassView.invalidate()
                    showFragment(index)
                }
                onClose = {
                    compassView.isMenuOpen = false
                    isMenuOpen = false
                    compassView.invalidate()
                    hidePetalMenu()
                }
            }
        }
        
        if (arcMenuView?.parent == null) {
            root.addView(arcMenuView)
        }
        arcMenuView?.animateOpen()
    }

    private fun hidePetalMenu() {
        val root = binding.root as ViewGroup
        arcMenuView?.animateClose {
            root.removeView(arcMenuView)
        }
    }

    private fun setupFragments() {
        // Show HomeFragment by default without pushing to empty history initially
        showFragment(0, addToHistory = false)
    }

    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isMenuOpen) {
                    showFragment(currentTabIndex, addToHistory = false) // Closes menu
                } else if (fragmentHistory.isNotEmpty()) {
                    val prevIndex = fragmentHistory.pop()
                    showFragment(prevIndex, addToHistory = false)
                } else if (currentTabIndex != 0) {
                    showFragment(0, addToHistory = false)
                } else {
                    finish()
                }
            }
        })
    }

    fun navigateToJelajahi(query: String) {
        showFragment(1)
        val fragment = fragmentMap[1] as? PenjelajahFragment
        fragment?.setSearchQueryFromVoice(query)
    }

    fun showFragment(index: Int, addToHistory: Boolean = true) {
        if (addToHistory && currentTabIndex != index && (fragmentHistory.isEmpty() || fragmentHistory.peek() != currentTabIndex)) {
            fragmentHistory.push(currentTabIndex)
        }
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
