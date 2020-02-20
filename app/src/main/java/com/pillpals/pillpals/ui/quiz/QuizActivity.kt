package com.pillpals.pillpals.ui.quiz

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pillpals.pillpals.R
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.Questions
import com.pillpals.pillpals.data.model.Quizzes
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DateHelper
import com.pillpals.pillpals.helpers.QuizHelper
import com.pillpals.pillpals.ui.AddDrugActivity
import com.pillpals.pillpals.ui.QuizCard
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import java.util.*
import io.realm.RealmObject.deleteFromRealm
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.Button
import com.pillpals.pillpals.ui.statistics.MedicationScoresActivity


class QuizActivity: AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var realm: Realm

    public lateinit var newStack: LinearLayout
    public lateinit var pausedStack: LinearLayout
    public lateinit var completedStack: LinearLayout
    public lateinit var pausedCollapseBtn: ImageButton
    public lateinit var completedCollapseBtn: ImageButton
    public lateinit var medicationScoresButton: Button

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
        medicationScoresButton = findViewById(R.id.medicationScoresButton)

        medicationScoresButton.setOnClickListener {
            val intent = Intent(this, MedicationScoresActivity::class.java)
            startActivityForResult(intent, 1)
        }

        clearTestData()

        createTestData()

        setUpQuizCards((readAllData(Quizzes::class.java) as RealmResults<out Quizzes>).sort("date"))
        
        setUpCollapsing()

        hideEmptyStacks()
    }

    private fun setUpQuizCards(quizzes: RealmResults<out Quizzes>) {
        for (quiz in quizzes) {
            addQuizCard(quiz)
        }
    }

    private fun addQuizCard(quiz: Quizzes){
        var newCard = QuizCard(this)
        newCard.nameText.text = quiz.name
        val cal = Calendar.getInstance()
        cal.time = quiz.date
        newCard.timeText.text = cal.getDisplayName(
            Calendar.MONTH,
            Calendar.SHORT,
            Locale.US
        ) + " " + cal.get(Calendar.DAY_OF_MONTH).toString() + ", " + cal.get(Calendar.YEAR).toString()

        if (QuizHelper.getQuestionsAnswered(quiz) == 0){
            //new stack
            newCard.scoreText.text = "?"
            newCard.button.setOnClickListener {
                val intent = Intent(this, QuizQuestionActivity::class.java)
                intent.putExtra("quiz-uid", quiz.uid)
                startActivityForResult(intent, 1)
            }
            newStack.addView(newCard)
        }
        else if (QuizHelper.getQuestionsAnswered(quiz) == 10){
            //completed stack
            newCard.scoreText.text = QuizHelper.getQuizScore(quiz).toString()

            newCard.scoreBackground.setCardBackgroundColor(Color.parseColor(getColorStringByScore(QuizHelper.getQuizScore(quiz))))
            newCard.button.text = "View"
            newCard.button.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.colorDarkGrey, null))
            newCard.button.setOnClickListener {
                val intent = Intent(this, QuizResultsActivity::class.java)
                intent.putExtra("quiz-uid", quiz.uid)
                startActivityForResult(intent, 1)
            }
            if (prefs.getBoolean(getString(R.string.quiz_completed_stack_collapsed), false)) {
                newCard.visibility = View.GONE
            }

            completedStack.addView(newCard)
        }
        else {
            //paused stack
            newCard.scoreText.text = "Q" + QuizHelper.getQuestionsAnswered(quiz).toString()
            newCard.button.setOnClickListener {
                val intent = Intent(this, QuizQuestionActivity::class.java)
                intent.putExtra("quiz-uid", quiz.uid)
                startActivityForResult(intent, 1)
            }
            if (prefs.getBoolean(getString(R.string.quiz_paused_stack_collapsed), false)) {
                newCard.visibility = View.GONE
            }

            pausedStack.addView(newCard)
        }

    }

    private fun getColorStringByScore(score: Int):String {
        return when {
            score > 8 -> "#43A047"
            score > 7 -> "#7CB342"
            score > 6 -> "#C0CA33"
            score > 5 -> "#FDD835"
            score > 4 -> "#FFB300"
            score > 3 -> "#FB8C00"
            else -> "#F4511E"
        }
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

        setUpQuizCards((readAllData(Quizzes::class.java) as RealmResults<out Quizzes>).sort("date"))

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
                question.medication = Realm.getDefaultInstance().where(Medications::class.java).findAll().random()

                quiz.questions.add(question)
            }
        }
    }

    private fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
        return realm.where(realmClass).findAll()
    }

    private fun clearTestData() {
        realm.beginTransaction()
            var questions = readAllData(Questions::class.java) as RealmResults<out Questions>
            var quizzes = readAllData(Quizzes::class.java) as RealmResults<out Quizzes>

            questions.deleteAllFromRealm()
            quizzes.deleteAllFromRealm()
        realm.commitTransaction()
    }
}