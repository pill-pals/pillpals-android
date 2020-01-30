package com.pillpals.pillbuddies.ui.search

import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillpals.pillbuddies.R
import android.os.*
import android.view.View.GONE
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginEnd
import androidx.transition.Visibility
import com.google.gson.Gson
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.helpers.DatabaseHelper
import com.pillpals.pillbuddies.helpers.SearchSuggestionCursor
import com.pillpals.pillbuddies.helpers.margin
import com.pillpals.pillbuddies.ui.DrugCard
import com.pillpals.pillbuddies.ui.EditMedicationIcon
import com.pillpals.pillbuddies.ui.medications.medication_info.MedicationInfoActivity
import io.realm.RealmResults
import kotlinx.android.synthetic.main.drug_card.view.*
import okhttp3.*
import okio.IOException
import java.util.*

class SearchFragment : Fragment() {
    public lateinit var medications: RealmResults<out Medications>
    public lateinit var searchView: android.widget.SearchView
    public lateinit var rootSearchView: ConstraintLayout
    public lateinit var searchResults: LinearLayout
    public lateinit var apiWarning: TextView
    public var handler: Handler = Handler(Looper.getMainLooper())
    public var runnable: Runnable? = null
    public var suggestions: MutableList<String> = mutableListOf()
    public var outerContext: SearchFragment = this
    public var updateSuggestionsFlag: Boolean = false
    public var clearQueriesFlag: Boolean = false
    public var showResultsFlag: Boolean = false
    public var refreshCardsFlag: Boolean = false
    public var apiDown: Boolean = false
    public var drugCards: MutableList<DrugCard?> = mutableListOf()
    public var upcomingDrugCards: MutableList<DrugCard?> = mutableListOf()
    public lateinit var loadingAnimation: RotateAnimation
    public var lastQuery: String? = null
    public var searchingUpcomingDrugs = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.searchView)
        rootSearchView = view.findViewById(R.id.rootSearchView)
        searchResults = view.findViewById(R.id.searchResults)
        apiWarning = view.findViewById(R.id.warningText)

        apiWarning.visibility = GONE

        outerContext = this
        handler = Handler(Looper.getMainLooper())

        loadingAnimation = RotateAnimation(0f, 360f, 55f, 55f)
        loadingAnimation.interpolator = LinearInterpolator()
        loadingAnimation.repeatCount = Animation.INFINITE
        loadingAnimation.duration = 700

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
                lastQuery = null

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
                                lastQuery = query
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
                        if(updateSuggestionsFlag) updateSuggestions()
                        if(clearQueriesFlag) clearQueries()
                        if(showResultsFlag) showResults()
                        if(refreshCardsFlag) refreshCards()
                        if(apiDown) showApiWarning()

                        updateSuggestionsFlag = false
                        clearQueriesFlag = false
                        showResultsFlag = false
                        refreshCardsFlag = false
                        apiDown = false
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
        val cursor = MatrixCursor(arrayOf("_id", "suggestion"))
        suggestions.forEachIndexed {index, suggestion ->
            // Create dropdown suggestions
            cursor.newRow()
                .add("_id", index + 1)
                .add("suggestion", suggestion)
        }

        searchView.suggestionsAdapter.changeCursor(cursor)
    }

    private fun clearQueries() {
        searchResults.removeAllViews()
    }

    private fun multipleDrugsExistsWithName(drug: DrugProduct): Boolean {
        searchingUpcomingDrugs = true
        val res = upcomingDrugCards.filter {
            it?.nameText?.text.toString() == drug.brand_name
        }.count() > 1
        searchingUpcomingDrugs = false
        return res
    }

    private fun showResults() {
        searchResults.removeAllViews()
        drugCards = mutableListOf()
        while(searchingUpcomingDrugs) {
            Thread.sleep(50)
        }
        upcomingDrugCards = mutableListOf()

        val dispatcher = Dispatcher()
        dispatcher.maxRequests = suggestions.count() * 3

        val client = OkHttpClient
            .Builder()
            .dispatcher(dispatcher)
            .build()

        val drugsToSearch = suggestions

        if(lastQuery != null && !suggestions.contains(lastQuery!!)) {
            drugsToSearch.add(0, lastQuery!!)
        }

        drugsToSearch.forEachIndexed {index, suggestion ->
            drugCards.add(addDrugCard(suggestion))
            val re = Regex("[^A-Za-z ]")
            val url = "https://health-products.canada.ca/api/drug/drugproduct/?brandname=${re.replace(suggestion, "")}"

            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    if(index < drugCards.count()) {
                        drugCards[index] = null
                    }
                    apiDown = true
                    refreshCardsFlag = true
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val jsonString = response.body!!.string()
                        val gson = Gson()
                        val drugProducts = gson.fromJson(jsonString, Array<DrugProduct>::class.java).toList()

                        // First for now
                        val firstDrugProduct = drugProducts.firstOrNull()

                        if (firstDrugProduct == null) {
                            drugCards[index] = null
                            refreshCardsFlag = true
                            return
                        }

                        // gather info and set to card
                        var newCard = DrugCard(outerContext.context!!)
                        newCard.nameText.text = firstDrugProduct.brand_name
                        newCard.button.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        newCard.overflowMenu.visibility = View.INVISIBLE
                        newCard.button.visibility = View.VISIBLE
                        newCard.button.text = "View"
                        newCard.button.margin(right = 0F)

                        newCard.drugCode = firstDrugProduct.drug_code
                        // Add button action here, using drug code ^
                        newCard.button.setOnClickListener {
                            val infoIntent = Intent(outerContext.context, MedicationInfoActivity::class.java)
                            infoIntent.putExtra("drug-code", firstDrugProduct.drug_code)
                            startActivityForResult(infoIntent, 1)
                        }


                        while(searchingUpcomingDrugs) {
                            Thread.sleep(50)
                        }
                        upcomingDrugCards.add(newCard)

                        if(multipleDrugsExistsWithName(firstDrugProduct)) {
                            drugCards[index] = null
                            refreshCardsFlag = true
                            return
                        }

                        val url = "https://health-products.canada.ca/api/drug/activeingredient/?id=${firstDrugProduct.drug_code}"

                        val request = Request.Builder().url(url).build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace()
                                if(index < drugCards.count()) {
                                    drugCards[index] = null
                                }
                                apiDown = true
                                refreshCardsFlag = true
                            }

                            override fun onResponse(call: Call, response: Response) {
                                response.use {
                                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                                    val jsonString = response.body!!.string()
                                    val gson = Gson()
                                    val activeIngredients = gson.fromJson(jsonString, Array<ActiveIngredient>::class.java).toList()

                                    val ingredientNameList = activeIngredients.fold(listOf<String>()) { acc, it ->
                                        acc.plus(it.ingredient_name)
                                    }

                                    newCard.timeText.text = ingredientNameList.joinToString()

                                    val url = "https://health-products.canada.ca/api/drug/route/?id=${firstDrugProduct.drug_code}"

                                    val request = Request.Builder().url(url).build()

                                    client.newCall(request).enqueue(object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            e.printStackTrace()
                                            if(index < drugCards.count()) {
                                                drugCards[index] = null
                                            }
                                            apiDown = true
                                            refreshCardsFlag = true
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            response.use {
                                                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                                                val jsonString = response.body!!.string()
                                                val gson = Gson()
                                                val administrationRoutes = gson.fromJson(jsonString, Array<AdministrationRoute>::class.java).toList()

                                                val firstRoute = administrationRoutes.firstOrNull()
                                                if(firstRoute != null) {
                                                    newCard.icon.setImageResource(administrationRouteToIcon(firstRoute.route_of_administration_name))

                                                    var colorString =
                                                        DatabaseHelper.getRandomColorString()
                                                    while(colorString == "#000000") { // Let's not let black be selected randomly
                                                        colorString = DatabaseHelper.getRandomColorString()
                                                    }
                                                    newCard.iconBackground.setCardBackgroundColor(Color.parseColor(colorString))

                                                    val administrationRoutesList = administrationRoutes.fold(listOf<String>()) { acc, it ->
                                                        acc.plus(it.route_of_administration_name)
                                                    }

                                                    newCard.lateText.text = administrationRoutesList.joinToString()
                                                }

                                                drugCards[index] = newCard

                                                refreshCardsFlag = true
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

    private fun addDrugCard(name: String): DrugCard {
        var newCard = DrugCard(this.context!!)

        newCard.nameText.text = name
        newCard.timeText.text = "..."
        newCard.icon.setImageResource(R.drawable.loader)
        newCard.icon.startAnimation(loadingAnimation)

        newCard.button.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

        newCard.overflowMenu.visibility = View.INVISIBLE

        searchResults.addView(newCard)

        return newCard
    }

    private fun refreshCards() {
        searchResults.removeAllViews()
        drugCards.forEach {
            if(it == null) return@forEach
            if(it.drugCode == 0) {
                it.icon.startAnimation(loadingAnimation)
            }
            searchResults.addView(it)
        }
    }

    private fun showApiWarning() {
        apiWarning.visibility = View.VISIBLE
    }

    private fun administrationRouteToIcon(route: String): Int {
        return when(true) {
            route.startsWith("Intra") -> R.drawable.ic_syringe
            route.startsWith("Oral") -> R.drawable.ic_pill_v5
            route.startsWith("Rectal") -> R.drawable.ic_tablet
            route.startsWith("Inhalation") -> R.drawable.ic_inhaler
            else -> R.drawable.ic_dropper
        }
    }
}

data class Autocomplete(val query: String, val suggestions: MutableList<String>)
data class DrugProduct(
    val drug_code: Int,
    val class_name: String,
    val drug_identification_number: String,
    val brand_name: String,
    val descriptor: String,
    val number_of_ais: String,
    val ai_group_no: String,
    val company_name: String
)
data class ActiveIngredient(
    val dosage_unit: String,
    val dosage_value: String,
    val drug_code: Int,
    val ingredient_name: String,
    val strength: String,
    val strength_unit: String
)
data class AdministrationRoute(
    val drug_code: Int,
    val route_of_administration_code: Int,
    val route_of_administration_name: String
)
