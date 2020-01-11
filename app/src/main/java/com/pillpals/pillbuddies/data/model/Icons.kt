package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject

open class Icons(
    var id: Int = 0,
    var icon: String = ""
) : RealmObject(){}
