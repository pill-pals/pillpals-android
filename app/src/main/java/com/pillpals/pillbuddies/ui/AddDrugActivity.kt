package com.pillpals.pillbuddies.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.google.android.material.button.MaterialButton
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getColorIDByString
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getIconIDByString
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getRandomColorString
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getRandomIcon
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getScheduleByUid
import io.realm.RealmObject.deleteFromRealm

class AddDrugActivity : AppCompatActivity() {

    public lateinit var editText: EditText
    public lateinit var editText2: EditText
    public lateinit var editText3: EditText
    public lateinit var addScheduleButton : MaterialButton
    public lateinit var deleteButton: TextView
    public lateinit var scheduleStack: LinearLayout
    public lateinit var bottomOptions: BottomOptions
    public lateinit var iconButton: MaterialButton

    public var scheduleRecordsSetToDelete = mutableListOf<ScheduleRecord>()
    public lateinit var scheduleIdList: ArrayList<String>
    public val toBeAdded: MutableList<Schedules> = ArrayList()
    public lateinit var colorString: String
    public lateinit var imageDrawable: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        setContentView(R.layout.activity_add_drug)
        editText = findViewById(R.id.editText)
        editText2 = findViewById(R.id.editText2)
        editText3 = findViewById(R.id.editText3)
        addScheduleButton = findViewById(R.id.addScheduleButton)
        deleteButton = findViewById(R.id.deleteButton)
        scheduleStack = findViewById(R.id.scheduleStack)
        iconButton = findViewById(R.id.iconButton)
        bottomOptions = findViewById(R.id.bottomOptions)
        bottomOptions.leftButton.text = "Save"
        bottomOptions.rightButton.text = "Cancel"

        // region Bottom Button Listeners
        if (intent.hasExtra("medication-uid")) {
            val medID: String = intent.getStringExtra("medication-uid")
            val medication = getMedicationByUid(medID) as Medications

            editText.setText(medication.name)
            editText2.setText(medication.dosage)
            editText3.setText(medication.notes)
            colorString = DatabaseHelper.getColorStringByID(medication.color_id)
            iconButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(
                DatabaseHelper.getColorStringByID(
                    medication.color_id
                )
            ))
            iconButton.icon = resources.getDrawable(DatabaseHelper.getDrawableIconById(this, medication.icon_id), theme)

            calculateScheduleRecords(medication.schedules)

