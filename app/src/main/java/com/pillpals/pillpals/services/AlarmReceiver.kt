package com.pillpals.pillpals.services

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.*
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DateHelper
import java.util.*
import com.pillpals.pillpals.ui.MainActivity
import java.time.LocalDateTime
import java.time.ZoneId
import androidx.preference.PreferenceManager
import com.pillpals.pillpals.PillPalsApplication
import com.pillpals.pillpals.helpers.AlarmNoiseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getCorrectIconDrawable
import com.pillpals.pillpals.ui.AlarmActivity


public class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra("stop-noise", false)) { //Need to be able to do this on :remote from elsewhere
            PillPalsApplication.alarmNoiseHelper.stopNoise()
            return
        }

        val notificationManager = NotificationManagerCompat.from(context)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val schedule = DatabaseHelper.getScheduleByUid(
            intent.getStringExtra("schedule-uid")
        ) as Schedules

        val now = Calendar.getInstance()
        if (schedule.occurrence!! > now.time) {
            return //Don't generate a notification for a schedule that's already been logged
        }

        if (intent.getBooleanExtra("cancel", false)) {
            notificationManager.cancel(schedule.uid!!.hashCode())
        } else {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val privateMode = sharedPreferences.getBoolean("private_notifications",false)
            val priorityValue = if(sharedPreferences.getBoolean("notifications_silent", false) == false) {
                NotificationCompat.PRIORITY_MAX
            } else {
                NotificationCompat.PRIORITY_LOW
            }
            if(!sharedPreferences.getBoolean("notifications", false)) {
                val medication = schedule.medication!!.first() as Medications
                val occurrenceLocal = LocalDateTime.parse(intent.getStringExtra("schedule-occurrence"))
                val occurrence = Date.from(occurrenceLocal.atZone(ZoneId.systemDefault()).toInstant())

                val title = getTitleString(medication, privateMode, occurrence, sharedPreferences)

                val drawable = if (privateMode) {
                    ContextCompat.getDrawable(context, context.getResources()
                        .getIdentifier("drawable/ic_clock", null, context.packageName))!!
                } else {
                    getCorrectIconDrawable(context, medication)
                }

                val iconBitmap = Bitmap.createBitmap((drawable.intrinsicWidth * 1.1).toInt(), (drawable.intrinsicHeight * 1.1).toInt(), Bitmap.Config.ARGB_8888)

                val iconCanvas = Canvas(iconBitmap)
                val paint = Paint()
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor(getColorStringByID(medication.color_id))
                paint.isAntiAlias = true
                iconCanvas.drawRoundRect(RectF(0f, 0f, iconCanvas.width.toFloat(), iconCanvas.height.toFloat()), (drawable.intrinsicWidth * 0.2).toFloat(), (drawable.intrinsicWidth * 0.2).toFloat(), paint)
                drawable.setBounds((drawable.intrinsicWidth * 0.1).toInt(), (drawable.intrinsicWidth * 0.1).toInt(), iconCanvas.width - (drawable.intrinsicWidth * 0.1).toInt(), iconCanvas.height - (drawable.intrinsicWidth * 0.1).toInt())
                drawable.draw(iconCanvas)

                var stopNoiseIntent = Intent(context, this::class.java)
                stopNoiseIntent.putExtra("stop-noise", true)
                var pendingStopNoiseIntent = PendingIntent.getBroadcast(context, -2, stopNoiseIntent, 0)

                // Set the notification content
                val mBuilder = NotificationCompat.Builder(context, context.getString(R.string.channel_id_rich))
                    .setSmallIcon(getDrawable(privateMode))
                    .setContentTitle(title)
                    .setPriority(priorityValue)
                    .setLargeIcon(iconBitmap)
                    .setTicker(getTickerString(privateMode, sharedPreferences))
                    .setDeleteIntent(pendingStopNoiseIntent)


                if(medication.notes.isNotEmpty() && !privateMode) {
                    mBuilder.setContentText("Notes:...")
                        .setStyle(NotificationCompat.BigTextStyle()
                            .bigText("Notes: " + medication.notes))
                }

                if (sharedPreferences.getBoolean("fullscreen_notifications", true)) {
                    var fullscreenIntent = Intent(context, AlarmActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    fullscreenIntent.putExtras(intent.extras!!)
                    var pendingIntent = PendingIntent.getActivity(context, -schedule.uid!!.hashCode(), fullscreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    mBuilder.setContentIntent(pendingIntent)
                        .setFullScreenIntent(pendingIntent, true)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                    PillPalsApplication.alarmNoiseHelper.startNoise()
                } else {
                    val mainIntent = Intent(context, MainActivity::class.java)
                    mainIntent.putExtras(intent.extras!!)
                    val pendingIntent = PendingIntent.getActivity(context, schedule.uid!!.hashCode(), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    mBuilder.setContentIntent(pendingIntent)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setChannelId(context.getString(R.string.channel_id_soft))
                }

                var logIntent = Intent(context, AlarmActivity::class.java).apply {
                    action = "LOG"
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                logIntent.putExtras(intent.extras!!)
                var pendingLogIntent = PendingIntent.getActivity(context, (schedule.uid!! + "LOG").hashCode(), logIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                var logAction = NotificationCompat.Action(R.drawable.ic_check_32, "LOG", pendingLogIntent)
                mBuilder.addAction(logAction)

                var snoozeIntent = Intent(context, AlarmActivity::class.java).apply {
                    action = "SNOOZE"
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                snoozeIntent.putExtras(intent.extras!!)
                var pendingSnoozeIntent = PendingIntent.getActivity(context, (schedule.uid!! + "SNOOZE").hashCode(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                var snoozeAction = NotificationCompat.Action(R.drawable.ic_clock_32, "SNOOZE", pendingSnoozeIntent)
                mBuilder.addAction(snoozeAction)


                var notification = mBuilder.build()
                if (sharedPreferences.getBoolean("fullscreen_notifications", true)) {
                    notification.flags = notification.flags or Notification.FLAG_INSISTENT
                }

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(schedule.uid!!.hashCode(), mBuilder.build())

                //setupAutoCancel(context, intent, schedule, 15, Calendar.MINUTE) //TODO: Refine this end time
            }
        }
    }

    private fun setupAutoCancel(context: Context, intent: Intent, schedule: Schedules, time: Int, unit: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cancelIntent = Intent(intent)
        cancelIntent.putExtra("cancel", true)

        val cancelAlarmSender = PendingIntent.getBroadcast(
            context,
            (schedule.uid!! + 1).hashCode(), //Can't be the same as the normal alarm ID
            cancelIntent,
            0
        )

        val now = Calendar.getInstance()
        val cancelDate = DateHelper.addUnitToDate(now.time, time, unit)

        am.setExact(AlarmManager.RTC, cancelDate.time, cancelAlarmSender)
        //TODO: Add an additional, softer notification if time expires on primary alarm
    }

    private fun getTitleString (medication: Medications, privateMode: Boolean, occurrence: Date, preferences: SharedPreferences):String {
        return if (privateMode) {
            preferences.getString("private_notification_message","You have a notification")?: "You have a notification"
        } else {
            medication.name + " due at " + DateHelper.dateToString(
                occurrence
            )
        }
    }

    private fun getTickerString (privateMode: Boolean, preferences: SharedPreferences):String {
        return if (privateMode) {
            preferences.getString("private_notification_message","You have a notification")?: "You have a notification"
        } else {
            "Time to take your medication!"
        }
    }

    private fun getDrawable (privateMode: Boolean):Int {
        if (privateMode) {
            return R.drawable.ic_clock
        } else {
            return R.drawable.ic_pill_v5
        }
    }
}
