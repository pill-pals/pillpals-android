package com.pillpals.pillpals.ui.medications.medication_info

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

import com.pillpals.pillpals.R
import com.pillpals.pillpals.ui.AddDrugActivity
import io.realm.Realm
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.pillpals.pillpals.data.model.DPDObjects
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.helpers.*
import com.shopify.promises.Promise
import io.realm.RealmList
import io.realm.RealmResults
import kotlinx.android.synthetic.main.medication_info_prompt.view.*
import org.w3c.dom.Text
import java.io.IOException
import java.util.*


class MedicationInfoActivity : AppCompatActivity() {

    public lateinit var tabLayout: TabLayout
    public lateinit var tabViewPager: ViewPager
    public lateinit var addButton: Button
    public lateinit var iconBackground: CardView
    public lateinit var medicationIcon: ImageView
    public lateinit var nameText: TextView
    public lateinit var dosageText: TextView
    public lateinit var alreadyAdded: TextView
    lateinit var updatingLoadingIcon: ProgressBar
    lateinit var updatingText: TextView
    lateinit var loadingAnimation: RotateAnimation
    lateinit var informationIcon: ImageView
    public var drugCode: Int = 0
    public var colorString: String = "#D3D3D3"
    public var administrationRoutes = listOf<String>()
    public var activeIngredients = listOf<String>()
    public var iconResource = R.drawable.ic_pill_v5
    public var dosageString = ""
    public var nameString = ""
    public var iconResourceString: String? = null
    var ndcCode: String? = null
    var rxcui: String? = null
    var linkingMedication: Boolean = false
    var splSetId: String? = null
    var loadedTabs = 0


    var resetData = false

    var drugSchedulesResponse: List<String>? = null // Tab 0
    var interactionsResponse: List<InteractionResult>? = null // Tab 1
    var sideEffectsResponse: List<SideEffectResult>? = null // Tab 1
    var descriptionResponse: String? = null // Tab 0
    var warningResponse: String? = null // Tab 2
    var overdosageResponse: String? = null // Tab 2
    var recallsResponse: RecallsResult? = null // Tab 2
    var colorResponse: ColorResult? = null // Tab 0
    var shapeResponse: ShapeResult? = null // Tab 0
    var packageSizesResponse: List<String>? = null // Tab 0
    var interactsWithAlcoholResponse: Boolean? = null // Tab 1
    var interactsWithCaffeineResponse: Boolean? = null // Tab 1

    private lateinit var realm: Realm

    private var tabFragments: List<MedicationInfoTextFragment> = mutableListOf(
        MedicationInfoTextFragment(),
        MedicationInfoTextFragment(),
        MedicationInfoTextFragment()
    )
    private var tabTitles: List<String> = mutableListOf(
        "Overview",
        "Side Effects",
        "Tips/Warnings"
    )
    private lateinit var tabPagerAdapter: TabPagerAdapter

    private var textParams : LayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    private var textTopMargin = 16
    private var headerSize = 20f
    private var bodySize = 16f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        realm = Realm.getDefaultInstance()
        setContentView(R.layout.activity_medication_info)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        loadingAnimation = RotateAnimation(0f, 360f, 21f, 21f)
        loadingAnimation.interpolator = LinearInterpolator()
        loadingAnimation.repeatCount = Animation.INFINITE
        loadingAnimation.duration = 700

        addButton = findViewById(R.id.addButton)
        alreadyAdded = findViewById(R.id.alreadyAdded)
        tabLayout = findViewById(R.id.tabLayout)
        tabViewPager = findViewById(R.id.tabViewPager)
        tabLayout.setupWithViewPager(tabViewPager)
        iconBackground = findViewById(R.id.iconBackground)
        medicationIcon = findViewById(R.id.medicationIcon)
        nameText = findViewById(R.id.nameText)
        dosageText = findViewById(R.id.dosageText)
        updatingLoadingIcon = findViewById(R.id.updatingLoadingIcon)
        updatingText = findViewById(R.id.updatingText)
        informationIcon = findViewById(R.id.informationIcon)

        updatingLoadingIcon.visibility = View.GONE
        updatingText.visibility = View.GONE

