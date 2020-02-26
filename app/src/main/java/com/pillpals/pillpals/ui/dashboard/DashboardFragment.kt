package com.pillpals.pillpals.ui.dashboard

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.pillpals.pillpals.R
import io.realm.Realm
import io.realm.RealmObject
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.data.model.Logs
import com.pillpals.pillpals.data.model.MoodLogs
import io.realm.RealmResults
import androidx.core.content.res.ResourcesCompat
import com.pillpals.pillpals.ui.DrugCard

import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import kotlinx.android.synthetic.main.drug_card.view.*
import java.util.*
import android.widget.TextView
import android.view.LayoutInflater
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.AnimatedVectorDrawable
import android.provider.Settings
import android.util.Log
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.children
import com.google.gson.Gson
import com.pillpals.pillpals.data.ActiveIngredient
import com.pillpals.pillpals.data.AdministrationRoute
import com.pillpals.pillpals.data.DrugProduct
import com.pillpals.pillpals.data.OpenFDANameResponse
import com.pillpals.pillpals.helpers.*
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getCorrectIconDrawable
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.obliterateSchedule
import com.pillpals.pillpals.ocrreader.OcrCaptureActivity
import com.pillpals.pillpals.ui.AddDrugActivity
import com.pillpals.pillpals.ui.medications.medication_info.MedicationInfoActivity
import com.pillpals.pillpals.ui.quiz.generateQuestion
import com.pillpals.pillpals.ui.search.SearchActivity
import com.shopify.promises.Promise
import com.shopify.promises.then
import kotlinx.android.synthetic.main.add_medication_prompt.view.*
import kotlinx.android.synthetic.main.scan_prompt.view.*
import kotlinx.android.synthetic.main.time_prompt.view.*
import okhttp3.*
import okio.IOException
import java.util.concurrent.TimeUnit


class DashboardFragment : Fragment() {

    public lateinit var currentStack: LinearLayout
    public lateinit var upcomingStack: LinearLayout
    public lateinit var completedStack: LinearLayout
    public lateinit var moodIconList: LinearLayout
    public lateinit var upcomingCollapseBtn: ImageButton
    public lateinit var completedCollapseBtn: ImageButton
    public lateinit var dashboardParent: ConstraintLayout
    lateinit var noMedicationsSection: ConstraintLayout
    lateinit var noSchedulesSection: ConstraintLayout
    lateinit var drugButton: Button
    //public var selectedMoodImage: String? = null

    private lateinit var prefs: SharedPreferences

    private lateinit var realm: Realm

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        FileWriter.createJSONStringFromData(context!!)
        val view = inflater!!.inflate(R.layout.fragment_dashboard, container, false)

        prefs = activity!!.getPreferences(Context.MODE_PRIVATE)

        //Realm.deleteRealm(Realm.getDefaultConfiguration())
        realm = Realm.getDefaultInstance()

        currentStack = view!!.findViewById(R.id.currentStack)
        upcomingStack = view.findViewById(R.id.upcomingStack)
        completedStack = view.findViewById(R.id.completedStack)
        moodIconList = view.findViewById(R.id.moodIconList)
        upcomingCollapseBtn = view.findViewById(R.id.upcomingCollapseBtn)
        completedCollapseBtn = view.findViewById(R.id.completedCollapseBtn)
        dashboardParent = view.findViewById(R.id.dashboardParent)
        noMedicationsSection = view.findViewById(R.id.noMedicationsSection)
        noSchedulesSection = view.findViewById(R.id.noSchedulesSection)
        drugButton = view.findViewById(R.id.drugButton)

