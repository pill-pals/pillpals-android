package com.pillpals.pillbuddies.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import android.graphics.Color
import android.util.Log
import io.realm.Realm
import androidx.cardview.widget.CardView


class EditMedicationIcon : AppCompatActivity() {

    public lateinit var colorLists : LinearLayout
    public lateinit var bottomOptions: BottomOptions
    public lateinit var lightColorList : LinearLayout
    public lateinit var mediumColorList : LinearLayout
    public lateinit var heavyColorList : LinearLayout
    var colorString = "#FFFFFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_edit_medication_icon)

        if(intent.hasExtra("medication-uid")) {
            val medID: String = intent.getStringExtra("medication-uid")
            val medication = DatabaseHelper.getMedicationByUid(medID) as Medications
            colorString = medication.color
        }

        colorLists = findViewById(R.id.colorLists)
        bottomOptions = findViewById(R.id.bottomOptions)
        bottomOptions.leftButton.text = "Apply"
        bottomOptions.rightButton.text = "Cancel"
        lightColorList = findViewById(R.id.lightColorList)
        mediumColorList = findViewById(R.id.mediumColorList)
        heavyColorList = findViewById(R.id.heavyColorList)

        addBorderToCards()
        for (i in 0 until lightColorList.getChildCount()) {
            val borderCard = lightColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView

            card.setOnClickListener {
                colorString = String.format("#%06X", 0xFFFFFF and (it as CardView).cardBackgroundColor.defaultColor)
                addBorderToCards()
            }
        }
        for (i in 0 until mediumColorList.getChildCount()) {
            val borderCard = mediumColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView

            card.setOnClickListener {
                colorString = String.format("#%06X", 0xFFFFFF and (it as CardView).cardBackgroundColor.defaultColor)
                addBorderToCards()
            }
        }
        for (i in 0 until heavyColorList.getChildCount()) {
            val borderCard = heavyColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView

            card.setOnClickListener {
                colorString = String.format("#%06X", 0xFFFFFF and (it as CardView).cardBackgroundColor.defaultColor)
                addBorderToCards()
            }
        }

        bottomOptions.leftButton.setOnClickListener{
            val resultIntent = Intent(this, EditScheduleActivity::class.java)
            resultIntent.putExtra("color-string", colorString)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        bottomOptions.rightButton.setOnClickListener{
            finish()
        }
    }

    fun addBorderToCards() {
        for (i in 0 until lightColorList.getChildCount()) {
            val borderCard = lightColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val cardColorString = String.format("#%06X", 0xFFFFFF and card.cardBackgroundColor.defaultColor)
            if (cardColorString == colorString) {
                borderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            else {
                borderCard.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }
        for (i in 0 until mediumColorList.getChildCount()) {
            val borderCard = mediumColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val cardColorString = String.format("#%06X", 0xFFFFFF and card.cardBackgroundColor.defaultColor)
            if (cardColorString == colorString) {
                borderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            else {
                borderCard.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }
        for (i in 0 until heavyColorList.getChildCount()) {
            val borderCard = heavyColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val cardColorString = String.format("#%06X", 0xFFFFFF and card.cardBackgroundColor.defaultColor)
            if (cardColorString == colorString) {
                borderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            else {
                borderCard.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }
    }
}
