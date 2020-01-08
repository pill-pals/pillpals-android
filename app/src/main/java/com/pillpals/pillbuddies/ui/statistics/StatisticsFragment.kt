package com.pillpals.pillbuddies.ui.statistics

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
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import android.graphics.Color.DKGRAY
import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.utils.Utils.getSDKInt
import android.graphics.DashPathEffect
import android.icu.text.SimpleDateFormat
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.pillpals.pillbuddies.data.model.Logs
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import io.realm.RealmResults
import java.util.*
import kotlin.collections.ArrayList

class StatisticsFragment : Fragment() {


    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_statistics, container, false)
        val marker = MarkerView(this.context, R.layout.custom_marker_view)

        realm = Realm.getDefaultInstance()

        val medications = DatabaseHelper.readAllData(Medications::class.java) as RealmResults<out Medications>

        var medicationSets = mutableListOf<ILineDataSet>()

        medications.forEach {
            val schedule = it.schedules.last()!!
            val logs = schedule.logs!!

            val entries = ArrayList<Entry>()
            val dateStringList = ArrayList<String>()

            for ((index, log) in logs.withIndex()) {
                val timeDifference = (log.occurrence!!.time - log.due!!.time) / 1000 / 60 // Minutes
                val dateString = SimpleDateFormat("dd-MM", Locale.getDefault()).format(log.due!!)
                dateStringList.add(dateString)
                val currentEntry = Entry(index.toFloat(), timeDifference.toFloat())
                entries.add(currentEntry)
            }

            val set = LineDataSet(entries, "${it.name} schedule")
            set.circleRadius = 8f
            set.lineWidth = 4f
            set.setCircleColor(Color.parseColor(it.color))
            set.setColor(Color.parseColor(it.color))

            medicationSets.add(set)
            //Log.d("TAG", medicationSets.toString())
        }


        val lineChart = view.findViewById(R.id.chart) as LineChart
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        //lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dateStringList)
        lineChart.setDrawMarkers(true)
        //lineChart.marker = marker
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.animateY(400)
        lineChart.xAxis.isGranularityEnabled = true
        lineChart.xAxis.granularity = 1.0f
        //lineChart.xAxis.labelCount = set.entryCount
        lineChart.xAxis.textSize = 16f
        lineChart.axisLeft.textSize = 16f

        lineChart.description.isEnabled = false
        lineChart.axisRight.isEnabled = false
        //lineChart.legend.isEnabled = false

        lineChart.data = LineData(medicationSets)
        lineChart.data.setValueTextSize(12f)
        return view
    }
}