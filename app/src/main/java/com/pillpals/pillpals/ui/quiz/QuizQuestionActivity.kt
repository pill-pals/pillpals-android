package com.pillpals.pillpals.ui.quiz

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.util.Calendar

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import io.realm.RealmObject.deleteFromRealm
import kotlinx.android.synthetic.main.delete_prompt.view.*
import kotlinx.android.synthetic.main.gallery_icon_card.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask

class QuizQuestionActivity : AppCompatActivity() {

    lateinit var realm: Realm
    lateinit var quiz: Quizzes
    lateinit var icon: ImageView
    lateinit var iconBackground: CardView
    lateinit var drugName: TextView
    lateinit var questionTitle: TextView
    lateinit var questionText: TextView
    lateinit var answer1btn: ImageButton
    lateinit var answer2btn: ImageButton
    lateinit var answer3btn: ImageButton
    lateinit var answer4btn: ImageButton
    lateinit var answer1: TextView
    lateinit var answer2: TextView
    lateinit var answer3: TextView
    lateinit var answer4: TextView
    lateinit var answer1constraint: ConstraintLayout
    lateinit var answer2constraint: ConstraintLayout
    lateinit var answer3constraint: ConstraintLayout
    lateinit var answer4constraint: ConstraintLayout
    lateinit var scrollView: ScrollView
    private val fadeoutTime = 350.toLong()
    private val correctAnswerShowDelay = 500.toLong()
    private val fadeinTime = 350.toLong()
    private val flashTime = 850.toLong()

    var buttonOnClickEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        realm = Realm.getDefaultInstance()

        setContentView(R.layout.activity_quiz_question)

        val quizUID = intent.getStringExtra("quiz-uid")
        quiz = DatabaseHelper.getQuizByUid(quizUID)!!

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(quiz.name)

        icon = findViewById(R.id.icon)
        iconBackground = findViewById(R.id.iconBackground)
        drugName = findViewById(R.id.drugName)
        questionTitle = findViewById(R.id.questionTitle)
        questionText = findViewById(R.id.questionText)
        answer1btn = findViewById(R.id.answer1btn)
        answer2btn = findViewById(R.id.answer2btn)
        answer3btn = findViewById(R.id.answer3btn)
        answer4btn = findViewById(R.id.answer4btn)
        answer1 = findViewById(R.id.answer1)
        answer2 = findViewById(R.id.answer2)
        answer3 = findViewById(R.id.answer3)
        answer4 = findViewById(R.id.answer4)
        answer1constraint = findViewById(R.id.answer1constraint)
        answer2constraint = findViewById(R.id.answer2constraint)
        answer3constraint = findViewById(R.id.answer3constraint)
        answer4constraint = findViewById(R.id.answer4constraint)
        scrollView = findViewById(R.id.scrollView)

