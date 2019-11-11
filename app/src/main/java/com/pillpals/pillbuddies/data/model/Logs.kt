package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject
import java.util.Date

open class Logs(
    var due: Date? = null,
    var occurrence: Date? = null,
    var uid: String? = null
) : RealmObject(){}
