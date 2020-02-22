package com.pillpals.pillpals.ui.quiz

import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.helpers.StatsHelper
import io.realm.Realm
import io.realm.RealmList
import java.io.IOException

fun mapMoodToString(i: Int):String {
    return when (i) {
        0 -> "Very Bad"
        1 -> "Bad"
        2 -> "Good"
        3 -> "Very Good"
        else -> "Missing Mood Data"
    }
}