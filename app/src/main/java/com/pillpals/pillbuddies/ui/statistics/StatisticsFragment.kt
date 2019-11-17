package com.pillpals.pillbuddies.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillpals.pillbuddies.R
import io.realm.Realm

class StatisticsFragment : Fragment() {


    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_statistics, container, false)

        realm = Realm.getDefaultInstance()

        return view
    }
}