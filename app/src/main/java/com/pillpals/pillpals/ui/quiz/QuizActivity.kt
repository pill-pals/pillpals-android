package com.pillpals.pillpals.ui.quiz

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pillpals.pillpals.R
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.children

class QuizActivity: AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    public lateinit var newStack: LinearLayout
    public lateinit var pausedStack: LinearLayout
    public lateinit var completedStack: LinearLayout
    public lateinit var pausedCollapseBtn: ImageButton
    public lateinit var completedCollapseBtn: ImageButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle("Quizzes")

        prefs = this.getPreferences(Context.MODE_PRIVATE)

        newStack = findViewById(R.id.newStack)
        pausedStack = findViewById(R.id.pausedStack)
        completedStack = findViewById(R.id.completedStack)
        pausedCollapseBtn = findViewById(R.id.pausedCollapseBtn)
        completedCollapseBtn = findViewById(R.id.completedCollapseBtn)

        setUpCollapsing()
    }

    private fun setUpCollapsing() {
        pausedCollapseBtn.setOnClickListener {
            toggleCollapse(pausedStack, pausedCollapseBtn)
        }
        if (prefs.getBoolean(getString(R.string.quiz_paused_stack_collapsed), false)) {
            pausedCollapseBtn.setImageResource(R.drawable.ic_circle_chevron_right_from_down)
        }
        pausedStack.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        pausedStack.layoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING)
        pausedStack.layoutTransition.enableTransitionType(LayoutTransition.APPEARING)

        completedCollapseBtn.setOnClickListener {
            toggleCollapse(completedStack, completedCollapseBtn)
        }
        if (prefs.getBoolean(getString(R.string.quiz_completed_stack_collapsed), true)) {
            completedCollapseBtn.setImageResource(R.drawable.ic_circle_chevron_right_from_down)
        }
        completedStack.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        completedStack.layoutTransition.enableTransitionType(LayoutTransition.DISAPPEARING)
        completedStack.layoutTransition.enableTransitionType(LayoutTransition.APPEARING)
    }

    private fun toggleCollapse(stack: LinearLayout, button: ImageButton) {
        var buttonChanged = false
        var previouslyCollapsed = false
        for (view in stack.children) {
            if (stack.indexOfChild(view) != 0) {
                previouslyCollapsed = (view.visibility == View.GONE)
                if (previouslyCollapsed) {
                    if (!buttonChanged) {
                        button.setImageResource(R.drawable.ic_circle_chevron_down_from_right)
                        (button.drawable as AnimatedVectorDrawable).start()
                        buttonChanged = true
                    }
                    view.visibility = View.VISIBLE
                } else {
                    if (!buttonChanged) {
                        button.setImageResource(R.drawable.ic_circle_chevron_right_from_down)
                        (button.drawable as AnimatedVectorDrawable).start()
                        buttonChanged = true
                    }
                    view.visibility = View.GONE
                }
            }
        }

        var prefKey = ""
        if (stack == pausedStack) {
            prefKey = getString(R.string.quiz_paused_stack_collapsed)
        } else { //stack == completedStack
            prefKey = getString(R.string.quiz_completed_stack_collapsed)
        }
        with (prefs.edit()) {
            //Set preference to collapse completed stack by default
            putBoolean(prefKey, !previouslyCollapsed)
            commit()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}