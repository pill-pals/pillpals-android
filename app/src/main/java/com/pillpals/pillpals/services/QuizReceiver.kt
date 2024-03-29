package com.pillpals.pillpals.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.pillpals.pillpals.R
import com.pillpals.pillpals.helpers.DatabaseHelper
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

        if(!sharedPreferences.getBoolean("notifications_quiz", false)) {
            val mainIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val drawable = ContextCompat.getDrawable(context, context.getResources()
                .getIdentifier("drawable/ic_clock", null, context.packageName))!!
            val iconBitmap = Bitmap.createBitmap((drawable.intrinsicWidth * 1.1).toInt(), (drawable.intrinsicHeight * 1.1).toInt(), Bitmap.Config.ARGB_8888)

            val iconCanvas = Canvas(iconBitmap)
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#28275E")
            paint.isAntiAlias = true
            iconCanvas.drawRoundRect(RectF(0f, 0f, iconCanvas.width.toFloat(), iconCanvas.height.toFloat()), (drawable.intrinsicWidth * 0.2).toFloat(), (drawable.intrinsicWidth * 0.2).toFloat(), paint)
            drawable.setBounds((drawable.intrinsicWidth * 0.1).toInt(), (drawable.intrinsicWidth * 0.1).toInt(), iconCanvas.width - (drawable.intrinsicWidth * 0.1).toInt(), iconCanvas.height - (drawable.intrinsicWidth * 0.1).toInt())
            drawable.draw(iconCanvas)

            if (quizStatus == 1) {
                val mBuilder =
                    NotificationCompat.Builder(context, context.getString(R.string.channel_id_soft))
                        .setSmallIcon(R.drawable.ic_pill_v5)
                        .setLargeIcon(iconBitmap)
                        .setContentTitle("New quiz available!")
                        .setPriority(priorityValue)
                        .setContentIntent(pendingIntent)
                        .setTicker("A new quiz is available!")
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

                notificationManager.notify(generateNotificationID(), mBuilder.build())

            }else if(quizStatus == 2){
                val mBuilder =
                    NotificationCompat.Builder(context, context.getString(R.string.channel_id_soft))
                        .setSmallIcon(R.drawable.ic_pill_v5)
                        .setLargeIcon(iconBitmap)
                        .setContentTitle("You have unfinished quizzes!")
                        .setPriority(priorityValue)
                        .setContentIntent(pendingIntent)
                        .setTicker("You have unfinished quizzes!")
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

                notificationManager.notify(generateNotificationID(), mBuilder.build())
            }
        }
    }

    private fun generateNotificationID(): Int{
        return System.currentTimeMillis().toInt()
    }
}