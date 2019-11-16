package com.pillpals.pillbuddies.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.pillpals.pillbuddies.R
import androidx.cardview.widget.CardView

class DrugCard : LinearLayout {

    private val VIEW_CHANGED = true
    private val VIEW_NOT_CHANGED = false
    private val DEFAULT_COLOR = Color.DKGRAY

    public lateinit var drugCard: CardView
    public lateinit var nameText: TextView
    public lateinit var altText: TextView
    public lateinit var lateText: TextView
    public lateinit var button: MaterialButton
    public lateinit var countdownLabel: TextView
    public lateinit var icon: ImageView
    public lateinit var doneImage: ImageView

    companion object {
        private var mSquareColor: Int = 0
        private var mPadding = 0
        private val originX = 0
        private val originY = 0
    }

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
        inflate(getContext(), R.layout.drug_card,this)

        //Get references to elements
        drugCard = findViewById(R.id.LogCard)
        altText  = findViewById(R.id.altText)
        lateText  = findViewById(R.id.lateText)
        nameText  = findViewById(R.id.nameText)
        button  = findViewById(R.id.button)
        countdownLabel = findViewById(R.id.countdownLabel)
        icon = findViewById(R.id.icon)
        doneImage = findViewById(R.id.doneImage)

        //Initialize elements
        button.visibility = GONE
        doneImage.visibility = GONE
        countdownLabel.visibility = GONE
        lateText.visibility = GONE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