        val outerContext = this.context!!
        drugButton.setOnClickListener {
            val addPrompt = LayoutInflater.from(this.context).inflate(R.layout.add_medication_prompt, null)

            val title = SpannableString("Add Medication")
            title.setSpan(
                ForegroundColorSpan(this!!.resources.getColor(R.color.colorLightGrey)),
                0,
                title.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this.context!!)
                .setView(addPrompt)
                .setTitle(title)

            val alertDialog = dialogBuilder.show()

            addPrompt.dialogScanBtn.setOnClickListener {
                alertDialog.dismiss()
                val scanPrompt = LayoutInflater.from(this.context).inflate(R.layout.scan_prompt, null)

                val scanTitle = SpannableString("Scan Medication")
                scanTitle.setSpan(
                    ForegroundColorSpan(this!!.resources.getColor(R.color.colorLightGrey)),
                    0,
                    scanTitle.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val scanDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(outerContext)
                    .setView(scanPrompt)
                    .setTitle(scanTitle)

                val scanAlertDialog = scanDialogBuilder.show()

                scanPrompt.dialogStartScanBtn.setOnClickListener {
                    scanAlertDialog.dismiss()
                    val intent = Intent(context, OcrCaptureActivity::class.java)
                    startActivityForResult(intent, 6)
                }

                scanPrompt.dialogCancelScanBtn.setOnClickListener {
                    scanAlertDialog.dismiss()
                }
            }

            addPrompt.dialogSearchBtn.setOnClickListener {
                alertDialog.dismiss()
                val intent = Intent(context, SearchActivity::class.java)
                startActivityForResult(intent, 4)
            }

            addPrompt.dialogManualBtn.setOnClickListener {
                alertDialog.dismiss()
                val intent = Intent(context, AddDrugActivity::class.java)
                startActivityForResult(intent, 5)
            }

            addPrompt.dialogCancelAddBtn.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        noMedicationsSection.visibility = View.GONE
        noSchedulesSection.visibility = View.GONE

        val allMedications = readAllData(Medications::class.java) as RealmResults<out Medications>
        val allSchedules = readAllData(Schedules::class.java) as RealmResults<out Schedules>

        if(allMedications.filter{ !it.deleted }.count() == 0) {
            noMedicationsSection.visibility = View.VISIBLE
        }
        else if(allSchedules.filter{ !it.deleted && it.medication?.firstOrNull() != null && !it.medication.first()!!.deleted }.count() == 0) {
            noSchedulesSection.visibility = View.VISIBLE
        }

        //region
        // Testing
        // *running clearDatabase will also clear the seed database
        // clearDatabase()
        // createTestData()
        //populateAllStacks(8)
        //endregion

        getActivity()!!.invalidateOptionsMenu()
        setUpSchedules(allSchedules.sort("occurrence"), true)
        setUpMoodTracker()

        setUpCollapsing()

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

        Log.i("del", allMedications.last()!!.dpd_object!!.first()!!.toString())

        return view
    }

    override fun onResume() {
        super.onResume()
        getActivity()!!.invalidateOptionsMenu()
        FileWriter.createJSONStringFromData(context!!)
        update()
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

    private fun setUpCollapsing() {
        upcomingCollapseBtn.setOnClickListener {
            toggleCollapse(upcomingStack, upcomingCollapseBtn)
        }
        if (prefs.getBoolean(getString(R.string.upcoming_stack_collapsed), false)) {
            upcomingCollapseBtn.setImageResource(R.drawable.ic_circle_chevron_right_from_down)
        }
        upcomingStack.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        upcomingStack.layoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING)
        upcomingStack.layoutTransition.enableTransitionType(LayoutTransition.APPEARING)

        completedCollapseBtn.setOnClickListener {
            toggleCollapse(completedStack, completedCollapseBtn)
        }
        if (prefs.getBoolean(getString(R.string.completed_stack_collapsed), true)) {
            completedCollapseBtn.setImageResource(R.drawable.ic_circle_chevron_right_from_down)
        }
        completedStack.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        completedStack.layoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING)
        completedStack.layoutTransition.enableTransitionType(LayoutTransition.APPEARING)
    }

