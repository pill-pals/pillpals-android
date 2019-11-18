package com.pillpals.pillbuddies.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.getMedicationByUid
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_add_drug.*
import kotlinx.android.synthetic.main.bottom_options.*
import java.util.*

class AddDrugActivity : AppCompatActivity() {

    public lateinit var editText: EditText
    public lateinit var editText2: EditText
    public lateinit var editText3: EditText
    public lateinit var bottomOptions: BottomOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        setContentView(R.layout.activity_add_drug)
        editText = findViewById(R.id.editText)
        editText2 = findViewById(R.id.editText2)
        editText3 = findViewById(R.id.editText3)
        bottomOptions = findViewById(R.id.bottomOptions)
        bottomOptions.leftButton.text = "Save"
        bottomOptions.rightButton.text = "Cancel"

        if (intent.hasExtra("medication-uid")) {
            val medID: String = intent.getStringExtra("medication-uid")
            val medication = getMedicationByUid(medID) as Medications

            editText.setText(medication.name)
            editText2.setText(medication.dosage)
            editText3.setText(medication.notes)

            bottomOptions.leftButton.setOnClickListener{
                if (editText.text.toString().trim().isNotEmpty() and editText2.text.toString().trim().isNotEmpty()) {
                    updateMedicationData(
                        medication,
                        editText.text.toString(),
                        editText2.text.toString(),
                        editText3.text.toString()
                    )
                    finish()
                } else{
                    Toast.makeText(applicationContext, "Please set a name and dosage", Toast.LENGTH_SHORT).show()
                }
            }
        } else{
            bottomOptions.leftButton.setOnClickListener{
                if (editText.text.toString().trim().isNotEmpty() and editText2.text.toString().trim().isNotEmpty()) {
                    createMedicationData(
                        editText.text.toString(),
                        editText2.text.toString(),
                        editText3.text.toString()
                    )
                    finish()
                } else{
                    Toast.makeText(applicationContext, "Please set a name and dosage", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bottomOptions.rightButton.setOnClickListener{
            finish()
        }


    }

    private fun updateMedicationData(medication: Medications, drugName: String, drugDose: String, drugNote: String) {
        Realm.getDefaultInstance().executeTransaction {
            medication.name = drugName
            medication.dosage = drugDose
            medication.notes = drugNote
        }
    }

    private fun createMedicationData(drugName: String, drugDose: String, drugNote: String) {
        Realm.getDefaultInstance().executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = drugName
            medication.dosage = drugDose
            medication.notes = drugNote
        }
    }
}

