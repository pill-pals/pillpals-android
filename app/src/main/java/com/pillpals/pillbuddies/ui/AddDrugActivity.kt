package com.pillpals.pillbuddies.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.util.Calendar

import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getMedicationByUid
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_add_drug.*
import kotlinx.android.synthetic.main.bottom_options.*
import kotlinx.android.synthetic.main.delete_prompt.view.*
import kotlinx.android.synthetic.main.prompts.view.*
import kotlinx.android.synthetic.main.prompts.view.dialogCancelBtn
import java.util.*
import kotlin.collections.ArrayList
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.helpers.DateHelper

import android.util.Log
class AddDrugActivity : AppCompatActivity() {

    public lateinit var editText: EditText
    public lateinit var editText2: EditText
    public lateinit var editText3: EditText
    public lateinit var deleteButton: TextView
    public lateinit var scheduleStack: LinearLayout
    public lateinit var bottomOptions: BottomOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        setContentView(R.layout.activity_add_drug)
        editText = findViewById(R.id.editText)
        editText2 = findViewById(R.id.editText2)
        editText3 = findViewById(R.id.editText3)
        deleteButton = findViewById(R.id.deleteButton)
        scheduleStack = findViewById(R.id.scheduleStack)
        bottomOptions = findViewById(R.id.bottomOptions)
        bottomOptions.leftButton.text = "Save"
        bottomOptions.rightButton.text = "Cancel"


        val testSchedule = ScheduleRecord(this)
        scheduleStack.addView(testSchedule)

        if (intent.hasExtra("medication-uid")) {
            val medID: String = intent.getStringExtra("medication-uid")
            val medication = getMedicationByUid(medID) as Medications

            editText.setText(medication.name)
            editText2.setText(medication.dosage)
            editText3.setText(medication.notes)

            var scheduleRecords = MutableList(0) { ScheduleRecord(this) }
            medication.schedules.forEach {
                if(isWeeklyRecurrence(it)) {

                }
            }
            //group the medications by time if recurrence type is weekly
            //create a ScheduleRecord for each group
            //timeText => time with AM/PM
            //recurrenceText => determined by recurrence type
            //dateText => determined by recurrence type and entries in the group
            //add all scheduleRecords to the stack

            bottomOptions.leftButton.setOnClickListener{
                if (editText.text.toString().trim().isNotEmpty() and editText2.text.toString().trim().isNotEmpty()) {
                    updateMedicationData(
                        medication,
                        editText.text.toString(),
                        editText2.text.toString(),
                        editText3.text.toString()
                    )
                    finish()
                } else{
                    Toast.makeText(applicationContext, "Please set a name and dosage", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            bottomOptions.leftButton.setOnClickListener{
                if (editText.text.toString().trim().isNotEmpty() and editText2.text.toString().trim().isNotEmpty()) {
                    createMedicationData(
                        editText.text.toString(),
                        editText2.text.toString(),
                        editText3.text.toString()
                    )
                    finish()
                } else{
                    Toast.makeText(applicationContext, "Please set a name and dosage", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bottomOptions.rightButton.setOnClickListener{
            finish()
        }

        if (intent.hasExtra("medication-uid")) {
            val medID: String = intent.getStringExtra("medication-uid")
            val medication = getMedicationByUid(medID) as Medications
            // Delete button
            deleteButton.setOnClickListener{
                val deleteDialog = LayoutInflater.from(this).inflate(R.layout.delete_prompt, null)

                val dialogBuilder = AlertDialog.Builder(this)
                    .setView(deleteDialog)
                    .setTitle("Delete " + medication.name)

                val deleteDrugName = deleteDialog!!.findViewById<TextView>(R.id.deleteDrugName)
                deleteDrugName.text = medication.name

                val deleteAlertDialog = dialogBuilder.show()
                deleteDialog.dialogConfirmBtn.setOnClickListener {
                    deleteAlertDialog.dismiss()
                    deleteMedication(medication)
                    finish()
                }

                deleteDialog.dialogCancelBtn.setOnClickListener {
                    deleteAlertDialog.dismiss()
                }
            }
        }
        else {
            deleteButton.visibility = LinearLayout.GONE
        }
    }

    private fun updateMedicationData(medication: Medications, drugName: String, drugDose: String, drugNote: String) {
        Realm.getDefaultInstance().executeTransaction {
            medication.name = drugName
            medication.dosage = drugDose
            medication.notes = drugNote
        }
    }

    private fun createMedicationData(drugName: String, drugDose: String, drugNote: String) {
        Realm.getDefaultInstance().executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = drugName
            medication.dosage = drugDose
            medication.notes = drugNote
        }
    }

    private fun deleteMedication(medication: Medications) {
        Log.i("oof", medication.toString())
        Realm.getDefaultInstance().executeTransaction {
            medication.deleted = true
        }
    }

    private fun isWeeklyRecurrence(schedule: Schedules):Boolean {
        //val a = (schedule.repetitionCount == 7 && DateHelper.getUnitByIndex(schedule.repetitionUnit!!) == Calendar.DATE)
        //val b = (schedule.repetitionCount == 1 && DateHelper.getUnitByIndex(schedule.repetitionUnit!!) == Calendar.WEEK)
        return (schedule.repetitionCount == 7 && DateHelper.getUnitByIndex(schedule.repetitionUnit!!) == Calendar.DATE)//a || b
    }
}

