package com.pillpals.pillbuddies.ui.medications

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.data.model.Medications
import com.pillpals.pillbuddies.ui.AddDrugActivity
import com.pillpals.pillbuddies.ui.DrugCard
import io.realm.Realm
import kotlinx.android.synthetic.main.prompts.view.*
import java.util.*
import android.content.Intent
import android.graphics.Color
import kotlinx.android.synthetic.main.prompts.*
import android.util.Log
class MedicationsFragment : Fragment() {

    public lateinit var drugButton: Button
    public lateinit var stack: LinearLayout

    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater!!.inflate(R.layout.fragment_medications, container,false)

        realm = Realm.getDefaultInstance()

        stack = view!!.findViewById(R.id.stack)

        drugButton = view!!.findViewById(R.id.drugButton)

        drugButton.setOnClickListener {
            val intent = Intent(context, AddDrugActivity::class.java)
            startActivityForResult(intent, 1)
        }

        updateMedicationList()

        return view
    }

    private fun updateMedicationList() {
        stack.removeAllViews()
        for (drug in realm.where(Medications::class.java).findAll()) {
            if (drug.deleted) {
                continue
            }

            addDrugCard(realm.copyFromRealm(drug))
        }
    }

    private fun addDrugCard(medication: Medications) {
        var newCard = DrugCard(this.context!!)

        newCard.nameText.text = medication.name
        newCard.altText.text = medication.dosage
        newCard.iconBackground.setCardBackgroundColor(Color.parseColor(medication.color))

        newCard.button.setOnClickListener {
            val intent = Intent(context, AddDrugActivity::class.java)
            intent.putExtra("medication-uid", medication.uid)
            startActivityForResult(intent, 1)
        }
        newCard.button.text = "Edit"
        newCard.button.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        newCard.button.visibility = View.VISIBLE
        
        stack.addView(newCard)
    }

    private fun createMedicationData(drugName: String, drugDose: String) {
        realm.executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = drugName
            medication.dosage = drugDose
        }
        updateMedicationList()
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updateMedicationList()
    }
}