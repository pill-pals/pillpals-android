package com.pillpals.pillpals.data

import com.pillpals.pillpals.data.model.*
import io.realm.Realm
import io.realm.RealmList
import java.util.*

fun seedQuestionTemplates(realm: Realm) {
    var qT = QuestionTemplates()

    for (i in 1..10) {
        qT.id = i
        realm.insertOrUpdate(qT)
    }
    for (i in 101..106){
        qT.id = i
        qT.canUseOnNonLinkedMedications = true
        realm.insertOrUpdate(qT)
    }
    for (i in 201..204){
        qT.id = i
        qT.notRelatedToMedication = true
        realm.insertOrUpdate(qT)
    }
}
