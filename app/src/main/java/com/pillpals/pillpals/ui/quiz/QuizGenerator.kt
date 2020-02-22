package com.pillpals.pillpals.ui.quiz

import com.pillpals.pillpals.ui.statistics.DataLogs
import com.pillpals.pillpals.ui.statistics.MissingLogs
import com.pillpals.pillpals.ui.statistics.TimeCount
import io.realm.Realm
import java.util.*
import kotlin.math.abs
import android.util.Log
import com.pillpals.pillpals.data.model.*
import com.pillpals.pillpals.helpers.DateHelper
import io.realm.kotlin.createObject
import java.io.IOException

class QuizGenerator() {
    companion object {
        val realm = Realm.getDefaultInstance()
        var questions: MutableList<QuestionTemplates> = realm.where(QuestionTemplates::class.java).findAll() as MutableList<QuestionTemplates>

        fun generateQuiz() {
            realm.executeTransaction {
                var quiz = it.createObject(Quizzes::class.java, UUID.randomUUID().toString())
                quiz.date = DateHelper.today()
                quiz.name = generateQuizName()

                var attemptedTemplates = mutableListOf<QuestionTemplates>()
                var selectedTemplates = mutableListOf<QuestionTemplates>()
                var generatedQuestions = mutableListOf<Questions>()

                var counter = 0
                while (counter <= 9) {
                    var template = getRandomTemplate(attemptedTemplates)
                    var question: Questions? = try {generateQuestion(template.id,getRandomMedication(template))} catch(e: IOException) {null}
                    attemptedTemplates.add(template)

                    if (question != null) {
                        counter++
                        selectedTemplates.add(template)
                        generatedQuestions.add(question)
                    }
                }

                //create objects in realm for linking
                for (i in 0..9) {
                    var question = it.createObject(Questions::class.java, UUID.randomUUID().toString())
                    question.question = generatedQuestions[i].question
                    question.answers = generatedQuestions[i].answers
                    question.correctAnswer = generatedQuestions[i].correctAnswer
                    question.medication = generatedQuestions[i].medication

                    selectedTemplates[i].questions.add(question)
                    quiz.questions.add(question)
                }
            }
        }

        private fun generateQuizName():String {
            //TODO: come up with algorithm for quiz names
            var quizzes = realm.where(Quizzes::class.java).findAll()
            return "Gen Quiz " + quizzes.size.toString()
        }

        private fun getRandomTemplate(attemptedTemplates: MutableList<QuestionTemplates>):QuestionTemplates {
            //TODO: rules on what templates to get (e.g. no repeats in same quiz)
            return realm.where(QuestionTemplates::class.java).findAll().random()
        }

        private fun getRandomMedication(template: QuestionTemplates):Medications {
            var query = realm.where(Medications::class.java).and().equalTo("deleted",false)

            if(!template.canUseOnNonLinkedMedications) {
               query.and().isNotEmpty("dpd_object")
            }

            return query.findAll().random()
        }
    }
}