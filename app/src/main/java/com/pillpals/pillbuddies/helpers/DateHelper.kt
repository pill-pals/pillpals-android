package com.pillpals.pillbuddies.helpers

import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat

class DateHelper {
    companion object {
        fun getUnitByIndex(index: Int): Int {
            return when(index) {
                0 -> Calendar.YEAR
                1 -> Calendar.MONTH
                2 -> Calendar.DATE
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
                Calendar.DATE -> 2
                Calendar.HOUR_OF_DAY -> 3
                Calendar.MINUTE -> 4
                Calendar.SECOND -> 5
                else -> 2
            }
        }
        fun today(): Date {
            val date = Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.set(Calendar.MILLISECOND, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            return cal.time
        }
        fun tomorrow(): Date {
            val date = Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DATE, 1)
            cal.set(Calendar.MILLISECOND, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            return cal.time
        }
        fun addUnitToDate(date: Date, n: Int, u: Int): Date {
            var cal = Calendar.getInstance()
            cal.time = date
            cal.add(u, n)
            return cal.time
        }
        @JvmStatic
        fun dateToString(date: Date): String {
            val format = SimpleDateFormat("h:mm a")
            return format.format(date)
        }
        fun secondsToCountdown(seconds: Long): String {
            if (seconds / 3600 < 1) {
                return "" + seconds / 60 + 1 + " min"
            }
            else {
                val hours = (seconds / 3600)
                val minutes = (seconds / 60) % 60
                return "${hours}:${minutes} hours"
            }
        }
    }
}
