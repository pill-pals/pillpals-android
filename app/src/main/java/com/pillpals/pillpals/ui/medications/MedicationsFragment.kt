package com.pillpals.pillpals.ui.medications

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.ui.AddDrugActivity
import com.pillpals.pillpals.ui.DrugCard
import io.realm.Realm
import java.util.*
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuInflater
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.pillpals.pillpals.data.model.DPDObjects
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getCorrectIconDrawable
import com.pillpals.pillpals.helpers.FileWriter
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.readAllData
import com.pillpals.pillpals.helpers.calculateScheduleRecords
import com.pillpals.pillpals.ui.ScheduleRecord
import com.pillpals.pillpals.ui.medications.medication_info.MedicationInfoActivity
import com.pillpals.pillpals.ui.search.SearchActivity
import io.realm.RealmResults
import kotlinx.android.synthetic.main.add_medication_prompt.view.*
import kotlinx.android.synthetic.main.delete_prompt.view.*
import kotlinx.android.synthetic.main.drug_card.view.*

class MedicationsFragment : Fragment() {

    public lateinit var drugButton: Button
    public lateinit var stack: LinearLayout

    private lateinit var prefs: SharedPreferences

    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        FileWriter.createJSONStringFromData(context!!)
        val view = inflater!!.inflate(R.layout.fragment_medications, container,false)

        realm = Realm.getDefaultInstance()

        prefs = activity!!.getPreferences(Context.MODE_PRIVATE)

        stack = view!!.findViewById(R.id.stack)
        stack.layoutTransition.enableTransitionType(LayoutTransition.CHANGING) //Makes collapsing smooth

        drugButton = view!!.findViewById(R.id.drugButton)
        getActivity()!!.invalidateOptionsMenu()

        drugButton.setOnClickListener {
            val addPrompt = LayoutInflater.from(this.context).inflate(R.layout.add_medication_prompt, null)

            val title = SpannableString("Add medication")
            title.setSpan(
                ForegroundColorSpan(this!!.resources.getColor(R.color.colorLightGrey)),
                0,
                title.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(context!!)
                .setView(addPrompt)
                .setTitle(title)

            val alertDialog = dialogBuilder.show()
            addPrompt.dialogScanBtn.setOnClickListener {
                alertDialog.dismiss()
                Log.i("scan", "in progress")
            }

            addPrompt.dialogSearchBtn.setOnClickListener {
                alertDialog.dismiss()
                val intent = Intent(context, SearchActivity::class.java)
                startActivityForResult(intent, 2)
            }

            addPrompt.dialogManualBtn.setOnClickListener {
                alertDialog.dismiss()
                val intent = Intent(context, AddDrugActivity::class.java)
                startActivityForResult(intent, 1)
            }



        }

        updateMedicationList()

        return view
    }

    override fun onResume() {
        super.onResume()
        getActivity()!!.invalidateOptionsMenu()
        FileWriter.createJSONStringFromData(context!!)
    }

