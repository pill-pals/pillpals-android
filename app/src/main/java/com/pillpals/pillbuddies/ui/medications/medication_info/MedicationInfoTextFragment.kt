package com.pillpals.pillbuddies.ui.medications.medication_info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.pillpals.pillbuddies.R
import io.realm.Realm

class MedicationInfoTextFragment : Fragment() {

    public lateinit var layout: LinearLayout

    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_medication_info_text, container,false)

        layout = view!!.findViewById(R.id.textLayout)

        realm = Realm.getDefaultInstance()

        return view
    }
}
