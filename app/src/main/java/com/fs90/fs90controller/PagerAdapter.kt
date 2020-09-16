package com.fs90.fs90controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    var fragments: ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        fragments.add(FragmentOneLinkController())
        return fragments[position]
    }

    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
    }
}