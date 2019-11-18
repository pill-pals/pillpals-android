package com.pillpals.pillbuddies.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pillpals.pillbuddies.data.model.Schedules
import java.util.*
import android.util.Log
import com.pillpals.pillbuddies.services.AlarmReceiver

class NotificationUtils {
    companion object {
        fun startAlarm(context: Context, schedule: Schedules) {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra("schedule-uid", schedule.uid)
            intent.putExtra("schedule-occurrence", DateHelper.convertToLocalDateViaInstant(schedule.occurrence!!).toString())

            val mAlarmSender = PendingIntent.getBroadcast(
                context,
                schedule.uid!!.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val c = Calendar.getInstance()
            c.time = schedule.occurrence
            val firstTime = c.timeInMillis

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val interval = DateHelper.getMillisecondsByUnit(
                schedule.repetitionUnit!!
            ) * schedule.repetitionCount!!
            am.setRepeating(AlarmManager.RTC_WAKEUP, firstTime, interval, mAlarmSender)
        }
    }
}