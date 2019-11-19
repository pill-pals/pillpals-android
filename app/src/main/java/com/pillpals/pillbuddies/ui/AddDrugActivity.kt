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

import androidx.appcompat.app.AppCompatActivity
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
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.helpers.DateHelper
import android.util.Log;

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

        if (intent.hasExtra("medication-uid")) {
            val medID: String = intent.getStringExtra("medication-uid")
            val medication = getMedicationByUid(medID) as Medications

            editText.setText(medication.name)
            editText2.setText(medication.dosage)
            editText3.setText(medication.notes)

            var scheduleRecords = MutableList(0) { ScheduleRecord(this) }
            var compiledScheduleRecords = MutableList(0) { CompiledScheduleRecord("")}

            medication.schedules.forEach {
                val timeString = DateHelper.dateToString(it.occurrence!!)
                if(isWeeklyRecurrence(it)) {
                    val cal = Calendar.getInstance()
                    cal.time = it.occurrence
                    if(compiledScheduleRecordExists(timeString, compiledScheduleRecords)) {
                        //add day of week to existing compiled schedule record
                        val compiledScheduleRecord = compiledScheduleRecords.find {it.time == timeString}
                        compiledScheduleRecord!!.daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1] = 1
                    } else {
                        //create new compiled schedule record
                        val compiledScheduleRecord = CompiledScheduleRecord(timeString)
                        compiledScheduleRecord.daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1] = 1
                        compiledScheduleRecords.add(compiledScheduleRecord)
                    }
                } else {
                    val scheduleRecord = ScheduleRecord(this)

                    scheduleRecord.timeText.text = timeString
                    scheduleRecord.recurrenceText.text = "every"
                    scheduleRecord.dateText.text = getRecurrenceString(it.repetitionUnit!!,it.repetitionCount!!)
                    scheduleRecords.add(scheduleRecord)
                }
            }

            //create scheduleRecords from compiledScheduleRecords
            compiledScheduleRecords.forEach {
                val scheduleRecord = ScheduleRecord(this)

                scheduleRecord.timeText.text = it.time
                scheduleRecord.recurrenceText.text = "on"
                scheduleRecord.dateText.text = getDaysOfWeekList(it.daysOfWeek)
                scheduleRecords.add(scheduleRecord)
            }

            scheduleRecords.forEach {
                scheduleStack.addView(it)
            }

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
        val a = (schedule.repetitionCount == 7 && DateHelper.getUnitByIndex(schedule.repetitionUnit!!) == Calendar.DATE)
        val b = (schedule.repetitionCount == 1 && DateHelper.getUnitByIndex(schedule.repetitionUnit!!) == Calendar.WEEK_OF_YEAR)
        return (a || b)
    }

    private fun getRecurrenceString(repetitionUnit: Int, repetitionCount: Int):String {
        if (repetitionCount == 1) {
            return when (DateHelper.getUnitByIndex(repetitionUnit)) {
                Calendar.YEAR -> "year"
                Calendar.MONTH -> "month"
                Calendar.WEEK_OF_YEAR -> "week"
                Calendar.DATE -> "day"
                Calendar.HOUR_OF_DAY -> "hour"
                Calendar.MINUTE -> "minute"
                Calendar.SECOND -> "second"
                else -> "MISSING"
            }
        } else {
            return when (DateHelper.getUnitByIndex(repetitionUnit)) {
                Calendar.YEAR -> "$repetitionCount years"
                Calendar.MONTH -> "$repetitionCount months"
                Calendar.WEEK_OF_YEAR -> "$repetitionCount weeks"
                Calendar.DATE -> "$repetitionCount days"
                Calendar.HOUR_OF_DAY -> "$repetitionCount hours"
                Calendar.MINUTE -> "$repetitionCount minutes"
                Calendar.SECOND ->  "$repetitionCount seconds"
                else -> "MISSING"
            }
        }
    }

    private fun compiledScheduleRecordExists(timeString: String, compiledScheduleRecords: MutableList<CompiledScheduleRecord>):Boolean {
        for (i in compiledScheduleRecords.indices) {
            if(timeString == compiledScheduleRecords[i].time)
                return true
        }
        return false
    }

    private fun getDaysOfWeekList(daysOfWeek: IntArray):String {
        val daysOfWeekList = listOf("Sun","Mon","Tue","Wed","Thurs","Fri","Sat")
        var list = ""
        var firstVal = true
        for (i in daysOfWeek.indices) {
            if (daysOfWeek[i] == 1 && firstVal) {
                list += daysOfWeekList[i]
                firstVal = false
            } else if (daysOfWeek[i] == 1) {
                list += ", ${daysOfWeekList[i]}"
            }
        }
        return list
    }
}

data class CompiledScheduleRecord(val time: String) {
    var daysOfWeek: IntArray = IntArray(7) {0}
}


