package com.pillpals.pillbuddies.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.TextView
import android.widget.LinearLayout
import com.pillpals.pillbuddies.R
import androidx.cardview.widget.CardView

class DosageTimeBox : LinearLayout {

    private val VIEW_CHANGED = true
    private val VIEW_NOT_CHANGED = false
    private val DEFAULT_COLOR = Color.GRAY

    public lateinit var timeCard: CardView
    public lateinit var timeText: TextView
    public lateinit var button: ImageButton

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
        inflate(this.context, R.layout.drug_card,this)

        //Get references to elements
        timeCard = findViewById(R.id.TimeBoxCard)
        timeText  = findViewById(R.id.timeText)
        button  = findViewById(R.id.timeBoxCloseButton)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
