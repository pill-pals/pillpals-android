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
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.data.model.Quizzes

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
    private lateinit var questions: List<Questions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        realm = Realm.getDefaultInstance()

        prefs = getPreferences(Context.MODE_PRIVATE)

        setContentView(R.layout.activity_medication_scores)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        questions = realm.where(Questions::class.java).findAll() as List<Questions>

        stack = this!!.findViewById(R.id.stack)
        stack.layoutTransition.enableTransitionType(LayoutTransition.CHANGING) //Makes collapsing smooth

        for (drug in realm.where(Medications::class.java).findAll()) {
            if (drug.deleted) {
                continue
            }

            addDrugCard(drug)
        }
        Log.i("text",stack.toString() )

        DatabaseHelper.logVisit("MedicationScoresActivity")
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

        val adherenceScoreValue = StatsHelper.calculateAdherenceScore(timeCounts)
        var adherenceScorePair = DataPair(this)
        adherenceScorePair.key.text = "Overall Adherence"
        adherenceScorePair.value.text = StatsHelper.getGradeStringFromTimeDifference(adherenceScoreValue)
        adherenceScorePair.drawableValue.visibility = View.GONE

        val allMoodLogs = DatabaseHelper.readAllData(MoodLogs::class.java) as RealmResults<out MoodLogs>
        var relevantMoodLogs = allMoodLogs.filter{ DatabaseHelper.moodLogIsRelatedToMedication(it,medication) }
        val avgMood = StatsHelper.calculateMoodScore(relevantMoodLogs)

        var avgMoodScorePair = DataPair(this)
        avgMoodScorePair.key.text = "Overall Mood"
        if (avgMood == -1f) {
            avgMoodScorePair.value.text = "No Logs"
            avgMoodScorePair.drawableValue.visibility = View.GONE
        } else {
            avgMoodScorePair.value.text = ""
            avgMoodScorePair.drawableValue.setImageResource(DatabaseHelper.getDrawableMoodIconById(this,avgMood.roundToInt()))
        }

        timeCounts = timeCounts.filter{it.time > DateHelper.addUnitToDate(DateHelper.today(),-7,Calendar.DATE)}

        val recentAdherenceScoreValue = StatsHelper.calculateAdherenceScore(timeCounts)
        var recentAdherenceScorePair = DataPair(this)
        recentAdherenceScorePair.key.text = "Recent Adherence"
        recentAdherenceScorePair.value.text = StatsHelper.getGradeStringFromTimeDifference(recentAdherenceScoreValue)
        recentAdherenceScorePair.drawableValue.visibility = View.GONE

        relevantMoodLogs = relevantMoodLogs.filter{it.date!! > DateHelper.addUnitToDate(DateHelper.today(),-7,Calendar.DATE)}

        val recentAvgMood = StatsHelper.calculateMoodScore(relevantMoodLogs)

        var recentAvgMoodScorePair = DataPair(this)
        recentAvgMoodScorePair.key.text = "Recent Mood"
        if (recentAvgMood == -1f) {
            recentAvgMoodScorePair.value.text = "No Logs"
            recentAvgMoodScorePair.drawableValue.visibility = View.GONE
        } else {
            recentAvgMoodScorePair.value.text = ""
            recentAvgMoodScorePair.drawableValue.setImageResource(DatabaseHelper.getDrawableMoodIconById(this,recentAvgMood.roundToInt()))
        }

        var quizScorePair = DataPair(this)
        var quizScore = StatsHelper.calculateQuizScore(medication, questions)
        quizScorePair.key.text = "Quiz Score"
        if (quizScore == -1f) {
            quizScorePair.value.text = "No data"
            quizScorePair.drawableValue.visibility = View.GONE
        } else {
            quizScorePair.value.text = (quizScore * 100).roundToInt().toString() + "%"
            quizScorePair.drawableValue.visibility = View.GONE
        }

        dataPairs.add(recentAdherenceScorePair)
        dataPairs.add(recentAvgMoodScorePair)
        dataPairs.add(adherenceScorePair)
        dataPairs.add(avgMoodScorePair)
        dataPairs.add(quizScorePair)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}