package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Photos(
    @PrimaryKey var uid: String = "",
    var icon: ByteArray? = null
) : RealmObject(){}
