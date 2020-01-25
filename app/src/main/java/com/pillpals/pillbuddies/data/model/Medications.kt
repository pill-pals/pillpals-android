package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject
import io.realm.RealmList
import io.realm.annotations.PrimaryKey

open class Medications(
    @PrimaryKey var uid: String = "",
    var dosage: String = "",
    var name: String = "",
    var schedules: RealmList<Schedules> = RealmList(),
    var notes: String = "",
    var color_id: Int = 0,
    var icon_id: Int = 0,
    var photo_icon: Boolean = false,
    var photo_uid: String = "",
    var deleted: Boolean = false
) : RealmObject(){}
