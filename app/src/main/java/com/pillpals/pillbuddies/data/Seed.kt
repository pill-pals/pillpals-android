package com.pillpals.pillbuddies.data

import com.pillpals.pillbuddies.data.model.Colors
import com.pillpals.pillbuddies.data.model.Icons
import io.realm.Realm


class Seed : Realm.Transaction {
    override fun execute(realm: Realm) {
        seedColors(realm)
        seedIcons(realm)
    }

    private fun seedColors(realm: Realm) {
        var color = Colors(0)
        color.color = "#D3D3D3"
        realm.insertOrUpdate(color)

        color = Colors(1)
        color.color = "#FFFFFF"
        realm.insertOrUpdate(color)

        color = Colors(2)
        color.color = "#000000"
        realm.insertOrUpdate(color)


        color = Colors(3)
        color.color = "#E08686"
        realm.insertOrUpdate(color)

        color = Colors(4)
        color.color = "#E8B57A"
        realm.insertOrUpdate(color)

        color = Colors(5)
        color.color = "#FBE297"
        realm.insertOrUpdate(color)

        color = Colors(6)
        color.color = "#ADE9C6"
        realm.insertOrUpdate(color)

        color = Colors(7)
        color.color = "#C1DCFF"
        realm.insertOrUpdate(color)

        color = Colors(8)
        color.color = "#DFC6F5"
        realm.insertOrUpdate(color)


        color = Colors(9)
        color.color = "#CF4343"
        realm.insertOrUpdate(color)

        color = Colors(10)
        color.color = "#D39144"
        realm.insertOrUpdate(color)

        color = Colors(11)
        color.color = "#F4D165"
        realm.insertOrUpdate(color)

        color = Colors(12)
        color.color = "#55C283"
        realm.insertOrUpdate(color)

        color = Colors(13)
        color.color = "#5B9BF1"
        realm.insertOrUpdate(color)

        color = Colors(14)
        color.color = "#AF75E5"
        realm.insertOrUpdate(color)


        color = Colors(15)
        color.color = "#8C1B1B"
        realm.insertOrUpdate(color)

        color = Colors(16)
        color.color = "#8C5D27"
        realm.insertOrUpdate(color)

        color = Colors(17)
        color.color = "#B08B19"
        realm.insertOrUpdate(color)

        color = Colors(18)
        color.color = "#176C3B"
        realm.insertOrUpdate(color)

        color = Colors(19)
        color.color = "#1A4886"
        realm.insertOrUpdate(color)

        color = Colors(20)
        color.color = "#461B6F"
        realm.insertOrUpdate(color)
    }

    private fun seedIcons(realm: Realm) {
        var icon = Icons(0)
        icon.icon = "ic_pill_v5"
        realm.insertOrUpdate(icon)
    }

    override fun hashCode(): Int {
        return Seed::class.java.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        return obj != null && obj is Seed
    }
}