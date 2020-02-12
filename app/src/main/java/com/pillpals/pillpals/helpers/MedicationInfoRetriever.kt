package com.pillpals.pillpals.helpers

import android.util.Log
import com.google.gson.Gson
import com.pillpals.pillpals.ui.search.ActiveIngredient
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
        MedicationInfoRetriever.activeIngredients(2).whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
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
    }
}