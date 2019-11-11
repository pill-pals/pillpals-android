package com.pillpals.pillbuddies.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import io.realm.Realm
import com.pillpals.pillbuddies.R

class MainActivity : AppCompatActivity() {
    public lateinit var currentStack: LinearLayout
    public lateinit var upcomingStack: LinearLayout
    public lateinit var completedStack: LinearLayout
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        // Get a Realm instance for this thread
        realm = Realm.getDefaultInstance()


        setContentView(R.layout.activity_main)

        currentStack = findViewById(R.id.currentStack)
        upcomingStack = findViewById(R.id.upcomingStack)
        completedStack = findViewById(R.id.completedStack)

        //create the Box and added to the parent above
        val newCard = DrugCard(this)
        newCard.medicationNameText.text = "Adderall"
        currentStack.addView(newCard)

        val newCardTwo = DrugCard(this)

        currentStack.addView(newCardTwo)
    }
}
