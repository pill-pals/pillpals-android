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

class QuizGenerator() {
    companion object {
        val realm = Realm.getDefaultInstance()

        fun generateQuiz() {
            realm.executeTransaction {
                var quiz = it.createObject(Quizzes::class.java, UUID.randomUUID().toString())
                quiz.date = DateHelper.today()
                quiz.name = generateQuizName()

                //get 10 question templates
                var selectedTemplates = mutableListOf<QuestionTemplates>()
                for (i in 0..9) {
                    selectedTemplates.add(getRandomTemplate())
                }

                //generate question from each template
                var generatedQuestions = mutableListOf<Questions>()
                selectedTemplates.forEach {
                    generatedQuestions.add(generateQuestion(it.id,getRandomMedication(it)))
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

        private fun getRandomTemplate():QuestionTemplates {
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