package com.pillpals.pillbuddies.ui.notifications

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.ui.AddDrugActivity
import com.pillpals.pillbuddies.ui.DrugCard
import io.realm.Realm
import kotlinx.android.synthetic.main.prompts.view.*
import java.util.*

class NotificationsFragment : Fragment() {


    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_notifications, container, false)

        realm = Realm.getDefaultInstance()

        return view
    }
}