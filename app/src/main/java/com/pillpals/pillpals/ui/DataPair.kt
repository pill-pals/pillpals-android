package com.pillpals.pillpals.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Schedules

class DataPair : LinearLayout {

    private val VIEW_CHANGED = true
    private val VIEW_NOT_CHANGED = false
    private val DEFAULT_COLOR = Color.DKGRAY

    public lateinit var key: TextView
    public lateinit var colon: TextView
    public lateinit var value: TextView
    public lateinit var drawableValue: ImageView

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
        inflate(this.context, R.layout.data_pair,this)

        //Get references to elements
        key = findViewById(R.id.key)
        colon = findViewById(R.id.colon)
        value = findViewById(R.id.value)
        drawableValue = findViewById(R.id.drawableValue)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
