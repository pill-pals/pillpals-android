package com.pillpals.pillbuddies.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillpals.pillbuddies.R
import android.widget.LinearLayout
import io.realm.Realm
import io.realm.RealmObject
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.data.model.Logs
import io.realm.RealmResults
import com.pillpals.pillbuddies.helpers.DateHelper

import android.util.Log
import com.pillpals.pillbuddies.ui.DrugCard

import com.pillpals.pillbuddies.helpers.NotificationUtils
import android.os.Handler
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getDrawableIconById
import java.util.*


class DashboardFragment : Fragment() {

    public lateinit var currentStack: LinearLayout
    public lateinit var upcomingStack: LinearLayout
    public lateinit var completedStack: LinearLayout
    private lateinit var realm: Realm

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_dashboard, container, false)

        realm = Realm.getDefaultInstance()

        currentStack = view!!.findViewById(R.id.currentStack)
        upcomingStack = view!!.findViewById(R.id.upcomingStack)
        completedStack = view!!.findViewById(R.id.completedStack)

        //region
        // Testing
        //clearDatabase()
        createTestData()
        //populateAllStacks(8)
        //endregion

        setUpScheduleCards(readAllData(Schedules::class.java) as RealmResults<out Schedules>)


        val handler = Handler()
        val timer = Timer()
        val doAsynchronousTask = object : TimerTask() {
            override fun run() {
                handler.post(Runnable {
                    try {
                        update()
                    } catch (e: Exception) {
                    }
                })
            }
        }
        timer.schedule(doAsynchronousTask, 0, 60000)

        return view
    }

    public fun setUpScheduleCards(schedules: RealmResults<out Schedules>) {
        for (databaseSchedule in schedules) {
            if (databaseSchedule.deleted || databaseSchedule.medication!!.first()!!.deleted) {
                continue
            }

            var testSchedule = realm.copyFromRealm(databaseSchedule)

            var loggedToday =
                testSchedule.logs.filter { it.occurrence!! > DateHelper.today() && it.occurrence!! < DateHelper.tomorrow() }
                    .count()

            val n = testSchedule.repetitionCount!!
            val u = DateHelper.getUnitByIndex(testSchedule.repetitionUnit!!)

            while (testSchedule.occurrence!! < DateHelper.today()) {
                testSchedule.occurrence = DateHelper.addUnitToDate(testSchedule.occurrence!!, n, u)
            }

            for (i in 1..loggedToday) {
                testSchedule.occurrence = DateHelper.addUnitToDate(testSchedule.occurrence!!, -n, u)
            }

            while (testSchedule.occurrence!! < DateHelper.tomorrow()) {
                if (testSchedule.occurrence!! > DateHelper.today()) {
                    val newSchedule = Schedules(
                        testSchedule.uid,
                        testSchedule.occurrence,
                        testSchedule.repetitionCount,
                        testSchedule.repetitionUnit,
                        testSchedule.logs
                    )
                    addDrugCard(newSchedule, databaseSchedule.medication!!.first()!!)
                }
                //testSchedule = realm.copyFromRealm(testSchedule)
                testSchedule.occurrence = DateHelper.addUnitToDate(testSchedule.occurrence!!, n, u)
            }
        }
    }

    private fun addDrugCard(schedule: Schedules, medication: Medications) {
        var newCard = DrugCard(this.context!!)

        newCard.nameText.text = medication.name
        newCard.altText.text = DateHelper.dateToString(schedule.occurrence!!)
        newCard.iconBackground.setCardBackgroundColor(Color.parseColor(getColorStringByID(medication.color_id)))
        newCard.icon.setImageResource(getDrawableIconById(this.context!!, medication.icon_id))

        val diff = schedule.occurrence!!.time - Date().time
        val seconds = diff / 1000
        newCard.countdownLabel.text = DateHelper.secondsToCountdown(seconds)

        newCard.button.setOnClickListener {
            drugLogFunction(schedule)
            update()
        }

        // MARK: Select stack and colour
        val paddingTime = 10
        val currentDate = Calendar.getInstance()
        currentDate.time = Date()
        currentDate.add(Calendar.MINUTE, paddingTime)
        val lateDate = Calendar.getInstance()
        lateDate.time = Date()
        lateDate.add(Calendar.MINUTE, -paddingTime)
        val currentLog = schedule.logs.filter { it.due == schedule.occurrence }
        if (currentLog.count() > 0) {
            // Completed
            newCard.doneImage.setVisibility(LinearLayout.VISIBLE)
            newCard.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorGrey))
            completedStack.addView(newCard)
        } else if (currentDate.time >= schedule.occurrence!!) {
            // Current
            newCard.button.setVisibility(LinearLayout.VISIBLE)
            if (lateDate.time >= schedule.occurrence!!) {
                newCard.lateText.setVisibility(LinearLayout.VISIBLE)
            }
            newCard.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorWhite))
            currentStack.addView(newCard)

            // Send notification
            NotificationUtils.startAlarm(this.context!!, schedule)
        } else {
            // Upcoming
            newCard.countdownLabel.setVisibility(LinearLayout.VISIBLE)
            newCard.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorWhite))
            upcomingStack.addView(newCard)
        }
    }

    private fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
        return realm.where(realmClass).findAll()
    }

    private fun drugLogFunction(schedule: Schedules) {
        val databaseSchedule =
            realm.where(Schedules::class.java).equalTo("uid", schedule.uid).findFirst()!!
        realm.executeTransaction {
            var newLog = it.createObject(Logs::class.java, UUID.randomUUID().toString())
            newLog.occurrence = Date()
            newLog.due = schedule.occurrence
            val n = databaseSchedule.repetitionCount!!
            val u = DateHelper.getUnitByIndex(databaseSchedule.repetitionUnit!!)
            databaseSchedule.occurrence = DateHelper.addUnitToDate(schedule.occurrence!!, n, u)
            databaseSchedule.logs.add(newLog)
        }
    }

    fun update() {
        currentStack.removeViews(1, currentStack.childCount - 1)
        upcomingStack.removeViews(1, upcomingStack.childCount - 1)
        completedStack.removeViews(1, completedStack.childCount - 1)
        setUpScheduleCards(readAllData(Schedules::class.java) as RealmResults<out Schedules>)
    }

    // region
    // Testing functions
    private fun populateAllStacks(n: Int) {
        val testCards = Array(n) { DrugCard(getContext()!!) }

        for (i in testCards.indices) {
            testCards[i].nameText.text = "Medication ${i + 1}"
            when (i % 3) {
                0 -> currentStack.addView(testCards[i])
                1 -> completedStack.addView(testCards[i])
                2 -> upcomingStack.addView(testCards[i])
            }
        }
    }

    private fun createTestData() {
        var medications = readAllData(Medications::class.java) as RealmResults<Medications>
        if (medications.count() == 0) {
            createTestMedicationData()
            medications = readAllData(Medications::class.java) as RealmResults<Medications>
        }
        var schedules = readAllData(Schedules::class.java) as RealmResults<Schedules>
        if (schedules.count() == 0) {
            medications.forEach() {
                createTestSchedulesData(it)
                schedules = readAllData(Schedules::class.java) as RealmResults<Schedules>
            }
        }
        var logs = readAllData(Logs::class.java) as RealmResults<Logs>
        if (logs.count() == 0) {
            schedules.forEach() {
                createTestLogsData(it)
                logs = readAllData(Logs::class.java) as RealmResults<Logs>
            }
        }
    }

    private fun clearDatabase() {
        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()
    }

    private fun createTestMedicationData() {
        realm.executeTransaction {
            val medications = Array(3){Medications()}
            val names = listOf("Database Drug 1", "Database Drug 2", "Database Drug 3")
            val dosages = listOf("50mg", "25mg", "200ml")

            for (i in medications.indices) {
                medications[i] = it.createObject(Medications::class.java, UUID.randomUUID().toString())
                medications[i].name = names[i]
                medications[i].dosage = dosages[i]
            }
        }
    }

    private fun createTestSchedulesData(medication: Medications) {
        realm.executeTransaction {
            val schedules = Array(4){Schedules()}

            val dates = listOf(Date(), Date(), DateHelper.tomorrow(), Date())
            val hours = listOf(22, 5, 13, 13)
            val counts = listOf(1,2,7,1)
            val units = listOf(2,2,2,6)

            for (i in schedules.indices) {
                val cal = Calendar.getInstance()

                cal.time = dates[i]
                cal.set(Calendar.MILLISECOND, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.HOUR_OF_DAY, hours[i])

                schedules[i] = it.createObject(Schedules::class.java, UUID.randomUUID().toString())
                schedules[i].occurrence = cal.time
                schedules[i].repetitionCount = counts[i]
                schedules[i].repetitionUnit = units[i]

                medication.schedules.add(schedules[i])
            }
        }
    }

    private fun createTestLogsData(schedule: Schedules) {
        realm.executeTransaction {
            val n = schedule.repetitionCount!!
            val u = DateHelper.getUnitByIndex(schedule.repetitionUnit!!)

            for (i in 5.downTo(1)) {
                val log = it.createObject(Logs::class.java, UUID.randomUUID().toString())
                val due = DateHelper.addUnitToDate(schedule.occurrence!!, -i * n, u)
                log.due = due
                val cal = Calendar.getInstance()
                cal.time = due
                cal.add(Calendar.MINUTE, (-10..10).random())
                log.occurrence = cal.time
                schedule.logs.add(log)
            }
        }
    }
}