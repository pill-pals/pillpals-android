package com.pillpals.pillbuddies.ui.medications.medication_info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

import com.pillpals.pillbuddies.R
import io.realm.Realm

class MedicationInfoFragment : Fragment() {

    public lateinit var tabLayout: TabLayout
    public lateinit var tabViewPager: ViewPager

    private lateinit var realm: Realm

    private var tabFragments: List<Fragment> = mutableListOf(
        MedicationInfoTextFragment(),
        MedicationInfoTextFragment(),
        MedicationInfoTextFragment(),
        MedicationInfoTextFragment()
    )
    private var tabTitles: List<String> = mutableListOf(
        "Overview",
        "Side Effects",
        "Tips",
        "Reviews"
    )
    private lateinit var tabPagerAdapter: TabPagerAdapter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_medication_info, container,false)

        realm = Realm.getDefaultInstance()

        tabLayout = view!!.findViewById(R.id.tabLayout)
        tabViewPager = view!!.findViewById(R.id.tabViewPager)
        tabLayout.setupWithViewPager(tabViewPager)

        tabPagerAdapter = TabPagerAdapter(activity!!.supportFragmentManager)
        tabViewPager.adapter = tabPagerAdapter

        return view
    }

    private inner class TabPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int) : Fragment {
            return tabFragments[position]
        }

        override fun getCount() : Int {
            return tabFragments.size
        }

        override fun getPageTitle(position: Int) : String {
            return tabTitles[position]
        }
    }
}
