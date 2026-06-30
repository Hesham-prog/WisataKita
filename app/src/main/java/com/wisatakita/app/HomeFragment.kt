package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wisatakita.app.data.UserPrefs
import com.wisatakita.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userPrefs = UserPrefs(requireContext())
        binding.tvGreeting.text = "Halo, ${userPrefs.getCurrentName()}!"

        setupCardPress(binding.cardMenuDestinasi) {
            startActivity(Intent(requireContext(), ListActivity::class.java))
        }
        setupCardPress(binding.cardMenuFavorit) {
            startActivity(Intent(requireContext(), GalleryActivity::class.java))
        }

        setupMusicToggle()
        syncMusicButton()
    }

    private fun setupMusicToggle() {
        binding.btnMusic.setOnClickListener {
            val intent = Intent(requireContext(), MusicService::class.java)
            if (MusicService.isPlaying) {
                intent.action = MusicService.ACTION_PAUSE
                MusicService.isPlaying = false
            } else {
                intent.action = MusicService.ACTION_RESUME
                MusicService.isPlaying = true
            }
            requireContext().startService(intent)
            syncMusicButton()
        }
    }

    private fun syncMusicButton() {
        binding.btnMusic.setImageResource(
            if (MusicService.isPlaying) R.drawable.ic_music_on else R.drawable.ic_music_off
        )
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

    override fun onResume() {
        super.onResume()
        syncMusicButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
