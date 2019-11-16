package com.pillpals.pillbuddies.helpers

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults

class DatabaseHelper {
    companion object{
        fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
            return Realm.getDefaultInstance().where(realmClass).findAll()
        }
    }
}