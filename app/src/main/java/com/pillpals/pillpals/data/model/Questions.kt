package com.pillpals.pillpals.data.model

import io.realm.RealmObject
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

open class Questions(
    @PrimaryKey var uid: String = "",
    var question: String = "",
    var answers: RealmList<String> = RealmList(),
    var correctAnswer: Int? = null,
    var userAnswer: Int? = null,
    var medication: Medications? = null,
    @LinkingObjects("questions")
    val quiz: RealmResults<Quizzes>? = null
    ) : RealmObject(){}
