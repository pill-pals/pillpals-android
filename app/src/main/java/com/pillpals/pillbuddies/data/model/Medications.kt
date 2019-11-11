package com.pillpals.pillbuddies.data.model

import io.realm.RealmObject

open class Medications(
    var dosage: String? = null,
    var name: String? = null,
    var uid: String? = null
) : RealmObject(){}
