package com.pillpals.pillbuddies.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.prompts.*
import kotlinx.android.synthetic.main.prompts.view.*
import java.util.*

class HomeFragment : Fragment() {

    public lateinit var drugButton: Button

    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_home, container,false)

        realm = Realm.getDefaultInstance()

        drugButton = view!!.findViewById(R.id.drugButton)

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

        return view
    }

    private fun createMedicationData(drugName: String, drugDose: String) {
        realm.executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = drugName
            medication.dosage = drugDose
        }
    }
}