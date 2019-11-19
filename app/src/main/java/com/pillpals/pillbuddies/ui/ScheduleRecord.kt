package com.pillpals.pillbuddies.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import android.widget.LinearLayout
import com.pillpals.pillbuddies.R

class ScheduleRecord : LinearLayout {

    private val VIEW_CHANGED = true
    private val VIEW_NOT_CHANGED = false
    private val DEFAULT_COLOR = Color.DKGRAY

    public lateinit var timeText: TextView
    public lateinit var recurrenceText: TextView
    public lateinit var dateText: TextView

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
        inflate(this.context, R.layout.schedule_record,this)

        //Get references to elements
        timeText = findViewById(R.id.timeText)
        recurrenceText = findViewById(R.id.recurrenceText)
        dateText = findViewById(R.id.dateText)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
