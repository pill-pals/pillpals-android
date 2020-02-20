package com.pillpals.pillpals.data


// Iteractions
data class InteractionsResponse(
    val nlmDisclaimer: String,
    val userInput: InteractionsUserInput,
    val fullInteractionTypeGroup: List<InteractionsFullInteractionTypeGroup>
)

data class InteractionsUserInput(
    val sources: List<String>,
    val rxcuis: List<String>
)

data class InteractionsFullInteractionTypeGroup(
    val sourceDisclaimer: String,
    val sourceName: String,
    val fullInteractionType: List<InteractionsFullInteractionType>
)

data class InteractionsFullInteractionTypeMinConcept(
    val rxcui: String,
    val name: String,
    val tty: String
)

data class InteractionsFullInteractionTypeInteractionConceptSourceConceptItem(
    val id: String,
    val name: String,
    val url: String
)

data class InteractionsFullInteractionTypeInteractionConcept(
    val minConceptItem: InteractionsFullInteractionTypeMinConcept,
    val sourceConceptItem: InteractionsFullInteractionTypeInteractionConceptSourceConceptItem
)

data class InteractionsFullInteractionTypeInteractionPair(
    val interactionConcept: List<InteractionsFullInteractionTypeInteractionConcept>,
    val description: String
)

data class InteractionsFullInteractionType(
    val comment: String,
    val minConcept: List<InteractionsFullInteractionTypeMinConcept>,
    val interactionPair: List<InteractionsFullInteractionTypeInteractionPair>
)


// mapi-us
data class Autocomplete(val query: String, val suggestions: MutableList<String>)


// DPD
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

data class DrugSchedule(
    val drug_code: Int,
    val schedule_name: String
)


// Open FDA
data class OpenFDAMetaResults(
    val skip: Int,
    val limit: Int,
    val total: Int
)

data class OpenFDAMeta(
    val disclaimer: String,
    val terms: String,
    val license: String,
    val last_updated: String,
    val results: OpenFDAMetaResults
)

data class OpenFDAResultPackaging(
    val marketing_start_date: String,
    val package_ndc: String,
    val description: String,
    val sample: Boolean
)

data class OpenFDAResultActiveIngredient(
    val strength: String,
    val name: String
)

data class OpenFDAResultOpenFDAObject(
    val is_original_packager: List<Boolean>?,
    val spl_set_id: List<String>?,
    val upc: List<String>?,
    val manufacturer_name: List<String>?,
    val rxcui: List<String>?,
    val unii: List<String>?
)

data class OpenFDAResult(
    val product_ndc: String,
    val generic_name: String,
    val labeler_name: String,
    val dea_schedule: String,
    val packaging: List<OpenFDAResultPackaging>,
    val brand_name_suffix: String,
    val brand_name: String,
    val active_ingredients: List<OpenFDAResultActiveIngredient>,
    val finished: Boolean,
    val openfda: OpenFDAResultOpenFDAObject,
    val listing_expiration_date: String,
    val marketing_category: String,
    val dosage_form: String,
    val spl_id: String,
    val route: List<String>,
    val marketing_start_date: String,
    val product_type: String,
    val product_id: String,
    val application_number: String,
    val brand_name_base: String,
    val pharm_class: List<String>
)

data class OpenFDAResponse(
    val meta: OpenFDAMeta,
    val results: List<OpenFDAResult>
)


