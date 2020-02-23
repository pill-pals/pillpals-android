package com.pillpals.pillpals.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.internal.NavigationMenu
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.Quizzes
import com.pillpals.pillpals.helpers.NotificationUtils
import com.pillpals.pillpals.helpers.QuizHelper
import com.pillpals.pillpals.ui.quiz.QuizActivity
import com.pillpals.pillpals.ui.quiz.QuizGenerator
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_search, R.id.navigation_medications, R.id.navigation_dashboard, R.id.navigation_statistics
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        QuizGenerator.tryGenerateQuiz()
        NotificationUtils.createNotificationChannel(this)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_profile -> {
            true
        }

        R.id.action_stars -> {
            val addIntent = Intent(this, QuizActivity::class.java)
            startActivity(addIntent)
            true
        }

        R.id.action_settings -> {
            val addIntent = Intent(this, SettingsActivity::class.java)
            startActivity(addIntent)
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.navigation_menu, menu)
        return true
        //return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val starIcon: MenuItem = menu!!.findItem(R.id.action_stars)
        val medList = readAllData(Medications::class.java) as RealmResults<out Medications>
        val quizList = readAllData(Quizzes::class.java) as RealmResults<out Quizzes>

        if(medList.all{it.dpd_object.isNullOrEmpty()} || quizList.any{ QuizHelper.getQuestionsAnswered(it) == 0 }){
            starIcon.setIcon(R.drawable.ic_navigation_stars_active)
        }else{
            starIcon.setIcon(R.drawable.ic_navigation_stars)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
        return Realm.getDefaultInstance().where(realmClass).findAll()
    }
}

