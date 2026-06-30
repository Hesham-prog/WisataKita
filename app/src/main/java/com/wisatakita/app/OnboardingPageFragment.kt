package com.wisatakita.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import com.wisatakita.app.databinding.FragmentOnboardingPageBinding

class OnboardingPageFragment : Fragment() {

    private var _binding: FragmentOnboardingPageBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_BODY = "body"
        private const val ARG_LOTTIE_RES = "lottie_res"

        fun newInstance(title: String, body: String, lottieRes: Int): OnboardingPageFragment {
            return OnboardingPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_BODY, body)
                    putInt(ARG_LOTTIE_RES, lottieRes)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString(ARG_TITLE) ?: ""
        val body = arguments?.getString(ARG_BODY) ?: ""
        val lottieRes = arguments?.getInt(ARG_LOTTIE_RES) ?: 0

        binding.tvOnboardingTitle.text = title
        binding.tvOnboardingBody.text = body

        // Configure Lottie
        try {
            if (lottieRes != 0) {
                binding.lottieOnboarding.setAnimation(lottieRes)
            }
            binding.lottieOnboarding.repeatCount = LottieDrawable.INFINITE
            binding.lottieOnboarding.playAnimation()
        } catch (e: Exception) {
            // Asset not present yet — hide gracefully
            binding.lottieOnboarding.visibility = View.INVISIBLE
        }

        // Staggered entry animations
        animateIn(binding.tvOnboardingTitle, delayMs = 200L, fromTranslationY = 60f)
        animateIn(binding.tvOnboardingBody, delayMs = 380L, fromTranslationY = 40f)
    }

    private fun animateIn(view: View, delayMs: Long, fromTranslationY: Float) {
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
            duration = 500
            startDelay = delayMs
            interpolator = DecelerateInterpolator()
        }
        val slide = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, fromTranslationY, 0f).apply {
            duration = 500
            startDelay = delayMs
            interpolator = DecelerateInterpolator(1.5f)
        }
        AnimatorSet().apply {
            playTogether(alpha, slide)
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
