package com.pillpals.pillpals.ui.quiz

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.pillpals.pillpals.ui.statistics.DataLogs
import com.pillpals.pillpals.ui.statistics.MissingLogs
import com.pillpals.pillpals.ui.statistics.TimeCount
import io.realm.Realm
import java.util.*
import kotlin.math.abs
import android.util.Log
import android.widget.Toast
import com.pillpals.pillpals.data.model.*
import com.pillpals.pillpals.helpers.DateHelper
import com.pillpals.pillpals.helpers.QuizHelper
import com.pillpals.pillpals.helpers.backgroundThreadToast
import io.realm.Sort
import io.realm.kotlin.createObject
import java.io.IOException

class QuizGenerator() {
    companion object {
        val realm = Realm.getDefaultInstance()
        var debounceSafeGenerate = false

        fun tryGenerateQuiz():Int {
            //Only generates a new quiz if no new quizzes exist and the last quiz was generated over 3 days ago
            Log.i("quiz", "tryGenerateQuiz() has been run")
            val allQuizzes = realm.where(Quizzes::class.java).findAll()
            val quizzesInTimeFrame = realm.where(Quizzes::class.java)
                .and()
                .isNotNull("date")
                .and()
                .greaterThanOrEqualTo("date", DateHelper.addUnitToDate(Date(),-3,Calendar.DATE))
                .findAll()

            var newQuizExists = false
            allQuizzes.forEach {
                if (QuizHelper.getQuestionsAnswered(it) == 0) newQuizExists = true
            }

            if (!newQuizExists && quizzesInTimeFrame.isEmpty()) {
                try{
                generateQuiz()
                }
                catch (e: IOException) {
                    Log.i("quiz", "Quiz generation failed")
                    return 4
                }
                Log.i("quiz", "New quiz generated")
                return 1
            } else if(newQuizExists){
                Log.i("quiz", "Quiz Available")
                return 2
            }else{
                Log.i("quiz", "Quiz generation not attempted")
                return 3
            }
        }

        fun asyncGenerateQuiz() {
            val handler = Handler(Looper.getMainLooper())
            val runnable = object: Runnable {
                override fun run() {
                    generateQuiz()
                }
            }
            handler.post(runnable)
        }

        fun generateQuiz() {
            realm.executeTransaction {
                var quiz = it.createObject(Quizzes::class.java, UUID.randomUUID().toString())
                quiz.date = Date()
                quiz.name = generateQuizName()

                var attemptedTemplates = mutableListOf<QuestionTemplates>()
                var selectedTemplates = mutableListOf<QuestionTemplates>()
                var generatedQuestions = mutableListOf<Questions>()

                var counter = 0
                while (counter <= 9) {
                    var template = getRandomTemplate(attemptedTemplates)
                    var question: Questions? = try {generateQuestion(template.id, getRandomMedication(template))} catch(e: IOException) {null}
                    Log.d("question",template.id.toString() + " - " + question.toString())
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
            return "Quiz " + quizzes.size.toString()
        }

        private fun getRandomTemplate(attemptedTemplates: MutableList<QuestionTemplates>):QuestionTemplates {
            var query = realm.where(QuestionTemplates::class.java)

            attemptedTemplates.forEach{
                query.notEqualTo("id", it.id)
            }
            var unattemptedTemplates = query.findAll()
            if (unattemptedTemplates.isEmpty()) throw IOException("Quiz Generation Failed: ran out of usable question templates")
            return unattemptedTemplates.random()
        }

        private fun getRandomMedication(template: QuestionTemplates):Medications? {
            if (template.notRelatedToMedication) return null
            var query = realm.where(Medications::class.java).and().equalTo("deleted",false)

            if(!template.canUseOnNonLinkedMedications) {
               query.and().isNotEmpty("dpd_object")
            }
            var usableMedications = query.findAll()
            if (usableMedications.isEmpty()) throw IOException("Quiz Generation Failed: no medications")
            return usableMedications.random()
        }
    }
}
