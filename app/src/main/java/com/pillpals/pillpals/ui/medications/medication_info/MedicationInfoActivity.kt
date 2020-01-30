package com.pillpals.pillpals.ui.medications.medication_info

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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

import com.pillpals.pillpals.R
import com.pillpals.pillpals.ui.AddDrugActivity
import io.realm.Realm
import android.view.MenuItem
import android.widget.ImageView
import androidx.cardview.widget.CardView


class MedicationInfoActivity : AppCompatActivity() {

    public lateinit var tabLayout: TabLayout
    public lateinit var tabViewPager: ViewPager
    public lateinit var addButton: Button
    public lateinit var iconBackground: CardView
    public lateinit var medicationIcon: ImageView
    public lateinit var nameText: TextView
    public lateinit var dosageText: TextView
    public var drugCode: Int = 0
    public var colorString: String = "#D3D3D3"
    public var administrationRoutes = listOf<String>()
    public var activeIngredients = listOf<String>()
    public var iconResource = R.drawable.ic_pill_v5
    public var dosageString = ""
    public var nameString = ""

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
        iconBackground = findViewById(R.id.iconBackground)
        medicationIcon = findViewById(R.id.medicationIcon)
        nameText = findViewById(R.id.nameText)
        dosageText = findViewById(R.id.dosageText)

        drugCode = intent.getIntExtra("drug-code", 0)
        colorString = intent.getStringExtra("icon-color")!!
        administrationRoutes = intent.getStringArrayListExtra("administration-routes")!!.toList()
        activeIngredients = intent.getStringArrayListExtra("active-ingredients")!!.toList()
        dosageString = intent.getStringExtra("dosage-string")!!
        nameString = intent.getStringExtra("name-text")!!
        if(intent.hasExtra("icon-resource")) {
            iconResource = intent.getIntExtra("icon-resource", R.drawable.ic_pill_v5)
        }

        iconBackground.setCardBackgroundColor(Color.parseColor(colorString))
        medicationIcon.setImageResource(iconResource)
        nameText.text = nameString
        dosageText.text = dosageString

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

    public fun tabFragmentLoaded() {
        setTabText(0, listOf("Active Ingredients", "Administration Routes"), listOf(bulletedList(activeIngredients), bulletedList(administrationRoutes)))
    }

    private fun bulletedList(list: List<String>): String {
        return "• " + list.joinToString("\n• ")
    }

    //Assumes that the headers and bodyText lists are ordered and have indices that correspond with each other 1:1
    private fun setTabText(tabIndex: Int, headers: List<String>, bodyText: List<String>) {
        val layout: LinearLayout = tabPagerAdapter.getItem(tabIndex).layout
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
        override fun getItem(position: Int) : MedicationInfoTextFragment {
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
