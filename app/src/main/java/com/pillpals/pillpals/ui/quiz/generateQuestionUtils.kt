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

fun mapGradeToRange(grade: String):String {
    return when (grade) {
        "A+" -> "10 minutes"
        "A" -> "20 minutes"
        "B+" -> "1 hour"
        "B" -> "2 hours"
        "C" -> "5 hours"
        "D" -> "10 hours"
        else -> "Over 10 hours or missed dose"
    }
}