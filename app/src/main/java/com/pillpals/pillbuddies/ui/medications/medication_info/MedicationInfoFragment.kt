package com.pillpals.pillbuddies.ui.medications.medication_info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayout

import com.pillpals.pillbuddies.R
import io.realm.Realm

class MedicationInfoFragment : Fragment() {

    public lateinit var tabLayout: TabLayout
    public lateinit var contentFrame: FrameLayout

    private lateinit var realm: Realm

    private lateinit var tabFragments: List<Fragment>
    private var loadingTab: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_medication_info, container,false)

        realm = Realm.getDefaultInstance()

        tabLayout = view!!.findViewById(R.id.tabLayout)
        contentFrame = view!!.findViewById(R.id.tabContentFrame)

        //TODO: Add these in a loop or something (they probably won't all be text fragments eventually anyway)
        tabFragments = mutableListOf(MedicationInfoTextFragment(), MedicationInfoTextFragment(), MedicationInfoTextFragment(), MedicationInfoTextFragment())

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                loadTabContent(tab.position)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                //TODO: Action if the currently selected tab is clicked again (could scroll back to top)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                //TODO: Action if a different tab is selected
            }
        })

        loadTabContent(0) //Initialize default (first) tab

        return view
    }

    private fun loadTabContent(tabInd: Int) {
        if (tabFragments.size != 0 && tabFragments.size > tabInd && !loadingTab) {
            loadingTab = true
            var fm : FragmentManager = activity!!.supportFragmentManager
            var ft : FragmentTransaction = fm.beginTransaction()
            ft.replace(R.id.tabContentFrame, tabFragments[tabInd])
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            ft.commit()
            loadingTab = false
        }
    }
}
