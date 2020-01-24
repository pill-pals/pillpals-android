package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject

open class Photos(
    var id: Int = 0,
    var icon: ByteArray? = null
) : RealmObject(){}
