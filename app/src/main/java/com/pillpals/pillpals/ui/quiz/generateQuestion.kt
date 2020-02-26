package com.pillpals.pillpals.ui.quiz

import android.util.Log
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.MoodLogs
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DateHelper
import com.pillpals.pillpals.helpers.MedicationInfoRetriever
import com.pillpals.pillpals.helpers.StatsHelper
import com.shopify.promises.Promise
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt


fun generateQuestion(id: Int, medication: Medications):Questions {
    val realm = Realm.getDefaultInstance()
    var question = Questions()
    question.medication = medication
    var correctAnswerString = ""
    var incorrectAnswers = mutableListOf<String>()

    var waitingForResponse = false

    when (id) {
        //----  Questions with a medication that requires a linked database drug  ----//
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
        //Placeholder
        2-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 2"
        }
        //Placeholder
        3-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 3"
        }
        //Placeholder
        4-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 4"
        }
        //Placeholder
        5-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 5"
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
        //Placeholder
        105-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 105"
        }
        //Placeholder
        106-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 105"
            throw IOException("question failed to generate")
        }

    }

    while(waitingForResponse) {
        Thread.sleep(50)
    }
    
    //if there are more than 3 incorrect answers, remove some
    incorrectAnswers.shuffle()
    incorrectAnswers = incorrectAnswers.take(3) as MutableList<String>

    //randomize Answers
    incorrectAnswers.add(correctAnswerString)
    incorrectAnswers.shuffle()
    for (i in 0..3) {
        question.answers.add(incorrectAnswers[i])
        if (correctAnswerString == incorrectAnswers[i]) {
            question.correctAnswer = i
        }
    }

    return question
}
