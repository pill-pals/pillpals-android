package com.pillpals.pillpals.ui.quiz

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
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
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.data.model.Quizzes
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DateHelper
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import java.util.*

class QuizActivity: AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var realm: Realm

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

        realm = Realm.getDefaultInstance()

        prefs = this.getPreferences(Context.MODE_PRIVATE)

        newStack = findViewById(R.id.newStack)
        pausedStack = findViewById(R.id.pausedStack)
        completedStack = findViewById(R.id.completedStack)
        pausedCollapseBtn = findViewById(R.id.pausedCollapseBtn)
        completedCollapseBtn = findViewById(R.id.completedCollapseBtn)

        createTestData()

        setUpQuizCards()
        
        setUpCollapsing()

        hideEmptyStacks()
    }

    private fun setUpQuizCards() {
        
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

    fun update() {
        pausedStack.layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING)
        completedStack.layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING)
        pausedStack.layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
        completedStack.layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
        pausedStack.layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
        completedStack.layoutTransition.disableTransitionType(LayoutTransition.CHANGING)
        newStack.removeViews(1, newStack.childCount - 1)
        pausedStack.removeViews(1, pausedStack.childCount - 1)
        completedStack.removeViews(1, completedStack.childCount - 1)
        
        setUpQuizCards()

        setUpCollapsing()

        hideEmptyStacks()
    }

    private fun hideEmptyStacks() {
        hideStackIfEmpty(newStack)
        hideStackIfEmpty(pausedStack)
        hideStackIfEmpty(completedStack)
    }

    private fun hideStackIfEmpty(stack: LinearLayout) {
        if (stack.childCount == 1) {
            stack.visibility = View.GONE
        } else {
            stack.visibility = View.VISIBLE
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

    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        update()
    }

    private fun createTestData() {
        var quizzes = readAllData(Quizzes::class.java) as RealmResults<Quizzes>
        if (quizzes.count() == 0) {
            createTestQuizData()
        }
    }

    private fun createTestQuizData() {
        val quizzes = Array(3){ Quizzes() }
        val names = listOf("Test Quiz 1", "Test Quiz 2", "Test Quiz 3")
        val dates = listOf(Date(), DateHelper.tomorrow(), DateHelper.addUnitToDate(Date(),2,Calendar.DATE))

        realm.executeTransaction {
            for (i in quizzes.indices) {
                quizzes[i] = it.createObject(Quizzes::class.java, UUID.randomUUID().toString())
                quizzes[i].name = names[i]
                quizzes[i].date = dates[i]
            }
        }

        val userAnswerList1 = listOf(null,null,null,null,null,null,null,null,null,null)
        val userAnswerList2 = listOf(1,2,1,1,1,2,null,null,null,null)
        val userAnswerList3 = listOf(1,2,1,1,1,2,1,1,1,2)

        createTestQuestions(quizzes[0],userAnswerList1)
        createTestQuestions(quizzes[1],userAnswerList2)
        createTestQuestions(quizzes[2],userAnswerList3)

    }

    private fun createTestQuestions(quiz: Quizzes, userAnswerList: List<Int?>){
        for (i in 0..9){
            realm.executeTransaction {
                val question = it.createObject(Questions::class.java, UUID.randomUUID().toString())
                question.question = "Question String"
                val answerList = RealmList<String>()
                answerList.add("Answer 1")
                answerList.add("Answer 2")
                answerList.add("Answer 3")
                answerList.add("Answer 4")
                question.answers = answerList
                question.correctAnswer = 1
                question.userAnswer = userAnswerList[i]
                question.medication = Realm.getDefaultInstance().where(Medications::class.java).findAll().random() as RealmResults<Medications>
                question.quiz = quiz as RealmResults<Quizzes>
            }
        }
    }

    private fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
        return realm.where(realmClass).findAll()
    }
}