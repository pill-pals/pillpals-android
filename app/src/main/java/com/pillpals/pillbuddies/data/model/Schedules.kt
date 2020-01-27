package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject
import java.util.Date
import io.realm.RealmList
import io.realm.annotations.PrimaryKey
import io.realm.annotations.LinkingObjects
import io.realm.RealmResults

open class Schedules(
    @PrimaryKey var uid: String? = null,
    var occurrence: Date? = null,
    var startDate: Date? = null,
    var repetitionCount: Int? = null,
    var repetitionUnit: Int? = null,
    var logs: RealmList<Logs> = RealmList(),
    var deleted: Boolean = false,
    @LinkingObjects("schedules")
    val medication: RealmResults<Medications>? = null
) : RealmObject(){}
