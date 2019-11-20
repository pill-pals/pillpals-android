package com.pillpals.pillbuddies.ui

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.pillpals.pillbuddies.R
import io.realm.Realm

class EditMedicationIcon : AppCompatActivity() {

    public lateinit var colorLists : LinearLayout
    public lateinit var bottomOptions: BottomOptions
    public lateinit var lightColorList : LinearLayout
    public lateinit var mediumColorList : LinearLayout
    public lateinit var heavyColorList : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_edit_medication_icon)

        colorLists = findViewById(R.id.colorLists)
        bottomOptions = findViewById(R.id.bottomOptions)
        bottomOptions.leftButton.text = "Apply"
        bottomOptions.rightButton.text = "Cancel"
        lightColorList = findViewById(R.id.lightColorList)
        mediumColorList = findViewById(R.id.mediumColorList)
        heavyColorList = findViewById(R.id.heavyColorList)

        bottomOptions.leftButton.setOnClickListener{
            finish()
        }

        bottomOptions.rightButton.setOnClickListener{
            finish()
        }
    }

}
