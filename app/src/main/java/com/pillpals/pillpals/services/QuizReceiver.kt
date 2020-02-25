package com.pillpals.pillpals.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.pillpals.pillpals.R
import com.pillpals.pillpals.ui.MainActivity
import com.pillpals.pillpals.ui.quiz.QuizGenerator

class QuizReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("quiz", "we in here")
        val notificationManager = NotificationManagerCompat.from(context)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val priorityValue = if(sharedPreferences.getBoolean("notifications_silent", false) == false) {
            NotificationCompat.PRIORITY_MAX
        } else {
            NotificationCompat.PRIORITY_LOW
        }
        val quizStatus = QuizGenerator.tryGenerateQuiz()

        if(!sharedPreferences.getBoolean("notifications", false)) {
            val mainIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            if (quizStatus == 1) {
                val mBuilder =
                    NotificationCompat.Builder(context, context.getString(R.string.channel_id_soft))
                        .setSmallIcon(R.drawable.ic_pill_v5)
                        .setContentTitle("New quiz available!")
                        .setPriority(priorityValue)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setTicker("A new quiz is available!")
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

                notificationManager.notify(generateNotificationID(), mBuilder.build())

            }else if(quizStatus == 2){
                val mBuilder =
                    NotificationCompat.Builder(context, context.getString(R.string.channel_id_soft))
                        .setSmallIcon(R.drawable.ic_pill_v5)
                        .setContentTitle("New quiz available!")
                        .setPriority(priorityValue)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setTicker("A new quiz is available!")
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

                notificationManager.notify(generateNotificationID(), mBuilder.build())
            }
        }
    }

    private fun generateNotificationID(): Int{
        return System.currentTimeMillis().toInt()
    }
}