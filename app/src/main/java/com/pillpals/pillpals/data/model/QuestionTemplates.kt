package com.pillpals.pillpals.data.model

import io.realm.RealmObject
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

open class QuestionTemplates(
    @PrimaryKey var uid: String = "",
    var genFnct: Function<Questions>? = null,
    var canUseOnNonLinkedMedications: Boolean = false,
    var questions: RealmList<Questions> = RealmList()
    ) : RealmObject(){}
