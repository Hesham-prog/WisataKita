package com.wisatakita.app

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.wisatakita.app.data.UserPrefs
import com.wisatakita.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel
    private var teamExpanded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userPrefs = UserPrefs(requireContext())
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[ProfileViewModel::class.java]

        binding.tvProfileName.text = userPrefs.getCurrentName()
        binding.tvProfileEmail.text = userPrefs.getCurrentEmail()

        setupRadarChart()
        setupRows()
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.tvStatsPill.text = getString(
                R.string.profile_stats_combined,
                stats.visitedCount,
                stats.reviewCount
            )
            renderRadar(stats.categoryCounts)
        }
        viewModel.refresh()

        binding.btnLogout.bounceClick()
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.profile_logout))
                .setMessage(getString(R.string.profile_logout_confirm))
                .setPositiveButton(getString(R.string.profile_logout)) { _, _ ->
                    userPrefs.logout()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }


    private fun setupRows() {
        binding.rowNotificationSettings.bounceClick()
        binding.rowNotificationSettings.setOnClickListener {
            HapticUtil.click(it)
            runCatching {
                startActivity(Intent().setClassName(requireContext(), "${requireContext().packageName}.NotificationSettingsActivity"))
            }.onFailure { error ->
                if (error is ActivityNotFoundException) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.profile_notification_settings))
                        .setMessage(getString(R.string.notif_permission_rationale))
                        .setPositiveButton(getString(R.string.ok), null)
                        .show()
                }
            }
        }

        binding.rowLanguageSettings.bounceClick()
        binding.rowLanguageSettings.setOnClickListener {
            HapticUtil.click(it)
            val languages = arrayOf(getString(R.string.lang_indonesian), getString(R.string.lang_english))
            val currentIndex = if (LanguageUtil.currentLanguage(requireContext()) == LanguageUtil.ENGLISH) 1 else 0
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.lang_title))
                .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                    val selected = if (which == 1) LanguageUtil.ENGLISH else LanguageUtil.INDONESIAN
                    if (selected != LanguageUtil.currentLanguage(requireContext())) {
                        LanguageUtil.setLanguage(requireContext(), selected)
                        requireActivity().recreate()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        binding.rowTeam.bounceClick()
        binding.rowTeam.setOnClickListener {
            HapticUtil.click(it)
            teamExpanded = !teamExpanded
            binding.teamInlineContainer.visibility = if (teamExpanded) View.VISIBLE else View.GONE
            if (teamExpanded && childFragmentManager.findFragmentById(R.id.teamInlineContainer) == null) {
                childFragmentManager.beginTransaction()
                    .replace(R.id.teamInlineContainer, TeamFragment())
                    .commit()
            }
        }
    }

    private fun setupRadarChart() {
        binding.radarCategory.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            setBackgroundColor(Color.TRANSPARENT)
            webColor = requireContext().getColor(R.color.glass_border)
            webColorInner = requireContext().getColor(R.color.glass_border)
            webAlpha = 70
            yAxis.axisMinimum = 0f
            yAxis.setDrawLabels(false)
            yAxis.gridColor = requireContext().getColor(R.color.glass_border)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = requireContext().getColor(R.color.cream_primary)
            xAxis.textSize = 11f
        }
    }

    private fun renderRadar(categoryCounts: Map<String, Int>) {
        val labels = if (LanguageUtil.currentLanguage(requireContext()) == LanguageUtil.ENGLISH) {
            listOf("Beach", "Mountain", "Temple", "Park", "Lake")
        } else {
            listOf("Pantai", "Gunung", "Candi", "Taman", "Danau")
        }
        val matchers = listOf(
            listOf("Pantai", "Beach"),
            listOf("Gunung", "Mountain"),
            listOf("Candi", "Sejarah", "Temple", "History"),
            listOf("Taman", "Alam", "Park", "Nature"),
            listOf("Danau", "Lake")
        )
        val entries = matchers.map { keywords ->
            val value = categoryCounts.entries.firstOrNull {
                keywords.any { keyword -> it.key.contains(keyword, ignoreCase = true) }
            }?.value ?: 0
            RadarEntry(value.coerceAtLeast(1).toFloat())
        }
        val dataSet = RadarDataSet(entries, getString(R.string.section_categories)).apply {
            color = requireContext().getColor(R.color.turquoise_primary)
            fillColor = requireContext().getColor(R.color.turquoise_primary)
            setDrawFilled(true)
            fillAlpha = 86
            lineWidth = 2.4f
            valueTextColor = requireContext().getColor(R.color.gold_primary)
            valueTextSize = 10f
        }
        binding.radarCategory.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.radarCategory.data = RadarData(dataSet)
        binding.radarCategory.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
