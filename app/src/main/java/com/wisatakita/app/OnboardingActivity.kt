package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.wisatakita.app.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    private val pages = listOf(
        Triple(
            R.string.onboarding_1_title,
            R.string.onboarding_1_body,
            R.raw.lottie_onboarding_explore
        ),
        Triple(
            R.string.onboarding_2_title,
            R.string.onboarding_2_body,
            R.raw.lottie_onboarding_collect
        ),
        Triple(
            R.string.onboarding_3_title,
            R.string.onboarding_3_body,
            R.raw.lottie_onboarding_smart
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(this)
        binding.viewPagerOnboarding.adapter = adapter

        TabLayoutMediator(binding.tabIndicator, binding.viewPagerOnboarding) { _, _ -> }.attach()

        binding.viewPagerOnboarding.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButtonLabel(position)
            }
        })
    }

    private fun updateButtonLabel(position: Int) {
        binding.btnNext.text = if (position == pages.size - 1) {
            getString(R.string.onboarding_start)
        } else {
            getString(R.string.onboarding_next)
        }
    }

    private fun setupButtons() {
        binding.btnSkip.setOnClickListener {
            HapticUtil.click(it)
            navigateToLogin()
        }

        binding.btnNext.setOnClickListener {
            HapticUtil.click(it)
            val current = binding.viewPagerOnboarding.currentItem
            if (current < pages.size - 1) {
                binding.viewPagerOnboarding.setCurrentItem(current + 1, true)
            } else {
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(R.anim.fade_in_slide_up, R.anim.fade_out)
        finishAffinity()
    }

    inner class OnboardingAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = pages.size

        override fun createFragment(position: Int): Fragment {
            val (titleRes, bodyRes, lottieRes) = pages[position]
            return OnboardingPageFragment.newInstance(
                title = getString(titleRes),
                body = getString(bodyRes),
                lottieRes = lottieRes
            )
        }
    }
}
