package com.pillpals.pillpals.data.model

import io.realm.RealmObject
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

open class QuestionTemplates(
    @PrimaryKey var id: Int = 0,
    var canUseOnNonLinkedMedications: Boolean = false,
    var notRelatedToMedication: Boolean = false,
    var questions: RealmList<Questions> = RealmList()
    ) : RealmObject(){}
