package com.pillpals.pillpals.ui.medications

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.ui.AddDrugActivity
import com.pillpals.pillpals.ui.DrugCard
import io.realm.Realm
import java.util.*
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuInflater
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.google.gson.Gson
import com.pillpals.pillpals.data.ActiveIngredient
import com.pillpals.pillpals.data.AdministrationRoute
import com.pillpals.pillpals.data.DrugProduct
import com.pillpals.pillpals.data.OpenFDANameResponse
import com.pillpals.pillpals.data.model.DPDObjects
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getCorrectIconDrawable
import com.pillpals.pillpals.helpers.FileWriter
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.readAllData
import com.pillpals.pillpals.helpers.calculateScheduleRecords
import com.pillpals.pillpals.helpers.margin
import com.pillpals.pillpals.ocrreader.OcrCaptureActivity
import com.pillpals.pillpals.ui.MainActivity
import com.pillpals.pillpals.ui.ScheduleRecord
import com.pillpals.pillpals.ui.medications.medication_info.MedicationInfoActivity
import com.pillpals.pillpals.ui.search.SearchActivity
import io.realm.RealmResults
import kotlinx.android.synthetic.main.add_medication_prompt.view.*
import kotlinx.android.synthetic.main.delete_prompt.view.*
import kotlinx.android.synthetic.main.drug_card.view.*
import kotlinx.android.synthetic.main.scan_prompt.view.*
import okhttp3.*
import okio.IOException
import java.util.concurrent.TimeUnit

class MedicationsFragment : Fragment() {

    public lateinit var drugButton: Button
    public lateinit var stack: LinearLayout

    private lateinit var prefs: SharedPreferences

    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        FileWriter.createJSONStringFromData(context!!)
        val view = inflater!!.inflate(R.layout.fragment_medications, container,false)

        realm = Realm.getDefaultInstance()

        prefs = activity!!.getPreferences(Context.MODE_PRIVATE)

        stack = view!!.findViewById(R.id.stack)
        stack.layoutTransition.enableTransitionType(LayoutTransition.CHANGING) //Makes collapsing smooth

        drugButton = view!!.findViewById(R.id.drugButton)
        getActivity()!!.invalidateOptionsMenu()

        val outerContext = this.context!!

        drugButton.setOnClickListener {
            val addPrompt = LayoutInflater.from(this.context).inflate(R.layout.add_medication_prompt, null)

            val title = SpannableString("Add medication")
            title.setSpan(
                ForegroundColorSpan(this!!.resources.getColor(R.color.colorLightGrey)),
                0,
                title.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this.context!!)
                .setView(addPrompt)
                .setTitle(title)

            val alertDialog = dialogBuilder.show()

            addPrompt.dialogScanBtn.setOnClickListener {
                alertDialog.dismiss()
                val scanPrompt = LayoutInflater.from(this.context).inflate(R.layout.scan_prompt, null)

                val scanTitle = SpannableString("Scan medication")
                scanTitle.setSpan(
                    ForegroundColorSpan(this!!.resources.getColor(R.color.colorLightGrey)),
                    0,
                    scanTitle.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val scanDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(outerContext)
                    .setView(scanPrompt)
                    .setTitle(scanTitle)

                val scanAlertDialog = scanDialogBuilder.show()

                scanPrompt.dialogStartScanBtn.setOnClickListener {
                    scanAlertDialog.dismiss()
                    val intent = Intent(context, OcrCaptureActivity::class.java)
                    startActivityForResult(intent, 5)
                }

                scanPrompt.dialogCancelScanBtn.setOnClickListener {
                    scanAlertDialog.dismiss()
                }
            }

            addPrompt.dialogSearchBtn.setOnClickListener {
                alertDialog.dismiss()
                val intent = Intent(context, SearchActivity::class.java)
                startActivityForResult(intent, 2)
            }

            addPrompt.dialogManualBtn.setOnClickListener {
                alertDialog.dismiss()
                val intent = Intent(context, AddDrugActivity::class.java)
                startActivityForResult(intent, 1)
            }

            addPrompt.dialogCancelAddBtn.setOnClickListener {
                alertDialog.dismiss()
            }

        }

        updateMedicationList()

        return view
    }

    override fun onResume() {
        super.onResume()
        getActivity()!!.invalidateOptionsMenu()
        FileWriter.createJSONStringFromData(context!!)
    }

