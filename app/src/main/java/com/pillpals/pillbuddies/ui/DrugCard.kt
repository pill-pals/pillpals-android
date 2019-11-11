package com.pillpals.pillbuddies.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.pillpals.pillbuddies.R

class DrugCard : LinearLayout {

    private val VIEW_CHANGED = true
    private val VIEW_NOT_CHANGED = false
    private val DEFAULT_COLOR = Color.DKGRAY

    public lateinit var medicationDueText: TextView
    public lateinit var medicationLateText: TextView
    public lateinit var medicationNameText: TextView
    public lateinit var medicationLogButton: MaterialButton

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
        medicationDueText  = findViewById(R.id.medicationDue)
        medicationLateText  = findViewById(R.id.medicationLate)
        medicationNameText  = findViewById(R.id.medicationName)
        medicationLogButton  = findViewById(R.id.logButton)

        //Initialize elements
        medicationLateText.setVisibility(GONE);
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}