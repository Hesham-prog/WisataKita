package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wisatakita.app.databinding.FragmentFavoritBinding

class FavoritFragment : Fragment() {

    private var _binding: FragmentFavoritBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KoleksiViewModel
    private lateinit var adapter: MultiModeDestinationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            requireParentFragment(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[KoleksiViewModel::class.java]
        adapter = MultiModeDestinationAdapter(
            PenjelajahFragment.ViewMode.CARD,
            onItemClick = { destination ->
                startActivity(Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra("DESTINATION_ID", destination.id)
                })
                activity?.overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out_scale)
            }
        )
        binding.recyclerFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFavorites.adapter = adapter
        viewModel.favorites.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.tvEmptyFavorites.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerFavorites.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
