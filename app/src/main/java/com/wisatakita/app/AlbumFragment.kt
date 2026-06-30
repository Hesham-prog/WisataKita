package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.wisatakita.app.databinding.FragmentAlbumBinding

class AlbumFragment : Fragment() {

    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KoleksiViewModel
    private val adapter = AlbumAdapter(mutableListOf(), onClick = { album ->
        startActivity(Intent(requireContext(), AlbumDetailActivity::class.java).apply {
            putExtra("ALBUM_ID", album.id)
        })
    }, onLongClick = {})

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            requireParentFragment(),
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[KoleksiViewModel::class.java]
        binding.recyclerAlbums.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerAlbums.adapter = adapter
        binding.btnOpenGallery.bounceClick()
        binding.btnOpenGallery.setOnClickListener {
            HapticUtil.click(it)
            startActivity(Intent(requireContext(), GalleryActivity::class.java))
        }
        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            adapter.updateData(albums)
            binding.tvEmptyAlbums.visibility = if (albums.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerAlbums.visibility = if (albums.isEmpty()) View.GONE else View.VISIBLE
            binding.btnOpenGallery.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
