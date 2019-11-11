package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject
import java.util.Date

open class Schedules(
    var occurrence: Date? = null,
    var repetitionCount: Int? = null,
    var repetitionUnit: Int? = null,
    var uid: String? = null
) : RealmObject(){}
