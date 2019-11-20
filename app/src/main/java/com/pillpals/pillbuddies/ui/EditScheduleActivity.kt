package com.pillpals.pillbuddies.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.pillpals.pillbuddies.R
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_add_drug.*

import kotlinx.android.synthetic.main.delete_prompt.view.*
import kotlinx.android.synthetic.main.delete_prompt.view.dialogCancelBtn
import kotlinx.android.synthetic.main.dosage_time_box.view.*
import kotlinx.android.synthetic.main.time_prompt.view.*

class EditScheduleActivity : AppCompatActivity() {

    public lateinit var addTimeButton : ImageButton
    public lateinit var bottomOptions: BottomOptions
    public lateinit var timeBoxConstraintLayout : ConstraintLayout
    public lateinit var simpleTimePicker : TimePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_edit_schedule)

        addTimeButton = findViewById(R.id.addTimeButton)
        bottomOptions = findViewById(R.id.bottomOptions)
        bottomOptions.leftButton.text = "Save"
        bottomOptions.rightButton.text = "Cancel"
        timeBoxConstraintLayout = findViewById(R.id.timeBoxConstraintLayout)

        addTimeButton.setOnClickListener {
            val timeDialog = LayoutInflater.from(this).inflate(R.layout.time_prompt, null)
            simpleTimePicker = timeDialog!!.findViewById(R.id.simpleTimePicker)

            val dialogBuilder = AlertDialog.Builder(this)
                .setView(timeDialog)
                .setTitle("Time Picker")

            val timeAlertDialog = dialogBuilder.show()
            timeDialog.dialogAddBtn.setOnClickListener {
                timeAlertDialog.dismiss()
                val timeObject = DosageTimeBox(this)
                val timeData = simpleTimePicker.hour.toString()+":"+simpleTimePicker.minute.toString()
                timeObject.timeBoxText.text= timeData
                timeBoxConstraintLayout.addView(timeObject)
            }

            timeDialog.dialogCancelBtn.setOnClickListener {
                timeAlertDialog.dismiss()
            }
        }

    }

}
