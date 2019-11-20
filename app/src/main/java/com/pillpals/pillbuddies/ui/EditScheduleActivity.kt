package com.pillpals.pillbuddies.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getMedicationByUid
import com.pillpals.pillbuddies.helpers.DateHelper.Companion.dateToString
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_add_drug.*

import kotlinx.android.synthetic.main.delete_prompt.view.*
import kotlinx.android.synthetic.main.delete_prompt.view.dialogCancelBtn
import kotlinx.android.synthetic.main.dosage_time_box.view.*
import kotlinx.android.synthetic.main.time_prompt.view.*
import java.util.*
import kotlin.collections.ArrayList

class EditScheduleActivity : AppCompatActivity() {

    public lateinit var addTimeButton : ImageButton
    public lateinit var bottomOptions: BottomOptions
    public lateinit var timeBoxList : LinearLayout
    public lateinit var simpleTimePicker : TimePicker
    public lateinit var mondayButton : ToggleButton
    public lateinit var tuesdayButton : ToggleButton
    public lateinit var wednesdayButton : ToggleButton
    public lateinit var thursdayButton : ToggleButton
    public lateinit var fridayButton : ToggleButton
    public lateinit var saturdayButton : ToggleButton
    public lateinit var sundayButton : ToggleButton
    public lateinit var dailyButton : ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_edit_schedule)
        val list: MutableList<DosageTimeBox> = ArrayList()
        val calList: MutableList<Calendar> = ArrayList()

        addTimeButton = findViewById(R.id.addTimeButton)
        bottomOptions = findViewById(R.id.bottomOptions)

        mondayButton = findViewById(R.id.mondayButton)
        tuesdayButton = findViewById(R.id.tuesdayButton)
        wednesdayButton = findViewById(R.id.wednesdayButton)
        thursdayButton = findViewById(R.id.thursdayButton)
        fridayButton = findViewById(R.id.fridayButton)
        saturdayButton = findViewById(R.id.saturdayButton)
        sundayButton = findViewById(R.id.sundayButton)
        dailyButton = findViewById(R.id.dailyButton)

        bottomOptions.leftButton.text = "Save"
        bottomOptions.rightButton.text = "Cancel"
        timeBoxList = findViewById(R.id.timeBoxList)

        addTimeButton.setOnClickListener {
            val timeDialog = LayoutInflater.from(this).inflate(R.layout.time_prompt, null)
            simpleTimePicker = timeDialog!!.findViewById(R.id.simpleTimePicker)

            val dialogBuilder = AlertDialog.Builder(this)
                .setView(timeDialog)
                .setTitle("Time Picker")

            val timeAlertDialog = dialogBuilder.show()
            timeDialog.dialogAddBtn.setOnClickListener {
                timeAlertDialog.dismiss()
                val cal = Calendar.getInstance()
                cal.set(Calendar.MILLISECOND, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MINUTE, simpleTimePicker.minute)
                cal.set(Calendar.HOUR_OF_DAY, simpleTimePicker.hour)

                val timeData = dateToString(cal.time)
                list.add(DosageTimeBox(this))
                calList.add(cal)
                list.last().timeBoxText.text= timeData
                timeBoxList.removeAllViews()
                list.forEach {
                    //Log.i("test", it.timeBoxText.text.toString())
                    timeBoxList.addView(it)
                }
            }

            timeDialog.dialogCancelBtn.setOnClickListener {
                timeAlertDialog.dismiss()
            }
        }

        bottomOptions.leftButton.setOnClickListener {
            val schedules: MutableList<Schedules> = ArrayList()
            Realm.getDefaultInstance().executeTransaction {realm->
                if(dailyButton.isChecked){
                    calList.forEach {
                        val schedule = realm.createObject(Schedules::class.java, UUID.randomUUID().toString())
                        schedule.occurrence=it.time
                        schedule.repetitionCount = 1
                        schedule.repetitionUnit = 2
                        schedules.add(schedule)
                    }
                }
                else{
                    calList.forEach {
                        if(sundayButton.isChecked){
                            val schedule = realm.createObject(Schedules::class.java, UUID.randomUUID().toString())
                            it.set(Calendar.DAY_OF_WEEK, 1)
                            schedule.occurrence=it.time
                            schedule.repetitionCount = 1
                            schedule.repetitionUnit = 6
                            schedules.add(schedule)
                        }
                        if(mondayButton.isChecked){
                            val schedule = realm.createObject(Schedules::class.java, UUID.randomUUID().toString())
                            it.set(Calendar.DAY_OF_WEEK, 2)
                            schedule.occurrence=it.time
                            schedule.repetitionCount = 1
                            schedule.repetitionUnit = 6
                            schedules.add(schedule)
                        }
                        if(tuesdayButton.isChecked){
                            val schedule = realm.createObject(Schedules::class.java, UUID.randomUUID().toString())
                            it.set(Calendar.DAY_OF_WEEK, 3)
                            schedule.occurrence=it.time
                            schedule.repetitionCount = 1
                            schedule.repetitionUnit = 6
                            schedules.add(schedule)
                        }
                        if(wednesdayButton.isChecked){
                            val schedule = realm.createObject(Schedules::class.java, UUID.randomUUID().toString())
                            it.set(Calendar.DAY_OF_WEEK, 4)
                            schedule.occurrence=it.time
                            schedule.repetitionCount = 1
                            schedule.repetitionUnit = 6
                            schedules.add(schedule)
                        }
                        if(thursdayButton.isChecked){
                            val schedule = realm.createObject(Schedules::class.java, UUID.randomUUID().toString())
                            it.set(Calendar.DAY_OF_WEEK, 5)
                            schedule.occurrence=it.time
                            schedule.repetitionCount = 1
                            schedule.repetitionUnit = 6
                            schedules.add(schedule)
                        }
                        if(fridayButton.isChecked){
                            val schedule = realm.createObject(Schedules::class.java, UUID.randomUUID().toString())
                            it.set(Calendar.DAY_OF_WEEK, 6)
                            schedule.occurrence=it.time
                            schedule.repetitionCount = 1
                            schedule.repetitionUnit = 6
                            schedules.add(schedule)
                        }
                        if(saturdayButton.isChecked){
                            val schedule = realm.createObject(Schedules::class.java, UUID.randomUUID().toString())
                            it.set(Calendar.DAY_OF_WEEK, 7)
                            schedule.occurrence=it.time
                            schedule.repetitionCount = 1
                            schedule.repetitionUnit = 6
                            schedules.add(schedule)
                        }
                    }
                }
                if(intent.hasExtra("medication-uid")){
                    val medication = getMedicationByUid(intent.getStringExtra("medication-uid"))
                    schedules.forEach{
                        medication!!.schedules.add(it)
                    }
                    val resultIntent = Intent(this, EditScheduleActivity::class.java)
                    resultIntent.putExtra("medication-uid", intent.getStringExtra("medication-uid"))
                    setResult(Activity.RESULT_OK, resultIntent)
                }
            }
            finish()
        }

        bottomOptions.rightButton.setOnClickListener {
            finish()
        }

    }

}
