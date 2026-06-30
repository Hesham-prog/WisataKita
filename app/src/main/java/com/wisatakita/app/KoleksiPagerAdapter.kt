package com.wisatakita.app

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class KoleksiPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoritFragment()
            1 -> PasporFragment()
            else -> AlbumFragment()
        }
    }
}
