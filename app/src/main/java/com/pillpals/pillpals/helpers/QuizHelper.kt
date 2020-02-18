package com.pillpals.pillpals.helpers

import com.pillpals.pillpals.ui.statistics.DataLogs
import com.pillpals.pillpals.ui.statistics.MissingLogs
import com.pillpals.pillpals.ui.statistics.TimeCount
import io.realm.Realm
import java.util.*
import kotlin.math.abs
import android.util.Log
import com.pillpals.pillpals.data.model.*

class QuizHelper {
    companion object {
       fun getQuizScore(quiz: Quizzes):Int {
           var score = 0
           quiz.questions.forEach {
               if (it.userAnswer == it.correctAnswer){
                   score += 1
               }
           }
           return score
       }
        fun getQuestionsAnswered(quiz: Quizzes):Int {
            var questionsAnswered = 0
            quiz.questions.forEach {
                if (it.userAnswer != null){
                    questionsAnswered += 1
                }
            }
            return questionsAnswered
        }
        fun getCorrectAnswerString(question: Questions):String {
            return if (question.correctAnswer == null) {
                "Missing Correct Answer"
            } else {
                question.answers[question.correctAnswer!!]!!
            }
        }
        fun getUserAnswerString(question: Questions):String {
            return if (question.userAnswer == null) {
                "Missing User Answer"
            } else {
                question.answers[question.userAnswer!!]!!
            }
        }
    }
}