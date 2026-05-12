package com.zhuowei.polling.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zhuowei.polling.ui.fragment.TicketFragment

class TicketPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TicketFragment.newInstance("0")
            1 -> TicketFragment.newInstance("2")
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}