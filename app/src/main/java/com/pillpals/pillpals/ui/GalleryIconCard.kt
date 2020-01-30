package com.pillpals.pillpals.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.pillpals.pillpals.R
import androidx.cardview.widget.CardView

class GalleryIconCard : CardView {

    public lateinit var card: CardView
    public lateinit var icon: CardView
    public lateinit var image: ImageView

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
        inflate(this.context, R.layout.gallery_icon_card, this)

        //Get references to elements
        card = findViewById(R.id.card)
        icon = findViewById(R.id.icon)
        image = findViewById(R.id.image)
    }
}
