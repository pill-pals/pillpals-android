package com.pillpals.pillpals.ui.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillpals.pillpals.R
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View.GONE
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.pillpals.pillpals.data.*
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.SearchSuggestionCursor
import com.pillpals.pillpals.helpers.margin
import com.pillpals.pillpals.ui.DrugCard
import com.pillpals.pillpals.ui.medications.medication_info.MedicationInfoActivity
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.drug_card.view.*
import okhttp3.*
import okio.IOException
import java.util.*
import java.util.concurrent.TimeUnit


// Used for linking DPDObjects to existing Medications
class SearchActivity : AppCompatActivity() {
    public lateinit var medications: RealmResults<out Medications>
    public lateinit var searchView: android.widget.SearchView
    public lateinit var rootSearchView: ConstraintLayout
    public lateinit var searchResults: LinearLayout
    public lateinit var apiWarning: TextView
    public var handler: Handler = Handler(Looper.getMainLooper())
    public var runnable: Runnable? = null
    public var suggestions: MutableList<String> = mutableListOf()
    public lateinit var outerContext: Context
    public var updateSuggestionsFlag: Boolean = false
    public var clearQueriesFlag: Boolean = false
    public var showResultsFlag: Boolean = false
    public var refreshCardsFlag: Boolean = false
    public var apiDown: Boolean = false
    public var drugCards: MutableList<DrugCard?> = mutableListOf()
    public var upcomingDrugCards: MutableList<DrugCard?> = mutableListOf()
    lateinit var searchLoading: ImageView
    public lateinit var loadingAnimation: RotateAnimation
    public var lastQuery: String? = null
    public var searchingUpcomingDrugs = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.fragment_search)

        searchView = findViewById(R.id.searchView)
        rootSearchView = findViewById(R.id.rootSearchView)
        searchResults = findViewById(R.id.searchResults)
        apiWarning = findViewById(R.id.warningText)
        searchLoading = findViewById(R.id.searchLoading)
        searchLoading.visibility = GONE

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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
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
                                    apiDown = true
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
                    showSearchLoading()
                    if(TextUtils.isDigitsOnly(query)) {
                        lastQuery = query
                        showResultsFlag = true
                    }
                    else {
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
                }
                else {
                    clearQueriesFlag = true
                }

                return false
            }
        })

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        var showResultsCounter = 0
        val handler = Handler()
        val timer = Timer()
        val doAsynchronousTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    try {
                        if(apiDown) {
                            apiDown = false
                            showApiWarning()
                        }
                        if(updateSuggestionsFlag) {
                            updateSuggestionsFlag = false
                            updateSuggestions()
                        }
                        if(clearQueriesFlag) {
                            clearQueriesFlag = false
                            clearQueries()
                        }
                        if(showResultsFlag) {
                            showResultsCounter = 0
                            showResultsFlag = false
                            showResults()
                        }
                        else {
                            showResultsCounter++
                            if(showResultsCounter > 19) {
                                hideSearchLoading()
                                showResultsCounter = 0
                            }
                        }
                        if(refreshCardsFlag) {
                            refreshCardsFlag = false
                            showResultsCounter = 0
                            refreshCards()
                        }
                    } catch (e: Exception) { }
                }
            }
        }
        timer.schedule(doAsynchronousTask, 0, 250)

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
        searchView.suggestionsAdapter = SearchSuggestionCursor(outerContext, cursor, searchView)
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

    private fun multipleDrugsExistsWithName(name: String, dosage: String): Boolean {
        searchingUpcomingDrugs = true
        val res = upcomingDrugCards.filter {
            it?.nameText?.text.toString() == name && it?.lateText?.text.toString() == dosage
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

        val client = OkHttpClient
            .Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val drugsToSearch = suggestions

        if(lastQuery != null && !suggestions.contains(lastQuery!!)) {
            drugsToSearch.add(0, lastQuery!!)
        }

        drugsToSearch.forEachIndexed {index, suggestion ->
            drugCards.add(addDrugCard(suggestion))

            var url = ""
            if(TextUtils.isDigitsOnly(suggestion)) {
                url = "https://health-products.canada.ca/api/drug/drugproduct/?din=${suggestion.padStart(8, '0')}"
            }
            else {
                val re = Regex("[^A-Za-z ]")
                url = "https://health-products.canada.ca/api/drug/drugproduct/?brandname=${re.replace(suggestion, "")}"
            }


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

                        val uniquelyNamedDrugs = drugProducts.fold(listOf<DrugProduct>()) {acc, drugProduct ->
                            if(acc.filter { it.brand_name == drugProduct.brand_name }.count() == 0) {
                                acc.plus(drugProduct)
                            }
                            else {
                                acc
                            }
                        }

                        // First for now
                        val firstDrugProduct = drugProducts.firstOrNull()

                        if (firstDrugProduct == null && index < drugCards.count()) {
                            drugCards[index] = null
                            refreshCardsFlag = true
                            return
                        }

                        drugProducts.forEachIndexed {namedIndex, drugProduct ->
                            // gather info and set to card
                            val newCard = DrugCard(outerContext)
                            newCard.nameText.text = drugProduct.brand_name
                            newCard.button.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                            newCard.overflowMenu.visibility = View.INVISIBLE
                            newCard.button.visibility = View.VISIBLE
                            newCard.button.text = "View"
                            newCard.button.margin(right = 0F)

                            newCard.drugCode = drugProduct.drug_code

                            while(searchingUpcomingDrugs) {
                                Thread.sleep(50)
                            }
                            upcomingDrugCards.add(newCard)

                            val url = "https://health-products.canada.ca/api/drug/activeingredient/?id=${drugProduct.drug_code}"

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

                                        val dosageValues = activeIngredients.fold(listOf<String>()) { acc, it ->
                                            acc.plus(it.strength)
                                        }

                                        val dosageUnits = activeIngredients.fold(listOf<String>()) { acc, it ->
                                            if(acc.contains(it.strength_unit)) acc
                                            else acc.plus(it.strength_unit)
                                        }

                                        newCard.dosageString = "${dosageValues.joinToString("/")} ${dosageUnits.joinToString("/")}"

                                        newCard.lateText.text = newCard.dosageString

                                        newCard.lateText.visibility = View.VISIBLE

                                        while(searchingUpcomingDrugs) {
                                            Thread.sleep(50)
                                        }
                                        upcomingDrugCards.add(newCard)

                                        if(multipleDrugsExistsWithName(drugProduct.brand_name, newCard.dosageString)) {
                                            drugCards[index] = null
                                            refreshCardsFlag = true
                                            return
                                        }

                                        newCard.activeIngredients = ingredientNameList

                                        newCard.timeText.text = ingredientNameList.joinToString()

                                        // Get other ID's from FDA
                                        val url = "https://api.fda.gov/drug/ndc.json?limit=100&search=brand_name:${drugProduct.brand_name.replace("( .*)".toRegex(), "")}"

                                        val request = Request.Builder().url(url).build()

                                        client.newCall(request).enqueue(object : Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                e.printStackTrace()
                                                if (index < drugCards.count()) {
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
                                                    val fdaResponse = gson.fromJson(jsonString, OpenFDANameResponse::class.java)

                                                    if(fdaResponse.error == null) {
                                                        val fdaResults = fdaResponse.results

                                                        val fdaResultWithDosage = fdaResults.filter {
                                                            if(it.active_ingredients == null) return@filter false
                                                            val totalVal = it.active_ingredients.fold(0f) {acc, it ->
                                                                acc + it.strength.replace("( .*)".toRegex(), "").toFloat()
                                                            }
                                                            it.active_ingredients.any {
                                                                it.strength.contains("${dosageValues.first()} ${dosageUnits.firstOrNull()?.toLowerCase()}")
                                                            } || dosageValues.firstOrNull()?.toFloat() == totalVal
                                                        }.firstOrNull()

                                                        val firstFdaResult = fdaResults.firstOrNull()

                                                        // SET FDA IDS
                                                        if(fdaResultWithDosage != null) {
                                                            newCard.ndcCode = fdaResultWithDosage.product_ndc
                                                            newCard.rxcui = fdaResultWithDosage.openfda.rxcui?.firstOrNull()
                                                            newCard.splSetId = fdaResultWithDosage.openfda.spl_set_id?.firstOrNull()
                                                        }
                                                        else if(firstFdaResult != null) {
                                                            newCard.ndcCode = firstFdaResult.product_ndc
                                                            newCard.rxcui = firstFdaResult.openfda.rxcui?.firstOrNull()
                                                            newCard.splSetId = firstFdaResult.openfda.spl_set_id?.firstOrNull()
                                                        }
                                                    }

                                                    val url = "https://health-products.canada.ca/api/drug/route/?id=${drugProduct.drug_code}"

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

                                                                var colorString =
                                                                    DatabaseHelper.getRandomColorString()
                                                                while(colorString == "#000000") { // Let's not let black be selected randomly
                                                                    colorString = DatabaseHelper.getRandomColorString()
                                                                }
                                                                newCard.iconBackground.setCardBackgroundColor(Color.parseColor(colorString))

                                                                var administrationRoutesList = listOf<String>()

                                                                val firstRoute = administrationRoutes.firstOrNull()
                                                                if(firstRoute != null) {
                                                                    newCard.icon.setImageResource(administrationRouteToIcon(firstRoute.route_of_administration_name))

                                                                    administrationRoutesList = administrationRoutes.fold(listOf<String>()) { acc, it ->
                                                                        acc.plus(it.route_of_administration_name)
                                                                    }

                                                                    //newCard.lateText.text = administrationRoutesList.joinToString()
                                                                }

                                                                // Add button action here, using drug code
                                                                newCard.button.setOnClickListener {
                                                                    val infoIntent = Intent(outerContext, MedicationInfoActivity::class.java)

                                                                    if(intent.hasExtra("medication-uid")) {
                                                                        // For linking DPDObject
                                                                        infoIntent.putExtra("link-medication", true)
                                                                    }
                                                                    else {
                                                                        infoIntent.putExtra("link-medication", false)
                                                                    }


                                                                    infoIntent.putExtra("drug-code", newCard.drugCode)
                                                                    infoIntent.putExtra("icon-color", colorString)
                                                                    infoIntent.putStringArrayListExtra("administration-routes", ArrayList(administrationRoutesList))
                                                                    infoIntent.putStringArrayListExtra("active-ingredients", ArrayList(newCard.activeIngredients))
                                                                    infoIntent.putExtra("dosage-string", newCard.dosageString)
                                                                    infoIntent.putExtra("name-text", newCard.nameText.text.toString())
                                                                    if(firstRoute != null) {
                                                                        infoIntent.putExtra("icon-resource", administrationRouteToIconString(firstRoute.route_of_administration_name))
                                                                    }

                                                                    infoIntent.putExtra("ndc-code", newCard.ndcCode)
                                                                    infoIntent.putExtra("rxcui", newCard.rxcui)
                                                                    infoIntent.putExtra("spl-set-id", newCard.splSetId)

                                                                    startActivityForResult(infoIntent, 1)
                                                                }

                                                                if(namedIndex > 0) {
                                                                    drugCards.add(newCard)
                                                                }
                                                                else {
                                                                    drugCards[index] = newCard
                                                                }

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
                }
            })
        }
    }

    private fun addDrugCard(name: String): DrugCard {
        var newCard = DrugCard(this)

        newCard.nameText.text = name
        newCard.timeText.text = "..."
        newCard.icon.setImageResource(R.drawable.loader)
        newCard.icon.startAnimation(loadingAnimation)

        newCard.button.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

        newCard.overflowMenu.visibility = View.INVISIBLE

        searchResults.addView(newCard)
        return newCard
    }

    private fun showSearchLoading() {
        searchLoading.startAnimation(loadingAnimation)
        searchLoading.setImageResource(R.drawable.loader)
        searchLoading.visibility = View.VISIBLE
    }

    private fun hideSearchLoading() {
        searchLoading.setImageDrawable(null)
        searchLoading.visibility = GONE
        searchLoading.startAnimation(loadingAnimation)
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

    public fun administrationRouteToIconString(route: String): String {
        return when(true) {
            route.startsWith("Intra") -> "ic_syringe"
            route.startsWith("Oral") -> "ic_pill_v5"
            route.startsWith("Rectal") -> "ic_tablet"
            route.startsWith("Inhalation") -> "ic_inhaler"
            else -> "ic_dropper"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (requestCode == 1) {
                if(intent.hasExtra("medication-uid")) {
                    // Going to modify medication here because this could be called from many places
                    val medicationUid = intent.getStringExtra("medication-uid")
                    if (medicationUid != null) {
                        val medication = DatabaseHelper.getMedicationByUid(medicationUid)
                        if (data.hasExtra("dpd-id")) {
                            Realm.getDefaultInstance().executeTransaction {
                                val dpdId = data.getIntExtra("dpd-id", 0)
                                val dpdObject = DatabaseHelper.getDPDObjectById(dpdId)
                                dpdObject!!.medications.add(medication)
                            }
                        }
                    }
                }

                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }
}
