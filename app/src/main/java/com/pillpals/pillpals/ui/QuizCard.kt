package com.pillpals.pillpals.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.pillpals.pillpals.R
import androidx.cardview.widget.CardView

class QuizCard : LinearLayout {

    private val VIEW_CHANGED = true
    private val VIEW_NOT_CHANGED = false
    private val DEFAULT_COLOR = Color.DKGRAY

    public lateinit var quizCard: CardView
    public lateinit var nameText: TextView
    public lateinit var timeText: TextView
    public lateinit var scoreText: TextView
    public lateinit var button: MaterialButton
    public lateinit var scoreBackground: CardView

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        //Inflate xml resource, pass "this" as the parent, we use <merge> tag in xml to avoid
        //redundant parent, otherwise a LinearLayout will be added to this LinearLayout ending up
        //with two view groups
        inflate(this.context, R.layout.quiz_card,this)

        //Get references to elements
        quizCard = findViewById(R.id.QuizCard)
        timeText  = findViewById(R.id.timeText)
        scoreText  = findViewById(R.id.scoreText)
        nameText  = findViewById(R.id.nameText)
        button  = findViewById(R.id.button)
        scoreBackground = findViewById(R.id.scoreBackground)
    }
}
