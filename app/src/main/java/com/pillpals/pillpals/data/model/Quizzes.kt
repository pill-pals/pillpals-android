package com.pillpals.pillpals.data.model

import io.realm.RealmObject
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import java.util.*

open class Quizzes(
    @PrimaryKey var uid: String = "",
    var date: Date? = null,
    var name: String = "",
    var questions: RealmList<Questions> = RealmList()
) : RealmObject(){}
