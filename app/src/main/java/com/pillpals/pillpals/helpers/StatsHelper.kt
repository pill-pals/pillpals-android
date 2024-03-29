package com.pillpals.pillpals.helpers

import com.pillpals.pillpals.ui.statistics.DataLogs
import com.pillpals.pillpals.ui.statistics.MissingLogs
import com.pillpals.pillpals.ui.statistics.TimeCount
import io.realm.Realm
import java.util.*
import kotlin.math.abs
import android.util.Log
import com.pillpals.pillpals.data.model.*

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

        fun getGradeStringFromTimeDifference (minuteDifference: Float):String {
            return when {
                minuteDifference > 1200f -> "F"
                minuteDifference > 600f -> "D"
                minuteDifference > 120f -> "C"
                minuteDifference > 60f -> "B"
                minuteDifference > 20f -> "B+"
                minuteDifference > 10f -> "A"
                minuteDifference == -1f -> "No Logs"
                else -> "A+"
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

            var dataLogs = listOf<DataLogs>()
            for (log in allLogs) {
                dataLogs = dataLogs.plus(DataLogs(log.occurrence!!,log.due!!))
            }

            //currently gives an offset of 1 whole day for a missed medication
            for (missingLog in allMissingLogs) {
                dataLogs = dataLogs.plus(DataLogs(DateHelper.addUnitToDate(missingLog.due,1,Calendar.DATE),missingLog.due))
            }

            dataLogs = dataLogs.sortedBy { it.due }

            return dataLogs.fold(mutableListOf<TimeCount>()) { acc, it ->
                val logDate = Calendar.getInstance()
                logDate.time = it.due!!

                val existingTimeCount = getTimeCount(logDate.time, timeSpanFilter, acc)

                if(existingTimeCount != null) {
                    val logsList = existingTimeCount.logs.plus(it)
                    var sum = 0f
                    logsList.forEach{
                        sum += abs(it.occurrence!!.time - it.due!!.time).toFloat()
                    }
                    var average = sum/(existingTimeCount.count + 1)
                    acc[acc.indexOf(existingTimeCount)] = TimeCount(logDate.time, existingTimeCount.count + 1, average, logsList)
                }
                else {
                    val logOffset = abs(it.occurrence!!.time - it.due!!.time).toFloat()
                    acc.add(TimeCount(logDate.time, 1, logOffset, listOf(it)))
                }

                acc
            }
        }

        private fun getTimeCount(time: Date, timeSpanFilter: String, timeCountList: List<TimeCount>): TimeCount? {
            val cal = Calendar.getInstance()
            cal.time = time
            return when (timeSpanFilter) {
                "Day" -> timeCountList.filter { equalTimeUnit(it,cal,listOf(Calendar.HOUR_OF_DAY,Calendar.DAY_OF_YEAR,Calendar.YEAR)) }.firstOrNull()
                "Week" -> timeCountList.filter { equalTimeUnit(it,cal,listOf(Calendar.DAY_OF_YEAR,Calendar.YEAR)) }.firstOrNull()
                "Month" -> timeCountList.filter { equalTimeUnit(it,cal,listOf(Calendar.DAY_OF_YEAR,Calendar.YEAR)) }.firstOrNull()
                "Year" -> timeCountList.filter { equalTimeUnit(it,cal,listOf(Calendar.MONTH,Calendar.YEAR)) }.firstOrNull()
                else -> timeCountList.filter { equalTimeUnit(it,cal,listOf(Calendar.HOUR_OF_DAY,Calendar.DAY_OF_YEAR,Calendar.YEAR)) }.firstOrNull()
            }
        }

        fun equalTimeUnit(timeCount: TimeCount, cal: Calendar, unitList: List<Int>):Boolean {
            val calData = Calendar.getInstance()
            calData.time = timeCount.time
            var equalFlag = true
            unitList.forEach{
                if (calData.get(it) != cal.get(it)) {
                    equalFlag = false
                }
            }
            return equalFlag
        }

        fun calculateQuizScore(medication: Medications, questions: List<Questions>):Float {
            var correctCount = 0
            var answeredCount = 0

            questions.forEach{
                if (it.medication == medication && it.userAnswer != null && it.userAnswer == it.correctAnswer) {
                    correctCount++
                }
                if (it.medication == medication && it.userAnswer != null) {
                    answeredCount++
                }
            }
            return if(answeredCount == 0) {
                -1f
            } else {
                correctCount*1f/answeredCount
            }
        }

        fun calculateAdherenceScore(timeCounts: List<TimeCount>):Float {
            var sum = 0f
            timeCounts.forEach{
                sum += it.offset
            }

            return if (timeCounts.count() == 0) {
                -1f
            } else {
                sum / timeCounts.count() / 1000 / 60
            }
        }

        fun calculateMoodScore(moodLogs: List<MoodLogs>):Float {
            var sum = 0f
            var count = 0f
            moodLogs.forEach{
                if (it.rating != null) {
                    sum += it.rating!!
                    count += 1f
                }
            }

            return if (count == 0f) {
                -1f
            } else {
                sum/count
            }
        }
    }
}