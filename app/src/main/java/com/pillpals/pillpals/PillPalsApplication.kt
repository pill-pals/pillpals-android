package com.pillpals.pillpals

import android.app.Application
import android.content.Intent
import io.realm.Realm
import io.realm.RealmConfiguration
import com.pillpals.pillpals.data.Seed
import com.pillpals.pillpals.helpers.AlarmNoiseHelper

class PillPalsApplication : Application() {
    companion object {
        lateinit var alarmNoiseHelper: AlarmNoiseHelper
    }

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        //val config = RealmConfiguration.Builder().build()
        val config = RealmConfiguration
            .Builder()
            .deleteRealmIfMigrationNeeded()
            .initialData(Seed())
            .build()
        Realm.setDefaultConfiguration(config)

        alarmNoiseHelper = AlarmNoiseHelper(this)
    }
}
