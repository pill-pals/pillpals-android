package com.pillpals.pillbuddies.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.pillpals.pillbuddies.R
import io.realm.Realm
import io.realm.RealmObject
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.data.model.Logs
import com.pillpals.pillbuddies.data.model.MoodLogs
import io.realm.RealmResults
import com.pillpals.pillbuddies.helpers.DateHelper
import androidx.core.content.res.ResourcesCompat
import android.util.Log
import com.pillpals.pillbuddies.ui.DrugCard

import com.pillpals.pillbuddies.helpers.NotificationUtils
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getDrawableIconById
import com.pillpals.pillbuddies.ui.DosageTimeBox
import kotlinx.android.synthetic.main.delete_prompt.view.*
import kotlinx.android.synthetic.main.drug_card.view.*
import kotlinx.android.synthetic.main.time_prompt.view.*
import kotlinx.android.synthetic.main.time_prompt.view.dialogCancelBtn
import java.util.*
import androidx.core.widget.PopupWindowCompat.showAsDropDown
import android.graphics.drawable.Drawable
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.view.View.MeasureSpec
import android.view.View.MeasureSpec.UNSPECIFIED
import android.widget.TextView
import android.content.Context.LAYOUT_INFLATER_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.view.LayoutInflater
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.PorterDuff
import androidx.cardview.widget.CardView
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.obliterateSchedule
import com.pillpals.pillbuddies.ui.AddDrugActivity
import io.realm.kotlin.createObject


class DashboardFragment : Fragment() {

    public lateinit var currentStack: LinearLayout
    public lateinit var upcomingStack: LinearLayout
    public lateinit var completedStack: LinearLayout
    public lateinit var moodIconList: LinearLayout
    //public var selectedMoodImage: String? = null

