package com.pillpals.pillpals.ui.statistics

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.LinearLayout

import androidx.appcompat.app.AppCompatActivity
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications

import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.ui.DataPair
import com.pillpals.pillpals.ui.DrugCard
import kotlinx.android.synthetic.main.drug_card.view.*
import io.realm.Realm


class MedicationScoresActivity : AppCompatActivity() {

    public lateinit var stack: LinearLayout
    private lateinit var realm: Realm
    private lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        realm = Realm.getDefaultInstance()

        prefs = getPreferences(Context.MODE_PRIVATE)

        setContentView(R.layout.activity_medication_scores)

        stack = this!!.findViewById(R.id.stack)
        stack.layoutTransition.enableTransitionType(LayoutTransition.CHANGING) //Makes collapsing smooth

        for (drug in realm.where(Medications::class.java).findAll()) {
            if (drug.deleted) {
                continue
            }

            addDrugCard(drug)
        }
        Log.i("text",stack.toString())
    }

    private fun addDrugCard(medication: Medications) {
        var newCard = DrugCard(this)

        newCard.nameText.text = medication.name
        newCard.altText.text = medication.dosage
        newCard.iconBackground.setCardBackgroundColor(Color.parseColor(getColorStringByID(medication.color_id)))
        newCard.icon.setImageDrawable(
            DatabaseHelper.getCorrectIconDrawable(
                this,
                medication
            )
        )
        newCard.overflowMenu.visibility = View.GONE

        newCard.drugCardLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        newCard.scheduleContainer.visibility = View.VISIBLE
        if (prefs.getBoolean(getString(R.string.schedule_preview_collapsed_prefix) + medication.uid, false)) {
            newCard.scheduleStack.visibility = View.GONE
            newCard.collapseButton.setImageResource(R.drawable.ic_circle_chevron_down_from_up)
        }

        newCard.collapseButton.setOnClickListener {
            toggleCollapse(newCard.scheduleStack, newCard.collapseButton, medication)
        }

        var dataPairs = calculateDataPairs(medication)
        dataPairs.forEach { dataPair ->
            newCard.scheduleStack.addView(dataPair)

        }

        stack.addView(newCard)
    }

    private fun calculateDataPairs(medication: Medications):MutableList<DataPair> {
        var dataPairs = mutableListOf<DataPair>()

        var dataPair1 = DataPair(this)
        dataPair1.key.text = "Data Key 1"
        dataPair1.value.text = "Value 1"
        dataPair1.drawableValue.visibility = View.GONE
        dataPairs.add(dataPair1)

        var dataPair2 = DataPair(this)
        dataPair2.key.text = "Data Key 2"
        dataPair2.value.text = "Value 2"
        dataPairs.add(dataPair2)

        return dataPairs
    }

    private fun toggleCollapse(stack: LinearLayout, button: ImageButton, medication: Medications) {
        var previouslyCollapsed = (stack.visibility == View.GONE)
        if (previouslyCollapsed) {
            button.setImageResource(R.drawable.ic_circle_chevron_up_from_down)
            (button.drawable as AnimatedVectorDrawable).start()
            stack.visibility = View.VISIBLE

        } else {
            button.setImageResource(R.drawable.ic_circle_chevron_down_from_up)
            (button.drawable as AnimatedVectorDrawable).start()
            stack.visibility = View.GONE
        }

        with (prefs.edit()) {
            putBoolean(getString(R.string.schedule_preview_collapsed_prefix) + medication.uid, !previouslyCollapsed)
            commit()
        }
    }
}