            //region Bottom button listeners
            bottomOptions.leftButton.setOnClickListener{
                if (editText.text.toString().trim().isNotEmpty() and editText2.text.toString().trim().isNotEmpty()) {
                    if(scheduleRecordsSetToDelete.count() > 0) {
                        val deleteDialog = LayoutInflater.from(this).inflate(R.layout.delete_schedules_prompt, null)

                        val dialogBuilder = AlertDialog.Builder(this)
                            .setView(deleteDialog)
                            .setTitle("Delete Schedules")

                        val deleteSchedules = deleteDialog!!.findViewById<TextView>(R.id.deleteSchedules)
                        val scheduleTexts = scheduleRecordsSetToDelete.map {
                           "${it.timeText.text} ${it.recurrenceText.text} ${it.dateText.text}"
                        }

                        deleteSchedules.text = scheduleTexts.joinToString(separator = "\n")

                        val deleteAlertDialog = dialogBuilder.show()
                        deleteDialog.dialogConfirmBtn.setOnClickListener {
                            deleteAlertDialog.dismiss()
                            val schedules = scheduleRecordsSetToDelete.flatMap { it.schedules }
                            DatabaseHelper.deleteSchedules(schedules)
                            updateMedicationData(
                                medication,
                                editText.text.toString(),
                                editText2.text.toString(),
                                editText3.text.toString()
                            )
                            finish()
                        }

                        deleteDialog.dialogCancelBtn.setOnClickListener {
                            deleteAlertDialog.dismiss()
                        }
                    }
                    else {
                        updateMedicationData(
                            medication,
                            editText.text.toString(),
                            editText2.text.toString(),
                            editText3.text.toString()
                        )
                        finish()
                    }
                } else{
                    Toast.makeText(applicationContext, "Please set a name and dosage", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            colorString = getRandomColorString()
            while(colorString == "#000000") { // Let's not let black be selected randomly
                colorString = getRandomColorString()
            }
            imageDrawable = getRandomIcon()
            iconButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorString))
            iconButton.icon = resources.getDrawable(DatabaseHelper.getDrawableIconById(this, getIconIDByString(imageDrawable)), theme)

            bottomOptions.leftButton.setOnClickListener{
                if (editText.text.toString().trim().isNotEmpty() and editText2.text.toString().trim().isNotEmpty()) {
                    if(scheduleRecordsSetToDelete.count() > 0) {
                        val deleteDialog = LayoutInflater.from(this).inflate(R.layout.delete_schedules_prompt, null)

                        val dialogBuilder = AlertDialog.Builder(this)
                            .setView(deleteDialog)
                            .setTitle("Delete Schedules")

                        val deleteSchedules = deleteDialog!!.findViewById<TextView>(R.id.deleteSchedules)
                        val scheduleTexts = scheduleRecordsSetToDelete.map {
                            "${it.dateText} ${it.recurrenceText} ${it.timeText}"
                        }

                        deleteSchedules.text = scheduleTexts.joinToString(separator = "\n")

                        val deleteAlertDialog = dialogBuilder.show()
                        deleteDialog.dialogConfirmBtn.setOnClickListener {
                            deleteAlertDialog.dismiss()
                            val schedules = scheduleRecordsSetToDelete.flatMap { it.schedules }
                            DatabaseHelper.deleteSchedules(schedules)
                            createMedicationData(
                                editText.text.toString(),
                                editText2.text.toString(),
                                editText3.text.toString()
                            )
                            finish()
                        }

                        deleteDialog.dialogCancelBtn.setOnClickListener {
                            deleteAlertDialog.dismiss()
                        }
                    }
                    else {
                        createMedicationData(
                            editText.text.toString(),
                            editText2.text.toString(),
                            editText3.text.toString()
                        )
                        finish()
                    }
                } else{
                    Toast.makeText(applicationContext, "Please set a name and dosage", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bottomOptions.rightButton.setOnClickListener{
            if(::scheduleIdList.isInitialized){
                val deleteDialog = LayoutInflater.from(this).inflate(R.layout.unsaved_schedules_prompt, null)

                val dialogBuilder = AlertDialog.Builder(this)
                    .setView(deleteDialog)
                    .setTitle("Discard Changes")

                val deleteAlertDialog = dialogBuilder.show()
                deleteDialog.dialogConfirmBtn.setOnClickListener {
                    deleteAlertDialog.dismiss()
                    Realm.getDefaultInstance().executeTransaction {
                        toBeAdded.forEach {
                            deleteFromRealm(it)
                        }
                    }
                    finish()
                }

                deleteDialog.dialogCancelBtn.setOnClickListener {
                    deleteAlertDialog.dismiss()
                }
            } else{
                finish()
            }
        }

        // endregion

        // region Delete button
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
        //endregion

        //region Add schedule button
        addScheduleButton.setOnClickListener {
            val addIntent = Intent(this, EditScheduleActivity::class.java)
            if (intent.hasExtra("medication-uid")) {
                addIntent.putExtra("medication-uid", intent.getStringExtra("medication-uid"))
            }
            startActivityForResult(addIntent, 1)
        }
        //endregion

        iconButton.setOnClickListener {
            val addIntent = Intent(this, EditMedicationIcon::class.java)
            addIntent.putExtra("color-string", colorString)
            if(intent.hasExtra("medication-uid")) {
                addIntent.putExtra("medication-uid", intent.getStringExtra("medication-uid"))
            }
            startActivityForResult(addIntent, 2)
        }
    }

    private fun calculateScheduleRecords(schedules: List<Schedules>) {
        // To contain all records that will be written to the view
        var scheduleRecords = mutableListOf<ScheduleRecord>()

        // The record set that contains the days of the week on which the medication is scheduled to reoccur weekly
        // Eg. To result in the string '8:00 AM on Mon, Wed' when added to scheduleRecords
        var compiledScheduleRecords = mutableListOf<CompiledScheduleRecord>()

        schedules.forEach {
            if (it.deleted ||
                scheduleRecordExistsByUid(scheduleRecordsSetToDelete, it.uid!!)) {
                return@forEach
            }

            val timeString = DateHelper.dateToString(it.occurrence!!)
            if(isWeeklyRecurrence(it)) {
                val cal = Calendar.getInstance()
                cal.time = it.occurrence
                if(compiledScheduleRecordExists(timeString, compiledScheduleRecords)) {
                    //add day of week to existing compiled schedule record
                    val compiledScheduleRecord = compiledScheduleRecords.find {it.time == timeString}
                    compiledScheduleRecord!!.daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1] = 1
                    compiledScheduleRecord.schedules.add(it)
                } else {
                    //create new compiled schedule record
                    val compiledScheduleRecord = CompiledScheduleRecord(timeString)
                    compiledScheduleRecord.daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1] = 1
                    compiledScheduleRecord.schedules.add(it)
                    compiledScheduleRecords.add(compiledScheduleRecord)
                }
            } else {
                val scheduleRecord = ScheduleRecord(this)

                scheduleRecord.timeText.text = timeString
                scheduleRecord.recurrenceText.text = "every"
                scheduleRecord.dateText.text = getRecurrenceString(it.repetitionUnit!!,it.repetitionCount!!)
                scheduleRecord.schedules = listOf(it)
                scheduleRecords.add(scheduleRecord)
            }
        }

        //create scheduleRecords from compiledScheduleRecords
        compiledScheduleRecords.forEach {
            val scheduleRecord = ScheduleRecord(this)

            scheduleRecord.timeText.text = it.time
            scheduleRecord.recurrenceText.text = "on"
            scheduleRecord.dateText.text = getDaysOfWeekList(it.daysOfWeek)
            scheduleRecord.schedules = it.schedules
            scheduleRecords.add(scheduleRecord)
        }

        addScheduleRecords(scheduleRecords, schedules)
    }

    private fun addScheduleRecords(scheduleRecords: List<ScheduleRecord>, schedules: List<Schedules>) {
        scheduleRecords.forEach { record ->
            record.deleteScheduleImage.setOnClickListener {
                scheduleRecordsSetToDelete.add(record)
                updateScheduleList()
            }
            scheduleStack.addView(record)
        }
    }

    private fun updateMedicationData(medication: Medications, drugName: String, drugDose: String, drugNote: String) {
        Realm.getDefaultInstance().executeTransaction {
            medication.name = drugName
            medication.dosage = drugDose
            medication.notes = drugNote
            medication.color_id = getColorIDByString(colorString)
            medication.icon_id = getIconIDByString(imageDrawable)

            if(::scheduleIdList.isInitialized){
                toBeAdded.forEach {
                    medication.schedules.add(it)
                }
            }
        }
    }

    private fun createMedicationData(drugName: String, drugDose: String, drugNote: String) {
        Realm.getDefaultInstance().executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = drugName
            medication.dosage = drugDose
            medication.notes = drugNote
            medication.color_id = getColorIDByString(colorString)
            medication.icon_id = getIconIDByString(imageDrawable)

            if(::scheduleIdList.isInitialized){
                toBeAdded.forEach {
                    medication.schedules.add(it)
                }
            }
        }
    }

