package com.pillpals.pillpals.data

import com.pillpals.pillpals.data.model.Colors
import com.pillpals.pillpals.data.model.Icons
import com.pillpals.pillpals.data.model.MoodIcons
import io.realm.Realm

fun seedQuestionTemplates(realm: Realm) {
        var color = Colors(0)
        color.color = "#D3D3D3"
        realm.insertOrUpdate(color)

        color = Colors(1)
        color.color = "#FFFFFF"
        realm.insertOrUpdate(color)

    }