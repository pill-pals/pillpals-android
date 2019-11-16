package com.pillpals.pillbuddies.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import com.pillpals.pillbuddies.helpers.DateHelper
import java.util.*
import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId

public class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val schedule = DatabaseHelper.getScheduleByUid(
            intent.getStringExtra("schedule-uid")
        ) as Schedules
        val medication = schedule.medication!!.first() as Medications
        val occurrenceLocal = LocalDateTime.parse(intent.getStringExtra("schedule-occurrence"))
        val occurrence = Date.from(occurrenceLocal.atZone(ZoneId.systemDefault()).toInstant())

        val title = medication.name + " due at " + DateHelper.dateToString(
            occurrence
        )

        val pendingIntent = PendingIntent.getActivity(context, schedule.uid!!.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT)

        // Set the notification content
        val mBuilder = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setSmallIcon(R.drawable.ic_pill_v5)
            .setContentTitle(title)
            .setContentText("Notes...")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Notes: Placeholder for drug notes"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(schedule.uid!!.hashCode(), mBuilder.build())
    }
}
