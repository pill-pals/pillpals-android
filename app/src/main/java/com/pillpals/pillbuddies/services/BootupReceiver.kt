package com.pillpals.pillbuddies.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import com.pillpals.pillbuddies.ui.dashboard.DashboardFragment
import io.realm.RealmResults

public class BootupReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val schedules = DatabaseHelper.readAllData(
            Schedules::class.java
        ) as RealmResults<out Schedules>
        DashboardFragment().setUpScheduleCards(schedules)
    }
}
