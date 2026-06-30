package com.wisatakita.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.wisatakita.app.databinding.FragmentKoleksiBinding

class KoleksiFragment : Fragment() {

    private var _binding: FragmentKoleksiBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KoleksiViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKoleksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[KoleksiViewModel::class.java]
        binding.viewPagerKoleksi.adapter = KoleksiPagerAdapter(this)
        TabLayoutMediator(binding.tabKoleksi, binding.viewPagerKoleksi) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.koleksi_favorit)
                1 -> getString(R.string.koleksi_paspor)
                else -> getString(R.string.koleksi_album)
            }
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
