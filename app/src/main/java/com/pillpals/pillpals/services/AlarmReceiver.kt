package com.pillpals.pillpals.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getCorrectIconDrawable
import com.pillpals.pillpals.helpers.NotificationUtils
import java.util.concurrent.TimeUnit


public class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManagerCompat.from(context)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val schedule = DatabaseHelper.getScheduleByUid(
            intent.getStringExtra("schedule-uid")
        ) as Schedules

        if (intent.getBooleanExtra("cancel", false)) {
            notificationManager.cancel(schedule.uid!!.hashCode())
        } else {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val priorityValue = if(sharedPreferences.getBoolean("notifications_silent", false) == false) {
                NotificationCompat.PRIORITY_MAX
            } else {
                NotificationCompat.PRIORITY_LOW
            }
            if(!sharedPreferences.getBoolean("notifications", false)) {
                val medication = schedule.medication!!.first() as Medications
                val occurrenceLocal = LocalDateTime.parse(intent.getStringExtra("schedule-occurrence"))
                val occurrence = Date.from(occurrenceLocal.atZone(ZoneId.systemDefault()).toInstant())

                val title = medication.name + " due at " + DateHelper.dateToString(
                    occurrence
                )

                val mainIntent = Intent(context, MainActivity::class.java)

                val pendingIntent = PendingIntent.getActivity(context, schedule.uid!!.hashCode(), mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val drawable = getCorrectIconDrawable(context, medication)

                val iconBitmap = Bitmap.createBitmap((drawable.intrinsicWidth * 1.1).toInt(), (drawable.intrinsicHeight * 1.1).toInt(), Bitmap.Config.ARGB_8888)

                val iconCanvas = Canvas(iconBitmap)
                val paint = Paint()
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor(getColorStringByID(medication.color_id))
                paint.isAntiAlias = true
                iconCanvas.drawRoundRect(RectF(0f, 0f, iconCanvas.width.toFloat(), iconCanvas.height.toFloat()), (drawable.intrinsicWidth * 0.2).toFloat(), (drawable.intrinsicWidth * 0.2).toFloat(), paint)
                drawable.setBounds((drawable.intrinsicWidth * 0.1).toInt(), (drawable.intrinsicWidth * 0.1).toInt(), iconCanvas.width - (drawable.intrinsicWidth * 0.1).toInt(), iconCanvas.height - (drawable.intrinsicWidth * 0.1).toInt())
                drawable.draw(iconCanvas)

                // Set the notification content
                val mBuilder = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.ic_pill_v5)
                    .setContentTitle(title)
                    .setPriority(priorityValue)
                    .setContentIntent(pendingIntent)
                    .setLargeIcon(iconBitmap)
                    .setOngoing(true)
                    .setTicker("It's time to take your medication!")
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

                if(medication.notes.isNotEmpty()) {
                    mBuilder.setContentText("Notes:...")
                        .setStyle(NotificationCompat.BigTextStyle()
                            .bigText("Notes: " + medication.notes))
                }

                // notificationId is a unique int for each notification that you must define
                notificationManager.notify(schedule.uid!!.hashCode(), mBuilder.build())

                //setupAutoCancel(context, intent, schedule)
            }
        }
    }

    private fun setupAutoCancel(context: Context, intent: Intent, schedule: Schedules) {
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
        val cancelDate = DateHelper.addUnitToDate(now.time, 10, Calendar.MINUTE) //TODO: Refine this end time

        am.setExact(AlarmManager.RTC, cancelDate.time, cancelAlarmSender)
    }
}
