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

data class OpenFDANameResultPackaging(
    val marketing_start_date: String,
    val package_ndc: String,
    val description: String,
    val sample: Boolean
)

data class OpenFDANameResultActiveIngredient(
    val strength: String,
    val name: String
)

data class OpenFDANameResultOpenFDAObject(
    val is_original_packager: List<Boolean>?,
    val spl_set_id: List<String>?,
    val upc: List<String>?,
    val manufacturer_name: List<String>?,
    val rxcui: List<String>?,
    val unii: List<String>?
)

data class OpenFDANameResult(
    val product_ndc: String,
    val generic_name: String,
    val labeler_name: String,
    val dea_schedule: String,
    val packaging: List<OpenFDANameResultPackaging>,
    val brand_name_suffix: String,
    val brand_name: String,
    val active_ingredients: List<OpenFDANameResultActiveIngredient>,
    val finished: Boolean,
    val openfda: OpenFDANameResultOpenFDAObject,
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

data class OpenFDANameResponse(
    val meta: OpenFDAMeta,
    val results: List<OpenFDANameResult>
)

data class OpenFDAAdverseEffectsAggregateResult(
    val term: String,
    val count: Int
)

data class OpenFDAAdverseEffectsAggregateResponse(
    val meta: OpenFDAMeta,
    val results: List<OpenFDAAdverseEffectsAggregateResult>
)

data class OpenFDALabelOpenFDAObject(
    val product_ndc: List<String>?,
    val is_original_packager: List<Boolean>?,
    val package_ndc: List<String>?,
    val generic_name: List<String>?,
    val spl_set_id: List<String>?,
    val upc: List<String>?,
    val brand_name: List<String>?,
    val manufacturer_name: List<String>?,
    val rxcui: List<String>?,
    val unii: List<String>?,
    val spl_id: List<String>?,
    val substance_name: List<String>?,
    val product_type: List<String>?,
    val route: List<String>?,
    val application_number: List<String>?
)

data class OpenFDALabelResult(
    val effective_time: String,
    val drug_interactions: List<String>,
    val recent_major_changes: List<String>,
    val geriatric_use: List<String>,
    val abuse: List<String>,
    val pharmacodynamics: List<String>,
    val description: List<String>,
    val nonclinical_toxicology: List<String>,
    val dosage_forms_and_strengths: List<String>,
    val storage_and_handling: List<String>,
    val mechanism_of_action: List<String>,
    val pharmacokinetics: List<String>,
    val indications_and_usage: List<String>,
    val dependence: List<String>,
    val set_id: String,
    val id: String,
    val description_table: List<String>,
    val pediatric_use: List<String>,
    val contraindications: List<String>,
    val drug_interactions_table: List<String>,
    val drug_abuse_and_dependence: List<String>,
    val pregnancy: List<String>,
    val spl_product_data_elements: List<String>,
    val boxed_warning: List<String>,
    val adverse_reactions_table: List<String>,
    val warnings_and_cautions: List<String>,
    val openfda: OpenFDALabelOpenFDAObject,
    val controlled_substance: List<String>,
    val version: String,
    val recent_major_changes_table: List<String>,
    val dosage_and_administration: List<String>,
    val adverse_reactions: List<String>,
    val animal_pharmacology_and_or_toxicology: List<String>,
    val spl_unclassified_section: List<String>,
    val use_in_specific_populations: List<String>,
    val how_supplied: List<String>,
    val information_for_patients: List<String>,
    val package_label_principal_display_panel: List<String>,
    val clinical_studies: List<String>,
    val spl_medguide: List<String>,
    val clinical_pharmacology: List<String>,
    val carcinogenesis_and_mutagenesis_and_impairment_of_fertility: List<String>,
    val spl_medguide_table: List<String>,
    val overdosage: List<String>,
    val instructions_for_use: List<String>
)

data class OpenFDALabelResponse(
    val meta: OpenFDAMeta,
    val results: List<OpenFDALabelResult>
)

data class OpenFDARecallsError(
    val code: String,
    val message: String
)

data class OpenFDARecallsResult(
    val country: String,
    val city: String,
    val reason_for_recall: String,
    val address_1: String,
    val address_2: String,
    val code_info: String,
    val product_quantity: String,
    val center_classification_date: String,
    val distribution_pattern: String,
    val state: String,
    val product_description: String,
    val report_date: String,
    val classification: String,
    val openfda: OpenFDALabelOpenFDAObject,
    val recall_number: String,
    val recalling_firm: String,
    val initial_firm_notification: String,
    val event_id: String,
    val product_type: String,
    val termination_date: String,
    val more_code_info: String?,
    val recall_initiation_date: String,
    val postal_code: String,
    val voluntary_mandated: String,
    val status: String
)

data class OpenFDARecallsResponse(
    val meta: OpenFDAMeta,
    val results: List<OpenFDARecallsResult>,
    val error: OpenFDARecallsError
)

