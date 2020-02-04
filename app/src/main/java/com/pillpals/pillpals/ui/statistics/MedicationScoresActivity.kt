package com.pillpals.pillpals.ui.statistics

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.LinearLayout

import androidx.appcompat.app.AppCompatActivity
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.MoodLogs

import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.helpers.DateHelper
import com.pillpals.pillpals.helpers.StatsHelper
import com.pillpals.pillpals.ui.DataPair
import com.pillpals.pillpals.ui.DrugCard
import kotlinx.android.synthetic.main.drug_card.view.*
import io.realm.Realm
import io.realm.RealmResults
import java.util.*
import kotlin.math.roundToInt


class MedicationScoresActivity : AppCompatActivity() {

    public lateinit var stack: LinearLayout
    private lateinit var realm: Realm
    private lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        realm = Realm.getDefaultInstance()

        prefs = getPreferences(Context.MODE_PRIVATE)

        setContentView(R.layout.activity_medication_scores)

        stack = this!!.findViewById(R.id.stack)
        stack.layoutTransition.enableTransitionType(LayoutTransition.CHANGING) //Makes collapsing smooth

        for (drug in realm.where(Medications::class.java).findAll()) {
            if (drug.deleted) {
                continue
            }

            addDrugCard(drug)
        }
        Log.i("text",stack.toString())
    }

    private fun addDrugCard(medication: Medications) {
        var newCard = DrugCard(this)

        newCard.nameText.text = medication.name
        newCard.altText.text = medication.dosage
        newCard.iconBackground.setCardBackgroundColor(Color.parseColor(getColorStringByID(medication.color_id)))
        newCard.icon.setImageDrawable(
            DatabaseHelper.getCorrectIconDrawable(
                this,
                medication
            )
        )
        newCard.overflowMenu.visibility = View.GONE

        newCard.drugCardLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        newCard.scheduleContainer.visibility = View.VISIBLE
        if (prefs.getBoolean(getString(R.string.schedule_preview_collapsed_prefix) + medication.uid, false)) {
            newCard.scheduleStack.visibility = View.GONE
            newCard.collapseButton.setImageResource(R.drawable.ic_circle_chevron_down_from_up)
        }

        newCard.collapseButton.setOnClickListener {
            toggleCollapse(newCard.scheduleStack, newCard.collapseButton, medication)
        }

        var dataPairs = calculateDataPairs(medication)
        dataPairs.forEach { dataPair ->
            newCard.scheduleStack.addView(dataPair)

        }

        stack.addView(newCard)
    }

    private fun calculateDataPairs(medication: Medications):MutableList<DataPair> {
        var dataPairs = mutableListOf<DataPair>()

        var adherenceScore = DataPair(this)
        adherenceScore.key.text = "Adherence Score"
        adherenceScore.value.text = calculateAdherenceScore(medication)
        adherenceScore.drawableValue.visibility = View.GONE
        dataPairs.add(adherenceScore)

        var recentMood = DataPair(this)
        recentMood.key.text = "Recent Mood"
        recentMood.value.text = ""
        recentMood.drawableValue.setImageResource(calculateRecentMoodScore(medication))
        dataPairs.add(recentMood)

        return dataPairs
    }

    private fun toggleCollapse(stack: LinearLayout, button: ImageButton, medication: Medications) {
        var previouslyCollapsed = (stack.visibility == View.GONE)
        if (previouslyCollapsed) {
            button.setImageResource(R.drawable.ic_circle_chevron_up_from_down)
            (button.drawable as AnimatedVectorDrawable).start()
            stack.visibility = View.VISIBLE

        } else {
            button.setImageResource(R.drawable.ic_circle_chevron_down_from_up)
            (button.drawable as AnimatedVectorDrawable).start()
            stack.visibility = View.GONE
        }

        with (prefs.edit()) {
            putBoolean(getString(R.string.schedule_preview_collapsed_prefix) + medication.uid, !previouslyCollapsed)
            commit()
        }
    }

    private fun calculateAdherenceScore(medication: Medications):String {
        var timeCounts = StatsHelper.averageLogsAcrossSchedules(medication,realm,"Days")

        timeCounts = timeCounts.filter{it.time > DateHelper.addUnitToDate(DateHelper.today(),-30,Calendar.DATE) && it.time < DateHelper.today()}

        var sum = 0f
        timeCounts.forEach{
            sum += it.offset
        }

        val avg = sum/timeCounts.count()

        return StatsHelper.getGradeStringFromTimeDifference(avg / 1000 / 60 )
    }

    private fun calculateRecentMoodScore(medication: Medications):Int {
        val allMoodLogs = DatabaseHelper.readAllData(MoodLogs::class.java) as RealmResults<out MoodLogs>

        val relevantMoodLogs = allMoodLogs.filter{ DatabaseHelper.moodLogIsRelatedToMedication(it,medication) }

        Log.i("test",medication.name + " - " + relevantMoodLogs.toString())

        var sum = 0f
        var count = 0f
        relevantMoodLogs.forEach{
            if (it.rating != null) {
                sum += it.rating!!
                count += 1f
            }
        }

        var avg = sum/count

        Log.i("test",medication.name + " : " + avg + " : " + count)

        return DatabaseHelper.getDrawableMoodIconById(this,avg.roundToInt())
    }
}