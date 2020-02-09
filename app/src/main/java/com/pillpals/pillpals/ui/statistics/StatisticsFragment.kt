package com.pillpals.pillpals.ui.statistics

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillpals.pillpals.R
import io.realm.Realm
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.children
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.pillpals.pillpals.data.model.Logs
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.helpers.DateHelper
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import io.realm.RealmResults
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs
import com.pillpals.pillpals.data.model.Schedules
import java.util.Calendar
import android.util.Log
import android.widget.*
import com.pillpals.pillpals.helpers.StatsHelper


class StatisticsFragment : Fragment() {

    private lateinit var realm: Realm
    private var repeatingColorHash = HashMap<String, Int>()
    public lateinit var legendStack: LinearLayout
    public lateinit var timeSpanFilterView: LinearLayout
    public lateinit var viewModeFilterView: LinearLayout
    public lateinit var leftTimeButton: ImageButton
    public lateinit var rightTimeButton: ImageButton
    public var filteredMedications = HashMap<String, Boolean>()
    public lateinit var barChart: BarChart
    public lateinit var medications: List<Medications>
    public var medicationSets = mutableListOf<IBarDataSet>()
    var axisStringList = ArrayList<String>()
    public lateinit var timeSpanFilter: Filter
    public lateinit var viewModeFilter: Filter
    public lateinit var graphHeader: TextView
    public var currentDate = DateHelper.today()
    public lateinit var medicationScoresButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)
        val marker = MarkerView(this.context, R.layout.custom_marker_view)

        barChart = view.findViewById(R.id.chart) as BarChart

        realm = Realm.getDefaultInstance()

        legendStack = view.findViewById(R.id.legendStack)
        timeSpanFilterView = view.findViewById(R.id.timeSpanFilter)
        viewModeFilterView = view.findViewById(R.id.viewModeFilter)
        graphHeader = view.findViewById(R.id.graphHeader)
        leftTimeButton = view.findViewById(R.id.leftTimeButton)
        rightTimeButton = view.findViewById(R.id.rightTimeButton)
        medicationScoresButton = view.findViewById(R.id.medicationScoresButton)

        medicationScoresButton.setOnClickListener {
            val intent = Intent(context, MedicationScoresActivity::class.java)
            startActivityForResult(intent, 1)
        }

        leftTimeButton.setOnClickListener{timeButtonClick(-1)}
        rightTimeButton.setOnClickListener{timeButtonClick(1)}

        timeSpanFilter = Filter(timeSpanFilterView,"Day")
        viewModeFilter = Filter(viewModeFilterView,"Timeline")

        setupFilter(timeSpanFilter)
        setupFilter(viewModeFilter)

        val allMedications = DatabaseHelper.readAllData(Medications::class.java) as RealmResults<out Medications>
        medications = allMedications.filter{!it.deleted}

        determineRepeatingColors()
        populateLegendStack()
        renderBarChart()

        return view
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
                val timeCounts = StatsHelper.averageLogsAcrossSchedules(it, realm, timeSpanFilter.selectedValue)
                //Log.i("test", timeCounts.toString())

                timeCounts.forEach{
                    var cal = Calendar.getInstance()
                    cal.time = it.time
                    //Log.i("test",cal.get(Calendar.DAY_OF_YEAR).toString())
                    if(cal.get(Calendar.DAY_OF_YEAR) == 31) {
                        Log.i("test",it.toString())
                    }
                }
                // Get timeCounts for data in time range only
                val dataPoints = if(viewModeFilter.selectedValue=="Timeline") {getTimeCountsInRange(timeCounts)} else {averageTimeCounts(timeCounts)}

                //Log.i("test",dataPoints.toString())

                //val schedule = it.schedules.first()!!
                //val logs = schedule.logs!!

                val entries = ArrayList<BarEntry>()
                axisStringList = ArrayList<String>()

                for ((index, dataPoints) in dataPoints.withIndex()) {
                    var timeDifference = dataPoints.value / 1000 / 60 // Minutes
                    if (dataPoints.value == -1f) timeDifference = -1f
                    var grade = StatsHelper.getGradeFromTimeDifference(timeDifference)
                    val dateString = getAxisString(dataPoints)
                    axisStringList.add(dateString)
                    val currentEntry = BarEntry(index.toFloat(), grade)
                    entries.add(currentEntry)
                }

                val set = BarDataSet(entries, "${it.name} schedule")
                set.setColor(getMutatedColor(it))

                medicationSets.add(set)
            }
        }
    }

    private fun getAxisString(dataPoints: DataPoint):String {
        var cal = Calendar.getInstance()
        cal.time = dataPoints.time

        return when (timeSpanFilter.selectedValue) {
            "Day" -> cal.get(Calendar.HOUR_OF_DAY).toString()
            "Week" -> cal.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.SHORT,Locale.US)
            "Month" -> cal.get(Calendar.DAY_OF_MONTH).toString()
            "Year" -> cal.getDisplayName(Calendar.MONTH,Calendar.SHORT,Locale.US).first().toString()
            else -> ""
        }
    }

    private fun getTimeCountsInRange(timeCounts: List<TimeCount>):MutableList<DataPoint> {
        var cal = Calendar.getInstance()
        cal.time = currentDate

        var rangedTimeCounts = mutableListOf<DataPoint>()

        val calIterator = Calendar.getInstance()
        calIterator.set(Calendar.MILLISECOND, 0)
        calIterator.set(Calendar.SECOND, 0)
        calIterator.set(Calendar.MINUTE, 0)
        calIterator.set(Calendar.HOUR_OF_DAY, 1)

        when (timeSpanFilter.selectedValue) {
            "Day" -> {
                calIterator.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR))
                calIterator.set(Calendar.MONTH, cal.get(Calendar.MONTH))
                calIterator.set(Calendar.YEAR, cal.get(Calendar.YEAR))

                for (i in 1..24) {
                    calIterator.set(Calendar.HOUR_OF_DAY, i)

                    var existingTimeCount = timeCounts.filter {StatsHelper.equalTimeUnit(it,calIterator,listOf(Calendar.HOUR_OF_DAY,Calendar.DAY_OF_YEAR,Calendar.YEAR))}.firstOrNull()
                    rangedTimeCounts.add(DataPoint(calIterator.time, getConditionalValueFromTimeCount(existingTimeCount)))
                }
            }
            "Week" -> {
                calIterator.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR))
                calIterator.set(Calendar.MONTH, cal.get(Calendar.MONTH))
                calIterator.set(Calendar.YEAR, cal.get(Calendar.YEAR))

                for (i in 1..7) {
                    calIterator.set(Calendar.DAY_OF_WEEK, i)

                    var existingTimeCount = timeCounts.filter {StatsHelper.equalTimeUnit(it,calIterator,listOf(Calendar.DAY_OF_WEEK,Calendar.DAY_OF_YEAR,Calendar.YEAR))}.firstOrNull()
                    rangedTimeCounts.add(DataPoint(calIterator.time, getConditionalValueFromTimeCount(existingTimeCount)))
                }
            }
            "Month" -> {
                calIterator.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR))
                calIterator.set(Calendar.MONTH, cal.get(Calendar.MONTH))
                calIterator.set(Calendar.DAY_OF_MONTH, 1)
                calIterator.set(Calendar.YEAR, cal.get(Calendar.YEAR))

                for (i in 1..cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {

                    var existingTimeCount = timeCounts.filter {StatsHelper.equalTimeUnit(it,calIterator,listOf(Calendar.DAY_OF_MONTH,Calendar.DAY_OF_YEAR,Calendar.YEAR))}.firstOrNull()
                    rangedTimeCounts.add(DataPoint(calIterator.time, getConditionalValueFromTimeCount(existingTimeCount)))

                    calIterator.time = DateHelper.addUnitToDate(calIterator.time,1,Calendar.DATE)
                }
            }
            "Year" -> {
                calIterator.set(Calendar.DAY_OF_YEAR, 1)
                calIterator.set(Calendar.MONTH, 0)
                calIterator.set(Calendar.YEAR, cal.get(Calendar.YEAR))

                for (i in 0..11) {

                    var existingTimeCount = timeCounts.filter {StatsHelper.equalTimeUnit(it,calIterator,listOf(Calendar.MONTH,Calendar.YEAR))}.firstOrNull()
                    rangedTimeCounts.add(DataPoint(calIterator.time, getConditionalValueFromTimeCount(existingTimeCount)))

                    calIterator.time = DateHelper.addUnitToDate(calIterator.time,1,Calendar.MONTH)
                }
            }
        }
        return rangedTimeCounts
    }

    private fun getConditionalValueFromTimeCount(timeCount: TimeCount?):Float {
        return timeCount?.offset ?: -1f
    }
    private fun averageTimeCounts(timeCounts: List<TimeCount>):MutableList<DataPoint> {

        var averagedData = mutableListOf<DataPoint>()

        val calIterator = Calendar.getInstance()
        calIterator.set(Calendar.MILLISECOND, 0)
        calIterator.set(Calendar.SECOND, 0)
        calIterator.set(Calendar.MINUTE, 0)
        calIterator.set(Calendar.HOUR_OF_DAY, 1)
        calIterator.set(Calendar.DAY_OF_YEAR, 1)
        calIterator.set(Calendar.MONTH, 0)
        calIterator.set(Calendar.YEAR, 2020)

        when (timeSpanFilter.selectedValue) {
            "Day" -> {
                for (i in 1..24) {
                    calIterator.set(Calendar.HOUR_OF_DAY, i)

                    var relevantTimeCounts = timeCounts.filter {StatsHelper.equalTimeUnit(it,calIterator,listOf(Calendar.HOUR_OF_DAY))}
                    averagedData.add(DataPoint(calIterator.time,averageRelevantTimeCounts(relevantTimeCounts)))
                }
            }
            "Week" -> {
                for (i in 1..7) {
                    calIterator.set(Calendar.DAY_OF_WEEK, i)

                    var relevantTimeCounts = timeCounts.filter {StatsHelper.equalTimeUnit(it,calIterator,listOf(Calendar.DAY_OF_WEEK))}
                    averagedData.add(DataPoint(calIterator.time,averageRelevantTimeCounts(relevantTimeCounts)))

                }
            }
            "Month" -> {
                for (i in 1..31) {

                    var relevantTimeCounts = timeCounts.filter {StatsHelper.equalTimeUnit(it,calIterator,listOf(Calendar.DAY_OF_MONTH))}
                    averagedData.add(DataPoint(calIterator.time,averageRelevantTimeCounts(relevantTimeCounts)))

                    calIterator.time = DateHelper.addUnitToDate(calIterator.time,1,Calendar.DATE)
                }
            }
            "Year" -> {
                for (i in 0..11) {
                    var relevantTimeCounts = timeCounts.filter {StatsHelper.equalTimeUnit(it,calIterator,listOf(Calendar.MONTH))}
                    averagedData.add(DataPoint(calIterator.time,averageRelevantTimeCounts(relevantTimeCounts)))

                    calIterator.time = DateHelper.addUnitToDate(calIterator.time,1,Calendar.MONTH)
                }
            }
        }

        return averagedData
    }

    private fun averageRelevantTimeCounts(relevantTimeCounts: List<TimeCount>):Float {
        if(relevantTimeCounts.isNotEmpty()) {
            var sum = 0f
            for (i in relevantTimeCounts) {
                sum += i.offset
            }
            return sum/relevantTimeCounts.size
        }
        else {
            return -1f
        }
    }

    private fun renderBarChart() {
        val gradeStringList = listOf("","F","D","C","B","B+","A","A+")
        var cal = Calendar.getInstance()
        cal.time = currentDate

        setChartHeader()

        determineMedicationSetData()
        barChart.setTouchEnabled(true)
        barChart.setPinchZoom(true)
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(axisStringList)
        barChart.setDrawMarkers(true)
        //barChart.marker = marker
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.animateY(400)
        barChart.xAxis.isGranularityEnabled = true
        barChart.xAxis.granularity = 1.0f
        //barChart.xAxis.labelCount = set.entryCount
        barChart.xAxis.textSize = 16f
        barChart.xAxis.labelCount = when (timeSpanFilter.selectedValue) {
            "Day" -> 8
            "Week" -> 7
            "Month" -> if(viewModeFilter.selectedValue == "Timeline") {
                cal.getActualMaximum(Calendar.DAY_OF_MONTH)/4
            } else {
                8
            }
            "Year" -> 12
            else -> 24
        }

        barChart.axisLeft.textSize = 16f
        barChart.axisLeft.axisMinimum = 0.5f
        barChart.axisLeft.axisMaximum = 7.5f
        barChart.axisLeft.valueFormatter = IndexAxisValueFormatter(gradeStringList)
        barChart.axisLeft.setDrawLabels(false)

        barChart.description.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false

        barChart.data = BarData(medicationSets)
        barChart.data.setValueTextSize(0f)

        barChart.data.setHighlightEnabled(false)
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.setDoubleTapToZoomEnabled(false)
        barChart.setDrawBorders(true)

        barChart.extraBottomOffset = 10f
        barChart.extraLeftOffset = 0f
        barChart.extraRightOffset = 0f


        val groupWidth = 0.7f
        val barWidthRatio = 0.875f

        barChart.data.barWidth = (groupWidth * barWidthRatio)/medicationSets.size
        if(medicationSets.size > 1) {
            barChart.groupBars(-0.5f, 1 - groupWidth, (groupWidth * (1 - barWidthRatio))/medicationSets.size)
        }
    }

    private fun setChartHeader(){
        if (viewModeFilter.selectedValue == "Timeline") {
            leftTimeButton.visibility = View.VISIBLE
            rightTimeButton.visibility = View.VISIBLE

            val cal = Calendar.getInstance()
            cal.time = currentDate
            val firstDayOfWeekCal = Calendar.getInstance()
            firstDayOfWeekCal.time = cal.time
            firstDayOfWeekCal.set(Calendar.DAY_OF_WEEK, firstDayOfWeekCal.firstDayOfWeek)
            val lastDayOfWeekCal = Calendar.getInstance()
            lastDayOfWeekCal.time = firstDayOfWeekCal.time
            lastDayOfWeekCal.time =
                DateHelper.addUnitToDate(lastDayOfWeekCal.time, 6, Calendar.DATE)
            graphHeader.text = when (timeSpanFilter.selectedValue) {
                "Day" -> cal.getDisplayName(
                    Calendar.MONTH,
                    Calendar.SHORT,
                    Locale.US
                ) + " " + cal.get(Calendar.DAY_OF_MONTH).toString() + ", " + cal.get(Calendar.YEAR).toString()
                "Week" -> firstDayOfWeekCal.getDisplayName(
                    Calendar.MONTH,
                    Calendar.SHORT,
                    Locale.US
                ) + " " + firstDayOfWeekCal.get(Calendar.DAY_OF_MONTH).toString() + " - " + lastDayOfWeekCal.getDisplayName(
                    Calendar.MONTH,
                    Calendar.SHORT,
                    Locale.US
                ) + " " + lastDayOfWeekCal.get(Calendar.DAY_OF_MONTH).toString()
                "Month" -> cal.getDisplayName(
                    Calendar.MONTH,
                    Calendar.LONG,
                    Locale.US
                ) + " " + cal.get(Calendar.YEAR).toString()
                "Year" -> cal.get(Calendar.YEAR).toString()
                else -> ""
            }
        } else {
            leftTimeButton.visibility = View.GONE
            rightTimeButton.visibility = View.GONE

            graphHeader.text = "Average " + timeSpanFilter.selectedValue
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
                resetCurrentDate()
                renderBarChart()
            }
        }
        styleFilter(filter)
    }

    private fun timeButtonClick(direction: Int){
        currentDate = DateHelper.addUnitToDate(currentDate,direction,when(timeSpanFilter.selectedValue){
            "Day"->Calendar.DATE
            "Week"->Calendar.WEEK_OF_YEAR
            "Month"->Calendar.MONTH
            "Year"->Calendar.YEAR
            else->Calendar.DATE
        })

        renderBarChart()
    }

    private fun resetCurrentDate(){
        currentDate = Date()
    }
}

data class TimeCount(val time: Date, val count: Int, val offset: Float, val logs: List<DataLogs>)
data class DataPoint(val time: Date, val value: Float)
data class Filter(var view: LinearLayout, var selectedValue: String)

data class MissingLogs(var schedule: Schedules, var due: Date)
data class DataLogs(var occurrence: Date, var due: Date)