    private fun deleteMedication(medication: Medications) {
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
        return compiledScheduleRecords.filter { it.time == timeString }.count() > 0
    }

    private fun scheduleRecordExistsByUid(scheduleRecords: List<ScheduleRecord>, uid: String): Boolean {
        return scheduleRecords.filter { scheduleRecord -> scheduleRecord.schedules.map { it.uid }.contains(uid) }.count() > 0
    }

    private fun getDaysOfWeekList(daysOfWeek: IntArray):String {
        val daysOfWeekList = listOf("Sun","Mon","Tue","Wed","Thurs","Fri","Sat")

        val daysList = daysOfWeek.mapIndexed { index, value ->
            if (value == 1) daysOfWeekList[index]
            else null
        }.filterNotNull()

        return daysList.joinToString()
    }

    private fun updateScheduleList() {
        scheduleStack.removeAllViews()
        if (intent.hasExtra("medication-uid")) {
            calculateScheduleRecords(getMedicationByUid(intent.getStringExtra("medication-uid"))!!.schedules)
        }
        calculateScheduleRecords(toBeAdded)
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1) { // Schedules
            if(data != null) {
                if(data.hasExtra("medication-uid")) {
                    scheduleStack.removeAllViews()
                    calculateScheduleRecords((getMedicationByUid(data.getStringExtra("medication-uid")!!)!!).schedules)
                } else if(data.hasExtra("schedule-id-list")){
                    scheduleIdList = data.getStringArrayListExtra("schedule-id-list")
                    scheduleIdList.forEach {
                        toBeAdded.add(getScheduleByUid(it)!!)
                    }
                    updateScheduleList()
                }
            }
        }
        else if (requestCode == 2) { // Icon
            if(data != null) {
                if(data.hasExtra("color-string")) {
                    colorString = data.getStringExtra("color-string")!!
                    imageDrawable = data.getStringExtra("image-string")!!
                    iconButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorString))
                    iconButton.icon = resources.getDrawable(DatabaseHelper.getDrawableIconById(this, getIconIDByString(imageDrawable)), theme)
                }
            }
        }

    }
}

data class CompiledScheduleRecord(val time: String) {
    var daysOfWeek: IntArray = IntArray(7) {0}
    var schedules: MutableList<Schedules> = mutableListOf()
}