    private fun toggleCollapse(stack: LinearLayout, button: ImageButton) {
        var buttonChanged = false
        var previouslyCollapsed = false
        for (view in stack.children) {
            if (stack.indexOfChild(view) != 0) {
                previouslyCollapsed = (view.visibility == View.GONE)
                if (previouslyCollapsed) {
                    if (!buttonChanged) {
                        button.setImageResource(R.drawable.ic_circle_chevron_down_from_right)
                        (button.drawable as AnimatedVectorDrawable).start()
                        buttonChanged = true
                    }
                    view.visibility = View.VISIBLE
                } else {
                    if (!buttonChanged) {
                        button.setImageResource(R.drawable.ic_circle_chevron_right_from_down)
                        (button.drawable as AnimatedVectorDrawable).start()
                        buttonChanged = true
                    }
                    view.visibility = View.GONE
                }
            }
        }

        var prefKey = ""
        if (stack == upcomingStack) {
            prefKey = getString(R.string.upcoming_stack_collapsed)
        } else { //stack == completedStack
            prefKey = getString(R.string.completed_stack_collapsed)
        }
        with (prefs.edit()) {
            //Set preference to collapse completed stack by default
            putBoolean(prefKey, !previouslyCollapsed)
            commit()
        }
    }

    //Popover menus
    //region
    private fun popoverMenuCurrent(v: View, medication: Medications, schedule: Schedules) {
        val popup = PopupMenu(context, v.overflowMenu)
        val inflater: MenuInflater = popup.menuInflater
        val dpdObject = medication.dpd_object?.firstOrNull()
        if(dpdObject == null) {
            inflater.inflate(R.menu.current_schedule, popup.menu)
        }
        else {
            inflater.inflate(R.menu.current_schedule_dpd, popup.menu)
        }

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

                        drugLogFunction(schedule, context!!, cal.time)
                        update()
                    }

