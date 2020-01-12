package com.pillpals.pillbuddies.ui.statistics

import android.content.res.ColorStateList
import android.util.Log
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillpals.pillbuddies.R
import io.realm.Realm
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import android.graphics.Color.DKGRAY
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.utils.Utils.getSDKInt
import android.graphics.DashPathEffect
import android.icu.text.SimpleDateFormat
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.core.graphics.ColorUtils
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.pillpals.pillbuddies.data.model.Logs
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getColorStringByID
import io.realm.RealmResults
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs

class StatisticsFragment : Fragment() {


    private lateinit var realm: Realm
    private var repeatingColorHash = HashMap<String, Int>()
    public lateinit var legendStack: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_statistics, container, false)
        val marker = MarkerView(this.context, R.layout.custom_marker_view)

        realm = Realm.getDefaultInstance()

        legendStack = view!!.findViewById(R.id.legendStack)

        val medications = DatabaseHelper.readAllData(Medications::class.java) as RealmResults<out Medications>

        determineRepeatingColors(medications)
        populateLegendStack(medications)
        var medicationSets = createMedicationSetData(medications)

        Log.d("HEY",repeatingColorHash.toString())

        val barChart = view.findViewById(R.id.chart) as BarChart
        barChart.setTouchEnabled(true)
        barChart.setPinchZoom(true)
        //barChart.xAxis.valueFormatter = IndexAxisValueFormatter(dateStringList)
        barChart.setDrawMarkers(true)
        //barChart.marker = marker
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.animateY(400)
        barChart.xAxis.isGranularityEnabled = true
        barChart.xAxis.granularity = 1.0f
        //barChart.xAxis.labelCount = set.entryCount
        barChart.xAxis.textSize = 16f
        barChart.axisLeft.textSize = 16f
        barChart.axisLeft.axisMinimum = 0f

        barChart.description.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false

        barChart.data = BarData(medicationSets)
        barChart.data.setValueTextSize(0f)
        barChart.data.barWidth = 0.15f

        barChart.data.setHighlightEnabled(false)
        barChart.groupBars(0f,0.3f,0.02f)
        barChart.invalidate()
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.setDoubleTapToZoomEnabled(false)
        return view
    }

    private fun populateLegendStack(medications: RealmResults<out Medications>) {
        medications.forEach {
            val legendItem = CheckBox(this.context!!)
            legendItem.text = it.name
            legendItem.setTextAppearance(R.style.TextAppearance_baseText)
            legendItem.buttonTintList = (ColorStateList.valueOf(getMutatedColor(it)))
            legendStack.addView(legendItem)
        }
    }

    private fun createMedicationSetData(medications: RealmResults<out Medications>):MutableList<IBarDataSet> {
        var medicationSets = mutableListOf<IBarDataSet>()
        medications.forEach {
            val schedule = it.schedules.first()!!
            val logs = schedule.logs!!

            val entries = ArrayList<BarEntry>()
            val dateStringList = ArrayList<String>()

            for ((index, log) in logs.withIndex()) {
                val timeDifference = abs(log.occurrence!!.time - log.due!!.time) / 1000 / 60 // Minutes
                val dateString = SimpleDateFormat("dd-MM", Locale.getDefault()).format(log.due!!)
                dateStringList.add(dateString)
                val currentEntry = BarEntry(index.toFloat(), timeDifference.toFloat())
                entries.add(currentEntry)
            }

            val set = BarDataSet(entries, "${it.name} schedule")
            set.setColor(getMutatedColor(it))

            medicationSets.add(set)
        }
        return medicationSets
    }

    private fun determineRepeatingColors(medications: RealmResults<out Medications>) {
        var colorOccurrences = HashMap<Int,Int>()

        medications.forEach{
            if (colorOccurrences[it.color_id] != null) {
                repeatingColorHash[it.uid] = colorOccurrences.getOrElse(it.color_id,{0})
                colorOccurrences[it.color_id] = colorOccurrences.getOrElse(it.color_id,{0}) + 1
            } else {
                repeatingColorHash[it.uid] = 0
                colorOccurrences[it.color_id] = 1
            }
        }
    }

    private fun getMutatedColor(medication: Medications):Int {
        var newColor = Color.parseColor(getColorStringByID(medication.color_id))

        var brighten = ColorUtils.calculateLuminance(newColor) < 0.5

        for (i in 1..repeatingColorHash.getOrElse(medication.uid,{0})) {
            newColor = if (brighten) {
                ColorUtils.blendARGB(newColor, Color.WHITE, 0.3f)
            } else {
                ColorUtils.blendARGB(newColor, Color.BLACK, 0.3f)
            }
        }
        return newColor
    }
}