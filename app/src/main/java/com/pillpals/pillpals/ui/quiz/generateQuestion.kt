package com.pillpals.pillpals.ui.quiz

import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.MoodLogs
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DateHelper
import com.pillpals.pillpals.helpers.StatsHelper
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

    when (id) {
        //----  Questions with a medication that requires a linked database drug  ----//
        //Placeholder
        1-> {

            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 1"
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
