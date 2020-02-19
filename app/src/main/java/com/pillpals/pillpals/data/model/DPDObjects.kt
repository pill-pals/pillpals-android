package com.pillpals.pillpals.data.model

import io.realm.RealmObject
import java.util.Date
import io.realm.RealmList
import io.realm.annotations.PrimaryKey
import io.realm.annotations.LinkingObjects
import io.realm.RealmResults

open class DPDObjects(
    @PrimaryKey var dpd_id: Int = 0,
    var name: String = "",
    var administrationRoutes: RealmList<String> = RealmList(),
    var activeIngredients: RealmList<String> = RealmList(),
    var dosageString: String = "",
    var medications: RealmList<Medications> = RealmList(),
    var ndc_id: String? = null,
    var rxcui: String? = null,
    var spl_set_id: String? = null
) : RealmObject(){}
