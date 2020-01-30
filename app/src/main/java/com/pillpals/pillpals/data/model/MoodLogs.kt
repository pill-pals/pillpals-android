package com.pillpals.pillpals.data.model

import io.realm.RealmObject
import java.util.Date
import io.realm.annotations.PrimaryKey

open class MoodLogs(
    @PrimaryKey var uid: String? = null,
    var date: Date? = null,
    var rating: Int? = null
) : RealmObject(){}
