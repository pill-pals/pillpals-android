package com.pillpals.pillbuddies.helpers

import java.util.Date
import java.util.Calendar

class DateHelper {
    companion object {
        fun getUnitByIndex(index: Int): Int {
            return when(index) {
                0 -> Calendar.YEAR
                1 -> Calendar.MONTH
                2 -> Calendar.DAY_OF_MONTH
                3 -> Calendar.HOUR_OF_DAY
                4 -> Calendar.MINUTE
                5 -> Calendar.SECOND
                else -> Calendar.DAY_OF_MONTH
            }
        }
        fun getIndexByUnit(unit: Int): Int {
            return when(unit) {
                Calendar.YEAR -> 0
                Calendar.MONTH -> 1
                Calendar.DAY_OF_MONTH -> 2
                Calendar.HOUR_OF_DAY -> 3
                Calendar.MINUTE -> 4
                Calendar.SECOND -> 5
                else -> 2
            }
        }
    }
}
