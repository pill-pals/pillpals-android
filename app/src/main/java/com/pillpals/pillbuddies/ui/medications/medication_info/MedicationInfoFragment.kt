package com.pillpals.pillbuddies.ui.medications.medication_info

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.ui.AddDrugActivity
import io.realm.Realm

class MedicationInfoFragment : Fragment() {

    public lateinit var tabLayout: TabLayout
    public lateinit var tabViewPager: ViewPager
    public lateinit var addButton: Button

    private lateinit var realm: Realm

    private var tabFragments: List<MedicationInfoTextFragment> = mutableListOf(
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

    private var textParams : LayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    private var textTopMargin = 16
    private var headerSize = 20f
    private var bodySize = 16f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_medication_info, container,false)

        realm = Realm.getDefaultInstance()

        addButton = view!!.findViewById(R.id.addButton)
        tabLayout = view!!.findViewById(R.id.tabLayout)
        tabViewPager = view!!.findViewById(R.id.tabViewPager)
        tabLayout.setupWithViewPager(tabViewPager)

        tabPagerAdapter = TabPagerAdapter(activity!!.supportFragmentManager)
        tabViewPager.adapter = tabPagerAdapter
        tabViewPager.offscreenPageLimit = 3

        textParams.topMargin = textTopMargin

        addButton.setOnClickListener {
            val intent = Intent(context, AddDrugActivity::class.java)
            startActivityForResult(intent, 1)
        }

        return view
    }

    //Assumes that the headers and bodyText lists are ordered and have indices that correspond with each other 1:1
    private fun setTabText(tabIndex: Int, headers: List<String>, bodyText: List<String>) {
        var layout: LinearLayout = tabFragments[tabIndex].layout
        resetText(layout)
        for ((index, headerText) in headers.withIndex()) {
            addHeader(layout, headerText)
            addBody(layout, bodyText[index])
        }
    }

    private fun resetText(layout: ViewGroup) {
        layout.removeAllViews()
    }

    private fun addHeader(layout: ViewGroup, text: String) {
        appendText(layout, text, headerSize)
    }

    private fun addBody(layout: ViewGroup, text: String) {
        appendText(layout, text, bodySize)
    }

    private fun appendText(layout: ViewGroup, text: String, fontSize: Float) {
        var newView = TextView(context)
        newView.text = text
        newView.textSize = fontSize
        newView.layoutParams = textParams

        layout.addView(newView)
    }

    private inner class TabPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
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
