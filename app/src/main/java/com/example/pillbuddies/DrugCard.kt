package com.example.pillbuddies

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.TextView
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton

class DrugCard : LinearLayout {

    private val VIEW_CHANGED = true
    private val VIEW_NOT_CHANGED = false
    private val DEFAULT_COLOR = Color.DKGRAY

    private lateinit var mPaint: Paint
    private lateinit var mRect: Rect

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
        inflate(getContext(), R.layout.drug_card,this);

        //Get references to text views
        var medicationDueText  = findViewById(R.id.medicationDue) as TextView;
        var medicationLateText  = findViewById(R.id.medicationLate) as TextView;
        var medicationNameText  = findViewById(R.id.medicationName) as TextView;
        var medicationLogButton  = findViewById(R.id.logButton) as MaterialButton;

        //Initially all views are gone
        medicationLateText.setVisibility(GONE);
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setViewBounds()
        canvas.drawRect(mRect, mPaint)
    }

    private fun setViewBounds() {
        mRect.left = originX + mPadding
        mRect.right = width - mPadding
        mRect.top = originY + mPadding
        mRect.bottom = height - mPadding
    }
}