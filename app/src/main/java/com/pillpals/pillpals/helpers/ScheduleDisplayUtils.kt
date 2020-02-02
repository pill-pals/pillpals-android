package com.pillpals.pillpals.helpers

import android.app.Activity
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.ui.ScheduleRecord
import java.util.*

fun calculateScheduleRecords(schedules: List<Schedules>, activityObj: Activity, recordSetToDelete: MutableList<ScheduleRecord> = mutableListOf()): MutableList<ScheduleRecord> {
    // To contain all records that will be written to the view
    var scheduleRecords = mutableListOf<ScheduleRecord>()

    // The record set that contains the days of the week on which the medication is scheduled to reoccur weekly
    // Eg. To result in the string '8:00 AM on Mon, Wed' when added to scheduleRecords
    var compiledScheduleRecords = mutableListOf<CompiledScheduleRecord>()

    schedules.forEach {
        if (it.deleted ||
            recordSetToDelete.isNotEmpty() && scheduleRecordExistsByUid(recordSetToDelete, it.uid!!)) {
            return@forEach
        }

        val timeString = DateHelper.dateToString(it.occurrence!!)
        if(isWeeklyRecurrence(it)) {
            val cal = Calendar.getInstance()
            cal.time = it.occurrence
            if(compiledScheduleRecordExists(timeString, compiledScheduleRecords)) {
                //add day of week to existing compiled schedule record
                val compiledScheduleRecord = compiledScheduleRecords.find {it.time == timeString}
                compiledScheduleRecord!!.daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1] = 1
                compiledScheduleRecord.schedules.add(it)
            } else {
                //create new compiled schedule record
                val compiledScheduleRecord = CompiledScheduleRecord(timeString)
                compiledScheduleRecord.daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1] = 1
                compiledScheduleRecord.schedules.add(it)
                compiledScheduleRecords.add(compiledScheduleRecord)
            }
        } else {
            val scheduleRecord = ScheduleRecord(activityObj)

            scheduleRecord.timeText.text = timeString
            scheduleRecord.recurrenceText.text = "every"
            scheduleRecord.dateText.text = getRecurrenceString(it.repetitionUnit!!,it.repetitionCount!!)
            scheduleRecord.schedules = listOf(it)
            scheduleRecords.add(scheduleRecord)
        }
    }

    //create scheduleRecords from compiledScheduleRecords
    compiledScheduleRecords.forEach {
        val scheduleRecord = ScheduleRecord(activityObj)

        scheduleRecord.timeText.text = it.time
        scheduleRecord.recurrenceText.text = "on"
        scheduleRecord.dateText.text = getDaysOfWeekList(it.daysOfWeek)
        scheduleRecord.schedules = it.schedules
        scheduleRecords.add(scheduleRecord)
    }

    return scheduleRecords
}

private fun isWeeklyRecurrence(schedule: Schedules):Boolean {
    val a = (schedule.repetitionCount == 7 && DateHelper.getUnitByIndex(schedule.repetitionUnit!!) == Calendar.DATE)
    val b = (schedule.repetitionCount == 1 && DateHelper.getUnitByIndex(schedule.repetitionUnit!!) == Calendar.WEEK_OF_YEAR)
    return (a || b)
}

private fun getRecurrenceString(repetitionUnit: Int, repetitionCount: Int):String {
    if (repetitionCount == 1) {
        return when (DateHelper.getUnitByIndex(repetitionUnit)) {
            Calendar.YEAR -> "year"
            Calendar.MONTH -> "month"
            Calendar.WEEK_OF_YEAR -> "week"
            Calendar.DATE -> "day"
            Calendar.HOUR_OF_DAY -> "hour"
            Calendar.MINUTE -> "minute"
            Calendar.SECOND -> "second"
            else -> "MISSING"
        }
    } else {
        return when (DateHelper.getUnitByIndex(repetitionUnit)) {
            Calendar.YEAR -> "$repetitionCount years"
            Calendar.MONTH -> "$repetitionCount months"
            Calendar.WEEK_OF_YEAR -> "$repetitionCount weeks"
            Calendar.DATE -> "$repetitionCount days"
            Calendar.HOUR_OF_DAY -> "$repetitionCount hours"
            Calendar.MINUTE -> "$repetitionCount minutes"
            Calendar.SECOND ->  "$repetitionCount seconds"
            else -> "MISSING"
        }
    }
}

private fun compiledScheduleRecordExists(timeString: String, compiledScheduleRecords: MutableList<CompiledScheduleRecord>):Boolean {
    return compiledScheduleRecords.filter { it.time == timeString }.count() > 0
}

private fun scheduleRecordExistsByUid(scheduleRecords: List<ScheduleRecord>, uid: String): Boolean {
    return scheduleRecords.filter { scheduleRecord -> scheduleRecord.schedules.map { it.uid }.contains(uid) }.count() > 0
}

private fun getDaysOfWeekList(daysOfWeek: IntArray):String {
    val daysOfWeekList = listOf("Sun","Mon","Tue","Wed","Thurs","Fri","Sat")

    val daysList = daysOfWeek.mapIndexed { index, value ->
        if (value == 1) daysOfWeekList[index]
        else null
    }.filterNotNull()

    return daysList.joinToString()
}

data class CompiledScheduleRecord(val time: String) {
    var daysOfWeek: IntArray = IntArray(7) {0}
    var schedules: MutableList<Schedules> = mutableListOf()
}