    private fun popoverMenuMedication(v: View, medication: Medications) {
        val popup = PopupMenu(context, v.overflowMenu)
        val inflater: MenuInflater = popup.menuInflater
        val dpdObject = medication.dpd_object?.firstOrNull()
        if(dpdObject == null) {
            inflater.inflate(R.menu.medication, popup.menu)
        }
        else {
            inflater.inflate(R.menu.medication_dpd, popup.menu)
        }
        popup.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.medicationDelete -> {
                    val deleteDialog = LayoutInflater.from(this.context).inflate(R.layout.delete_prompt, null)

                    val title = SpannableString("Delete " + medication.name)
                    title.setSpan(
                        ForegroundColorSpan(this!!.resources.getColor(R.color.colorLightGrey)),
                        0,
                        title.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(context!!)
                        .setView(deleteDialog)
                        .setTitle(title)

                    val deleteDrugName = deleteDialog!!.findViewById<TextView>(R.id.deleteDrugName)
                    deleteDrugName.text = medication.name

                    val deleteAlertDialog = dialogBuilder.show()
                    deleteDialog.dialogConfirmBtn.setOnClickListener {
                        deleteAlertDialog.dismiss()
                        deleteMedication(medication)
                        updateMedicationList()
                    }

                    deleteDialog.dialogCancelBtn.setOnClickListener {
                        deleteAlertDialog.dismiss()
                    }
                }
                R.id.viewDrugInfo -> {
                    if(dpdObject == null) return@setOnMenuItemClickListener true

                    val intent = Intent(context, MedicationInfoActivity::class.java)
                    intent.putExtra("drug-code", dpdObject.dpd_id)
                    intent.putExtra("icon-color", getColorStringByID(medication.color_id))
                    intent.putStringArrayListExtra("administration-routes", ArrayList(dpdObject.administrationRoutes))
                    intent.putStringArrayListExtra("active-ingredients",  ArrayList(dpdObject.activeIngredients))
                    intent.putExtra("dosage-string", dpdObject.dosageString)
                    intent.putExtra("name-text", dpdObject.name)
                    startActivityForResult(intent, 2)
                }
                R.id.linkMedication -> {
                    val intent = Intent(context, SearchActivity::class.java)
                    intent.putExtra("medication-uid", medication.uid)
                    startActivityForResult(intent, 3)
                }
            }
            true
        }
        popup.show()
    }

    private fun updateMedicationList() {
        stack.removeAllViews()
        for (drug in realm.where(Medications::class.java).findAll()) {
            if (drug.deleted) {
                continue
            }

            addDrugCard(drug)
        }
    }

    private fun addDrugCard(medication: Medications) {
        var newCard = DrugCard(this.context!!)

        newCard.nameText.text = medication.name
        newCard.altText.text = medication.dosage
        newCard.iconBackground.setCardBackgroundColor(Color.parseColor(getColorStringByID(medication.color_id)))
        newCard.icon.setImageDrawable(getCorrectIconDrawable(this.context!!, medication))

        newCard.setOnClickListener {
            val dpdObject = medication.dpd_object?.firstOrNull() ?: return@setOnClickListener

            val intent = Intent(context, MedicationInfoActivity::class.java)
            intent.putExtra("drug-code", dpdObject.dpd_id)
            intent.putExtra("icon-color", getColorStringByID(medication.color_id))
            intent.putStringArrayListExtra("administration-routes", ArrayList(dpdObject.administrationRoutes))
            intent.putStringArrayListExtra("active-ingredients",  ArrayList(dpdObject.activeIngredients))
            intent.putExtra("dosage-string", dpdObject.dosageString)
            intent.putExtra("name-text", dpdObject.name)
            startActivityForResult(intent, 4)
        }

        newCard.button.setOnClickListener {
            val intent = Intent(context, AddDrugActivity::class.java)
            intent.putExtra("medication-uid", medication.uid)
            startActivityForResult(intent, 1)
        }
        newCard.button.text = "Edit"
        newCard.button.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        newCard.button.visibility = View.VISIBLE

        newCard.overflowMenu.setOnClickListener {
            popoverMenuMedication(newCard, medication)
        }

        newCard.drugCardLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        var recordList = calculateScheduleRecords(medication.schedules, activity!!)
        recordList.forEach { record ->
            record.deleteScheduleImage.visibility = View.GONE
            newCard.scheduleStack.addView(record)
        }

        newCard.scheduleContainer.visibility = View.VISIBLE
        if (prefs.getBoolean(getString(R.string.schedule_preview_collapsed_prefix) + medication.uid, false)) {
            newCard.scheduleStack.visibility = View.GONE
            newCard.collapseButton.setImageResource(R.drawable.ic_circle_chevron_down_from_up)
        }

        newCard.collapseButton.setOnClickListener {
            toggleCollapse(newCard.scheduleStack, newCard.collapseButton, medication)
        }
        
        stack.addView(newCard)
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

        //TODO: Use preferences to save collapsed state of each stack, similar to dashboard
    }

    private fun createMedicationData(drugName: String, drugDose: String) {
        realm.executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = drugName
            medication.dosage = drugDose
        }
        updateMedicationList()
    }

    private fun deleteMedication(medication: Medications) {
        realm.executeTransaction {
            // medication is copied from realm, not the real object
            val databaseDrug = realm.where(Medications::class.java).equalTo("uid", medication.uid).findFirst()!!
            databaseDrug.deleted = true
            for (schedule in databaseDrug.schedules) {
                schedule.deleted = true
            }
        }
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 1 -> add drug
        // 2 -> view drug info
        // 3 -> link drug
        // 4 -> search for drug
        // 5 -> scanner
        // 6 -> medication info for scanned drug

        if(data != null) {
            if(requestCode == 5) {
                if(data.hasExtra("din")) {
                    val din = data.getStringExtra("din")
                    if(din != null) getFromDin(din)
                }
            }
        }

        val schedules = readAllData(Schedules::class.java) as RealmResults<out Schedules>
        schedules.forEach {
            if(it.medication?.firstOrNull() == null) DatabaseHelper.obliterateSchedule(it)
        }

        updateMedicationList()
    }

    fun backgroundThreadToast(context: Context, msg: String, length: Int) {
        if (context != null && msg != null) {
            Handler(Looper.getMainLooper()).post(object: Runnable {
                override fun run() {
                    Toast.makeText(context, msg, length).show()
                }
            });
        }
    }

    fun getFromDin(din: String) {
        val url = "https://health-products.canada.ca/api/drug/drugproduct/?din=${din}"

        val client = OkHttpClient
            .Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder().url(url).build()

        var ingredientNameList = listOf<String>()
        var dosageString = ""
        var ndcCode: String? = ""
        var rxcui: String? = ""
        var splSetId: String? = ""


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                        return
                    }

                    val jsonString = response.body!!.string()
                    val gson = Gson()
                    val drugProducts = gson.fromJson(jsonString, Array<DrugProduct>::class.java).toList()

                    // First for now
                    val firstDrugProduct = drugProducts.firstOrNull()

                    firstDrugProduct ?: return backgroundThreadToast(context!!, "Drug not found with din $din. Please try again.", Toast.LENGTH_LONG)

                    backgroundThreadToast(context!!, "Drug found: ${firstDrugProduct.brand_name}. Loading...", Toast.LENGTH_LONG)

                    drugProducts.forEachIndexed {namedIndex, drugProduct ->
                        val url = "https://health-products.canada.ca/api/drug/activeingredient/?id=${drugProduct.drug_code}"

                        val request = Request.Builder().url(url).build()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                e.printStackTrace()
                            }

                            override fun onResponse(call: Call, response: Response) {
                                response.use {
                                    if (!response.isSuccessful) {
                                        backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                        return
                                    }

                                    val jsonString = response.body!!.string()
                                    val gson = Gson()
                                    val activeIngredients = gson.fromJson(jsonString, Array<ActiveIngredient>::class.java).toList()

                                    ingredientNameList = activeIngredients.fold(listOf<String>()) { acc, it ->
                                        acc.plus(it.ingredient_name)
                                    }

                                    val dosageValues = activeIngredients.fold(listOf<String>()) { acc, it ->
                                        acc.plus(it.strength)
                                    }

                                    val dosageUnits = activeIngredients.fold(listOf<String>()) { acc, it ->
                                        if(acc.contains(it.strength_unit)) acc
                                        else acc.plus(it.strength_unit)
                                    }

                                    dosageString = "${dosageValues.joinToString("/")} ${dosageUnits.joinToString("/")}"

                                    // Get other ID's from FDA
                                    val url = "https://api.fda.gov/drug/ndc.json?limit=100&search=brand_name:${drugProduct.brand_name.replace("( .*)".toRegex(), "")}"

                                    val request = Request.Builder().url(url).build()

                                    client.newCall(request).enqueue(object : Callback {
                                        override fun onFailure(call: Call, e: IOException) {
                                            backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                            e.printStackTrace()
                                        }

                                        override fun onResponse(call: Call, response: Response) {
                                            response.use {
                                                if (response.isSuccessful) {
                                                    val jsonString = response.body!!.string()
                                                    val gson = Gson()
                                                    val fdaResponse = gson.fromJson(jsonString, OpenFDANameResponse::class.java)

                                                    if(fdaResponse.error == null) {
                                                        val fdaResults = fdaResponse.results

                                                        val fdaResultWithDosage = fdaResults?.filter {
                                                            if(it.active_ingredients == null) return@filter false
                                                            val totalVal = it.active_ingredients.fold(0f) {acc, it ->
                                                                acc + it.strength.replace("( .*)".toRegex(), "").toFloat()
                                                            }
                                                            it.active_ingredients.any {
                                                                it.strength.contains("${dosageValues.first()} ${dosageUnits.firstOrNull()?.toLowerCase()}")
                                                            } || dosageValues.firstOrNull()?.toFloat() == totalVal
                                                        }?.firstOrNull()

                                                        val firstFdaResult = fdaResults?.firstOrNull()

                                                        // SET FDA IDS

                                                        if(fdaResultWithDosage != null) {
                                                            ndcCode = fdaResultWithDosage.product_ndc
                                                            rxcui = fdaResultWithDosage.openfda.rxcui?.firstOrNull()
                                                            splSetId = fdaResultWithDosage.openfda.spl_set_id?.firstOrNull()
                                                        }
                                                        else if(firstFdaResult != null) {
                                                            ndcCode = firstFdaResult.product_ndc
                                                            rxcui = firstFdaResult.openfda.rxcui?.firstOrNull()
                                                            splSetId = firstFdaResult.openfda.spl_set_id?.firstOrNull()
                                                        }
                                                    }
                                                }



                                                val url = "https://health-products.canada.ca/api/drug/route/?id=${drugProduct.drug_code}"

                                                val request = Request.Builder().url(url).build()

                                                client.newCall(request).enqueue(object :
                                                    Callback {
                                                    override fun onFailure(call: Call, e: IOException) {
                                                        backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                                        e.printStackTrace()
                                                    }

                                                    override fun onResponse(call: Call, response: Response) {
                                                        response.use {
                                                            if (!response.isSuccessful) {
                                                                backgroundThreadToast(context!!, "API Error. Please try again.", Toast.LENGTH_SHORT)
                                                                return
                                                            }

                                                            val jsonString = response.body!!.string()
                                                            val gson = Gson()
                                                            val administrationRoutes = gson.fromJson(jsonString, Array<AdministrationRoute>::class.java).toList()

                                                            var colorString =
                                                                DatabaseHelper.getRandomColorString()
                                                            while(colorString == "#000000") { // Let's not let black be selected randomly
                                                                colorString = DatabaseHelper.getRandomColorString()
                                                            }

                                                            var administrationRoutesList = listOf<String>()

                                                            val firstRoute = administrationRoutes.firstOrNull()
                                                            if(firstRoute != null) {

                                                                administrationRoutesList = administrationRoutes.fold(listOf<String>()) { acc, it ->
                                                                    acc.plus(it.route_of_administration_name)
                                                                }
                                                            }

                                                            val infoIntent = Intent(context, MedicationInfoActivity::class.java)


                                                            infoIntent.putExtra("link-medication", false)


                                                            infoIntent.putExtra("drug-code", drugProduct.drug_code)
                                                            infoIntent.putExtra("icon-color", colorString)
                                                            infoIntent.putStringArrayListExtra("administration-routes", ArrayList(administrationRoutesList))
                                                            infoIntent.putStringArrayListExtra("active-ingredients", ArrayList(ingredientNameList))
                                                            infoIntent.putExtra("dosage-string", dosageString)
                                                            infoIntent.putExtra("name-text", drugProduct.brand_name)
                                                            if(firstRoute != null) {
                                                                infoIntent.putExtra("icon-resource", administrationRouteToIconString(firstRoute.route_of_administration_name))
                                                            }

                                                            infoIntent.putExtra("ndc-code", ndcCode)
                                                            infoIntent.putExtra("rxcui", rxcui)
                                                            infoIntent.putExtra("spl-set-id", splSetId)

                                                            startActivityForResult(infoIntent, 6)
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

    fun administrationRouteToIconString(route: String): String {
        return when(true) {
            route.startsWith("Intra") -> "ic_syringe"
            route.startsWith("Oral") -> "ic_pill_v5"
            route.startsWith("Rectal") -> "ic_tablet"
            route.startsWith("Inhalation") -> "ic_inhaler"
            else -> "ic_dropper"
        }
    }
}