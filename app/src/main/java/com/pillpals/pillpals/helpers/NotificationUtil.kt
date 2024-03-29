package com.pillpals.pillpals.helpers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Schedules
import java.util.*
import com.pillpals.pillpals.services.AlarmReceiver
import com.pillpals.pillpals.services.QuizReceiver
import com.pillpals.pillpals.ui.quiz.QuizGenerator
import io.realm.RealmResults
import java.util.concurrent.TimeUnit

class NotificationUtils {
    companion object {
        fun startAlarm(context: Context, schedule: Schedules) {
            val mAlarmSender = getPendingIntent(context, schedule)

            val c = Calendar.getInstance()
            var scheduleTime = schedule.occurrence!!
//            var now = Calendar.getInstance()
//            var lowerNotifBound = DateHelper.addUnitToDate(now.time, -10, Calendar.MINUTE)
//            while (DateHelper.addUnitToDate(scheduleTime, schedule.repetitionCount!!, schedule.repetitionUnit!!) < now.time) {
//                scheduleTime = DateHelper.addUnitToDate(scheduleTime, schedule.repetitionCount!!, schedule.repetitionUnit!!)
//            }

            while (scheduleTime!! < DateHelper.yesterdayAt12pm()) {
                scheduleTime = DateHelper.addUnitToDate(scheduleTime, schedule.repetitionCount!!, DateHelper.getUnitByIndex(schedule.repetitionUnit!!))
            }

            c.time = scheduleTime
            val firstTime = c.timeInMillis

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            val interval = DateHelper.getMillisecondsByUnit(
//                DateHelper.getUnitByIndex(schedule.repetitionUnit!!)
//            ) * schedule.repetitionCount!!
//            am.setRepeating(AlarmManager.RTC_WAKEUP, firstTime, interval, mAlarmSender)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, firstTime, mAlarmSender)
        }

        fun updateAlarms(context: Context) {
            val schedules = DatabaseHelper.readAllData(
                Schedules::class.java
            ) as RealmResults<out Schedules>
            for (schedule in schedules) {
                if(schedule.deleted || schedule.medication?.firstOrNull() == null || schedule.medication.first()!!.deleted) continue
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                am.cancel(getPendingIntent(context, schedule))
                if(!schedule.deleted) startAlarm(context, schedule)
            }
        }

        fun getPendingIntent(context: Context, schedule: Schedules, uid: Int = schedule.uid!!.hashCode()) : PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra("schedule-uid", schedule.uid)
            intent.putExtra("schedule-occurrence", DateHelper.convertToLocalDateViaInstant(schedule.occurrence!!).toString())
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

            return PendingIntent.getBroadcast(
                context,
                uid,
                intent,
                0
            )
        }

        fun snoozeAlarm(context: Context, schedule: Schedules) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(schedule.uid.hashCode())

            val mAlarmSender = getPendingIntent(context, schedule, (schedule.uid!! + "alarm_snooze").hashCode())

            var c = Calendar.getInstance()
            var snoozeTime = DateHelper.addUnitToDate(c.time, 15, Calendar.MINUTE) //TODO: Make snooze time editable
            c.time = snoozeTime
            val alarmTime = c.timeInMillis

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setExact(AlarmManager.RTC_WAKEUP, alarmTime, mAlarmSender)
        }

        fun createNotificationChannels(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var name = context.getString(R.string.channel_name_soft)
                var description = context.getString(R.string.channel_description_soft)
                var importance = NotificationManager.IMPORTANCE_HIGH
                var channel = NotificationChannel(context.getString(R.string.channel_id_soft), name, importance)
                channel.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                var notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager!!.createNotificationChannel(channel)

                name = context.getString(R.string.channel_name_rich)
                description = context.getString(R.string.channel_description_rich)
                channel = NotificationChannel(context.getString(R.string.channel_id_rich), name, importance)
                channel.description = description
                channel.enableLights(true)
                channel.vibrationPattern = longArrayOf(0)
                channel.setSound(null, null)
                //Vibration and sound is handled elsewhere (using AlarmNoiseHelper)

//                var soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//                var audioAttributes = AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .setUsage(AudioAttributes.USAGE_ALARM)
//                    .build()
//                channel.setSound(soundUri, audioAttributes)

                notificationManager!!.createNotificationChannel(channel)
            }
        }

        fun createQuizNotifications(context: Context){
            if(PendingIntent.getBroadcast(context, 0, Intent(context, QuizReceiver::class.java), PendingIntent.FLAG_NO_CREATE) == null) {
                val mIntent = Intent(context, QuizReceiver::class.java)
                val mPendingIntent = PendingIntent.getBroadcast(context, 0, mIntent, 0)
                val mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                mAlarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                    1000 * 60 * 60 * 24, mPendingIntent
                )
                Log.i("quiz", "Quiz Receiver Initiated")
            }else{
                Log.i("quiz", "Quiz Receiver Already Active")
            }
        }
    }
}