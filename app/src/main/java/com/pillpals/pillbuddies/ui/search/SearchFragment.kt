package com.pillpals.pillbuddies.ui.search

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Color
import android.util.Log

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
import android.os.*
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import com.pillpals.pillbuddies.data.model.Logs
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.data.model.Schedules
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillbuddies.helpers.SearchSuggestionCursor
import com.pillpals.pillbuddies.ui.AddDrugActivity
import com.pillpals.pillbuddies.ui.DrugCard
import com.pillpals.pillbuddies.ui.MainActivity
import io.realm.RealmResults
import okhttp3.*
import okio.IOException
import java.io.StringReader
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs

class SearchFragment : Fragment() {
    public lateinit var medications: RealmResults<out Medications>
    public lateinit var searchView: android.widget.SearchView
    public lateinit var rootSearchView: ConstraintLayout
    public lateinit var searchResults: LinearLayout
    public var handler: Handler = Handler(Looper.getMainLooper())
    public var runnable: Runnable? = null
    public var suggestions: List<String> = listOf()
    public var outerContext: SearchFragment = this
    public var updateSuggestionsFlag: Boolean = false
    public var clearQueriesFlag: Boolean = false
    public var showResultsFlag: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.searchView)
        rootSearchView = view.findViewById(R.id.rootSearchView)
        searchResults = view.findViewById(R.id.searchResults)
        outerContext = this
        handler = Handler(Looper.getMainLooper())

        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 1

        val netInterceptor = Interceptor { chain ->
            chain.proceed(chain.request())
        }

        val client = OkHttpClient
            .Builder()
            .addNetworkInterceptor(netInterceptor)
            .dispatcher(dispatcher)
            .build()

        searchView.setOnQueryTextListener(object: android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String): Boolean {
                // Remove all previous callbacks.
                if(runnable != null) handler.removeCallbacks(runnable!!)

                runnable = object: Runnable {
                    override fun run() {
                        if(query.isNotEmpty()) {
                            val url = "http://mapi-us.iterar.co/api/autocomplete?query=${query}"

                            val request = Request.Builder().url(url).build()

                            client.newCall(request).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    e.printStackTrace()
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    response.use {
                                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                                        val jsonString = response.body!!.string()
                                        val gson = Gson()
                                        val autocomplete = gson.fromJson(jsonString, Autocomplete::class.java)

                                        suggestions = autocomplete.suggestions
                                        updateSuggestionsFlag = true
                                    }
                                }
                            })
                        }
                        else {
                            clearQueriesFlag = true
                        }
                    }
                }

                handler.postDelayed(runnable!!, 200)

                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                rootSearchView.requestFocus()

                if(query.isNotEmpty()) {
                    val url = "http://mapi-us.iterar.co/api/autocomplete?query=${query}"

                    val request = Request.Builder().url(url).build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use {
                                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                                val jsonString = response.body!!.string()
                                val gson = Gson()
                                val autocomplete = gson.fromJson(jsonString, Autocomplete::class.java)

                                suggestions = autocomplete.suggestions
                                showResultsFlag = true
                            }
                        }
                    })
                }
                else {
                    clearQueriesFlag = true
                }

                return false
            }
        })

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val handler = Handler()
        val timer = Timer()
        val doAsynchronousTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    try {
                        when(true) {
                            updateSuggestionsFlag -> updateSuggestions()
                            clearQueriesFlag -> clearQueries()
                            showResultsFlag -> showResults()
                        }
                        updateSuggestionsFlag = false
                        clearQueriesFlag = false
                        showResultsFlag = false
                    } catch (e: Exception) { }
                }
            }
        }
        timer.schedule(doAsynchronousTask, 0, 200)

        searchView.setOnSuggestionListener(object: android.widget.SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                val selection = cursor.getString(cursor.getColumnIndex("suggestion"))
                searchView.setQuery(selection, true)

                return true
            }
        })

        val cursor = MatrixCursor(arrayOf("_id", "suggestion"))
        searchView.suggestionsAdapter = SearchSuggestionCursor(this.context!!, cursor, searchView)

        return view
    }

    private fun updateSuggestions() {
        //searchResults.removeAllViews()
        val cursor = MatrixCursor(arrayOf("_id", "suggestion"))
        suggestions.forEachIndexed {index, suggestion ->
            // Create dropdown suggestions
            cursor.newRow()
                .add("_id", index + 1)
                .add("suggestion", suggestion)

            //addDrugCard(suggestion)
        }

        searchView.suggestionsAdapter.changeCursor(cursor)
    }

    private fun clearQueries() {
        searchResults.removeAllViews()
    }

    private fun showResults() {
        searchResults.removeAllViews()
        suggestions.forEach {suggestion ->
            addDrugCard(suggestion)
        }
    }

    private fun addDrugCard(name: String) {
        var newCard = DrugCard(this.context!!)

        newCard.nameText.text = name
        //newCard.altText.text = medication.dosage
        //newCard.iconBackground.setCardBackgroundColor(Color.parseColor(getColorStringByID(medication.color_id)))
//        newCard.icon.setImageResource(
//            DatabaseHelper.getDrawableIconById(
//                this.context!!,
//                medication.icon_id
//            )
//        )

//        newCard.button.setOnClickListener {
//            val intent = Intent(context, AddDrugActivity::class.java)
//            intent.putExtra("medication-uid", medication.uid)
//            startActivityForResult(intent, 1)
//        }

        newCard.button.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

        newCard.overflowMenu.visibility = View.INVISIBLE

        searchResults.addView(newCard)
    }
}

data class Autocomplete(val query: String, val suggestions: List<String>)
