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
import android.icu.text.SimpleDateFormat
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.children
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.pillpals.pillbuddies.data.model.Logs
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.helpers.DateHelper
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getColorStringByID
import io.realm.RealmResults
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs
import android.widget.TextView
import java.util.Calendar

class StatisticsFragment : Fragment() {


    private lateinit var realm: Realm
    private var repeatingColorHash = HashMap<String, Int>()
    public lateinit var legendStack: LinearLayout
    public lateinit var timeSpanFilterView: LinearLayout
    public lateinit var viewModeFilterView: LinearLayout
    public var filteredMedications = HashMap<String, Boolean>()
    public lateinit var barChart: BarChart
    public lateinit var medications: RealmResults<out Medications>
    public var medicationSets = mutableListOf<IBarDataSet>()
    var dateStringList = ArrayList<String>()
    public lateinit var timeSpanFilter: Filter
    public lateinit var viewModeFilter: Filter
    public lateinit var graphHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_statistics, container, false)
        val marker = MarkerView(this.context, R.layout.custom_marker_view)

        barChart = view.findViewById(R.id.chart) as BarChart

        realm = Realm.getDefaultInstance()

        legendStack = view!!.findViewById(R.id.legendStack)
        timeSpanFilterView = view!!.findViewById(R.id.timeSpanFilter)
        viewModeFilterView = view!!.findViewById(R.id.viewModeFilter)
        graphHeader = view!!.findViewById(R.id.graphHeader)

        timeSpanFilter = Filter(timeSpanFilterView,"Day")
        viewModeFilter = Filter(viewModeFilterView,"Timeline")

        setupFilter(timeSpanFilter)
        setupFilter(viewModeFilter)

        medications = DatabaseHelper.readAllData(Medications::class.java) as RealmResults<out Medications>

        determineRepeatingColors()
        populateLegendStack()
        renderBarChart()

        return view
    }

    private fun averageLogsAcrossSchedules(medication: Medications): List<TimeCount> {
        val schedules = medication.schedules

        var allLogs = schedules.fold(listOf<Logs>()) { acc, it -> acc.plus(it.logs) }
        allLogs = allLogs.sortedBy { it.due }

        val timeCount = allLogs.fold(mutableListOf<TimeCount>()) { acc, it ->
            val logDate = Calendar.getInstance()
            logDate.time = it.due!!
            logDate.set(Calendar.MILLISECOND, 0)
            logDate.set(Calendar.SECOND, 0)
            logDate.set(Calendar.MINUTE, 0)
            when (timeSpanFilter.selectedValue) {
                "Day" -> null // Sums in hours
                "Week" -> logDate.set(Calendar.HOUR_OF_DAY, 0) // Sums in days
                "Month" -> { // Sums in weeks
                    logDate.set(Calendar.HOUR_OF_DAY, 0)
                    logDate.set(Calendar.DATE, 0)
                }
                "Year" -> { // Sums in weeks
                    logDate.set(Calendar.HOUR_OF_DAY, 0)
                    logDate.set(Calendar.DATE, 0)
                }
            }

            val existingTimeCount = getTimeCount(logDate.time, acc)

            if(existingTimeCount != null) {
                val logsList = existingTimeCount.logs.plus(it)
                val average = logsList.fold(0f) { sum, log ->
                    val logOffset = (it.occurrence!!.time - it.due!!.time).toFloat() // Calculate y value of bar here
                    sum + logOffset
                } / logsList.count()
                acc[acc.indexOf(existingTimeCount)] = TimeCount(logDate.time, existingTimeCount.count + 1, average, logsList)
            }
            else {
                val logOffset = (it.occurrence!!.time - it.due!!.time).toFloat()
                acc.add(TimeCount(logDate.time, 1, logOffset, listOf(it)))
            }

            acc
        }

        return timeCount
    }

    private fun populateLegendStack() {
        medications.forEach {
            val legendItem = CheckBox(this.context!!)
            legendItem.text = it.name
            legendItem.setTextAppearance(R.style.TextAppearance_baseText)
            legendItem.isChecked = true
            legendItem.setOnCheckedChangeListener { view, isChecked ->
                updateFilteredMedications(view.text.toString(), isChecked)
            }
            legendItem.buttonTintList = (ColorStateList.valueOf(getMutatedColor(it)))
            legendStack.addView(legendItem)
        }
    }

    private fun determineMedicationSetData(){
        medicationSets.clear()
        medications.forEach {
            if (filteredMedications[it.name] != false) {
                // Average logs on time across schedules
                val timeCounts = averageLogsAcrossSchedules(it)
                //val schedule = it.schedules.first()!!
                //val logs = schedule.logs!!

                Log.i("test", timeCounts.toString())

                val entries = ArrayList<BarEntry>()
                dateStringList = ArrayList<String>()

                for ((index, timeCount) in timeCounts.withIndex()) {
                    val timeDifference =
                        abs(timeCount.offset) / 1000 / 60 // Minutes
                    val dateString =
                        SimpleDateFormat("dd-MM", Locale.getDefault()).format(timeCount.time)
                    dateStringList.add(dateString)
                    val currentEntry = BarEntry(index.toFloat(), timeDifference)
                    entries.add(currentEntry)
                }

                val set = BarDataSet(entries, "${it.name} schedule")
                set.setColor(getMutatedColor(it))

                medicationSets.add(set)
            }
        }
    }

    private fun renderBarChart() {
        val date = Date()
        val cal = Calendar.getInstance()
        cal.time = date

        graphHeader.text = when (timeSpanFilter.selectedValue) {
            "Day" -> cal.get(Calendar.DAY_OF_YEAR).toString()
            "Week" -> cal.get(Calendar.WEEK_OF_YEAR).toString()
            "Month" -> cal.get(Calendar.MONTH).toString()
            "Year" -> cal.get(Calendar.YEAR).toString()
            else -> cal.get(Calendar.DAY_OF_YEAR).toString()
        }

        determineMedicationSetData()
        barChart.setTouchEnabled(true)
        barChart.setPinchZoom(true)
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(dateStringList)
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

        barChart.data.setHighlightEnabled(false)
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.setDoubleTapToZoomEnabled(false)

        val groupWidth = 0.7f
        val barWidthRatio = 0.875f

        barChart.data.barWidth = (groupWidth * barWidthRatio)/medicationSets.size
        if(medicationSets.size > 1) {
            barChart.groupBars(-0.5f, 1 - groupWidth, (groupWidth * (1 - barWidthRatio))/medicationSets.size)
        }
    }

    private fun determineRepeatingColors() {
        var colorOccurrences = HashMap<Int, Int>()

        medications.forEach {
            if (colorOccurrences[it.color_id] != null) {
                repeatingColorHash[it.uid] = colorOccurrences.getOrElse(it.color_id, { 0 })
                colorOccurrences[it.color_id] = colorOccurrences.getOrElse(it.color_id, { 0 }) + 1
            } else {
                repeatingColorHash[it.uid] = 0
                colorOccurrences[it.color_id] = 1
            }
        }
    }

    private fun getMutatedColor(medication: Medications): Int {
        var newColor = Color.parseColor(getColorStringByID(medication.color_id))

        var brighten = ColorUtils.calculateLuminance(newColor) < 0.5

        for (i in 1..repeatingColorHash.getOrElse(medication.uid, { 0 })) {
            newColor = if (brighten) {
                ColorUtils.blendARGB(newColor, Color.WHITE, 0.3f)
            } else {
                ColorUtils.blendARGB(newColor, Color.BLACK, 0.3f)
            }
        }
        return newColor
    }

    private fun updateFilteredMedications(name: String, isChecked: Boolean) {
        filteredMedications[name] = isChecked
        renderBarChart()
    }

    private fun getTimeCount(time: Date, timeCountList: List<TimeCount>): TimeCount? {
        return timeCountList.filter { it.time == time }.firstOrNull()
    }

    private fun styleFilter(filter: Filter) {
        filter.view.children.forEach() {
            if(it.tag == filter.selectedValue) {
                it.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorGrey, null))
            } else {
                it.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null))
            }
        }
    }

    private fun setupFilter(filter: Filter) {
        filter.view.children.forEach {
            it.setOnClickListener(){
                filter.selectedValue = it.tag.toString()
                styleFilter(filter)
                renderBarChart()
            }
        }
        styleFilter(filter)
    }
}

data class AverageLogOffset(val offset: Float, val time: Date)
data class TimeCount(val time: Date, val count: Int, val offset: Float, val logs: List<Logs>)
data class Filter(var view: LinearLayout, var selectedValue: String)