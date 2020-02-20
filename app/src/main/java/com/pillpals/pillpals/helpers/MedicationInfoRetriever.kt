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
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

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
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

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
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val interactionsResponse = gson.fromJson(jsonString, InteractionsResponse::class.java)

                            var res: List<InteractionResult> = listOf()

                            // Use first source I guess. Might specify to try for DrugBank later
                            val source = interactionsResponse.fullInteractionTypeGroup.firstOrNull()

                            source ?: return resolve(res)

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
    }
}

data class InteractionResult(
    var rxcuis: List<String>,
    var interaction: String
)

