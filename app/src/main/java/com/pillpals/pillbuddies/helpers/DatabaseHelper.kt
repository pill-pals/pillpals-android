package com.pillpals.pillbuddies.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.pillpals.pillbuddies.data.model.*
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import kotlin.random.Random
import kotlin.random.nextInt
import java.io.ByteArrayOutputStream

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
        fun getRandomColorString(): String {
            return Realm.getDefaultInstance().where(Colors::class.java).findAll().random().color
        }
        // Gets a random color that hasn't been used before if possible, otherwise gives a random one that has been used
        // Never gives black (#000000)
        fun getRandomUniqueColorString(): String {
            var possibleColors = (0 until Realm.getDefaultInstance().where(Colors::class.java).count().toInt()).toMutableList()
            possibleColors.remove(2) // Removes black from the list of possible colours
            for (medication in readAllData(Medications::class.java) as RealmResults<Medications>) {
                possibleColors.remove(medication.color_id)
            }
            if (possibleColors.size > 0) {
                return getColorStringByID(possibleColors[Random.nextInt(possibleColors.size)])
            } else {
                return getRandomColorString()
            }
        }
        fun deleteSchedules(schedules: List<Schedules>) {
            for (schedule in schedules) {
                Realm.getDefaultInstance().executeTransaction {
                    schedule.deleted = true
                    schedule.deletedDate = DateHelper.today()
                }
            }
        }
        fun obliterateSchedule(schedule: Schedules) {
            Realm.getDefaultInstance().executeTransaction {
                schedule.deleteFromRealm()
            }
        }
        fun getIconByID(id: Int): String {
            return Realm.getDefaultInstance().where(Icons::class.java).equalTo("id", id).findFirst()!!.icon
        }
        fun getIconIDByString(icon: String): Int {
            return Realm.getDefaultInstance().where(Icons::class.java).equalTo("icon", icon).findFirst()!!.id
        }
        fun getRandomIcon(): String {
            return Realm.getDefaultInstance().where(Icons::class.java).findAll().random().icon
        }
        fun getMoodIconByID(id: Int): String {
            return Realm.getDefaultInstance().where(MoodIcons::class.java).equalTo("id", id).findFirst()!!.icon
        }
        fun getMoodIconIDByString(icon: String): Int {
            return Realm.getDefaultInstance().where(MoodIcons::class.java).equalTo("icon", icon).findFirst()!!.id
        }
        fun getDrawableIconById(context: Context, id: Int): Int {
            val icon = getIconByID(id)
            return context.getResources()
                .getIdentifier("drawable/$icon", null, context.packageName)
        }
        fun convertBitmapToByteArray(icon: Bitmap): ByteArray{
            val base = ByteArrayOutputStream()
            icon.compress(Bitmap.CompressFormat.PNG, 100, base)
            return base.toByteArray()
        }
        fun convertByteArrayToBitmap(byteArray: ByteArray?): Bitmap{
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        }
        fun getByteArrayById(id: String): ByteArray?{
            return Realm.getDefaultInstance().where(Photos::class.java).equalTo("uid", id).findFirst()!!.icon
        }
        fun getCorrectIconDrawable(context: Context, medication: Medications): Drawable {
            if(medication.photo_icon){
                return BitmapDrawable(context.resources, Bitmap.createScaledBitmap(convertByteArrayToBitmap(getByteArrayById(medication.photo_uid)), 64, 64, false))
            }else{
                return ContextCompat.getDrawable(context, getDrawableIconById(context, medication.icon_id))!!
            }
        }
    }
}
