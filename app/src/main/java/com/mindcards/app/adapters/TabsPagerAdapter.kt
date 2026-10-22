package com.mindcards.app.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mindcards.app.ui.CardSetsFragment
import com.mindcards.app.ui.GameFragment
import com.mindcards.app.ui.SettingsFragment

class TabsPagerAdapter(a: FragmentActivity) : FragmentStateAdapter(a) {
    override fun getItemCount() = 3
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> CardSetsFragment()
        1 -> GameFragment()
        else -> SettingsFragment()
    }
}
