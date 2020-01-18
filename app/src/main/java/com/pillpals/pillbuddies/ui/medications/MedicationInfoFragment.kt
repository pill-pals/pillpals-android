package com.pillpals.pillbuddies.ui.medications

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.pillpals.pillbuddies.R
import io.realm.Realm

class MedicationInfoFragment : Fragment() {

    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_medication_info, container,false)

        realm = Realm.getDefaultInstance()

        return view
    }
}
