package com.pillpals.pillpals.ui.quiz

import android.util.Log
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.MoodLogs
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.helpers.*
import com.shopify.promises.Promise
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt


fun generateQuestion(id: Int, medication: Medications?):Questions {
    val ALL_OF_THE_ABOVE = "All of the above"

    val realm = Realm.getDefaultInstance()
    var question = Questions()
    var correctAnswerString = ""
    var incorrectAnswers = mutableListOf<String>()

    var waitingForResponse = false
    var throwFromResponse = false

    if (id < 200) {
        question.medication = medication
        medication!!

        when (id) {
            //----  Questions with a medication that requires a linked database drug  ----//
            // Alcohol / Caffeine
            1-> {
                val dpd_object = medication.dpd_object?.firstOrNull()

                dpd_object ?: return question

                val bothString = "Alcohol and Caffeine"
                val justAlcString = "Just Alcohol"
                val justCafString = "Just Caffeine"
                val neitherString = "Neither Alcohol nor Caffeine"
                val questionString = "Which does ${medication.name} have some degree of interaction with?"

                val interactsWithAlcoholPromise = MedicationInfoRetriever.interactsWithAlcohol(dpd_object.ndc_id ?: "null")
                val interactsWithCaffeinePromise = MedicationInfoRetriever.interactsWithCaffeine(dpd_object.ndc_id ?: "null")

                var interactsWithAlcoholResponse = false
                var interactsWithCaffeineResponse = false

                waitingForResponse = true

                interactsWithAlcoholPromise.whenComplete { result: Promise.Result<Boolean, RuntimeException> ->
                    when (result) {
                        is Promise.Result.Success -> {
                            // Use result here
                            interactsWithAlcoholResponse = result.value
                        }
                        is Promise.Result.Error -> interactsWithAlcoholResponse = false
                    }
                    interactsWithCaffeinePromise.whenComplete { result: Promise.Result<Boolean, RuntimeException> ->
                        when (result) {
                            is Promise.Result.Success -> {
                                // Use result here
                                interactsWithCaffeineResponse = result.value
                            }
                            is Promise.Result.Error -> interactsWithCaffeineResponse = false
                        }

                        when (true) {
                            interactsWithAlcoholResponse && interactsWithCaffeineResponse -> {
                                correctAnswerString = bothString
                                incorrectAnswers.add(justAlcString)
                                incorrectAnswers.add(justCafString)
                                incorrectAnswers.add(neitherString)
                            }
                            interactsWithAlcoholResponse && !interactsWithCaffeineResponse -> {
                                correctAnswerString = justAlcString
                                incorrectAnswers.add(bothString)
                                incorrectAnswers.add(justCafString)
                                incorrectAnswers.add(neitherString)
                            }
                            !interactsWithAlcoholResponse && interactsWithCaffeineResponse -> {
                                correctAnswerString = justCafString
                                incorrectAnswers.add(bothString)
                                incorrectAnswers.add(justAlcString)
                                incorrectAnswers.add(neitherString)
                            }
                            !interactsWithAlcoholResponse && !interactsWithCaffeineResponse -> {
                                correctAnswerString = neitherString
                                incorrectAnswers.add(bothString)
                                incorrectAnswers.add(justAlcString)
                                incorrectAnswers.add(justCafString)
                            }
                        }

                        question.question = questionString

                        waitingForResponse = false
                    }
                }
            }
            //Most common side effect
            2-> {
                val dpd_object = medication.dpd_object?.firstOrNull()

                dpd_object ?: return question
                val qString = "What is the most common side effect of ${medication.name}?"

                val allIncorrect: List<String> = listOf("Death", "Suicidal thoughts", "Anxiety", "Nausea", "Vomiting", "Dizziness",
                    "Drowsiness", "Headache", "Insomnia", "Chest pain", "Asthma", "Loss of appetite", "Tremors", "Dry mouth", "Fever",
                    "Muscle pain", "Hair loss", "Soreness", "Swelling", "Dry skin", "Dyspnoea")

                val sideEffectsPromise = MedicationInfoRetriever.sideEffects(dpd_object.ndc_id ?: "null")

                var currentPercent = 0f
                var currentResult = ""

                waitingForResponse = true

                sideEffectsPromise.whenComplete { result: Promise.Result<List<SideEffectResult>, RuntimeException> ->
                    when (result) {
                        is Promise.Result.Success -> {
                            // Use result here
                            result.value.forEach{
                                if(it.percent >= currentPercent){
                                    currentPercent=it.percent
                                    currentResult=it.sideEffect.toLowerCase().capitalize()
                                }
                            }
                        }
                        is Promise.Result.Error -> throwFromResponse = true
                    }

                    correctAnswerString = currentResult

                    incorrectAnswers = allIncorrect.filter { it.toLowerCase() != currentResult.toLowerCase() }.shuffled().take(3) as MutableList<String>

                    question.question = qString

                    waitingForResponse = false
                }
            }
            //Intake method
            3-> {
                val dpd_object = medication.dpd_object?.firstOrNull()

                dpd_object ?: return question
                val qString = "Which of the following is an intake method of ${medication.name}?"

                val allIncorrect: List<String> = listOf("Oral", "Topical", "Intravenous", "Ophthalmic", "Intramuscular", "Dental",
                    "Inhalation", "Rectal", "Dialysis", "Disinfectant")

                val intakePromise = MedicationInfoRetriever.intakeRoutes(dpd_object.dpd_id)
                var resultList = mutableListOf<String>()
                waitingForResponse = true

                intakePromise.whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
                    when (result) {
                        is Promise.Result.Success -> {
                            // Use result here
                            result.value.forEach{
                                resultList.add(it.replace("( .*)".toRegex(), ""))
                            }
                        }
                        is Promise.Result.Error -> throwFromResponse = true
                    }

                    correctAnswerString = resultList.random()

                    incorrectAnswers = allIncorrect.filter { !resultList.contains(it) }.shuffled().take(3) as MutableList<String>

                    question.question = qString

                    waitingForResponse = false
                }
            }
            //Color
            4-> {
                val dpd_object = medication.dpd_object?.firstOrNull()

                dpd_object ?: return question

                val listOfColours = listOf(
                    "Red",
                    "Green",
                    "Blue",
                    "White",
                    "Orange",
                    "Black",
                    "Purple",
                    "Yellow",
                    "Pink",
                    "Brown",
                    "Teal",
                    "Magenta"
                )

                val questionString = "Which color are your ${medication.name} medications?"

                val colorPromise = MedicationInfoRetriever.color(dpd_object.ndc_id ?: "null")

                var colorResponse: ColorResult? = null

                waitingForResponse = true

                colorPromise.whenComplete { result: Promise.Result<ColorResult, RuntimeException> ->
                    when (result) {
                        is Promise.Result.Success -> {
                            // Use result here
                            colorResponse = result.value
                        }
                        is Promise.Result.Error -> throwFromResponse = true
                    }

                    if(colorResponse == null) {
                        throwFromResponse = true
                    }
                    else {
                        var colorList = colorResponse!!.colorName?.split(",")

                        colorList = colorList?.fold(listOf<String>()) {acc, it ->
                            acc.plus(it.replace("(\\(.*)".toRegex(), "").toLowerCase().capitalize())
                        }

                        if(colorList == null) {
                            throwFromResponse = true
                        }
                        else {
                            correctAnswerString = colorList.random()
                            incorrectAnswers = listOfColours.filter { !colorList.contains(it) }.shuffled().take(3) as MutableList<String>

                            question.question = questionString

                            waitingForResponse = false
                        }
                    }
                }
            }
            //Shape
            5-> {
                val dpd_object = medication.dpd_object?.firstOrNull()

                dpd_object ?: return question

                val listOfShapes = listOf(
                    "Capsule",
                    "Tablet",
                    "Liquid",
                    "Powder",
                    "Aerosol",
                    "Syrup",
                    "Oval"
                )

                val questionString = "Which form does your ${medication.name} take?"

                val shapePromise = MedicationInfoRetriever.shape(dpd_object.ndc_id ?: "null")

                var shapeResponse: ShapeResult? = null

                waitingForResponse = true

                shapePromise.whenComplete { result: Promise.Result<ShapeResult, RuntimeException> ->
                    when (result) {
                        is Promise.Result.Success -> {
                            // Use result here
                            shapeResponse = result.value
                        }
                        is Promise.Result.Error -> throwFromResponse = true
                    }

                    if(shapeResponse == null) {
                        throwFromResponse = true
                    }
                    else {
                        var shape = shapeResponse!!.shapeName

                        if(shape == null) {
                            throwFromResponse = true
                        }
                        else {
                            correctAnswerString = shape.toLowerCase().capitalize()
                            incorrectAnswers = listOfShapes.filter { it != correctAnswerString }.shuffled().take(3) as MutableList<String>

                            question.question = questionString

                            waitingForResponse = false
                        }
                    }
                }
            }
            //Classification
            6-> {
                val dpd_object = medication.dpd_object?.firstOrNull()

                dpd_object ?: return question
                val qString = "What is the Controlled Drug and Substances Act schedule classification of ${medication.name}?"

                val allIncorrect: List<String> = listOf("Prescription", "Over The Counter (OTC)", "Homeopathic", "Narcotic (CDSA I)",
                    "Schedule G (CDSA IV)", "Ethical", "Targeted (CDSA IV)", "Schedule D", "Narcotic", "Schedule G (CDSA III)",
                    "Schedule C", "Narcotic (CDSA II)", "Unclassified")

                val schedulePromise = MedicationInfoRetriever.drugSchedules(dpd_object.dpd_id)
                var resultList = mutableListOf<String>()
                waitingForResponse = true

                schedulePromise.whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
                    when (result) {
                        is Promise.Result.Success -> {
                            // Use result here
                            result.value.forEach{
                                if(it == ""){
                                    resultList.add("Unclassified")
                                }else {
                                    resultList.add(it)
                                }
                            }
                        }
                        is Promise.Result.Error -> throwFromResponse = true
                    }

                    correctAnswerString = resultList.random()

                    incorrectAnswers = allIncorrect.filter { !resultList.contains(it) }.shuffled().take(3) as MutableList<String>

                    question.question = qString

                    waitingForResponse = false
                }
            }
            //Active ingredient
            7-> {
                val dpd_object = medication.dpd_object?.firstOrNull()

                dpd_object ?: return question
                val qString = "Which of the following is an active ingredient of ${medication.name}?"

                val allIncorrect: List<String> = listOf("Phenolate Sodium", "Menthol", "Isopropyl Alcohol",
                    "Magnesium (Magnesium Oxide)", "Salicylic Acid", "Nicotinamide", "Ginger", "Folic Acid",
                    "Iron (Ferrous Fumerate)", "Tin (Stannous Chloride)", "Lithium Carbonate", "Vitamin D (Cod Liver Oil)",
                    "Sodium Chloride", "Calcium (Calcium Citrate)", "Acetaminophen", "Ibuprofen", "Amylase", "Acetic Acid",
                    "Zinc Oxide", "Titanium Dioxide", "Caffeine", "Dextrose", "Codeine Phosphate", "Sodium Bicarbonate",
                    "Guaifenesin", "Citric Acid", "Gelatin", "Oxytocin", "Iodine", "Vitamin A", "Vitamin E", "Vitamin B2",
                    "Ascorbic Acid", "Ramipril", "Paracetamol", "Insulin", "Codeine", "Protease", "Lipase", "Water",
                    "Talc", "Ammonia", "Mineral Oil", "Camphor", "Glycerine", "Estrogen", "Progesterone", "Citalopram",
                    "Fluvoxamine", "Paroxetine", "Sertraline", "Mixed Salts Amphetamine", "Sodium", "Leucine", "Morphine Sulfate")

                val ingredientPromise = MedicationInfoRetriever.activeIngredients(dpd_object.dpd_id)
                var resultList = mutableListOf<String>()
                waitingForResponse = true

                ingredientPromise.whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
                    when (result) {
                        is Promise.Result.Success -> {
                            // Use result here
                            result.value.forEach{fullString ->
                                resultList.add(fullString.split(" ").joinToString(" ") { it.toLowerCase().capitalize() })
                            }
                        }
                        is Promise.Result.Error -> throwFromResponse = true
                    }

                    correctAnswerString = resultList.random()

                    incorrectAnswers = allIncorrect.filter { !resultList.contains(it) }.shuffled().take(3) as MutableList<String>

                    question.question = qString

                    waitingForResponse = false
                }
            }


            //----  Questions that can be used on any medication  ----//
            //Overall Adherence Score
            101-> {
                val timeCounts = StatsHelper.averageLogsAcrossSchedules(medication,realm,"Days")
                val adherenceScoreValue = StatsHelper.calculateAdherenceScore(timeCounts)
                if (adherenceScoreValue == -1f) throw IOException("Question $id failed to generate.")

                correctAnswerString = StatsHelper.getGradeStringFromTimeDifference(adherenceScoreValue)

                var allPossibleAnswers = mutableListOf("F","D","C","B","B+","A","A+")
                incorrectAnswers = allPossibleAnswers.filter{ it != correctAnswerString } as MutableList<String>

                question.question = "My current overall adherence score for ${medication.name} is:"
            }
            //Recent Adherence Score
            102-> {
                var timeCounts = StatsHelper.averageLogsAcrossSchedules(medication,realm,"Days")
                timeCounts = timeCounts.filter{it.time > DateHelper.addUnitToDate(
                    DateHelper.today(),-7,
                    Calendar.DATE) && it.time <= DateHelper.today()}
                val adherenceScoreValue = StatsHelper.calculateAdherenceScore(timeCounts)
                if (adherenceScoreValue == -1f) throw IOException("Question $id failed to generate.")

                correctAnswerString = StatsHelper.getGradeStringFromTimeDifference(adherenceScoreValue)

                var allPossibleAnswers = mutableListOf("F","D","C","B","B+","A","A+")
                incorrectAnswers = allPossibleAnswers.filter{ it != correctAnswerString } as MutableList<String>

                question.question = "My recent adherence score (last 7 days) for ${medication.name} is:"
            }
            //Overall Mood
            103-> {
                val allMoodLogs = DatabaseHelper.readAllData(MoodLogs::class.java) as RealmResults<out MoodLogs>
                var relevantMoodLogs = allMoodLogs.filter{ DatabaseHelper.moodLogIsRelatedToMedication(it,medication) }
                val avgMood = StatsHelper.calculateMoodScore(relevantMoodLogs)

                if (avgMood == -1f) throw IOException("Question $id failed to generate.")

                correctAnswerString = mapMoodToString(avgMood.roundToInt())
                var allPossibleAnswers = mutableListOf("Very Bad","Bad","Good","Very Good")
                incorrectAnswers = allPossibleAnswers.filter{ it != correctAnswerString } as MutableList<String>

                question.question = "The average feelings I had logged about ${medication.name} is:"
            }
            //Recent Mood
            104-> {
                val allMoodLogs = DatabaseHelper.readAllData(MoodLogs::class.java) as RealmResults<out MoodLogs>
                var relevantMoodLogs = allMoodLogs.filter{ DatabaseHelper.moodLogIsRelatedToMedication(it,medication) }
                relevantMoodLogs = relevantMoodLogs.filter{it.date!! > DateHelper.addUnitToDate(DateHelper.today(),-7,Calendar.DATE) && it.date!! <= DateHelper.today()}
                val avgMood = StatsHelper.calculateMoodScore(relevantMoodLogs)

                if (avgMood == -1f) throw IOException("Question $id failed to generate.")

                correctAnswerString = mapMoodToString(avgMood.roundToInt())
                var allPossibleAnswers = mutableListOf("Very Bad","Bad","Good","Very Good")
                incorrectAnswers = allPossibleAnswers.filter{ it != correctAnswerString } as MutableList<String>

                question.question = "The recent (last 7 days) feelings I had logged about ${medication.name} is:"
            }
        }
    } else {
        when (id) {
            //----  General knowledge questions with no medication  ----//
            // What to do if drug is recalled
            201 -> {
                //https://www.webmd.com/a-to-z-guides/what-is-a-drug-recall
                question.question = "Which of these should you do if a drug you're taking is recalled?"
                correctAnswerString = "Talk to your pharmacist/doctor"
                incorrectAnswers = mutableListOf(
                    "Flush the remaining medication down the toilet",
                    "Continue taking the remaining medication",
                    "Resell the remaining medication"
                )
            }
            // Causes of drug recall
            202 -> {
                //https://www.webmd.com/a-to-z-guides/what-is-a-drug-recall
                //https://www.ncbi.nlm.nih.gov/pubmed/26843501
                question.question = "Which of these is a possible reason for a drug to be recalled?"
                correctAnswerString = ALL_OF_THE_ABOVE
                incorrectAnswers = mutableListOf(
                    "Mislabelling",
                    "Packaging error",
                    "Contamination",
                    "Incorrect potency",
                    "Defective product",
                    "Unanticipated health risk"
                )
            }
            // What is a drug according to Canada Drug and Food Act
            203 -> {
                //https://datac.ca/understanding-drug-schedules/
                question.question = "According to the National Food and Drugs Act, what is NOT considered a drug?"
                val correctAnswerStrings = mutableListOf("Vitamins", "Nutritional supplements", "Illegal narcotics", "Energy drinks")
                correctAnswerString = correctAnswerStrings.random()
                incorrectAnswers = mutableListOf("Biologically-derived products like vaccines", "Tissues and organs", "Disinfectants", "Prescription pharmaceuticals", "Non-prescription pharmaceuticals")
            }
            // What is unique to Canadian Drug Schedules
            204 -> {
                //https://datac.ca/understanding-drug-schedules/
                val scheduleType = listOf("Schedule I", "Schedule II", "Schedule III", "Unscheduled").random()
                var uniqueToScheduleI = mutableListOf("Require a prescription for sale", "Are the most strictly regulated drugs", "Are subject to provincial professional requirements on control")
                var uniqueToScheduleII = mutableListOf("Require only intervention from pharmacist at point of sale", "Only sometimes require a professional referral", "Do not require a prescription, but cannot be self-selected by patient")
                var uniqueToScheduleIII = mutableListOf("Are available on the shelf from a pharmacist", "Are primarily subject to local professional requirements on control")
                var uniqueToUnscheduled = mutableListOf("Can be sold at any retail outlet", "Do not require professional supervision")

                question.question = "According to Canada's National Drug Schedules, what is unique about $scheduleType drugs?"

                when (scheduleType) {
                    "Schedule I" -> {
                        correctAnswerString = uniqueToScheduleI.random()
                        incorrectAnswers.addAll(uniqueToScheduleII)
                        incorrectAnswers.addAll(uniqueToScheduleIII)
                        incorrectAnswers.addAll(uniqueToUnscheduled)
                    }
                    "Schedule II" -> {
                        correctAnswerString = uniqueToScheduleII.random()
                        incorrectAnswers.addAll(uniqueToScheduleI)
                        incorrectAnswers.addAll(uniqueToScheduleIII)
                        incorrectAnswers.addAll(uniqueToUnscheduled)
                    }
                    "Schedule III" -> {
                        correctAnswerString = uniqueToScheduleIII.random()
                        incorrectAnswers.addAll(uniqueToScheduleI)
                        incorrectAnswers.addAll(uniqueToScheduleII)
                        incorrectAnswers.addAll(uniqueToUnscheduled)
                    }
                    else -> {
                        correctAnswerString = uniqueToUnscheduled.random()
                        incorrectAnswers.addAll(uniqueToScheduleI)
                        incorrectAnswers.addAll(uniqueToScheduleII)
                        incorrectAnswers.addAll(uniqueToScheduleIII)
                    }
                }
            }
            // Adherence grades
            205 -> {
                var grades = mutableListOf("A+","A","B+","B","C","D","F")
                val grade = grades.random()
                question.question = "According to how PillPals grades adherence, getting the grade $grade means you took your medication within:"

                correctAnswerString = mapGradeToRange(grade)
                grades.remove(grade)
                incorrectAnswers = grades.map{ mapGradeToRange(it) } as MutableList<String>
            }
            // Consistently unable to take a dose
            206 -> {
                question.question = "What should you do if you're usually unable to take your prescription at a certain time?"
                correctAnswerString = "Talk to your pharmacist/doctor about adjusting the schedule"
                incorrectAnswers = mutableListOf(
                    "Always take that dose early or late",
                    "Just skip that dose",
                    "Take more medication at a different time to balance it",
                    "Stop taking the medication at all"
                )
            }
        }
    }



    while(waitingForResponse) {
        if(throwFromResponse) throw IOException("Question $id failed to generate.")
        Thread.sleep(50)
    }
    if(throwFromResponse) throw IOException("Question $id failed to generate.")

    if(incorrectAnswers.count() < 3) throw IOException("Question $id failed to generate.")

    //if there are more than 3 incorrect answers, remove some
    incorrectAnswers.shuffle()
    incorrectAnswers = incorrectAnswers.take(3) as MutableList<String>

    //randomize Answers
    if (correctAnswerString == ALL_OF_THE_ABOVE) {
        incorrectAnswers.shuffle()
        incorrectAnswers.add(correctAnswerString)
    } else {
        incorrectAnswers.add(correctAnswerString)
        incorrectAnswers.shuffle()
    }

    for (i in 0..3) {
        question.answers.add(incorrectAnswers[i])
        if (correctAnswerString == incorrectAnswers[i]) {
            question.correctAnswer = i
        }
    }

    return question
}
