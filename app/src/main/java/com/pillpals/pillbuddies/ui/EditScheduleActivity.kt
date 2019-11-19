package com.pillpals.pillbuddies.ui

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.pillpals.pillbuddies.R
import io.realm.Realm

import kotlinx.android.synthetic.main.activity_edit_schedule.*

class EditScheduleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_edit_schedule)

    }

}
