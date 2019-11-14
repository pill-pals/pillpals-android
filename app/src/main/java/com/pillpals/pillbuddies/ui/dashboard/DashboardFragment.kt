package com.pillpals.pillbuddies.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.pillpals.pillbuddies.R
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import io.realm.Realm
import io.realm.RealmObject
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.data.model.Schedules
import io.realm.RealmResults
import java.util.UUID
import java.util.Date
import java.util.Calendar
import com.pillpals.pillbuddies.helpers.DateHelper
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet

import com.google.android.material.button.MaterialButton
import android.util.Log
import com.pillpals.pillbuddies.ui.DrugCard
import kotlinx.android.synthetic.main.drug_card.view.*


class DashboardFragment : Fragment() {

    public lateinit var currentStack: LinearLayout
    public lateinit var upcomingStack: LinearLayout
    public lateinit var completedStack: LinearLayout
    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_dashboard, container,false)

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

        return view
    }

    fun setUpScheduleCards(schedules: RealmResults<out Schedules>) {
        for (databaseSchedule in schedules) {
            var testSchedule = realm.copyFromRealm(databaseSchedule)

            var loggedToday = testSchedule.logs.filter { it.occurrence!! > DateHelper.today() && it.occurrence!! < DateHelper.tomorrow() }.count()
            val n = testSchedule.repetitionCount!!
            val u = DateHelper.getUnitByIndex(testSchedule.repetitionUnit!!)

            while (testSchedule.occurrence!! < DateHelper.today()) {
                testSchedule.occurrence = DateHelper.addUnitToDate(testSchedule.occurrence!!, n, u)
            }

            for (i in 1..loggedToday) {
                testSchedule.occurrence = DateHelper.addUnitToDate(testSchedule.occurrence!!, -n, u)
            }

            while(testSchedule.occurrence!! < DateHelper.tomorrow()) {
                if(testSchedule.occurrence!! > DateHelper.today()) {
                    addDrugCard(testSchedule, databaseSchedule.medication!!.first()!!)
                }
                //testSchedule = realm.copyFromRealm(testSchedule)
                testSchedule.occurrence = DateHelper.addUnitToDate(testSchedule.occurrence!!, n, u)
            }
        }
    }

    private fun addDrugCard(schedule: Schedules, medication: Medications) {
        var new = DrugCard(this.context!!)

        new.medicationNameText.text = medication.name
        new.medicationDueText.text = DateHelper.dateToString(schedule.occurrence!!)
        val diff = Date().time - schedule.occurrence!!.time
        val seconds = diff / 1000
        //new.medicationCountdownText.text = DateHelper.secondsToCountdown(seconds)
        new.medicationLogButton.setOnClickListener {
            Log.i("click","clicked ${medication.name}")
        }

        // MARK: Select stack and colour
        val currentDate = Calendar.getInstance()
        currentDate.time = Date()
        currentDate.add(Calendar.MINUTE, 10)
        val lateDate = Calendar.getInstance()
        lateDate.time = Date()
        lateDate.add(Calendar.MINUTE, -10)
        val currentLog = schedule.logs.filter { it.due == schedule.occurrence }
        if (currentLog.count() > 0) {
            // Completed
            //new.medicationDoneImage.setVisibility(LinearLayout.VISIBLE)
            new.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorLightGrey))
            completedStack.addView(new)
        }
        else if (currentDate.time >= schedule.occurrence!!) {
            // Current
            new.logButton.setVisibility(LinearLayout.VISIBLE)
            if (lateDate.time >= schedule.occurrence!!) {
                new.medicationLateText.setVisibility(LinearLayout.VISIBLE)
            }
            new.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorWhite))
            currentStack.addView(new)
        }
        else {
            // Upcoming
            //new.medicationCountdownLabel.setVisibility(LinearLayout.VISIBLE)
            new.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorWhite))
            upcomingStack.addView(new)
        }
    }

    private fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
        return realm.where(realmClass).findAll()
    }

    // region
    // Testing functions
    private fun populateAllStacks(n: Int) {
        val testCards = Array(n) { DrugCard(getContext()!!) }

        for (i in testCards.indices) {
            testCards[i].medicationNameText.text = "Medication ${i + 1}"
            when(i % 3) {
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
            createTestSchedulesData(medications.first()!!)
            schedules = readAllData(Schedules::class.java) as RealmResults<Schedules>
        }
        Log.i("yeet", schedules.first()?.occurrence.toString())
        Log.i("yeet", medications.first()?.name)
        Log.i("yeet", schedules.first()?.medication.toString())
    }

    private fun clearDatabase() {
        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()
    }

    private fun createTestMedicationData() {
        realm.executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = "Database Drug 1"
            medication.dosage = "50mg"
        }
    }

    private fun createTestSchedulesData(medication: Medications) {
        realm.executeTransaction {
            val firstSchedule = it.createObject(Schedules::class.java, UUID.randomUUID().toString())

            val date = Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.set(Calendar.MILLISECOND, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.HOUR_OF_DAY, 8)

            firstSchedule.occurrence = cal.time

            firstSchedule.repetitionCount = 1
            firstSchedule.repetitionUnit = DateHelper.getIndexByUnit(Calendar.DAY_OF_MONTH)
            medication.schedules.add(firstSchedule)

            val secondSchedule = it.createObject(Schedules::class.java, UUID.randomUUID().toString())

            val cal2 = Calendar.getInstance()
            cal2.time = date
            cal2.set(Calendar.MILLISECOND, 0)
            cal2.set(Calendar.SECOND, 0)
            cal2.set(Calendar.MINUTE, 0)
            cal2.set(Calendar.HOUR_OF_DAY, 20)
            secondSchedule.occurrence = cal2.time

            secondSchedule.repetitionCount = 1
            secondSchedule.repetitionUnit = DateHelper.getIndexByUnit(Calendar.DAY_OF_MONTH)
            medication.schedules.add(secondSchedule)
        }
    }
}