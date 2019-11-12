package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject
import java.util.Date
import io.realm.annotations.PrimaryKey
import io.realm.annotations.LinkingObjects
import io.realm.RealmResults

open class Logs(
    @PrimaryKey var uid: String? = null,
    var due: Date? = null,
    var occurrence: Date? = null,
    @LinkingObjects("logs")
    val schedule: RealmResults<Schedules>? = null
) : RealmObject(){}
