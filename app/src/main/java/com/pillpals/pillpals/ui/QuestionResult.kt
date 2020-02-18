package com.pillpals.pillpals.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.QuizHelper

class QuestionResult : LinearLayout {

    private val VIEW_CHANGED = true
    private val VIEW_NOT_CHANGED = false
    private val DEFAULT_COLOR = Color.DKGRAY

    public lateinit var question: Questions
    public var questionIndex: Int
    public lateinit var questionTitle: TextView
    public lateinit var questionText: TextView
    public lateinit var userAnswer: TextView
    public lateinit var correctAnswer: TextView
    public lateinit var userAnswerStack: LinearLayout
    public lateinit var icon: ImageView

    companion object {
        private var mSquareColor: Int = 0
        private var mPadding = 0
        private val originX = 0
        private val originY = 0
    }

    constructor(question: Questions, questionIndex: Int, context: Context) : super(context) {
        this.question = question
        this.questionIndex = questionIndex
        init()
    }

    constructor(question: Questions, questionIndex: Int,context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.question = question
        this.questionIndex = questionIndex
        init()
    }

    constructor(question: Questions, questionIndex: Int,context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.question = question
        this.questionIndex = questionIndex
        init()
    }

    private fun init() {
        //Inflate xml resource, pass "this" as the parent, we use <merge> tag in xml to avoid
        //redundant parent, otherwise a LinearLayout will be added to this LinearLayout ending up
        //with two view groups
        inflate(this.context, R.layout.question_result,this)

        //Get references to elements
        questionTitle = findViewById(R.id.questionTitle)
        questionText = findViewById(R.id.questionText)
        userAnswer = findViewById(R.id.userAnswer)
        correctAnswer = findViewById(R.id.correctAnswer)
        userAnswerStack = findViewById(R.id.userAnswerStack)
        icon = findViewById(R.id.icon)

        questionTitle.text = "Question" + (questionIndex + 1).toString()
        questionText.text = question.question
        userAnswer.text = QuizHelper.getUserAnswerString(question)
        correctAnswer.text = QuizHelper.getCorrectAnswerString(question)

        if (question.userAnswer == question.correctAnswer) {
            icon.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorGreen, null))
            userAnswerStack.visibility = View.GONE
        } else {
            icon.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorRed, null))
            icon.setImageDrawable(
                ContextCompat.getDrawable(context,
                    context.getResources()
                        .getIdentifier("drawable/highlight_off_24px", null, context.packageName)
                )
            )
        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
