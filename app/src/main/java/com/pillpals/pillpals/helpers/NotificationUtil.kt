package com.pillpals.pillpals.helpers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Schedules
import java.util.*
import com.pillpals.pillpals.services.AlarmReceiver
import io.realm.RealmResults
import java.util.concurrent.TimeUnit

class NotificationUtils {
    companion object {
        fun startAlarm(context: Context, schedule: Schedules) {
            val mAlarmSender = getPendingIntent(context, schedule)

            val c = Calendar.getInstance()
            var scheduleTime = schedule.occurrence
            var now = Calendar.getInstance()
            var lowerNotifBound = DateHelper.addUnitToDate(now.time, -10, Calendar.MINUTE)
            while (scheduleTime!! < lowerNotifBound) {
                scheduleTime = DateHelper.addUnitToDate(scheduleTime, schedule.repetitionCount!!, schedule.repetitionUnit!!)
            }
            c.time = scheduleTime
            val firstTime = c.timeInMillis

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val interval = DateHelper.getMillisecondsByUnit(
                schedule.repetitionUnit!!
            ) * schedule.repetitionCount!!
            am.setRepeating(AlarmManager.RTC_WAKEUP, firstTime, interval, mAlarmSender)
        }

        fun updateAlarms(context: Context) {
            val schedules = DatabaseHelper.readAllData(
                Schedules::class.java
            ) as RealmResults<out Schedules>
            for (schedule in schedules) {
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.cancel(getPendingIntent(context, schedule))
                startAlarm(context, schedule)
            }
        }

        private fun getPendingIntent(context: Context, schedule: Schedules) : PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra("schedule-uid", schedule.uid)
            intent.putExtra("schedule-occurrence", DateHelper.convertToLocalDateViaInstant(schedule.occurrence!!).toString())
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)

            return PendingIntent.getBroadcast(
                context,
                schedule.uid!!.hashCode(),
                intent,
                0
            )
        }

        fun snoozeAlarm(context: Context, schedule: Schedules) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(schedule.uid.hashCode())

            val mAlarmSender = getPendingIntent(context, schedule)

            var c = Calendar.getInstance()
            var snoozeTime = DateHelper.addUnitToDate(c.time, 2, Calendar.MINUTE) //TODO: Change snooze to 15 minutes or something
            c.time = snoozeTime
            val alarmTime = c.timeInMillis

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setExact(AlarmManager.RTC_WAKEUP, alarmTime, mAlarmSender)
        }

        fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.channel_name)
                val description = context.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(context.getString(R.string.channel_id), name, importance)
                channel.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager!!.createNotificationChannel(channel)
            }
        }
    }
}