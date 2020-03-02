package com.pillpals.pillpals.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isEmpty
import com.google.android.flexbox.FlexboxLayout
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DateHelper
import com.pillpals.pillpals.helpers.DateHelper.Companion.dateToString
import io.realm.Realm
import kotlinx.android.synthetic.main.time_prompt.view.*

import java.util.*
import kotlin.collections.ArrayList

class EditScheduleActivity : AppCompatActivity() {

    public lateinit var addTimeButton : Button
    public lateinit var bottomOptions: BottomOptions
    public lateinit var timeBoxList : FlexboxLayout
    public lateinit var simpleTimePicker : TimePicker
    public lateinit var mondayButton : ToggleButton
    public lateinit var tuesdayButton : ToggleButton
    public lateinit var wednesdayButton : ToggleButton
    public lateinit var thursdayButton : ToggleButton
    public lateinit var fridayButton : ToggleButton
    public lateinit var saturdayButton : ToggleButton
    public lateinit var sundayButton : ToggleButton
    public lateinit var dailyButton : ToggleButton

    public lateinit var weekdayButton : ToggleButton
    public lateinit var intervalButton : ToggleButton
    public lateinit var weekdayOptions : ConstraintLayout
    public lateinit var intervalOptions : ConstraintLayout

    public lateinit var intervalNumBox : EditText
    public lateinit var intervalScaleList : Spinner
    public lateinit var startDatePicker: DatePicker

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

        weekdayButton = findViewById(R.id.weekdayButton)
        intervalButton = findViewById(R.id.intervalButton)
        weekdayOptions = findViewById(R.id.weekdayOptions)
        intervalOptions = findViewById(R.id.intervalOptions)
        intervalNumBox = findViewById(R.id.intervalNumBox)
        intervalScaleList = findViewById(R.id.intervalScaleList)
        intervalScaleList.setSelection(1)

        bottomOptions.leftButton.text = "Save"
        bottomOptions.rightButton.text = "Cancel"
        timeBoxList = findViewById(R.id.timeBoxList)

        startDatePicker = findViewById(R.id.startDatePicker)

        weekdayButton.setOnClickListener{
            if(weekdayButton.isChecked){
                intervalButton.setChecked(false)
                weekdayOptions.visibility = View.VISIBLE
                intervalOptions.visibility = View.INVISIBLE
            }else{
                weekdayButton.setChecked(true)
            }
        }

        intervalButton.setOnClickListener{
            if(intervalButton.isChecked){
                weekdayButton.setChecked(false)
                intervalOptions.visibility = View.VISIBLE
                weekdayOptions.visibility = View.INVISIBLE
            }else{
                intervalButton.setChecked(true)
            }
        }

        addTimeButton.setOnClickListener {
            val timeDialog = LayoutInflater.from(this).inflate(R.layout.time_prompt, null)
            simpleTimePicker = timeDialog!!.findViewById(R.id.simpleTimePicker)

            val title = SpannableString("Time Picker")
            title.setSpan(
                ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null)),
                0,
                title.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(timeDialog)
                .setTitle(title)

            val timeAlertDialog = dialogBuilder.show()
            timeDialog.dialogAddBtn.setOnClickListener {
                val cal = Calendar.getInstance()
                var repeat = false
                cal.set(Calendar.MILLISECOND, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MINUTE, simpleTimePicker.minute)
                cal.set(Calendar.HOUR_OF_DAY, simpleTimePicker.hour)

                calList.forEach{
                    if(cal.time == it.time){
                        Toast.makeText(applicationContext, "Please submit a unique time", Toast.LENGTH_SHORT).show()
                        repeat = true
                    }
                }

                if(repeat == false) {
                    timeAlertDialog.dismiss()
                    val timeData = dateToString(cal.time)
                    list.add(DosageTimeBox(this))
                    calList.add(cal)
                    list.last().timeBoxText.text = timeData

                    val listData = list.last()
                    val calData = cal
                    list.last().button.setOnClickListener {
                        //Log.i("test", it.parent.parent.toString())
                        list.remove(listData)
                        calList.remove(calData)

                        timeBoxList.removeAllViews()
                        list.forEach {
                            //Log.i("test", it.timeBoxText.text.toString())
                            timeBoxList.addView(it)
                        }
                    }

                    timeBoxList.removeAllViews()
                    list.forEach {
                        //Log.i("test", it.timeBoxText.text.toString())
                        timeBoxList.addView(it)
                    }
                }
            }