    private lateinit var realm: Realm

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_dashboard, container, false)

        //Realm.deleteRealm(Realm.getDefaultConfiguration())
        realm = Realm.getDefaultInstance()

        currentStack = view!!.findViewById(R.id.currentStack)
        upcomingStack = view!!.findViewById(R.id.upcomingStack)
        completedStack = view!!.findViewById(R.id.completedStack)
        moodIconList = view!!.findViewById(R.id.moodIconList)

        //region
        // Testing
        // *running clearDatabase will also clear the seed database
        // clearDatabase()
        createTestData()
        //populateAllStacks(8)
        //endregion

        setUpScheduleCards(readAllData(Schedules::class.java) as RealmResults<out Schedules>)
        setUpMoodTracker()

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

    //Mood tracker
    private fun getCurrentMoodLog():MoodLogs? {
        return realm.where(MoodLogs::class.java).equalTo("date", DateHelper.today()).findFirst()
    }

    private fun createOrUpdateCurrentMoodLog(rating: Int) {
        var currentMoodLog = getCurrentMoodLog()

        realm.executeTransaction {
            if (currentMoodLog == null) {
                var cal = Calendar.getInstance()
                cal.time = Date()
                cal.set(Calendar.MILLISECOND, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.HOUR_OF_DAY, 0)

                currentMoodLog = it.createObject(MoodLogs::class.java, UUID.randomUUID().toString())
                currentMoodLog!!.date = cal.time
            }

            currentMoodLog!!.rating = rating
        }
    }

    private fun setUpMoodTracker() {
        for (i in 0 until moodIconList.getChildCount()) {
            val card = moodIconList.getChildAt(i) as CardView
            val image = card.getChildAt(0) as ImageView

            card.setOnClickListener {
                //selectedMoodImage = image.tag as String
                //Log.v("HELLO", selectedMoodImage)
                createOrUpdateCurrentMoodLog(DatabaseHelper.getMoodIconIDByString(image.tag as String))
                updateMoodStyles()
            }
        }
        updateMoodStyles()
    }

    private fun updateMoodStyles() {
        for (i in 0 until moodIconList.getChildCount()) {
            val card = moodIconList.getChildAt(i) as CardView
            val image = card.getChildAt(0) as ImageView
            val cardImageDrawable = image.tag
            if (getCurrentMoodLog() != null && cardImageDrawable == DatabaseHelper.getMoodIconByID(getCurrentMoodLog()?.rating!!)) {
                card.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                image.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null))
            }
            else {
                card.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
                image.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorGrey, null))
            }
        }
    }

    //Popover menus
    //region
    private fun popoverMenuCurrent(v: View, schedule: Schedules) {
        val popup = PopupMenu(context, v.overflowMenu)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.current_schedule, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.currentLogAtTime -> {
                    val timeDialog = LayoutInflater.from(this.context).inflate(R.layout.time_prompt, null)
                    val simpleTimePicker = timeDialog.findViewById<TimePicker>(R.id.simpleTimePicker)

                    val title = SpannableString("Time Picker")
                    title.setSpan(
                        ForegroundColorSpan(this.context!!.resources.getColor(R.color.colorLightGrey)),
                        0,
                        title.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val dialogBuilder = AlertDialog.Builder(this.context!!)
                        .setView(timeDialog)
                        .setTitle(title)

                    timeDialog.findViewById<TextView>(R.id.timeTextPrompt).text = "Select log time"
                    timeDialog.findViewById<Button>(R.id.dialogAddBtn).text = "Log"

                    val timeAlertDialog = dialogBuilder.show()
                    timeDialog.dialogAddBtn.setOnClickListener {
                        timeAlertDialog.dismiss()
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.MILLISECOND, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MINUTE, simpleTimePicker.minute)
                        cal.set(Calendar.HOUR_OF_DAY, simpleTimePicker.hour)

                        drugLogFunction(schedule, cal.time)
                        update()
                    }

                    timeDialog.dialogCancelBtn.setOnClickListener {
                        timeAlertDialog.dismiss()
                    }
                }
            }
            true
        }
        popup.show()
    }

    private fun popoverMenuUpcoming(v: View, schedule: Schedules) {
        val popup = PopupMenu(context, v.overflowMenu)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.upcoming_schedule, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.upcomingLogAtTime -> {
                    val timeDialog = LayoutInflater.from(this.context).inflate(R.layout.time_prompt, null)
                    val simpleTimePicker = timeDialog.findViewById<TimePicker>(R.id.simpleTimePicker)

                    val title = SpannableString("Time Picker")
                    title.setSpan(
                        ForegroundColorSpan(this.context!!.resources.getColor(R.color.colorLightGrey)),
                        0,
                        title.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val dialogBuilder = AlertDialog.Builder(this.context!!)
                        .setView(timeDialog)
                        .setTitle(title)

                    timeDialog.findViewById<TextView>(R.id.timeTextPrompt).text = "Select log time"
                    timeDialog.findViewById<Button>(R.id.dialogAddBtn).text = "Log"

                    val timeAlertDialog = dialogBuilder.show()
                    timeDialog.dialogAddBtn.setOnClickListener {
                        timeAlertDialog.dismiss()
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.MILLISECOND, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MINUTE, simpleTimePicker.minute)
                        cal.set(Calendar.HOUR_OF_DAY, simpleTimePicker.hour)

                        drugLogFunction(schedule, cal.time)
                        update()
                    }

                    timeDialog.dialogCancelBtn.setOnClickListener {
                        timeAlertDialog.dismiss()
                    }
                }
            }
            true
        }
        popup.show()
    }

    private fun popoverMenuCompleted(v: View, schedule: Schedules, log: Logs) {
        val popup = PopupMenu(context, v.overflowMenu)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.completed_schedule, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.completedChangeLogTime -> {
                    val timeDialog = LayoutInflater.from(this.context).inflate(R.layout.time_prompt, null)
                    val simpleTimePicker = timeDialog.findViewById<TimePicker>(R.id.simpleTimePicker)

                    val title = SpannableString("Time Picker")
                    title.setSpan(
                        ForegroundColorSpan(this.context!!.resources.getColor(R.color.colorLightGrey)),
                        0,
                        title.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val dialogBuilder = AlertDialog.Builder(this.context!!)
                        .setView(timeDialog)
                        .setTitle(title)

                    timeDialog.findViewById<TextView>(R.id.timeTextPrompt).text = "Select log time"
                    timeDialog.findViewById<Button>(R.id.dialogAddBtn).text = "Log"

                    val timeAlertDialog = dialogBuilder.show()
                    timeDialog.dialogAddBtn.setOnClickListener {
                        undoLog(log)

                        timeAlertDialog.dismiss()
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.MILLISECOND, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MINUTE, simpleTimePicker.minute)
                        cal.set(Calendar.HOUR_OF_DAY, simpleTimePicker.hour)

                        drugLogFunction(schedule, cal.time)
                        update()
                    }

                    timeDialog.dialogCancelBtn.setOnClickListener {
                        timeAlertDialog.dismiss()
                    }
                }
                R.id.completedUndoLog -> {
                    undoLog(log)
                    update()
                }
            }
            true
        }
        popup.show()
    }
    //endregion

    fun setUpScheduleCards(schedules: RealmResults<out Schedules>) {
        for (databaseSchedule in schedules) {
            if (databaseSchedule.medication.isNullOrEmpty()) {
                obliterateSchedule(databaseSchedule)
                continue
            }
            else if (databaseSchedule.deleted || databaseSchedule.medication!!.first()!!.deleted) {
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

        newCard.icon.setOnClickListener {
            val intent = Intent(context, AddDrugActivity::class.java)
            intent.putExtra("medication-uid", medication.uid)
            startActivityForResult(intent, 1)
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
            newCard.logtimeLabel.text = DateHelper.dateToString(currentLog.first().occurrence!!)
            newCard.logtimeLabel.setVisibility(LinearLayout.VISIBLE)
            newCard.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorGrey))
            newCard.overflowMenu.setOnClickListener {
                popoverMenuCompleted(newCard, schedule, currentLog.first())
            }
            completedStack.addView(newCard)
        } else if (currentDate.time >= schedule.occurrence!!) {
            // Current
            newCard.button.setVisibility(LinearLayout.VISIBLE)
            if (lateDate.time >= schedule.occurrence!!) {
                newCard.lateText.setVisibility(LinearLayout.VISIBLE)
            }
            newCard.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorWhite))
            newCard.overflowMenu.setOnClickListener {
                popoverMenuCurrent(newCard, schedule)
            }
            currentStack.addView(newCard)
            // Send notification
            NotificationUtils.startAlarm(this.context!!, schedule)
        } else {
            // Upcoming
            newCard.countdownLabel.setVisibility(LinearLayout.VISIBLE)
            newCard.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorWhite))
            newCard.overflowMenu.setOnClickListener {
                popoverMenuUpcoming(newCard, schedule)
            }
            upcomingStack.addView(newCard)
        }
    }

    private fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
        return realm.where(realmClass).findAll()
    }

    private fun drugLogFunction(schedule: Schedules, time: Date = Date()) {
        val databaseSchedule =
            realm.where(Schedules::class.java).equalTo("uid", schedule.uid).findFirst()!!
        realm.executeTransaction {
            var newLog = it.createObject(Logs::class.java, UUID.randomUUID().toString())
            newLog.occurrence = time
            newLog.due = schedule.occurrence
            val n = databaseSchedule.repetitionCount!!
            val u = DateHelper.getUnitByIndex(databaseSchedule.repetitionUnit!!)
            databaseSchedule.occurrence = DateHelper.addUnitToDate(schedule.occurrence!!, n, u)
            databaseSchedule.logs.add(newLog)
        }
    }

    private fun undoLog(log: Logs) {
        realm.executeTransaction {
            val databaseLog = realm.where(Logs::class.java).equalTo("uid", log.uid).findFirst()!!
            val schedule = databaseLog.schedule!!.first()!!
            val n = schedule.repetitionCount!!
            val u = DateHelper.getUnitByIndex(schedule.repetitionUnit!!)

            schedule.occurrence = DateHelper.addUnitToDate(schedule.occurrence!!, -n, u)
            databaseLog.deleteFromRealm()
        }
    }

    fun update() {
        currentStack.removeViews(1, currentStack.childCount - 1)
        upcomingStack.removeViews(1, upcomingStack.childCount - 1)
        completedStack.removeViews(1, completedStack.childCount - 1)
        setUpScheduleCards(readAllData(Schedules::class.java) as RealmResults<out Schedules>)
        hideEmptyStacks()
    }

    private fun hideEmptyStacks() {
        hideStackIfEmpty(currentStack)
        hideStackIfEmpty(upcomingStack)
        hideStackIfEmpty(completedStack)
    }

    private fun hideStackIfEmpty(stack: LinearLayout) {
        if (stack.childCount == 1) {
            stack.visibility = View.GONE
        } else {
            stack.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        update()
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
        var moodLogs = readAllData(MoodLogs::class.java) as RealmResults<MoodLogs>
        if (moodLogs.count() == 0) {
            createTestMoodLogData()
            //moodLogs = readAllData(MoodLogs::class.java) as RealmResults<MoodLogs>
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

    private fun createTestMoodLogData() {
        realm.executeTransaction {
            val moodLogs = Array(3){MoodLogs()}
            val dates = listOf(0,1,2)
            val ratings = listOf(1,2,3)

            for (i in moodLogs.indices) {
                var cal = Calendar.getInstance()
                cal.time = DateHelper.addUnitToDate(Date(),dates[i],DateHelper.getIndexByUnit(Calendar.DATE))
                cal.set(Calendar.MILLISECOND, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.HOUR_OF_DAY, 0)


                moodLogs[i] = it.createObject(MoodLogs::class.java, UUID.randomUUID().toString())
                moodLogs[i].rating = ratings[i]
                moodLogs[i].date = cal.time
            }
        }
    }
    //endregion
}