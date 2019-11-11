package com.example.pillbuddies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val parent = findViewById(R.id.currentStack) as LinearLayout
        //create the Box and added to the parent above
        val newCard = DrugCard(this)

        parent.addView(newCard)

        val newCardTwo = DrugCard(this)

        parent.addView(newCardTwo)
    }
}