            timeDialog.dialogCancelBtn.setOnClickListener {
                timeAlertDialog.dismiss()
            }
        }

        bottomOptions.leftButton.setOnClickListener {
            if (timeBoxList.isEmpty() || noWeekdayChecked() || noIntervalSelected()) {
                val errorDialog = LayoutInflater.from(this).inflate(R.layout.missing_schedule_info_prompt, null)
                val missingTimeText = errorDialog!!.findViewById<TextView>(R.id.missingTimeText)
                val missingWeekdaysText = errorDialog!!.findViewById<TextView>(R.id.missingWeekdaysText)
                val missingIntervalText = errorDialog!!.findViewById<TextView>(R.id.missingIntervalText)
                missingTimeText.visibility = View.GONE
                missingWeekdaysText.visibility = View.GONE
                missingIntervalText.visibility = View.GONE
                if(timeBoxList.isEmpty()) {
                    missingTimeText.visibility = View.VISIBLE
                }
                if(noWeekdayChecked()) {
                    missingWeekdaysText.visibility = View.VISIBLE
                }
                if(noIntervalSelected()) {
                    missingIntervalText.visibility = View.VISIBLE
                }

                val title = SpannableString("Error Saving Schedule")
                title.setSpan(
                    ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null)),
                    0,
                    title.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val dialogBuilder = AlertDialog.Builder(this)
                    .setView(errorDialog)
                    .setTitle(title)

                val errorAlertDialog = dialogBuilder.show()
                errorDialog.dialogCancelBtn.setOnClickListener {
                    errorAlertDialog.dismiss()
                }

            } else {
                val schedules: MutableList<Schedules> = ArrayList()
                Realm.getDefaultInstance().executeTransaction { realm ->
                    if (weekdayButton.isChecked) {
                        calList.forEach {
                            if(it.time.before(Date())){
                                it.add(Calendar.DATE, 1)
                            }
                        }

                        if (dailyButton.isChecked || (sundayButton.isChecked && mondayButton.isChecked && tuesdayButton.isChecked && wednesdayButton.isChecked && thursdayButton.isChecked && fridayButton.isChecked && saturdayButton.isChecked)) {
                            calList.forEach {
                                val schedule = realm.createObject(
                                    Schedules::class.java,
                                    UUID.randomUUID().toString()
                                )
                                schedule.occurrence = it.time
                                schedule.startDate = it.time
                                schedule.repetitionCount = 1
                                schedule.repetitionUnit = 2
//                                schedule.repetitionCount = 5 //For testing (5 minute interval)
//                                schedule.repetitionUnit = 4
                                schedules.add(schedule)
                            }
                        } else {
                            calList.forEach {
                                if (sundayButton.isChecked) {
                                    val schedule = realm.createObject(
                                        Schedules::class.java,
                                        UUID.randomUUID().toString()
                                    )
                                    it.set(Calendar.DAY_OF_WEEK, 1)
                                    schedule.occurrence = it.time
                                    schedule.startDate = it.time
                                    schedule.repetitionCount = 1
                                    schedule.repetitionUnit = 6
                                    schedules.add(schedule)
                                }
                                if (mondayButton.isChecked) {
                                    val schedule = realm.createObject(
                                        Schedules::class.java,
                                        UUID.randomUUID().toString()
                                    )
                                    it.set(Calendar.DAY_OF_WEEK, 2)
                                    schedule.occurrence = it.time
                                    schedule.startDate = it.time
                                    schedule.repetitionCount = 1
                                    schedule.repetitionUnit = 6
                                    schedules.add(schedule)
                                }
                                if (tuesdayButton.isChecked) {
                                    val schedule = realm.createObject(
                                        Schedules::class.java,
                                        UUID.randomUUID().toString()
                                    )
                                    it.set(Calendar.DAY_OF_WEEK, 3)
                                    schedule.occurrence = it.time
                                    schedule.startDate = it.time
                                    schedule.repetitionCount = 1
                                    schedule.repetitionUnit = 6
                                    schedules.add(schedule)
                                }
                                if (wednesdayButton.isChecked) {
                                    val schedule = realm.createObject(
                                        Schedules::class.java,
                                        UUID.randomUUID().toString()
                                    )
                                    it.set(Calendar.DAY_OF_WEEK, 4)
                                    schedule.occurrence = it.time
                                    schedule.startDate = it.time
                                    schedule.repetitionCount = 1
                                    schedule.repetitionUnit = 6
                                    schedules.add(schedule)
                                }
                                if (thursdayButton.isChecked) {
                                    val schedule = realm.createObject(
                                        Schedules::class.java,
                                        UUID.randomUUID().toString()
                                    )
                                    it.set(Calendar.DAY_OF_WEEK, 5)
                                    schedule.occurrence = it.time
                                    schedule.startDate = it.time
                                    schedule.repetitionCount = 1
                                    schedule.repetitionUnit = 6
                                    schedules.add(schedule)
                                }
                                if (fridayButton.isChecked) {
                                    val schedule = realm.createObject(
                                        Schedules::class.java,
                                        UUID.randomUUID().toString()
                                    )
                                    it.set(Calendar.DAY_OF_WEEK, 6)
                                    schedule.occurrence = it.time
                                    schedule.startDate = it.time
                                    schedule.repetitionCount = 1
                                    schedule.repetitionUnit = 6
                                    schedules.add(schedule)
                                }
                                if (saturdayButton.isChecked) {
                                    val schedule = realm.createObject(
                                        Schedules::class.java,
                                        UUID.randomUUID().toString()
                                    )
                                    it.set(Calendar.DAY_OF_WEEK, 7)
                                    schedule.occurrence = it.time
                                    schedule.startDate = it.time
                                    schedule.repetitionCount = 1
                                    schedule.repetitionUnit = 6
                                    schedules.add(schedule)
                                }
                            }
                        }
                    } else {
                        calList.forEach {
                            val schedule = realm.createObject(
                                Schedules::class.java,
                                UUID.randomUUID().toString()
                            )
                            val cal = Calendar.getInstance()
                            cal.time = it.time
                            cal.set(Calendar.MILLISECOND, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.DAY_OF_MONTH, startDatePicker.dayOfMonth)
                            cal.set(Calendar.MONTH, startDatePicker.month)
                            cal.set(Calendar.YEAR, startDatePicker.year)

                            schedule.occurrence = cal.time
                            schedule.startDate = cal.time
                            schedule.repetitionCount = intervalNumBox.text.toString().toInt()

                            schedule.repetitionUnit = when (intervalScaleList.selectedItem) {
                                "Hours" -> DateHelper.getIndexByUnit(Calendar.HOUR_OF_DAY)
                                "Days" -> DateHelper.getIndexByUnit(Calendar.DATE)
                                "Weeks" -> DateHelper.getIndexByUnit(Calendar.WEEK_OF_YEAR)
                                else -> DateHelper.getIndexByUnit(Calendar.DATE)
                            }

                            schedules.add(schedule)
                        }
                    }

                    val strings: MutableList<String> = ArrayList()
                    schedules.forEach {
                        strings.add(it.uid!!)
                    }
                    val resultIntent = Intent(this, EditScheduleActivity::class.java)
                    resultIntent.putStringArrayListExtra("schedule-id-list", ArrayList(strings))
                    setResult(Activity.RESULT_OK, resultIntent)
                }
                finish()
            }
        }

        bottomOptions.rightButton.setOnClickListener {
            finish()
        }

    }

    fun noWeekdayChecked():Boolean {
        return (weekdayButton.isChecked && !(dailyButton.isChecked || sundayButton.isChecked || mondayButton.isChecked || tuesdayButton.isChecked || wednesdayButton.isChecked || thursdayButton.isChecked || fridayButton.isChecked || saturdayButton.isChecked))
    }

    fun noIntervalSelected():Boolean {
        return (intervalButton.isChecked && intervalNumBox.text.toString().equals(""))
    }

    fun buttonClicked(view: View){
        if(view == findViewById(R.id.dailyButton)){
            mondayButton.setChecked(false)
            tuesdayButton.setChecked(false)
            wednesdayButton.setChecked(false)
            thursdayButton.setChecked(false)
            fridayButton.setChecked(false)
            saturdayButton.setChecked(false)
            sundayButton.setChecked(false)
        }else{
            dailyButton.setChecked(false)
        }
    }

}
