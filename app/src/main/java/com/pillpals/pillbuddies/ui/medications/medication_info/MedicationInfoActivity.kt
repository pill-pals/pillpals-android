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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.ui.AddDrugActivity
import io.realm.Realm
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.MenuItem
import androidx.core.app.NavUtils


class MedicationInfoActivity : AppCompatActivity() {

    public lateinit var tabLayout: TabLayout
    public lateinit var tabViewPager: ViewPager
    public lateinit var addButton: Button
    public var drugCode: Int = 0

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_medication_info)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        addButton = findViewById(R.id.addButton)
        tabLayout = findViewById(R.id.tabLayout)
        tabViewPager = findViewById(R.id.tabViewPager)
        tabLayout.setupWithViewPager(tabViewPager)
        drugCode = intent.getIntExtra("drug-code", 0)

        tabPagerAdapter = TabPagerAdapter(supportFragmentManager)
        tabViewPager.adapter = tabPagerAdapter
        tabViewPager.offscreenPageLimit = 3
        tabViewPager.pageMargin = 48

        textParams.topMargin = textTopMargin

        addButton.setOnClickListener {
            val intent = Intent(this, AddDrugActivity::class.java)
            startActivityForResult(intent, 1)
        }
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
        var newView = TextView(this)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