        setPageContentsForQuestion(QuizHelper.getQuestionsAnswered(quiz))
    }

    private fun setPageContentsForQuestion(index: Int){
        val question = quiz!!.questions[index]!!
        if (question.medication == null) {
            iconBackground.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            icon.setImageDrawable(ContextCompat.getDrawable(this, this.getResources()
                .getIdentifier("drawable/ic_pill_v5", null, this.packageName)))
            icon.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null))
            drugName.text = "Knowledge Question"
        } else {
            iconBackground.setCardBackgroundColor(Color.parseColor(getColorStringByID(question.medication!!.color_id)))
            icon.setImageDrawable(
                DatabaseHelper.getCorrectIconDrawable(
                    this,
                    question.medication!!
                )
            )
            icon.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorBlack, null))
            drugName.text = question.medication!!.name
        }
        questionTitle.text = "Question " + (index + 1).toString()
        questionText.text = question.question

        setUpButtons(question, index)

    }

    private fun answerQuestion(question: Questions, answer: Int) {
        realm.executeTransaction{
            question.userAnswer = answer
        }
    }

    private fun loadNextQuestion(index: Int){
        if(index == 9) {
            val intent = Intent(this, QuizResultsActivity::class.java)
            intent.putExtra("quiz-uid", quiz.uid)
            startActivityForResult(intent, 1)
            this.finish()
        } else {
            setPageContentsForQuestion(index + 1)
        }
    }

    private fun handleButtonOnClick(question: Questions, answer: Int, index: Int) {
        if (buttonOnClickEnabled) {
            var debounceTimer = Timer()
            debounceTimer.schedule(timerTask {
                buttonOnClickEnabled = true
            },fadeinTime+fadeoutTime*2+correctAnswerShowDelay+200)
            buttonOnClickEnabled = false

            answerQuestion(question,answer)
            animateViewOut(question,index)
            flashColor(question, answer)
        }
    }

    private fun flashColor(question: Questions, answer: Int) {
        if(question.correctAnswer == answer) {
            scrollView.setBackgroundColor(
                ResourcesCompat.getColor(
                    getResources(),
                    R.color.colorLightGreen,
                    null
                )
            )
        } else {
            scrollView.setBackgroundColor(
                ResourcesCompat.getColor(
                    getResources(),
                    R.color.colorLightRed,
                    null
                )
            )
        }
        questionTitle.animate().alpha(1f)
            .setDuration(flashTime)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    scrollView.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null))

                }
            })
    }

    private fun animateViewOut(question: Questions, index: Int) {
        questionText.animate().alpha(0f)
            .setDuration(fadeoutTime)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animateDelay(question, index)
                }
            })

        if(question.correctAnswer != 0) {
            answer1.animate().alpha(0f)
                .setDuration(fadeoutTime)
                .setListener(null)
            answer1constraint.animate().translationXBy(-900f)
                .setDuration(fadeoutTime)
                .setListener(null)
                .interpolator = DecelerateInterpolator()
        }
        if(question.correctAnswer != 1) {
            answer2.animate().alpha(0f)
                .setDuration(fadeoutTime)
                .setListener(null)
            answer2constraint.animate().translationXBy(900f)
                .setDuration(fadeoutTime)
                .setListener(null)
                .interpolator = DecelerateInterpolator()
        }
        if(question.correctAnswer != 2) {
            answer3.animate().alpha(0f)
                .setDuration(fadeoutTime)
                .setListener(null)
            answer3constraint.animate().translationXBy(-900f)
                .setDuration(fadeoutTime)
                .setListener(null)
                .interpolator = DecelerateInterpolator()
        }
        if(question.correctAnswer != 3) {
            answer4.animate().alpha(0f)
                .setDuration(fadeoutTime)
                .setListener(null)
            answer4constraint.animate().translationXBy(900f)
                .setDuration(fadeoutTime)
                .setListener(null)
                .interpolator = DecelerateInterpolator()
        }
    }

    private fun animateDelay(question: Questions, index: Int) {
        questionText.animate().alpha(0f)
            .setDuration(correctAnswerShowDelay)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animateCorrectAnswerOut(question,index)
                }
            })
    }

    private fun animateCorrectAnswerOut(question: Questions, index: Int) {
        questionText.animate().alpha(0f)
            .setDuration(fadeoutTime)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    loadNextQuestion(index)
                    animateViewIn()
                }
            })
        if(question.correctAnswer == 0) {
            answer1.animate().alpha(0f)
                .setDuration(fadeoutTime)
                .setListener(null)
            answer1constraint.animate().translationXBy(-900f)
                .setDuration(fadeoutTime)
                .setListener(null)
                .interpolator = DecelerateInterpolator()
        }
        if(question.correctAnswer == 1) {
            answer2.animate().alpha(0f)
                .setDuration(fadeoutTime)
                .setListener(null)
            answer2constraint.animate().translationXBy(900f)
                .setDuration(fadeoutTime)
                .setListener(null)
                .interpolator = DecelerateInterpolator()
        }
        if(question.correctAnswer == 2) {
            answer3.animate().alpha(0f)
                .setDuration(fadeoutTime)
                .setListener(null)
            answer3constraint.animate().translationXBy(-900f)
                .setDuration(fadeoutTime)
                .setListener(null)
                .interpolator = DecelerateInterpolator()
        }
        if(question.correctAnswer == 3) {
            answer4.animate().alpha(0f)
                .setDuration(fadeoutTime)
                .setListener(null)
            answer4constraint.animate().translationXBy(900f)
                .setDuration(fadeoutTime)
                .setListener(null)
                .interpolator = DecelerateInterpolator()
        }
    }

    private fun animateViewIn() {
        questionText.animate().alpha(1f)
            .setDuration(fadeinTime)
            .setListener(null)
        answer1.animate().alpha(1f)
            .setDuration(fadeinTime)
            .setListener(null)
        answer2.animate().alpha(1f)
            .setDuration(fadeinTime)
            .setListener(null)
        answer3.animate().alpha(1f)
            .setDuration(fadeinTime)
            .setListener(null)
        answer4.animate().alpha(1f)
            .setDuration(fadeinTime)
            .setListener(null)
        answer1constraint.animate().translationXBy(900f)
            .setDuration(fadeinTime)
            .setListener(null)
            .interpolator = DecelerateInterpolator()
        answer2constraint.animate().translationXBy(-900f)
            .setDuration(fadeinTime)
            .setListener(null)
            .interpolator = DecelerateInterpolator()
        answer3constraint.animate().translationXBy(900f)
            .setDuration(fadeinTime)
            .setListener(null)
            .interpolator = DecelerateInterpolator()
        answer4constraint.animate().translationXBy(-900f)
            .setDuration(fadeinTime)
            .setListener(null)
            .interpolator = DecelerateInterpolator()
    }

    private fun setUpButtons(question: Questions, index: Int) {
        answer1.text = question.answers[0]
        answer1btn.setOnClickListener{handleButtonOnClick(question,0,index)}
        answer2.text = question.answers[1]
        answer2btn.setOnClickListener{handleButtonOnClick(question,1,index)}
        answer3.text = question.answers[2]
        answer3btn.setOnClickListener{handleButtonOnClick(question,2,index)}
        answer4.text = question.answers[3]
        answer4btn.setOnClickListener{handleButtonOnClick(question,3,index)}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}