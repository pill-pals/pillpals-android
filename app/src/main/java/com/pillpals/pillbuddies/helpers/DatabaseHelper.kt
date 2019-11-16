package com.pillpals.pillbuddies.helpers

import com.pillpals.pillbuddies.data.model.Schedules
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults

class DatabaseHelper {
    companion object{
        fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
            return Realm.getDefaultInstance().where(realmClass).findAll()
        }
        fun getScheduleByUid(uid: String): Schedules? {
            return Realm.getDefaultInstance().where(Schedules::class.java).equalTo("uid", uid).findFirst()
        }
    }
}