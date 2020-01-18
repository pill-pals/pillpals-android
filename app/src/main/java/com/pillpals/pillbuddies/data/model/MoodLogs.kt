package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject
import java.util.Date
import io.realm.annotations.PrimaryKey
import io.realm.annotations.LinkingObjects
import io.realm.RealmResults

open class MoodLogs(
    @PrimaryKey var uid: String? = null,
    var date: Date? = null,
    var rating: Int? = null
) : RealmObject(){}
