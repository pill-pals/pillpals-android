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

        populateAllStacks(8)
    }

    private fun populateAllStacks(n: Int) {
        val testCards = Array(n) { DrugCard(this) }

        for (i in testCards.indices) {
            testCards[i].medicationNameText.text = "Medication ${i + 1}"
            when(i % 3) {
                0 -> currentStack.addView(testCards[i])
                1 -> completedStack.addView(testCards[i])
                2 -> upcomingStack.addView(testCards[i])
            }
        }
    }
}
