package com.pillpals.pillbuddies.helpers

import android.content.Context
import com.pillpals.pillbuddies.data.model.Colors
import com.pillpals.pillbuddies.data.model.Icons
import com.pillpals.pillbuddies.data.model.Medications
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
        fun getMedicationByUid(uid: String): Medications? {
            return Realm.getDefaultInstance().where(Medications::class.java).equalTo("uid", uid).findFirst()
        }
        fun getColorStringByID(id: Int): String {
            return Realm.getDefaultInstance().where(Colors::class.java).equalTo("id", id).findFirst()!!.color
        }
        fun getColorIDByString(color: String): Int {
            return Realm.getDefaultInstance().where(Colors::class.java).equalTo("color", color).findFirst()!!.id
        }
        fun deleteSchedules(schedules: List<Schedules>) {
            for (schedule in schedules) {
                Realm.getDefaultInstance().executeTransaction {
                    schedule.deleted = true
                }
            }
        }
        fun getIconById(id: Int): String {
            return Realm.getDefaultInstance().where(Icons::class.java).equalTo("id", id).findFirst()!!.icon
        }
        fun getDrawableIconById(context: Context, id: Int): Int {
            val icon = getIconById(id)
            return context.getResources()
                .getIdentifier("drawable/$icon", null, context.packageName)
        }
    }
}
