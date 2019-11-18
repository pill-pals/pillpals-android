package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject
import io.realm.RealmList
import io.realm.annotations.PrimaryKey

open class Medications(
    @PrimaryKey var uid: String = "",
    var dosage: String = "",
    var name: String = "",
    var schedules: RealmList<Schedules> = RealmList(),
    var notes: String = ""
) : RealmObject(){}
