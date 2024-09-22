package com.savestatus.wsstatussaver.fragments

import com.savestatus.wsstatussaver.adapter.PagerAdapter
import com.savestatus.wsstatussaver.fragments.base.AbsStatusesFragment
import com.savestatus.wsstatussaver.fragments.pager.HomeStatusesFragment

class HomeFragment : AbsStatusesFragment() {
    override fun onCreatePagerAdapter(): PagerAdapter {
        return PagerAdapter(this, HomeStatusesFragment::class.java.name)
    }
}