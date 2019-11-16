package com.pillpals.pillbuddies.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import com.pillpals.pillbuddies.ui.AddDrugActivity
import com.pillpals.pillbuddies.ui.DrugCard
import io.realm.Realm
import kotlinx.android.synthetic.main.prompts.view.*
import java.util.*
import android.content.Intent

class HomeFragment : Fragment() {

    public lateinit var drugButton: Button
    public lateinit var btnNewActivity: Button
    public lateinit var stack: LinearLayout

    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_home, container,false)

        realm = Realm.getDefaultInstance()

        drugButton = view!!.findViewById(R.id.drugButton)
        stack = view!!.findViewById(R.id.stack)

        drugButton.setOnClickListener{

            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this.context).inflate(R.layout.prompts, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this.context)
                .setView(mDialogView)
                .setTitle("Medication Form")
            //show dialog

            //dialogAddBtn = mDialogView!!.findViewById(R.id.dialogAddBtn)
            //dialogCancelBtn = mDialogView!!.findViewById(R.id.dialogCancelBtn)
            //medNameUserInput = mDialogView!!.findViewById(R.id.medNameUserInput)
            //dosageNameUserInput = mDialogView!!.findViewById(R.id.dosageNameUserInput)

            val  mAlertDialog = mBuilder.show()
            mDialogView.dialogAddBtn.setOnClickListener {
                mAlertDialog.dismiss()
                val name = mDialogView.medNameUserInput.text.toString()
                val dosage = mDialogView.dosageNameUserInput.text.toString()
                createMedicationData(name, dosage)
                //Log.i("medications", DatabaseHelper.readAllData(Medications::class.java).toString())
                //DatabaseHelper.readAllData(Medications::class.java)
            }
            //cancel button click of custom layout
            mDialogView.dialogCancelBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }

        }

        btnNewActivity = view!!.findViewById(R.id.btnNewActivity)

        btnNewActivity.setOnClickListener {
            val intent = Intent(context, AddDrugActivity::class.java)
            startActivity(intent);
        }

        updateMedicationList()

        return view
    }

    private fun updateMedicationList() {
        stack.removeAllViews()
        for (drug in realm.where(Medications::class.java).findAll()) {
            addDrugCard(realm.copyFromRealm(drug))
        }
    }

    private fun addDrugCard(medication: Medications) {
        var new = DrugCard(this.context!!)

        new.medicationNameText.text = medication.name
        new.medicationDueText.text = medication.dosage

        new.medicationLogButton.setOnClickListener {
            //Edit drug settings
        }
        new.medicationLogButton.text = "Edit"
        new.medicationLogButton.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        new.medicationLogButton.visibility = View.VISIBLE
        
        stack.addView(new)
    }

    private fun createMedicationData(drugName: String, drugDose: String) {
        realm.executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = drugName
            medication.dosage = drugDose
        }
        updateMedicationList()
    }
}