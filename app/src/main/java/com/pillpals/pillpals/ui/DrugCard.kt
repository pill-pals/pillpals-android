package com.pillpals.pillpals.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.*
import com.google.android.material.button.MaterialButton
import com.pillpals.pillpals.R
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
    public lateinit var logtimeLabel: TextView
    public lateinit var icon: ImageView
    public lateinit var iconBackground: CardView
    public lateinit var overflowMenu: ImageButton
    lateinit var loadingIcon: ProgressBar
    public var drugCode: Int = 0
    public var activeIngredients = listOf<String>()
    public var dosageString = ""
    public var ndcCode: String? = null
    public var rxcui: String? = null
    var splSetId: String? = null

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
        drugCard = findViewById(R.id.LogCard)
        altText  = findViewById(R.id.timeText)
        lateText  = findViewById(R.id.lateText)
        nameText  = findViewById(R.id.nameText)
        button  = findViewById(R.id.button)
        countdownLabel = findViewById(R.id.countdownLabel)
        logtimeLabel = findViewById(R.id.logtimeLabel)
        icon = findViewById(R.id.icon)
        iconBackground = findViewById(R.id.iconBackground)
        overflowMenu = findViewById(R.id.overflowMenu)
        loadingIcon = findViewById(R.id.loadingIcon)

        //Initialize elements
        loadingIcon.visibility = GONE
        button.visibility = GONE
        countdownLabel.visibility = GONE
        logtimeLabel.visibility = GONE
        lateText.visibility = GONE
    }
}
