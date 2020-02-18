package com.pillpals.pillpals.ui.quiz

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.util.Calendar

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getMedicationByUid
import io.realm.Realm

import java.util.*
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DateHelper
import com.google.android.material.button.MaterialButton
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.data.model.Quizzes
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.convertByteArrayToBitmap
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getByteArrayById
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorIDByString
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getIconByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getIconIDByString
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getRandomIcon
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getRandomUniqueColorString
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getScheduleByUid
import com.pillpals.pillpals.helpers.QuizHelper
import com.pillpals.pillpals.helpers.calculateScheduleRecords
import com.pillpals.pillpals.ui.QuestionResult
import com.pillpals.pillpals.ui.QuizCard
import io.realm.RealmObject.deleteFromRealm
import kotlinx.android.synthetic.main.delete_prompt.view.*
import org.w3c.dom.Text

class QuizResultsActivity : AppCompatActivity() {

    lateinit var realm: Realm
    lateinit var quiz: Quizzes
    lateinit var quizCardStack: LinearLayout
    lateinit var resultsStack: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        realm = Realm.getDefaultInstance()

        setContentView(R.layout.activity_quiz_results)

        val quizUID = intent.getStringExtra("quiz-uid")
        quiz = DatabaseHelper.getQuizByUid(quizUID)!!

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(quiz.name + " Results")

        quizCardStack = findViewById(R.id.quizCardStack)
        resultsStack = findViewById(R.id.resultsStack)

        addQuizCard()
        createQuestionResults()
    }

    private fun createQuestionResults() {
        quiz.questions.forEachIndexed{ index,question ->
            var questionResult = QuestionResult(question,index,this)
            resultsStack.addView(questionResult)
        }
    }

    private fun addQuizCard() {
        var quizCard = QuizCard(this)
        quizCard.nameText.text = quiz.name
        val cal = Calendar.getInstance()
        cal.time = quiz.date
        quizCard.timeText.text = cal.getDisplayName(
            Calendar.MONTH,
            Calendar.SHORT,
            Locale.US
        ) + " " + cal.get(Calendar.DAY_OF_MONTH).toString() + ", " + cal.get(Calendar.YEAR).toString()
        quizCard.scoreText.text = QuizHelper.getQuizScore(quiz).toString()

        quizCard.scoreBackground.setCardBackgroundColor(Color.parseColor(getColorStringByScore(QuizHelper.getQuizScore(quiz))))
        quizCard.button.visibility = View.GONE

        quizCardStack.addView(quizCard)
    }
    
    private fun getColorStringByScore(score: Int):String {
        return when {
            score > 8 -> "#43A047"
            score > 7 -> "#7CB342"
            score > 6 -> "#C0CA33"
            score > 5 -> "#FDD835"
            score > 4 -> "#FFB300"
            score > 3 -> "#FB8C00"
            else -> "#F4511E"
        }
    }
}