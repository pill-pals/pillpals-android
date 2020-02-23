package com.pillpals.pillpals.ui

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.preference.PreferenceManager
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getCorrectIconDrawable
import com.pillpals.pillpals.helpers.DateHelper
import com.pillpals.pillpals.helpers.NotificationUtils
import com.pillpals.pillpals.helpers.drugLogFunction

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class AlarmActivity : AppCompatActivity() {

    public lateinit var message: TextView
    public lateinit var iconCard: CardView
    public lateinit var icon: ImageView
    public lateinit var alarmTime: TextView
    public lateinit var alarmNote: TextView
    public lateinit var logButton: Button
    public lateinit var snoozeButton: Button
    public lateinit var openButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        var schedule = DatabaseHelper.getScheduleByUid(
            intent.getStringExtra("schedule-uid")
        ) as Schedules
        var medication = schedule.medication!!.first() as Medications

        if (intent.action == "LOG") {
            alarmLogFunc(schedule)
        } else if (intent.action == "SNOOZE") {
            alarmSnoozeFunc(schedule)
        } else {
            setupViewVars()

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            message.text = sharedPreferences.getString("fullscreen_notification_message", "It's time to take your medication!")

            alarmTime.text = DateHelper.dateToString(schedule.occurrence!!)

            if (medication.notes.isNotEmpty()) {
                alarmNote.text = medication.notes
            }

            iconCard.setCardBackgroundColor(Color.parseColor(getColorStringByID(medication.color_id)))
            icon.setImageDrawable(getCorrectIconDrawable(this, medication))

            logButton.setOnClickListener {
                alarmLogFunc(schedule)
            }

            snoozeButton.setOnClickListener {
                alarmSnoozeFunc(schedule)
            }

            openButton.setOnClickListener {
                alarmOpenFunc()
            }
        }
    }

    private fun setupViewVars() {
        message = findViewById(R.id.alarmMessage)
        iconCard = findViewById(R.id.alarmIconBackground)
        icon = findViewById(R.id.alarmIcon)
        alarmTime = findViewById(R.id.alarmTime)
        alarmNote = findViewById(R.id.alarmNote)
        logButton = findViewById(R.id.alarmLogButton)
        snoozeButton = findViewById(R.id.alarmSnoozeButton)
        openButton = findViewById(R.id.alarmOpenAppButton)
    }

    private fun alarmLogFunc(schedule: Schedules) {
        drugLogFunction(schedule, this)
        finish()
    }

    private fun alarmSnoozeFunc(schedule: Schedules) {
        NotificationUtils.snoozeAlarm(this, schedule)
        finish()
    }

    private fun alarmOpenFunc() {
        var newIntent = Intent(this, MainActivity::class.java)
        startActivity(newIntent)
    }
}