        drugCode = intent.getIntExtra("drug-code", 0)
        colorString = intent.getStringExtra("icon-color")!!
        administrationRoutes = intent.getStringArrayListExtra("administration-routes")!!.toList()
        activeIngredients = intent.getStringArrayListExtra("active-ingredients")!!.toList()
        dosageString = intent.getStringExtra("dosage-string")!!
        nameString = intent.getStringExtra("name-text")!!
        linkingMedication = intent.getBooleanExtra("link-medication", false)
        ndcCode = intent.getStringExtra("ndc-code")
        rxcui = intent.getStringExtra("rxcui")
        splSetId = intent.getStringExtra("spl-set-id")


        iconResourceString = intent.getStringExtra("icon-resource")
        if (iconResourceString == null) {
            iconResourceString = "ic_pill_v5"
        }

        alreadyAdded.visibility = View.GONE
        if(linkingMedication) {
            addButton.text = "+ Link"
        }
        else if(userTakesDrug()) {
            addButton.setTextColor(resources.getColor(R.color.colorLightGrey))
            addButton.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.colorDarkishGrey, null))
            alreadyAdded.visibility = View.VISIBLE
        }

        iconResource = DatabaseHelper.getDrawableIconById(this, DatabaseHelper.getIconIDByString(iconResourceString!!))

        iconBackground.setCardBackgroundColor(Color.parseColor(colorString))
        medicationIcon.setImageResource(iconResource)
        nameText.text = nameString
        dosageText.text = dosageString

        tabPagerAdapter = TabPagerAdapter(supportFragmentManager)
        tabViewPager.adapter = tabPagerAdapter
        tabViewPager.offscreenPageLimit = 3
        tabViewPager.pageMargin = 48

        textParams.topMargin = textTopMargin

        addButton.setOnClickListener {
            if(linkingMedication) {
                val intent = Intent(this, MedicationInfoActivity::class.java)
                intent.putExtra("dpd-id", drugCode)
                intent.putExtra("color-string", colorString)
                intent.putExtra("image-string", iconResourceString)
                intent.putExtra("name-string", nameString)
                intent.putExtra("dosage-string", dosageString)
                intent.putExtra("ndc-code", ndcCode)
                intent.putExtra("rxcui", rxcui)
                intent.putExtra("spl-set-id", splSetId)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            else {
                val intent = Intent(this, AddDrugActivity::class.java)
                intent.putExtra("dpd-id", drugCode)
                intent.putExtra("color-string", colorString)
                intent.putExtra("image-string", iconResourceString)
                intent.putExtra("name-string", nameString)
                intent.putExtra("dosage-string", dosageString)
                intent.putExtra("ndc-code", ndcCode)
                intent.putExtra("rxcui", rxcui)
                intent.putExtra("spl-set-id", splSetId)
                startActivityForResult(intent, 1)
            }
        }

        informationIcon.setOnClickListener {openInfoDialog()}

        val handler = Handler()
        val timer = Timer()
        val doAsynchronousTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    try {
                        if(resetData) {
                            resetData = false
                            updatingLoadingIcon.visibility = View.GONE
                            updatingText.visibility = View.GONE
                            setTabFragmentData()
                        }
                    } catch (e: IOException) { }
                }
            }
        }
        timer.schedule(doAsynchronousTask, 0, 250)

        addDrugToDPDTable()

        DatabaseHelper.logVisit("MedicationInfoActivity")
    }

    public fun tabFragmentLoaded() {
        setTabText(0, listOf("Active Ingredients", "Administration Routes"), listOf(bulletedList(activeIngredients), bulletedList(administrationRoutes)))
        loadedTabs++
        if(loadedTabs >= tabFragments.count()) fillAPIData()
    }

    fun setTabFragmentData() {
        // Stored non-retrieved data
        var tabZeroTitles = listOf("Active Ingredients", "Administration Routes")
        var tabZeroValues = listOf(bulletedList(activeIngredients), bulletedList(administrationRoutes))
        var tabOneTitles = listOf<String>()
        var tabOneValues = listOf<String>()
        var tabTwoTitles = listOf<String>()
        var tabTwoValues = listOf<String>()

        // Organize extra data

        // Tab 0 - Overview
        if(descriptionResponse != null) {
            tabZeroTitles = listOf("Description").plus(tabZeroTitles)
            tabZeroValues = listOf(descriptionResponse!!).plus(tabZeroValues)
        }

        if(colorResponse?.colorName != null) {
            tabZeroTitles = tabZeroTitles.plus("Color")
            tabZeroValues = tabZeroValues.plus(colorResponse!!.colorName!!)
        }

        if(shapeResponse?.shapeName != null) {
            tabZeroTitles = tabZeroTitles.plus("Color")
            tabZeroValues = tabZeroValues.plus(shapeResponse!!.shapeName!!)
        }

        if(packageSizesResponse != null
            && packageSizesResponse!!.isNotEmpty()
            && packageSizesResponse!!.first().replace("(\\s)".toRegex(), "").count() > 0 ) {
            tabZeroTitles = tabZeroTitles.plus("Package Sizes")
            tabZeroValues = tabZeroValues.plus(bulletedList(packageSizesResponse!!))
        }

        if(drugSchedulesResponse != null && drugSchedulesResponse!!.isNotEmpty()) {
            tabZeroTitles = tabZeroTitles.plus("Drug Schedules")
            tabZeroValues = tabZeroValues.plus(bulletedList(drugSchedulesResponse!!))
        }

        // Tab 1 - Side Effects
        if(sideEffectsResponse != null && sideEffectsResponse!!.isNotEmpty()) {
            tabOneTitles = tabOneTitles.plus("Side Effects")
            tabOneValues = tabOneValues.plus(bulletedList(sideEffectsResponse!!.fold(listOf()) {acc, it ->
                acc.plus("${it.sideEffect} (${it.percent * 100f}%)")
            }))
        }

        if(interactsWithAlcoholResponse == true) {
            tabOneTitles = tabOneTitles.plus("Interacts with alcohol")
            tabOneValues = tabOneValues.plus("This drug may have some degree of an interaction with alcohol")
        }

        if(interactsWithCaffeineResponse == true) {
            tabOneTitles = tabOneTitles.plus("Interacts with caffeine")
            tabOneValues = tabOneValues.plus("This drug may have some degree of an interaction with caffeine")
        }

        if(interactionsResponse != null && interactionsResponse!!.isNotEmpty() && rxcui != null) {
            if(interactionsResponse!!.filter { it.rxcuis.contains(rxcui!!) }.count() > 0) {
                tabOneTitles = tabOneTitles.plus("Interactions with your linked drugs")
                tabOneValues = tabOneValues.plus(bulletedList(
                    interactionsResponse!!.filter {
                        it.rxcuis.contains(rxcui!!)
                    }.fold(listOf()) {acc, it ->
                        acc.plus(it.interaction)
                    })
                )
            }
        }

        // Tab 2 - Warnings/Tips
        if(warningResponse != null) {
            tabTwoTitles = tabTwoTitles.plus("Box Warning")
            tabTwoValues = tabTwoValues.plus(warningResponse!!)
        }

        if(overdosageResponse != null) {
            tabTwoTitles = tabTwoTitles.plus("Overdosage Description")
            tabTwoValues = tabTwoValues.plus(overdosageResponse!!)
        }

        if(recallsResponse != null) {
            tabTwoTitles = tabTwoTitles.plus("Reported Recalls")
            if(!recallsResponse!!.hasBeenRecalled) {
                tabTwoValues = tabTwoValues.plus("No reported recalls found in the FDA database")
            }
            else if(recallsResponse!!.hasBeenRecalled && !recallsResponse!!.anyMandatoryRecalls) {
                tabTwoValues = tabTwoValues.plus("This drug has been voluntarily recalled in the following quantities:\n" + bulletedList(recallsResponse!!.recallQuantities))
            }
            else if(recallsResponse!!.hasBeenRecalled && recallsResponse!!.anyMandatoryRecalls) {
                tabTwoValues = tabTwoValues.plus("This drug has been recalled in the following quantities:\n" + bulletedList(recallsResponse!!.recallQuantities))
            }
        }

        // If empty
        if(tabZeroValues.isEmpty()) tabZeroValues = tabZeroValues.plus("No data found in online resources")
        if(tabOneValues.isEmpty()) tabOneValues = tabOneValues.plus("No data found in online resources")
        if(tabTwoValues.isEmpty()) tabTwoValues = tabTwoValues.plus("No data found in online resources")

        // Set tabs
        setTabLoading(0, false)
        setTabLoading(1, false)
        setTabLoading(2, false)

        setTabText(0, tabZeroTitles, tabZeroValues)
        setTabText(1, tabOneTitles, tabOneValues)
        setTabText(2, tabTwoTitles, tabTwoValues)
    }

    private fun bulletedList(list: List<String>): String {
        return "• " + list.joinToString("\n• ")
    }

    //Assumes that the headers and bodyText lists are ordered and have indices that correspond with each other 1:1
    private fun setTabText(tabIndex: Int, headers: List<String>, bodyText: List<String>) {
        val layout: LinearLayout = tabPagerAdapter.getItem(tabIndex).layout
        resetText(layout)
        for ((index, bodyText) in bodyText.withIndex()) {
            if(headers.count() > index) {
                appendText(layout, headers[index], "header")
            }
            appendText(layout, bodyText, "body")
        }
    }

    private fun setTabLoading(tabIndex: Int, loading: Boolean) {
        tabPagerAdapter.getItem(tabIndex).setTabLoading(loading)
    }

    private fun addDrugToDPDTable() {
        val existingRow = realm.where(DPDObjects::class.java).equalTo("dpd_id", drugCode).findFirst()
        if(existingRow != null) return

        realm.executeTransaction {
            val dpdObject = it.createObject(DPDObjects::class.java, drugCode)
            dpdObject.name = nameString
            val administrationRoutesRealmList = RealmList<String>()
            administrationRoutesRealmList.addAll(administrationRoutes)
            dpdObject.administrationRoutes = administrationRoutesRealmList
            val activeIngredientsRealmList = RealmList<String>()
            activeIngredientsRealmList.addAll(activeIngredients)
            dpdObject.activeIngredients = activeIngredientsRealmList
            dpdObject.dosageString = dosageString
            dpdObject.ndc_id = ndcCode
            dpdObject.rxcui = rxcui
            dpdObject.spl_set_id = splSetId
        }
    }

    private fun userTakesDrug(): Boolean {
        val dpdObject = realm.where(DPDObjects::class.java).equalTo("dpd_id", drugCode).findFirst() ?: return false
        return dpdObject.medications.filter { !it.deleted }.count() > 0
    }

    private fun fillAPIData() {
        // Fill info from  DB to start
        // fillStoredData()

        //updatingLoadingIcon.setImageResource(R.drawable.loader)
        updatingLoadingIcon.startAnimation(loadingAnimation)

        updatingLoadingIcon.visibility = View.VISIBLE
        updatingText.visibility = View.VISIBLE

        setTabLoading(0, true)
        setTabLoading(1, true)
        setTabLoading(2, true)

        // Waterfall the requests for now
        val drugSchedulesPromise = MedicationInfoRetriever.drugSchedules(drugCode)
        val interactionsPromise = MedicationInfoRetriever.interactions(allUsersRxcuis().plus(rxcui).filterNotNull())
        val sideEffectsPromise = MedicationInfoRetriever.sideEffects(ndcCode ?: "null")
        val descriptionPromise = MedicationInfoRetriever.description(ndcCode ?: "null")
        val warningPromise = MedicationInfoRetriever.warning(ndcCode ?: "null")
        val overdosagePromise = MedicationInfoRetriever.overdosage(ndcCode ?: "null")
        val recallsPromise = MedicationInfoRetriever.recalls(ndcCode ?: "null")
        val colorPromise = MedicationInfoRetriever.color(ndcCode ?: "null")
        val shapePromise = MedicationInfoRetriever.shape(ndcCode ?: "null")
        val packageSizesPromise = MedicationInfoRetriever.packageSizes(ndcCode ?: "null")
        val interactsWithAlcoholPromise = MedicationInfoRetriever.interactsWithAlcohol(ndcCode ?: "null")
        val interactsWithCaffeinePromise = MedicationInfoRetriever.interactsWithCaffeine(ndcCode ?: "null")


        drugSchedulesPromise.whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    drugSchedulesResponse = result.value
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
            Log.i("test", "drugSchedulesPromise")
            interactionsPromise.whenComplete { result: Promise.Result<List<InteractionResult>, RuntimeException> ->
                when (result) {
                    is Promise.Result.Success -> {
                        // Use result here
                        interactionsResponse = result.value
                    }
                    is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                }
                Log.i("test", "interactionsPromise")
                sideEffectsPromise.whenComplete { result: Promise.Result<List<SideEffectResult>, RuntimeException> ->
                    when (result) {
                        is Promise.Result.Success -> {
                            // Use result here
                            sideEffectsResponse = result.value
                        }
                        is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                    }
                    Log.i("test", "sideEffectsPromise")
                    descriptionPromise.whenComplete { result: Promise.Result<String, RuntimeException> ->
                        when (result) {
                            is Promise.Result.Success -> {
                                // Use result here
                                descriptionResponse = result.value
                            }
                            is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                        }
                        Log.i("test", "descriptionPromise")
                        warningPromise.whenComplete { result: Promise.Result<String, RuntimeException> ->
                            when (result) {
                                is Promise.Result.Success -> {
                                    // Use result here
                                    warningResponse = result.value
                                }
                                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                            }
                            Log.i("test", "warningPromise")
                            overdosagePromise.whenComplete { result: Promise.Result<String, RuntimeException> ->
                                when (result) {
                                    is Promise.Result.Success -> {
                                        // Use result here
                                        overdosageResponse = result.value
                                    }
                                    is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                                }
                                Log.i("test", "overdosagePromise")
                                recallsPromise.whenComplete { result: Promise.Result<RecallsResult, RuntimeException> ->
                                    when (result) {
                                        is Promise.Result.Success -> {
                                            // Use result here
                                            recallsResponse = result.value
                                        }
                                        is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                                    }
                                    Log.i("test", "recallsPromise")
                                    colorPromise.whenComplete { result: Promise.Result<ColorResult, RuntimeException> ->
                                        when (result) {
                                            is Promise.Result.Success -> {
                                                // Use result here
                                                colorResponse = result.value
                                            }
                                            is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                                        }
                                        Log.i("test", "colorPromise")
                                        shapePromise.whenComplete { result: Promise.Result<ShapeResult, RuntimeException> ->
                                            when (result) {
                                                is Promise.Result.Success -> {
                                                    // Use result here
                                                    shapeResponse = result.value
                                                }
                                                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                                            }
                                            Log.i("test", "shapePromise")
                                            packageSizesPromise.whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
                                                when (result) {
                                                    is Promise.Result.Success -> {
                                                        // Use result here
                                                        packageSizesResponse = result.value
                                                    }
                                                    is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                                                }
                                                Log.i("test", "packageSizesPromise")
                                                interactsWithAlcoholPromise.whenComplete { result: Promise.Result<Boolean, RuntimeException> ->
                                                    when (result) {
                                                        is Promise.Result.Success -> {
                                                            // Use result here
                                                            interactsWithAlcoholResponse = result.value
                                                        }
                                                        is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                                                    }
                                                    Log.i("test", "interactsWithAlcoholPromise")
                                                    interactsWithCaffeinePromise.whenComplete { result: Promise.Result<Boolean, RuntimeException> ->
                                                        when (result) {
                                                            is Promise.Result.Success -> {
                                                                // Use result here
                                                                interactsWithCaffeineResponse = result.value
                                                            }
                                                            is Promise.Result.Error -> Log.i("Error", result.error.message!!)
                                                        }

                                                        Log.i("test", "interactsWithCaffeinePromise")
                                                        resetData = true

                                                        // Set new stored data
                                                        // updateMedicationData()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun allUsersMedications(): RealmResults<out Medications> {
        return DatabaseHelper.readAllData(Medications::class.java) as RealmResults<out Medications>
    }

    private fun allUsersRxcuis(): List<String> {
        return allUsersMedications().fold(listOf<String?>()) {acc, it -> acc.plus(it.dpd_object?.firstOrNull()?.rxcui)}.filterNotNull()
    }

    private fun resetText(layout: ViewGroup) {
        layout.removeAllViews()
    }

    private fun appendText(layout: ViewGroup, text: String, style: String) {
        var newView = TextView(this)
        newView.text = text
        newView.layoutParams = textParams

        if (style == "header"){
            newView.textSize = headerSize
        }
        else if (style == "body") {
            newView.textSize = bodySize
        }


        layout.addView(newView)
    }

    private fun openInfoDialog() {
        val gradeInfoDialog = LayoutInflater.from(this).inflate(R.layout.medication_info_prompt, null)

        val title = SpannableString("Medication Info Viewer")
        title.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.colorLightGrey)),
            0,
            title.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(gradeInfoDialog)
            .setTitle(title)

        val timeAlertDialog = dialogBuilder.show()

        gradeInfoDialog.dialogCancelBtn.setOnClickListener {
            timeAlertDialog.dismiss()
        }
    }

    private inner class TabPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int) : MedicationInfoTextFragment {
            return tabFragments[position]
        }

        override fun getCount() : Int {
            return tabFragments.size
        }

        override fun getPageTitle(position: Int) : String {
            return tabTitles[position]
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
        if(userTakesDrug()) {
            addButton.setTextColor(resources.getColor(R.color.colorLightGrey))
            addButton.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.colorDarkishGrey, null))
            alreadyAdded.visibility = View.VISIBLE
        }
    }
}
