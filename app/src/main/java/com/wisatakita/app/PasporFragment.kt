package com.wisatakita.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.wisatakita.app.databinding.FragmentPasporBinding

class PasporFragment : Fragment() {

    private var _binding: FragmentPasporBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KoleksiViewModel
    private val adapter = StampAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPasporBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            requireParentFragment(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[KoleksiViewModel::class.java]
        binding.recyclerStamps.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerStamps.adapter = adapter
        viewModel.stamps.observe(viewLifecycleOwner) { stamps ->
            adapter.submit(stamps)
            binding.tvEmptyStamps.visibility = if (stamps.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerStamps.visibility = if (stamps.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
