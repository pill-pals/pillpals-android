package com.pillpals.pillpals.data.model

import io.realm.RealmObject

open class MoodIcons(
    var id: Int = 0,
    var icon: String = ""
) : RealmObject(){}
