package com.pillpals.pillpals.helpers

import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId

class DateHelper {
    companion object {
        fun convertToLocalDateViaInstant(dateToConvert: Date): LocalDateTime {
            return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }
        fun getUnitByIndex(index: Int): Int {
            return when(index) {
                0 -> Calendar.YEAR
                1 -> Calendar.MONTH
                2 -> Calendar.DATE
                3 -> Calendar.HOUR_OF_DAY
                4 -> Calendar.MINUTE
                5 -> Calendar.SECOND
                6 -> Calendar.WEEK_OF_YEAR
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
                Calendar.WEEK_OF_YEAR -> 6
                else -> 2
            }
        }
        fun getMillisecondsByUnit(unit: Int): Long {
            return when(unit) {
                Calendar.YEAR -> 1000L * 60L * 60L * 24L * 7L * 52L
                Calendar.MONTH -> 1000L * 60L * 60L * 24L * 30L
                Calendar.DATE -> 1000 * 60 * 60 * 24
                Calendar.HOUR_OF_DAY -> 1000 * 60 * 60
                Calendar.MINUTE -> 1000 * 60
                Calendar.SECOND -> 1000
                Calendar.WEEK_OF_YEAR -> 1000 * 60 * 60 * 24 * 7
                else -> 1000 * 60 * 60 * 24
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
        fun yesterday(): Date {
            val date = Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DATE, -1)
            cal.set(Calendar.MILLISECOND, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            return cal.time
        }
        fun yesterdayAt12pm(): Date {
            val date = yesterday()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.set(Calendar.HOUR_OF_DAY, 12)
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
            return if (seconds / 3600 < 1) {
                "${(seconds / 60 + 1)}m"
            }
            else {
                val hours = (seconds / 3600)
                val minutes = (seconds / 60) % 60
                val minutesString = if (minutes > 9) minutes.toString().take(2) else "0" + minutes.toString().take(2)
                return "${hours}h ${minutesString}m"
            }
        }
    }
}

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
    }
}
