package com.pillpals.pillpals.helpers

import android.util.Log
import com.google.gson.Gson
import com.pillpals.pillpals.data.*
import com.shopify.promises.Promise
import okhttp3.*
import okio.IOException
import java.util.concurrent.TimeUnit



import okhttp3.Call
import okhttp3.OkHttpClient
import java.util.*

object OkHttpUtils {
    fun cancelCallWithTag(client: OkHttpClient, tag: String) {
        for (call in client.dispatcher.queuedCalls()) {
            if (call.request().tag() == tag)
                call.cancel()
        }
        for (call in client.dispatcher.runningCalls()) {
            if (call.request().tag() == tag)
                call.cancel()
        }
    }
}

class MedicationInfoRetriever {
    companion object {
        /* Example of use
        MedicationInfoRetriever.activeIngredients(73177).whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun activeIngredients(dpdId: Int): Promise<List<String>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://health-products.canada.ca/api/drug/activeingredient/?id=${dpdId}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val activeIngredients = gson.fromJson(jsonString, Array<ActiveIngredient>::class.java).toList()

                            val ingredientNameList = activeIngredients.fold(listOf<String>()) { acc, it ->
                                acc.plus(it.ingredient_name)
                            }

                            val dosageValues = activeIngredients.fold("") { acc, it ->
                                if(acc.isNotEmpty()) acc + "/" + it.strength
                                else acc + it.strength
                            }

                            val dosageUnits = activeIngredients.fold(listOf<String>()) { acc, it ->
                                if(acc.contains(it.strength_unit)) acc
                                else acc.plus(it.strength_unit)
                            }.joinToString("/")

                            resolve(ingredientNameList)
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.intakeRoutes(73177).whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun intakeRoutes(dpdId: Int): Promise<List<String>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://health-products.canada.ca/api/drug/route/?id=${dpdId}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val administrationRoutes = gson.fromJson(jsonString, Array<AdministrationRoute>::class.java).toList()

                            val administrationRouteNames = administrationRoutes.fold(listOf<String>()) { acc, it ->
                                acc.plus(it.route_of_administration_name)
                            }

                            resolve(administrationRouteNames)
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.drugSchedules(73177).whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun drugSchedules(dpdId: Int): Promise<List<String>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://health-products.canada.ca/api/drug/schedule/?id=${dpdId}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val drugSchedules = gson.fromJson(jsonString, Array<DrugSchedule>::class.java).toList()

                            val drugScheduleNames = drugSchedules.fold(listOf<String>()) { acc, it ->
                                acc.plus(if(it.schedule_name == "OTC") "Over The Counter (OTC)" else it.schedule_name)
                            }

                            resolve(drugScheduleNames)
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.interactions(listOf("207106", "152923", "656659")).whenComplete { result: Promise.Result<List<InteractionResult>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun interactions(rxcuis: List<String>): Promise<List<InteractionResult>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://rxnav.nlm.nih.gov/REST/interaction/list.json?rxcuis=${rxcuis.joinToString("+")}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val interactionsResponse = gson.fromJson(jsonString, InteractionsResponse::class.java)

                            var res: List<InteractionResult> = listOf()

                            // Use first source I guess. Might specify to try for DrugBank later
                            val source = interactionsResponse.fullInteractionTypeGroup?.firstOrNull()

                            source ?: return@onResponse resolve(res)

                            source.fullInteractionType.forEach { fullInteractionType ->
                                fullInteractionType.interactionPair.forEach { interactionPair ->
                                    val interactionRxcuis = interactionPair.interactionConcept.fold(listOf<String>()) { acc, it ->
                                        acc.plus(it.minConceptItem.rxcui)
                                    }

                                    res = res.plus(InteractionResult(interactionRxcuis, interactionPair.description))
                                }
                            }

                            resolve(res.distinctBy { interactionResult -> interactionResult.interaction })
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.sideEffects("54092-381").whenComplete { result: Promise.Result<List<SideEffectResult>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun sideEffects(ndcId: String): Promise<List<SideEffectResult>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/event.json?search=patient.drug.openfda.product_ndc:\"${ndcId}\"&count=patient.reaction.reactionmeddrapt.exact"

                Log.i("here", url)

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val adverseEffectResults = gson.fromJson(jsonString, OpenFDAAdverseEffectsAggregateResponse::class.java)

                            if(adverseEffectResults.error != null) return resolve(listOf())

                            var res: List<SideEffectResult> = listOf()

                            val termObjects = adverseEffectResults.results

                            if(termObjects == null || termObjects.isEmpty()) return resolve(res)

                            val totalCount = termObjects.fold(0) {acc, it -> acc + it.count}.toFloat()

                            res = termObjects.fold(listOf<SideEffectResult>()) {acc, it -> acc.plus(SideEffectResult(it.term, it.count, it.count.toFloat() / totalCount))}

                            resolve(res)
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.description("54092-381").whenComplete { result: Promise.Result<String, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun description(ndcId: String): Promise<String, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/label.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val labelResult = gson.fromJson(jsonString, OpenFDALabelResponse::class.java)

                            if(labelResult.error != null) return resolve("")

                            val label = labelResult.results?.firstOrNull()

                            label ?: return resolve("")

                            val description = label.description?.firstOrNull()

                            description ?: return resolve("")

                            resolve(description.removePrefix("11 DESCRIPTION "))
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.warning("54092-381").whenComplete { result: Promise.Result<String, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun warning(ndcId: String): Promise<String, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/label.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val labelResult = gson.fromJson(jsonString, OpenFDALabelResponse::class.java)

                            if(labelResult.error != null) return resolve("")

                            val label = labelResult.results?.firstOrNull()

                            label ?: return resolve("")

                            val warningsAndCautions = label.warnings_and_cautions?.firstOrNull()
                            val warnings = label.warnings?.firstOrNull()
                            val boxedWarnings = label.boxed_warning?.firstOrNull()

                            if(warningsAndCautions != null) {
                                resolve(
                                    warningsAndCautions
                                        .removePrefix("5 WARNINGS AND PRECAUTIONS ")
                                        .replace("(\\(5\\..\\))".toRegex(), "\n")
                                        .replace("(•)".toRegex(), "\n •")
                                )
                            }
                            else if(warnings != null) {
                                resolve(warnings.removePrefix("WARNINGS ").replace("(•)".toRegex(), "\n •"))
                            }
                            else if(boxedWarnings != null) {
                                resolve(boxedWarnings.replace("(•)".toRegex(), "\n •"))
                            }
                            else {
                                resolve("")
                            }
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.overdosage("54092-381").whenComplete { result: Promise.Result<String, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun overdosage(ndcId: String): Promise<String, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/label.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val labelResult = gson.fromJson(jsonString, OpenFDALabelResponse::class.java)

                            if(labelResult.error != null) return resolve("")

                            val label = labelResult.results?.firstOrNull()

                            label ?: return resolve("")

                            val overdosage = label.overdosage?.firstOrNull()

                            overdosage ?: return resolve("")

                            resolve(overdosage.removePrefix("10 OVERDOSAGE "))
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.recalls("54092-189").whenComplete { result: Promise.Result<RecallsResult, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun recalls(ndcId: String): Promise<RecallsResult, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/enforcement.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val recallsResult = gson.fromJson(jsonString, OpenFDARecallsResponse::class.java)

                            if(recallsResult.error != null) return resolve(RecallsResult(false, false, listOf()))

                            val recalls = recallsResult.results

                            if(recalls == null || recalls.isEmpty()) return resolve(RecallsResult(false, false, listOf()))

                            var mandated = false

                            val recallQuantitiesList = recalls.fold(listOf<String>()) {acc, it ->
                                if(it.voluntary_mandated.contains("Mandated")) mandated = true
                                acc.plus(it.product_quantity)
                            }

                            resolve(RecallsResult(recallQuantitiesList.any(), mandated, recallQuantitiesList))
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.color("54092-189").whenComplete { result: Promise.Result<ColorResult, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun color(ndcId: String): Promise<ColorResult, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://rxnav.nlm.nih.gov/REST/ndcproperties.json?id=${ndcId}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val propertiesResponse = gson.fromJson(jsonString, RxNormPropertiesResponse::class.java)

                            propertiesResponse ?: return resolve(ColorResult(false, null))

                            val properties = propertiesResponse.ndcPropertyList?.ndcProperty?.firstOrNull()?.propertyConceptList?.propertyConcept

                            properties ?: return resolve(ColorResult(false, null))

                            val colorProperty = properties.filter { it.propName == "COLORTEXT" }.firstOrNull()

                            colorProperty ?: return resolve(ColorResult(false, null))

                            resolve(ColorResult(true, colorProperty.propValue))

//                            val colorHex = colorProperty.propValue.replace("(;.*)".toRegex(), "")
//
//                            // Get color name
//                            val url = "https://www.thecolorapi.com/id?hex=${colorHex}"
//
//                            val request = Request.Builder().url(url).build()
//
//                            onCancel {
//                                reject(RuntimeException("Canceled"))
//                            }
//
//                            client.newCall(request).enqueue(object : Callback {
//                                override fun onFailure(call: Call, e: IOException) {
//                                    e.printStackTrace()
//                                    resolve(ColorResult(true, "#${colorHex}", null))
//                                }
//
//                                    response.use {
//                                        if (!response.isSuccessful) {
//                                reject(RuntimeException("Failed"))
//                                throw IOException("Unexpected code $response")
//                            }
//
//                                        val jsonString = response.body!!.string()
//                                        val gson = Gson()
//                                        val colorResponse = gson.fromJson(jsonString, ColorAPIResponse::class.java)
//
//                                        colorResponse ?: return resolve(ColorResult(true, "#${colorHex}", null))
//
//                                        val name = colorResponse.name.value
//
//                                        resolve(ColorResult(true, "#${colorHex}", name))
//                                    }
//                                }
//                            })
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.shape("54092-189").whenComplete { result: Promise.Result<ShapeResult, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun shape(ndcId: String): Promise<ShapeResult, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://rxnav.nlm.nih.gov/REST/ndcproperties.json?id=${ndcId}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val propertiesResponse = gson.fromJson(jsonString, RxNormPropertiesResponse::class.java)

                            propertiesResponse ?: return resolve(ShapeResult(false, null))

                            val properties = propertiesResponse.ndcPropertyList?.ndcProperty?.firstOrNull()?.propertyConceptList?.propertyConcept

                            properties ?: return resolve(ShapeResult(false, null))

                            val shapeProperty = properties.filter { it.propName == "SHAPETEXT" }.firstOrNull()

                            shapeProperty ?: return resolve(ShapeResult(false, null))

                            resolve(ShapeResult(true, shapeProperty.propValue))
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.packageSizes("54092-189").whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun packageSizes(ndcId: String): Promise<List<String>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://rxnav.nlm.nih.gov/REST/ndcproperties.json?id=${ndcId}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val propertiesResponse = gson.fromJson(jsonString, RxNormPropertiesResponse::class.java)

                            propertiesResponse ?: return resolve(listOf())

                            val packageList = propertiesResponse.ndcPropertyList?.ndcProperty?.firstOrNull()?.packagingList?.packaging

                            packageList ?: return resolve(listOf())

                            resolve(packageList)
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.interactsWithAlcohol("68788-0285").whenComplete { result: Promise.Result<Boolean, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun interactsWithAlcohol(ndcId: String): Promise<Boolean, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/label.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val labelResult = gson.fromJson(jsonString, OpenFDALabelResponse::class.java)

                            if(labelResult.error != null) return resolve(false)

                            val label = labelResult.results?.firstOrNull()

                            label ?: return resolve(false)

                            val warningsAndCautions = label.warnings_and_cautions?.firstOrNull()
                            val warnings = label.warnings?.firstOrNull()
                            val boxedWarnings = label.boxed_warning?.firstOrNull()

                            if(warningsAndCautions != null) {
                                resolve(warningsAndCautions.toLowerCase(Locale.ENGLISH).contains(" alcohol "))
                            }
                            else if(warnings != null) {
                                resolve(warnings.toLowerCase(Locale.ENGLISH).contains(" alcohol "))
                            }
                            else if(boxedWarnings != null) {
                                resolve(boxedWarnings.toLowerCase(Locale.ENGLISH).contains(" alcohol "))
                            }
                            else {
                                resolve(false)
                            }
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.interactsWithCaffeine("68788-0285").whenComplete { result: Promise.Result<Boolean, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun interactsWithCaffeine(ndcId: String): Promise<Boolean, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/label.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                reject(RuntimeException("Failed"))
                                throw IOException("Unexpected code $response")
                            }

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val labelResult = gson.fromJson(jsonString, OpenFDALabelResponse::class.java)

                            if(labelResult.error != null) return resolve(false)

                            val label = labelResult.results?.firstOrNull()

                            label ?: return resolve(false)

                            val warningsAndCautions = label.warnings_and_cautions?.firstOrNull()
                            val warnings = label.warnings?.firstOrNull()
                            val boxedWarnings = label.boxed_warning?.firstOrNull()

                            if(warningsAndCautions != null) {
                                resolve(warningsAndCautions.toLowerCase(Locale.ENGLISH).contains(" caffeine "))
                            }
                            else if(warnings != null) {
                                resolve(warnings.toLowerCase(Locale.ENGLISH).contains(" caffeine "))
                            }
                            else if(boxedWarnings != null) {
                                resolve(boxedWarnings.toLowerCase(Locale.ENGLISH).contains(" caffeine "))
                            }
                            else {
                                resolve(false)
                            }
                        }
                    }
                })
            }
        }
    }
}

data class InteractionResult(
    var rxcuis: List<String>,
    var interaction: String
)

data class SideEffectResult(
    var sideEffect: String,
    var rawCount: Int,
    var percent: Float
)

data class RecallsResult(
    val hasBeenRecalled: Boolean,
    val anyMandatoryRecalls: Boolean,
    val recallQuantities: List<String>
)

data class ColorResult(
    val hasColor: Boolean,
    val colorName: String?
)


data class ShapeResult(
    val hasShape: Boolean,
    val shapeName: String?
)

