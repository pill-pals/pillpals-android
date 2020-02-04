package com.pillpals.pillpals.helpers

import com.pillpals.pillpals.data.model.Logs
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.ui.statistics.DataLogs
import com.pillpals.pillpals.ui.statistics.MissingLogs
import com.pillpals.pillpals.ui.statistics.TimeCount
import io.realm.Realm
import java.util.*
import kotlin.math.abs
import android.util.Log

class StatsHelper {
    companion object {
        fun getMissingLogsForSchedule(schedule: Schedules, realm: Realm, endDate: Date = DateHelper.today()): List<MissingLogs> {
            val counterSchedule = realm.copyFromRealm(schedule)

            val n = schedule.repetitionCount!!
            val u = DateHelper.getUnitByIndex(schedule.repetitionUnit!!)

            var missingLogs = listOf<MissingLogs>()
            counterSchedule.occurrence = schedule.startDate

            while (counterSchedule.occurrence!! < endDate) {
                val missing = schedule.logs.filter { it.due!! == counterSchedule.occurrence }.none()
                if(missing) {
                    // There's no log at counterSchedule.occurrence.
                    // Could create some new log, or add missing logs to a data class that's checked in averageLogsAcrossSchedules
                    // MissingLog is an example of a somewhat useful data class, just a placeholder though
                    // Log.i("test","Missing Log: " + counterSchedule.occurrence)
                    missingLogs = missingLogs.plus(MissingLogs(schedule, counterSchedule.occurrence!!))
                }

                counterSchedule.occurrence = DateHelper.addUnitToDate(counterSchedule.occurrence!!, n, u)
            }

            return missingLogs
        }

        fun getGradeFromTimeDifference (minuteDifference: Float):Float {
            return when {
                minuteDifference > 1200f -> 1f
                minuteDifference > 600f -> 2f
                minuteDifference > 120f -> 3f
                minuteDifference > 60f -> 4f
                minuteDifference > 20f -> 5f
                minuteDifference > 10f -> 6f
                minuteDifference == -1f -> 0f
                else -> 7f
            }
        }

        fun averageLogsAcrossSchedules(medication: Medications, realm: Realm, timeSpanFilter: String): List<TimeCount> {
            val schedules = medication.schedules

            var allLogs = schedules.fold(listOf<Logs>()) { acc, it -> acc.plus(it.logs) }

            var allMissingLogs = schedules.fold(listOf<MissingLogs>()) { acc, it ->
                var missingLogs = if (it.deleted) {
                    getMissingLogsForSchedule(it,realm,it.deletedDate!!)
                } else
                {
                    getMissingLogsForSchedule(it,realm)
                }

                acc.plus(missingLogs)
            }

            Log.i("test",allMissingLogs.toString())

            var dataLogs = listOf<DataLogs>()
            for (log in allLogs) {
                dataLogs = dataLogs.plus(DataLogs(log.occurrence!!,log.due!!))
            }

            //currently gives an offset of 1 whole day for a missed medication
            for (missingLog in allMissingLogs) {
                dataLogs = dataLogs.plus(DataLogs(DateHelper.addUnitToDate(missingLog.due,1,Calendar.DATE),missingLog.due))
            }

            dataLogs = dataLogs.sortedBy { it.due }

            Log.i("test",dataLogs.toString())

            return dataLogs.fold(mutableListOf<TimeCount>()) { acc, it ->
                val logDate = Calendar.getInstance()
                logDate.time = it.due!!
                logDate.set(Calendar.MILLISECOND, 0)
                logDate.set(Calendar.SECOND, 0)
                logDate.set(Calendar.MINUTE, 0)
                when (timeSpanFilter) {
                    "Day" -> null // Avg in hours
                    "Week" -> logDate.set(Calendar.HOUR_OF_DAY, 0) // Avg in days
                    "Month" -> logDate.set(Calendar.HOUR_OF_DAY, 0) // Avg in days
                    "Year" -> { // Avg in Months
                        logDate.set(Calendar.HOUR_OF_DAY, 0)
                        logDate.set(Calendar.DAY_OF_MONTH, 1)
                    }
                }
                val existingTimeCount = getTimeCount(logDate.time, acc)

                if(existingTimeCount != null) {
                    val logsList = existingTimeCount.logs.plus(it)
                    val average = logsList.fold(0f) { sum, log ->
                        val logOffset = abs(it.occurrence!!.time - it.due!!.time).toFloat() // Calculate y value of bar here
                        sum + logOffset
                    } / logsList.count()
                    acc[acc.indexOf(existingTimeCount)] = TimeCount(logDate.time, existingTimeCount.count + 1, average, logsList)
                }
                else {
                    val logOffset = (it.occurrence!!.time - it.due!!.time).toFloat()
                    acc.add(TimeCount(logDate.time, 1, logOffset, listOf(it)))
                }

                acc
            }
        }

        private fun getTimeCount(time: Date, timeCountList: List<TimeCount>): TimeCount? {
            return timeCountList.filter { it.time == time }.firstOrNull()
        }
    }
}