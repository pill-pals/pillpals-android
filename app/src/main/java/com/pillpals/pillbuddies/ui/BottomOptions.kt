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
import androidx.constraintlayout.widget.ConstraintLayout

class BottomOptions : ConstraintLayout {

    public lateinit var leftButton: MaterialButton
    public lateinit var rightButton: MaterialButton


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
        inflate(getContext(), R.layout.bottom_options,this)

        leftButton = findViewById(R.id.leftButton)
        rightButton = findViewById(R.id.rightButton)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
