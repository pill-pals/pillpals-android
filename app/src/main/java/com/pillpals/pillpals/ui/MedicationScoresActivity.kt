package com.pillpals.pillpals.ui

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.util.Calendar

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getMedicationByUid

import java.util.*
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.ui.medications.medication_info.MedicationInfoActivity
import io.realm.RealmObject.deleteFromRealm
import kotlinx.android.synthetic.main.delete_prompt.view.*
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

        newCard.setOnClickListener {
            val dpdObject = medication.dpd_object?.firstOrNull() ?: return@setOnClickListener

            val intent = Intent(this, MedicationInfoActivity::class.java)
            intent.putExtra("drug-code", dpdObject.dpd_id)
            intent.putExtra("icon-color", getColorStringByID(medication.color_id))
            intent.putStringArrayListExtra("administration-routes", ArrayList(dpdObject.administrationRoutes))
            intent.putStringArrayListExtra("active-ingredients",  ArrayList(dpdObject.activeIngredients))
            intent.putExtra("dosage-string", dpdObject.dosageString)
            intent.putExtra("name-text", dpdObject.name)
            startActivityForResult(intent, 2)
        }

        newCard.drugCardLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        newCard.scheduleContainer.visibility = View.VISIBLE
        if (prefs.getBoolean(getString(R.string.schedule_preview_collapsed_prefix) + medication.uid, false)) {
            newCard.scheduleStack.visibility = View.GONE
            newCard.collapseButton.setImageResource(R.drawable.ic_circle_chevron_down_from_up)
        }

        newCard.collapseButton.setOnClickListener {
            toggleCollapse(newCard.scheduleStack, newCard.collapseButton, medication)
        }

        stack.addView(newCard)
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
            putBoolean(getString(com.pillpals.pillpals.R.string.schedule_preview_collapsed_prefix) + medication.uid, !previouslyCollapsed)
            commit()
        }

        //TODO: Use preferences to save collapsed state of each stack, similar to dashboard
    }

}