                    timeDialog.dialogCancelBtn.setOnClickListener {
                        timeAlertDialog.dismiss()
                    }
                }
                R.id.viewDrugInfo -> {
                    if(dpdObject == null) return@setOnMenuItemClickListener true

                    val intent = Intent(context, MedicationInfoActivity::class.java)
                    intent.putExtra("drug-code", dpdObject.dpd_id)
                    intent.putExtra("icon-color", getColorStringByID(medication.color_id))
                    intent.putStringArrayListExtra("administration-routes", ArrayList(dpdObject.administrationRoutes))
                    intent.putStringArrayListExtra("active-ingredients",  ArrayList(dpdObject.activeIngredients))
                    intent.putExtra("dosage-string", dpdObject.dosageString)
                    intent.putExtra("name-text", dpdObject.name)
                    intent.putExtra("ndc-code", dpdObject.ndc_id)
                    intent.putExtra("rxcui", dpdObject.rxcui)
                    intent.putExtra("spl-set-id", dpdObject.spl_set_id)
                    startActivityForResult(intent, 2)
                }
                R.id.linkMedication -> {
                    val intent = Intent(context, SearchActivity::class.java)
                    intent.putExtra("medication-uid", medication.uid)
                    startActivityForResult(intent, 3)
                }
            }
            true
        }
        popup.show()
    }

    private fun popoverMenuUpcoming(v: View, medication: Medications, schedule: Schedules) {
        val popup = PopupMenu(context, v.overflowMenu)
        val inflater: MenuInflater = popup.menuInflater
        val dpdObject = medication.dpd_object?.firstOrNull()
        if(dpdObject == null) {
            inflater.inflate(R.menu.upcoming_schedule, popup.menu)
        }
        else {
            inflater.inflate(R.menu.upcoming_schedule_dpd, popup.menu)
        }
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

                        drugLogFunction(schedule, context!!, cal.time)
                        update()
                    }

                    timeDialog.dialogCancelBtn.setOnClickListener {
                        timeAlertDialog.dismiss()
                    }
                }
                R.id.viewDrugInfo -> {
                    if(dpdObject == null) return@setOnMenuItemClickListener true

                    val intent = Intent(context, MedicationInfoActivity::class.java)
                    intent.putExtra("drug-code", dpdObject.dpd_id)
                    intent.putExtra("icon-color", getColorStringByID(medication.color_id))
                    intent.putStringArrayListExtra("administration-routes", ArrayList(dpdObject.administrationRoutes))
                    intent.putStringArrayListExtra("active-ingredients",  ArrayList(dpdObject.activeIngredients))
                    intent.putExtra("dosage-string", dpdObject.dosageString)
                    intent.putExtra("name-text", dpdObject.name)
                    intent.putExtra("ndc-code", dpdObject.ndc_id)
                    intent.putExtra("rxcui", dpdObject.rxcui)
                    intent.putExtra("spl-set-id", dpdObject.spl_set_id)
                    startActivityForResult(intent, 2)
                }
                R.id.linkMedication -> {
                    val intent = Intent(context, SearchActivity::class.java)
                    intent.putExtra("medication-uid", medication.uid)
                    startActivityForResult(intent, 3)
                }
            }
            true
        }
        popup.show()
    }

    private fun popoverMenuCompleted(v: View, medication: Medications, schedule: Schedules, log: Logs) {
        val popup = PopupMenu(context, v.overflowMenu)
        val inflater: MenuInflater = popup.menuInflater
        val dpdObject = medication.dpd_object?.firstOrNull()
        if(dpdObject == null) {
            inflater.inflate(R.menu.completed_schedule, popup.menu)
        }
        else {
            inflater.inflate(R.menu.completed_schedule_dpd, popup.menu)
        }
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

                        drugLogFunction(schedule, context!!, cal.time)
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
                R.id.viewDrugInfo -> {
                    if(dpdObject == null) return@setOnMenuItemClickListener true

                    val intent = Intent(context, MedicationInfoActivity::class.java)
                    intent.putExtra("drug-code", dpdObject.dpd_id)
                    intent.putExtra("icon-color", getColorStringByID(medication.color_id))
                    intent.putStringArrayListExtra("administration-routes", ArrayList(dpdObject.administrationRoutes))
                    intent.putStringArrayListExtra("active-ingredients",  ArrayList(dpdObject.activeIngredients))
                    intent.putExtra("dosage-string", dpdObject.dosageString)
                    intent.putExtra("name-text", dpdObject.name)
                    intent.putExtra("ndc-code", dpdObject.ndc_id)
                    intent.putExtra("rxcui", dpdObject.rxcui)
                    intent.putExtra("spl-set-id", dpdObject.spl_set_id)
                    startActivityForResult(intent, 2)
                }
                R.id.linkMedication -> {
                    val intent = Intent(context, SearchActivity::class.java)
                    intent.putExtra("medication-uid", medication.uid)
                    startActivityForResult(intent, 3)
                }
            }
            true
        }
        popup.show()
    }
    //endregion

    fun setUpSchedules(schedules: RealmResults<out Schedules>, addCards: Boolean) {
        for (databaseSchedule in schedules) {
            if (databaseSchedule.deleted || databaseSchedule.medication!!.first()!!.deleted) {
                continue
            }

            //Primarily for calling setUpSchedules from BootupReceiver without inflating fragment
            if (!::realm.isInitialized) {
                realm = Realm.getDefaultInstance()
            }

            val testSchedule = realm.copyFromRealm(databaseSchedule)

            // Time range: Today OR the later half of yesterday

            val loggedInTimeRange =
                testSchedule.logs.filter {
                    it.occurrence!! > DateHelper.yesterdayAt12pm() && it.occurrence!! < DateHelper.tomorrow()
                }.count()

            val n = testSchedule.repetitionCount!!
            val u = DateHelper.getUnitByIndex(testSchedule.repetitionUnit!!)

            while (testSchedule.occurrence!! < DateHelper.yesterdayAt12pm()) {
                testSchedule.occurrence = DateHelper.addUnitToDate(testSchedule.occurrence!!, n, u)
            }

            for (i in 1..loggedInTimeRange) {
                testSchedule.occurrence = DateHelper.addUnitToDate(testSchedule.occurrence!!, -n, u)
            }

            if (addCards) {
                while (testSchedule.occurrence!! < DateHelper.tomorrow()) {
                    if (testSchedule.occurrence!! > DateHelper.yesterdayAt12pm()) {
                        val newSchedule = Schedules(
                            testSchedule.uid,
                            testSchedule.occurrence,
                            testSchedule.startDate,
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
    }

    private fun addDrugCard(schedule: Schedules, medication: Medications) {
        var newCard = DrugCard(this.context!!)

        newCard.nameText.text = medication.name
        newCard.altText.text = when(true) {
            schedule.occurrence!! > DateHelper.today() -> DateHelper.dateToString(schedule.occurrence!!)
            schedule.occurrence!! < DateHelper.today() && schedule.occurrence!! > DateHelper.yesterdayAt12pm() -> "${DateHelper.dateToString(schedule.occurrence!!)} Yesterday"
            else -> DateHelper.dateToString(schedule.occurrence!!)
        }

        newCard.iconBackground.setCardBackgroundColor(Color.parseColor(getColorStringByID(medication.color_id)))
        newCard.icon.setImageDrawable(getCorrectIconDrawable(this.context!!, medication))

        val diff = schedule.occurrence!!.time - Date().time
        val seconds = diff / 1000
        newCard.countdownLabel.text = DateHelper.secondsToCountdown(seconds)


        newCard.button.setOnClickListener {
            drugLogFunction(schedule, context!!)
            update()
        }

        newCard.icon.setOnClickListener {
            val dpdObject = medication.dpd_object?.firstOrNull() ?: return@setOnClickListener

            val intent = Intent(context, MedicationInfoActivity::class.java)
            intent.putExtra("drug-code", dpdObject.dpd_id)
            intent.putExtra("icon-color", getColorStringByID(medication.color_id))
            intent.putStringArrayListExtra("administration-routes", ArrayList(dpdObject.administrationRoutes))
            intent.putStringArrayListExtra("active-ingredients",  ArrayList(dpdObject.activeIngredients))
            intent.putExtra("dosage-string", dpdObject.dosageString)
            intent.putExtra("name-text", dpdObject.name)
            intent.putExtra("ndc-code", dpdObject.ndc_id)
            intent.putExtra("rxcui", dpdObject.rxcui)
            intent.putExtra("spl-set-id", dpdObject.spl_set_id)
            startActivityForResult(intent, 2)
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
            newCard.logtimeLabel.visibility = LinearLayout.VISIBLE
            newCard.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorGrey))
            newCard.overflowMenu.setOnClickListener {
                popoverMenuCompleted(newCard, medication, schedule, currentLog.first())
            }

            if (prefs.getBoolean(getString(R.string.completed_stack_collapsed), true)) {
                newCard.visibility = View.GONE
            }

            completedStack.addView(newCard)
        } else if (currentDate.time >= schedule.occurrence!!) {
            // Current
            newCard.button.visibility = LinearLayout.VISIBLE
            if (lateDate.time >= schedule.occurrence!!) {
                newCard.lateText.visibility = LinearLayout.VISIBLE
            }
            newCard.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorWhite))
            newCard.overflowMenu.setOnClickListener {
                popoverMenuCurrent(newCard, medication, schedule)
            }
            currentStack.addView(newCard)
        } else {
            // Upcoming
            newCard.countdownLabel.visibility = LinearLayout.VISIBLE
            newCard.drugCard.setCardBackgroundColor(this.resources.getColor(R.color.colorWhite))
            newCard.overflowMenu.setOnClickListener {
                popoverMenuUpcoming(newCard, medication, schedule)
            }

            if (prefs.getBoolean(getString(R.string.upcoming_stack_collapsed), false)) {
                newCard.visibility = View.GONE
            }

            upcomingStack.addView(newCard)
        }
    }

    private fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
        return realm.where(realmClass).findAll()
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
        upcomingStack.layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING)
        completedStack.layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING)
        upcomingStack.layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
        completedStack.layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
        upcomingStack.layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
        completedStack.layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
        currentStack.removeViews(1, currentStack.childCount - 1)
        upcomingStack.removeViews(1, upcomingStack.childCount - 1)
        completedStack.removeViews(1, completedStack.childCount - 1)

        noMedicationsSection.visibility = View.GONE
        noSchedulesSection.visibility = View.GONE

        val allMedications = readAllData(Medications::class.java) as RealmResults<out Medications>
        val allSchedules = readAllData(Schedules::class.java) as RealmResults<out Schedules>

        if(allMedications.filter{ !it.deleted }.count() == 0) {
            noMedicationsSection.visibility = View.VISIBLE
        }
        else if(allSchedules.filter{ !it.deleted && it.medication?.firstOrNull() != null && !it.medication.first()!!.deleted }.count() == 0) {
            noSchedulesSection.visibility = View.VISIBLE
        }

        setUpSchedules(allSchedules.sort("occurrence"), true)

        setUpCollapsing()

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

        // 1 -> clicking card icon
        // 2 -> view drug info
        // 3 -> link medication
        // 4 -> Search
        // 5 -> Add drug
        // 6 -> OCR
        // 7 -> Info to add (from OCR)

        if(data != null) {
            if(requestCode == 6) {
                if(data.hasExtra("din")) {
                    val din = data.getStringExtra("din")
                    if(din != null) getFromDin(din)
                }
            }
        }

        val schedules = readAllData(Schedules::class.java) as RealmResults<out Schedules>
        schedules.forEach {
            if(it.medication?.firstOrNull() == null) obliterateSchedule(it)
        }

        update()
    }

    fun getFromDin(din: String) {
        val url = "https://health-products.canada.ca/api/drug/drugproduct/?din=${din}"

        val client = OkHttpClient
            .Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder().url(url).build()

        var ingredientNameList = listOf<String>()
        var dosageString = ""
        var ndcCode: String? = ""
        var rxcui: String? = ""
        var splSetId: String? = ""


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                        return
                    }

                    val jsonString = response.body!!.string()
                    val gson = Gson()
                    val drugProducts = gson.fromJson(jsonString, Array<DrugProduct>::class.java).toList()

                    // First for now
                    val firstDrugProduct = drugProducts.firstOrNull()

                    firstDrugProduct ?: return backgroundThreadToast(context!!, "Drug not found with din $din. Please try again.", Toast.LENGTH_LONG)

                    backgroundThreadToast(context!!, "Drug found: ${firstDrugProduct.brand_name}. Loading...", Toast.LENGTH_LONG)

                    drugProducts.forEachIndexed {namedIndex, drugProduct ->
                        val url = "https://health-products.canada.ca/api/drug/activeingredient/?id=${drugProduct.drug_code}"

                        val request = Request.Builder().url(url).build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                e.printStackTrace()
                            }

                            override fun onResponse(call: Call, response: Response) {
                                response.use {
                                    if (!response.isSuccessful) {
                                        backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                        return
                                    }

                                    val jsonString = response.body!!.string()
                                    val gson = Gson()
                                    val activeIngredients = gson.fromJson(jsonString, Array<ActiveIngredient>::class.java).toList()

                                    ingredientNameList = activeIngredients.fold(listOf<String>()) { acc, it ->
                                        acc.plus(it.ingredient_name)
                                    }

                                    val dosageValues = activeIngredients.fold(listOf<String>()) { acc, it ->
                                        acc.plus(it.strength)
                                    }

                                    val dosageUnits = activeIngredients.fold(listOf<String>()) { acc, it ->
                                        if(acc.contains(it.strength_unit)) acc
                                        else acc.plus(it.strength_unit)
                                    }

                                    dosageString = "${dosageValues.joinToString("/")} ${dosageUnits.joinToString("/")}"

                                    // Get other ID's from FDA
                                    val url = "https://api.fda.gov/drug/ndc.json?limit=100&search=brand_name:${drugProduct.brand_name.replace("( .*)".toRegex(), "")}"

                                    val request = Request.Builder().url(url).build()

                                    client.newCall(request).enqueue(object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                            e.printStackTrace()
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            response.use {
                                                if (response.isSuccessful) {
                                                    val jsonString = response.body!!.string()
                                                    val gson = Gson()
                                                    val fdaResponse = gson.fromJson(jsonString, OpenFDANameResponse::class.java)

                                                    if(fdaResponse.error == null) {
                                                        val fdaResults = fdaResponse.results

                                                        val fdaResultWithDosage = fdaResults?.filter {
                                                            if(it.active_ingredients == null) return@filter false
                                                            val totalVal = it.active_ingredients.fold(0f) {acc, it ->
                                                                acc + it.strength.replace("( .*)".toRegex(), "").toFloat()
                                                            }
                                                            it.active_ingredients.any {
                                                                it.strength.contains("${dosageValues.first()} ${dosageUnits.firstOrNull()?.toLowerCase()}")
                                                            } || dosageValues.firstOrNull()?.toFloat() == totalVal
                                                        }?.firstOrNull()

                                                        val firstFdaResult = fdaResults?.firstOrNull()

                                                        // SET FDA IDS

                                                        if(fdaResultWithDosage != null) {
                                                            ndcCode = fdaResultWithDosage.product_ndc
                                                            rxcui = fdaResultWithDosage.openfda.rxcui?.firstOrNull()
                                                            splSetId = fdaResultWithDosage.openfda.spl_set_id?.firstOrNull()
                                                        }
                                                        else if(firstFdaResult != null) {
                                                            ndcCode = firstFdaResult.product_ndc
                                                            rxcui = firstFdaResult.openfda.rxcui?.firstOrNull()
                                                            splSetId = firstFdaResult.openfda.spl_set_id?.firstOrNull()
                                                        }
                                                    }
                                                }



                                                val url = "https://health-products.canada.ca/api/drug/route/?id=${drugProduct.drug_code}"

                                                val request = Request.Builder().url(url).build()

                                                client.newCall(request).enqueue(object :
                                                    Callback {
                                                    override fun onFailure(call: Call, e: IOException) {
                                                        backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                                        e.printStackTrace()
                                                    }

                                                    override fun onResponse(call: Call, response: Response) {
                                                        response.use {
                                                            if (!response.isSuccessful) {
                                                                backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                                                return
                                                            }

                                                            val jsonString = response.body!!.string()
                                                            val gson = Gson()
                                                            val administrationRoutes = gson.fromJson(jsonString, Array<AdministrationRoute>::class.java).toList()

                                                            var colorString =
                                                                DatabaseHelper.getRandomColorString()
                                                            while(colorString == "#000000") { // Let's not let black be selected randomly
                                                                colorString = DatabaseHelper.getRandomColorString()
                                                            }

                                                            var administrationRoutesList = listOf<String>()

                                                            val firstRoute = administrationRoutes.firstOrNull()
                                                            if(firstRoute != null) {

                                                                administrationRoutesList = administrationRoutes.fold(listOf<String>()) { acc, it ->
                                                                    acc.plus(it.route_of_administration_name)
                                                                }
                                                            }

                                                            val infoIntent = Intent(context, MedicationInfoActivity::class.java)


                                                            infoIntent.putExtra("link-medication", false)


                                                            infoIntent.putExtra("drug-code", drugProduct.drug_code)
                                                            infoIntent.putExtra("icon-color", colorString)
                                                            infoIntent.putStringArrayListExtra("administration-routes", ArrayList(administrationRoutesList))
                                                            infoIntent.putStringArrayListExtra("active-ingredients", ArrayList(ingredientNameList))
                                                            infoIntent.putExtra("dosage-string", dosageString)
                                                            infoIntent.putExtra("name-text", drugProduct.brand_name)
                                                            if(firstRoute != null) {
                                                                infoIntent.putExtra("icon-resource", administrationRouteToIconString(firstRoute.route_of_administration_name))
                                                            }

                                                            infoIntent.putExtra("ndc-code", ndcCode)
                                                            infoIntent.putExtra("rxcui", rxcui)
                                                            infoIntent.putExtra("spl-set-id", splSetId)

                                                            startActivityForResult(infoIntent, 7)
                                                        }
                                                    }
                                                })
                                            }
                                        }
                                    })
                                }
                            }
                        })
                    }
                }
            }
        })
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
                schedules[i].startDate = DateHelper.addUnitToDate(cal.time,-5,Calendar.DATE)

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
                cal.add(Calendar.MINUTE, (-1300..1300).random())
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