package com.savestatus.wsstatussaver.adapter

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.savestatus.wsstatussaver.model.StatusType
import com.savestatus.wsstatussaver.fragments.base.AbsPagerFragment

class PagerAdapter(private val fragment: Fragment, private val fragmentClassName: String) :
    FragmentStateAdapter(fragment) {

    private val mFragments: MutableList<FragmentHolder> = ArrayList()
    private val mFragmentFactory: FragmentFactory = fragment
        .childFragmentManager
        .fragmentFactory

    override fun getItemCount(): Int {
        return mFragments.size
    }

    override fun createFragment(position: Int): Fragment {
        val mCurrentHolder = mFragments[position]
        return mFragmentFactory.instantiate(fragment.requireContext().classLoader, mCurrentHolder.className!!).apply {
            arguments = mCurrentHolder.arguments
        }
    }

    fun getPageTitle(position: Int): CharSequence {
        return mFragments[position].title!!
    }

    private class FragmentHolder {
        var className: String? = null
        var arguments: Bundle? = null
        var title: String? = null
    }

    init {
        for (type in StatusType.values()) {
            val holder = FragmentHolder().apply {
                className = fragmentClassName
                arguments = bundleOf(AbsPagerFragment.EXTRA_TYPE to type)
                title = fragment.getString(type.nameRes)
            }
            mFragments.add(holder)
        }
    }
}