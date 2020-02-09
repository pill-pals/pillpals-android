package com.pillpals.pillpals.ui.statistics

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

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

        var timeCounts = StatsHelper.averageLogsAcrossSchedules(medication,realm,"Days")

        val adherenceScoreValue = calculateAdherenceScore(timeCounts)
        var adherenceScore = DataPair(this)
        adherenceScore.key.text = "Overall Score"
        adherenceScore.value.text = StatsHelper.getGradeStringFromTimeDifference(adherenceScoreValue)
        adherenceScore.drawableValue.visibility = View.GONE

        val allMoodLogs = DatabaseHelper.readAllData(MoodLogs::class.java) as RealmResults<out MoodLogs>
        var relevantMoodLogs = allMoodLogs.filter{ DatabaseHelper.moodLogIsRelatedToMedication(it,medication) }
        val avgMood = calculateRecentMoodScore(relevantMoodLogs)

        var avgMoodScore = DataPair(this)
        avgMoodScore.key.text = "Overall Mood"
        if (avgMood == -1f) {
            avgMoodScore.value.text = "No Logs"
            avgMoodScore.drawableValue.visibility = View.GONE
        } else {
            avgMoodScore.value.text = ""
            avgMoodScore.drawableValue.setImageResource(DatabaseHelper.getDrawableMoodIconById(this,avgMood.roundToInt()))
        }

        timeCounts = timeCounts.filter{it.time > DateHelper.addUnitToDate(DateHelper.today(),-3,Calendar.DATE) && it.time <= DateHelper.today()}

        val recentAdherenceScoreValue = calculateAdherenceScore(timeCounts)
        var recentAdherenceScore = DataPair(this)
        recentAdherenceScore.key.text = "Recent Adherence"
        recentAdherenceScore.value.text = StatsHelper.getGradeStringFromTimeDifference(recentAdherenceScoreValue)
        recentAdherenceScore.drawableValue.visibility = View.GONE

        relevantMoodLogs = relevantMoodLogs.filter{it.date!! > DateHelper.addUnitToDate(DateHelper.today(),-3,Calendar.DATE) && it.date!! <= DateHelper.today()}

        val recentAvgMood = calculateRecentMoodScore(relevantMoodLogs)

        var recentAvgMoodScore = DataPair(this)
        recentAvgMoodScore.key.text = "Recent Mood"
        if (recentAvgMood == -1f) {
            recentAvgMoodScore.value.text = "No Logs"
            recentAvgMoodScore.drawableValue.visibility = View.GONE
        } else {
            recentAvgMoodScore.value.text = ""
            recentAvgMoodScore.drawableValue.setImageResource(DatabaseHelper.getDrawableMoodIconById(this,recentAvgMood.roundToInt()))
        }



        dataPairs.add(recentAdherenceScore)
        dataPairs.add(recentAvgMoodScore)
        dataPairs.add(adherenceScore)
        dataPairs.add(avgMoodScore)

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

    private fun calculateAdherenceScore(timeCounts: List<TimeCount>):Float {
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

    private fun calculateRecentMoodScore(relevantMoodLogs: List<MoodLogs>):Float {
        //Log.i("test",medication.name + " - " + relevantMoodLogs.toString())
        var sum = 0f
        var count = 0f
        relevantMoodLogs.forEach{
            if (it.rating != null) {
                sum += it.rating!!
                count += 1f
            }
        }

        //Log.i("test",medication.name + " : " + avg + " : " + count)

        return if (count == 0f) {
            -1f
        } else {
            sum/count
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}