    private fun popoverMenuMedication(v: View, medication: Medications) {
        val popup = PopupMenu(context, v.overflowMenu)
        val inflater: MenuInflater = popup.menuInflater
        val dpdObject = medication.dpd_object?.firstOrNull()
        if(dpdObject == null) {
            inflater.inflate(R.menu.medication, popup.menu)
        }
        else {
            inflater.inflate(R.menu.medication_dpd, popup.menu)
        }
        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.medicationDelete -> {
                    val deleteDialog = LayoutInflater.from(this.context).inflate(R.layout.delete_prompt, null)

                    val title = SpannableString("Delete " + medication.name)
                    title.setSpan(
                        ForegroundColorSpan(this!!.resources.getColor(R.color.colorLightGrey)),
                        0,
                        title.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(context!!)
                        .setView(deleteDialog)
                        .setTitle(title)

                    val deleteDrugName = deleteDialog!!.findViewById<TextView>(R.id.deleteDrugName)
                    deleteDrugName.text = medication.name

                    val deleteAlertDialog = dialogBuilder.show()
                    deleteDialog.dialogConfirmBtn.setOnClickListener {
                        deleteAlertDialog.dismiss()
                        deleteMedication(medication)
                        updateMedicationList()
                    }

                    deleteDialog.dialogCancelBtn.setOnClickListener {
                        deleteAlertDialog.dismiss()
                    }
                }
                R.id.viewDrugInfo -> {
                    if(dpdObject == null) return@setOnMenuItemClickListener true

                    val intent = Intent(context, MedicationInfoActivity::class.java)
                    intent.putExtra("drug-code", dpdObject.dpd_id)
                    intent.putExtra("icon-color", getColorStringByID(medication.color_id))
                    intent.putStringArrayListExtra("administration-routes", ArrayList(dpdObject.administrationRoutes))
                    intent.putStringArrayListExtra("active-ingredients",  ArrayList(dpdObject.activeIngredients))
                    intent.putExtra("dosage-string", dpdObject.dosageString)
                    intent.putExtra("name-text", dpdObject.name)
                    startActivityForResult(intent, 2)
                }
                R.id.linkMedication -> {
                    val intent = Intent(context, SearchActivity::class.java)
                    intent.putExtra("medication-uid", medication.uid)
                    startActivityForResult(intent, 3)
                }
            }
            true
        }
        popup.show()
    }

    private fun updateMedicationList() {
        stack.removeAllViews()
        for (drug in realm.where(Medications::class.java).findAll()) {
            if (drug.deleted) {
                continue
            }

            addDrugCard(drug)
        }
    }

    private fun addDrugCard(medication: Medications) {
        var newCard = DrugCard(this.context!!)

        newCard.nameText.text = medication.name
        newCard.altText.text = medication.dosage
        newCard.iconBackground.setCardBackgroundColor(Color.parseColor(getColorStringByID(medication.color_id)))
        newCard.icon.setImageDrawable(getCorrectIconDrawable(this.context!!, medication))

        newCard.setOnClickListener {
            val dpdObject = medication.dpd_object?.firstOrNull() ?: return@setOnClickListener

            val intent = Intent(context, MedicationInfoActivity::class.java)
            intent.putExtra("drug-code", dpdObject.dpd_id)
            intent.putExtra("icon-color", getColorStringByID(medication.color_id))
            intent.putStringArrayListExtra("administration-routes", ArrayList(dpdObject.administrationRoutes))
            intent.putStringArrayListExtra("active-ingredients",  ArrayList(dpdObject.activeIngredients))
            intent.putExtra("dosage-string", dpdObject.dosageString)
            intent.putExtra("name-text", dpdObject.name)
            startActivityForResult(intent, 4)
        }

        newCard.button.setOnClickListener {
            val intent = Intent(context, AddDrugActivity::class.java)
            intent.putExtra("medication-uid", medication.uid)
            startActivityForResult(intent, 1)
        }
        newCard.button.text = "Edit"
        newCard.button.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        newCard.button.visibility = View.VISIBLE

        newCard.overflowMenu.setOnClickListener {
            popoverMenuMedication(newCard, medication)
        }

        newCard.drugCardLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        var recordList = calculateScheduleRecords(medication.schedules, activity!!)
        recordList.forEach { record ->
            record.deleteScheduleImage.visibility = View.GONE
            newCard.scheduleStack.addView(record)
        }

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
            putBoolean(getString(R.string.schedule_preview_collapsed_prefix) + medication.uid, !previouslyCollapsed)
            commit()
        }

        //TODO: Use preferences to save collapsed state of each stack, similar to dashboard
    }

    private fun createMedicationData(drugName: String, drugDose: String) {
        realm.executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = drugName
            medication.dosage = drugDose
        }
        updateMedicationList()
    }

    private fun deleteMedication(medication: Medications) {
        realm.executeTransaction {
            // medication is copied from realm, not the real object
            val databaseDrug = realm.where(Medications::class.java).equalTo("uid", medication.uid).findFirst()!!
            databaseDrug.deleted = true
        }
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 1 -> add drug
        // 2 -> view drug info
        // 3 -> link drug
        // 4 -> search for drug

        val schedules = readAllData(Schedules::class.java) as RealmResults<out Schedules>
        schedules.forEach {
            if(it.medication?.firstOrNull() == null) DatabaseHelper.obliterateSchedule(it)
        }

        updateMedicationList()
    }
}