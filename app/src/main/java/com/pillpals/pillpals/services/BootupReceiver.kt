package com.pillpals.pillpals.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.NotificationUtils
import com.pillpals.pillpals.ui.dashboard.DashboardFragment
import io.realm.RealmResults

public class BootupReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val schedules = DatabaseHelper.readAllData(
            Schedules::class.java
        ) as RealmResults<out Schedules>
        DashboardFragment().setUpSchedules(schedules, false)
        NotificationUtils.createNotificationChannel(context)
        NotificationUtils.updateAlarms(context)
    }
}
