package com.pillpals.pillpals.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.util.Calendar

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.pillpals.pillpals.R
import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getMedicationByUid
import io.realm.Realm

import java.util.*
import com.pillpals.pillpals.data.model.Schedules
import com.pillpals.pillpals.helpers.DateHelper
import com.google.android.material.button.MaterialButton
import com.pillpals.pillpals.data.model.DPDObjects
import com.pillpals.pillpals.helpers.DatabaseHelper
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.convertByteArrayToBitmap
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getByteArrayById
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorIDByString
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getColorStringByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getDPDObjectById
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getIconByID
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getIconIDByString
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getRandomIcon
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getRandomUniqueColorString
import com.pillpals.pillpals.helpers.DatabaseHelper.Companion.getScheduleByUid
import com.pillpals.pillpals.helpers.calculateScheduleRecords
import com.pillpals.pillpals.ui.search.SearchActivity
import io.realm.RealmObject.deleteFromRealm
import kotlinx.android.synthetic.main.delete_prompt.view.*

class AddDrugActivity : AppCompatActivity() {

    public lateinit var editText: EditText
    public lateinit var editText2: EditText
    public lateinit var editText3: EditText
    public lateinit var addScheduleButton : MaterialButton
    public lateinit var deleteButton: TextView
    lateinit var linkMedicationButton: TextView
    lateinit var unlinkMedicationButton: TextView
    public lateinit var scheduleStack: LinearLayout
    public lateinit var bottomOptions: BottomOptions
    public lateinit var iconButton: MaterialButton

    public var scheduleRecordsSetToDelete = mutableListOf<ScheduleRecord>()
    public lateinit var scheduleIdList: ArrayList<String>
    public val toBeAdded: MutableList<Schedules> = ArrayList()
    public lateinit var colorString: String
    public lateinit var imageDrawable: String
    public var photoBoolean = false

    private var dpdIdToLink: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)

        setContentView(R.layout.activity_add_drug)
        editText = findViewById(R.id.editText)
        editText2 = findViewById(R.id.editText2)
        editText3 = findViewById(R.id.editText3)
        addScheduleButton = findViewById(R.id.addScheduleButton)
        deleteButton = findViewById(R.id.deleteButton)
        linkMedicationButton = findViewById(R.id.linkMedicationButton)
        unlinkMedicationButton = findViewById(R.id.unlinkMedicationButton)
        scheduleStack = findViewById(R.id.scheduleStack)
        iconButton = findViewById(R.id.iconButton)
        bottomOptions = findViewById(R.id.bottomOptions)
        bottomOptions.leftButton.text = "Save"
        bottomOptions.rightButton.text = "Cancel"

        linkMedicationButton.visibility = View.GONE
        unlinkMedicationButton.visibility = View.GONE

        // region Bottom Button Listeners
        if (intent.hasExtra("medication-uid")) {
            val medID: String = intent.getStringExtra("medication-uid")
            val medication = getMedicationByUid(medID) as Medications

            if(medication.dpd_object?.firstOrNull() == null) {
                linkMedicationButton.visibility = View.VISIBLE
            }
            else {
                unlinkMedicationButton.visibility = View.VISIBLE
            }

            linkMedicationButton.setOnClickListener {
                val linkIntent = Intent(this, SearchActivity::class.java)
                linkIntent.putExtra("medication-uid", medication.uid)

                startActivityForResult(linkIntent, 3)
            }

            unlinkMedicationButton.setOnClickListener {
                startUnlinkDialog(medication)
            }

            editText.setText(medication.name)
            editText2.setText(medication.dosage)
            editText3.setText(medication.notes)
            photoBoolean = medication.photo_icon
            colorString = getColorStringByID(medication.color_id)
            iconButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(
                getColorStringByID(
                    medication.color_id
                )
            ))
            if (photoBoolean) {
                imageDrawable = medication.photo_uid
                iconButton.icon = BitmapDrawable(resources, Bitmap.createScaledBitmap(convertByteArrayToBitmap(getByteArrayById(imageDrawable)), 64, 64, false))
                iconButton.iconTint = null
            } else {
                imageDrawable = getIconByID(medication.icon_id)
                iconButton.icon = resources.getDrawable(DatabaseHelper.getDrawableIconById(this, medication.icon_id), theme)
                iconButton.iconTint = ColorStateList.valueOf(Color.parseColor("#0F0F0F"))
            }

            addScheduleRecords(calculateScheduleRecords(medication.schedules, this, scheduleRecordsSetToDelete))

            //region Bottom button listeners
            bottomOptions.leftButton.setOnClickListener{
                if (editText.text.toString().trim().isNotEmpty() and editText2.text.toString().trim().isNotEmpty()) {
                    if(scheduleRecordsSetToDelete.count() > 0) {
                        val deleteDialog = LayoutInflater.from(this).inflate(R.layout.delete_schedules_prompt, null)

                        val title = SpannableString("Delete Schedules")
                        title.setSpan(
                            ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null)),
                            0,
                            title.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val dialogBuilder = AlertDialog.Builder(this)
                            .setView(deleteDialog)
                            .setTitle(title)

                        val deleteSchedules = deleteDialog!!.findViewById<TextView>(R.id.deleteSchedules)
                        val scheduleTexts = scheduleRecordsSetToDelete.map {
                           "${it.timeText.text} ${it.recurrenceText.text} ${it.dateText.text}"
                        }

                        val whiteScheduleTexts = SpannableString(scheduleTexts.joinToString(separator = "\n"))
                        whiteScheduleTexts.setSpan(
                            ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null)),
                            0,
                            whiteScheduleTexts.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        deleteSchedules.text = whiteScheduleTexts

                        val deleteAlertDialog = dialogBuilder.show()
                        deleteDialog.dialogConfirmBtn.setOnClickListener {
                            deleteAlertDialog.dismiss()
                            val schedules = scheduleRecordsSetToDelete.flatMap { it.schedules }
                            DatabaseHelper.deleteSchedules(schedules)
                            updateMedicationData(
                                medication,
                                editText.text.toString(),
                                editText2.text.toString(),
                                editText3.text.toString()
                            )
                            finish()
                        }

                        deleteDialog.dialogCancelBtn.setOnClickListener {
                            deleteAlertDialog.dismiss()
                        }
                    }
                    else {
                        updateMedicationData(
                            medication,
                            editText.text.toString(),
                            editText2.text.toString(),
                            editText3.text.toString()
                        )
                        finish()
                    }
                } else{
                    Toast.makeText(applicationContext, "Please set a name and dosage", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Can't be black, otherwise tries to get an unused colour if possible
            colorString = intent.getStringExtra("color-string") ?: getRandomUniqueColorString()
            imageDrawable = intent.getStringExtra("image-string") ?: getRandomIcon()
            editText.setText(intent.getStringExtra("name-string") ?: "")
            editText2.setText(intent.getStringExtra("dosage-string") ?: "")

            iconButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorString))
            iconButton.icon = resources.getDrawable(DatabaseHelper.getDrawableIconById(this, getIconIDByString(imageDrawable)), theme)



            if(dpdIdToLink == null || dpdIdToLink == 0) {

                linkMedicationButton.visibility = View.VISIBLE
            }
            else {

            }

            linkMedicationButton.setOnClickListener {
                val linkIntent = Intent(this, SearchActivity::class.java)

                startActivityForResult(linkIntent, 4)
            }

            unlinkMedicationButton.setOnClickListener {
                startUnlinkDialog(null)
            }

            bottomOptions.leftButton.setOnClickListener{
                if (editText.text.toString().trim().isNotEmpty() and editText2.text.toString().trim().isNotEmpty()) {
                    if(scheduleRecordsSetToDelete.count() > 0) {
                        val deleteDialog = LayoutInflater.from(this).inflate(R.layout.delete_schedules_prompt, null)

                        val title = SpannableString("Delete Schedules")
                        title.setSpan(
                            ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null)),
                            0,
                            title.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val dialogBuilder = AlertDialog.Builder(this)
                            .setView(deleteDialog)
                            .setTitle(title)

                        val deleteSchedules = deleteDialog!!.findViewById<TextView>(R.id.deleteSchedules)
                        val scheduleTexts = scheduleRecordsSetToDelete.map {
                            "${it.dateText} ${it.recurrenceText} ${it.timeText}"
                        }

                        deleteSchedules.text = scheduleTexts.joinToString(separator = "\n")

                        val deleteAlertDialog = dialogBuilder.show()
                        deleteDialog.dialogConfirmBtn.setOnClickListener {
                            deleteAlertDialog.dismiss()
                            val schedules = scheduleRecordsSetToDelete.flatMap { it.schedules }
                            DatabaseHelper.deleteSchedules(schedules)
                            createMedicationData(
                                editText.text.toString(),
                                editText2.text.toString(),
                                editText3.text.toString()
                            )
                            finish()
                        }

                        deleteDialog.dialogCancelBtn.setOnClickListener {
                            deleteAlertDialog.dismiss()
                        }
                    }
                    else {
                        createMedicationData(
                            editText.text.toString(),
                            editText2.text.toString(),
                            editText3.text.toString()
                        )
                        finish()
                    }
                } else{
                    Toast.makeText(applicationContext, "Please set a name and dosage", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bottomOptions.rightButton.setOnClickListener{
            if(::scheduleIdList.isInitialized){
                val deleteDialog = LayoutInflater.from(this).inflate(R.layout.unsaved_schedules_prompt, null)

                val title = SpannableString("Discard Changes")
                title.setSpan(
                    ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null)),
                    0,
                    title.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val dialogBuilder = AlertDialog.Builder(this)
                    .setView(deleteDialog)
                    .setTitle(title)

                val deleteAlertDialog = dialogBuilder.show()
                deleteDialog.dialogConfirmBtn.setOnClickListener {
                    deleteAlertDialog.dismiss()
                    Realm.getDefaultInstance().executeTransaction {
                        toBeAdded.forEach {
                            deleteFromRealm(it)
                        }
                    }
                    finish()
                }

                deleteDialog.dialogCancelBtn.setOnClickListener {
                    deleteAlertDialog.dismiss()
                }
            } else{
                finish()
            }
        }

        // endregion

        // region Delete button
        if (intent.hasExtra("medication-uid")) {
            val medID: String = intent.getStringExtra("medication-uid")
            val medication = getMedicationByUid(medID) as Medications
            // Delete button
            deleteButton.setOnClickListener{
                val deleteDialog = LayoutInflater.from(this).inflate(R.layout.delete_prompt, null)

                val title = SpannableString("Delete " + medication.name)
                title.setSpan(
                    ForegroundColorSpan(this!!.resources.getColor(R.color.colorLightGrey)),
                    0,
                    title.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val dialogBuilder = AlertDialog.Builder(this!!)
                    .setView(deleteDialog)
                    .setTitle(title)

                val deleteDrugName = deleteDialog!!.findViewById<TextView>(R.id.deleteDrugName)
                deleteDrugName.text = medication.name

                val deleteAlertDialog = dialogBuilder.show()
                deleteDialog.dialogConfirmBtn.setOnClickListener {
                    deleteAlertDialog.dismiss()
                    deleteMedication(medication)
                    finish()
                }

                deleteDialog.dialogCancelBtn.setOnClickListener {
                    deleteAlertDialog.dismiss()
                }
            }
        }
        else {
            deleteButton.visibility = LinearLayout.GONE
        }
        //endregion

        //region Add schedule button
        addScheduleButton.setOnClickListener {
            val addIntent = Intent(this, EditScheduleActivity::class.java)
            if (intent.hasExtra("medication-uid")) {
                addIntent.putExtra("medication-uid", intent.getStringExtra("medication-uid"))
            }
            startActivityForResult(addIntent, 1)
        }
        //endregion

        iconButton.setOnClickListener {
            val addIntent = Intent(this, EditMedicationIcon::class.java)
            addIntent.putExtra("color-string", colorString)
            addIntent.putExtra("image-string", imageDrawable)
            if(intent.hasExtra("medication-uid")) {
                addIntent.putExtra("medication-uid", intent.getStringExtra("medication-uid"))
            }
            startActivityForResult(addIntent, 2)
        }
    }



    private fun updateMedicationData(medication: Medications, drugName: String, drugDose: String, drugNote: String) {
        Realm.getDefaultInstance().executeTransaction {
            medication.name = drugName
            medication.dosage = drugDose
            medication.notes = drugNote
            medication.photo_icon = photoBoolean
            medication.color_id = getColorIDByString(colorString)
            if(photoBoolean){
                medication.photo_uid = imageDrawable
            }else {
                medication.icon_id = getIconIDByString(imageDrawable)
            }

            if(::scheduleIdList.isInitialized){
                toBeAdded.forEach {
                    medication.schedules.add(it)
                }
            }
        }
    }

    private fun createMedicationData(drugName: String, drugDose: String, drugNote: String) {
        Realm.getDefaultInstance().executeTransaction {
            val medication = it.createObject(Medications::class.java, UUID.randomUUID().toString())
            medication.name = drugName
            medication.dosage = drugDose
            medication.notes = drugNote
            medication.photo_icon = photoBoolean
            medication.color_id = getColorIDByString(colorString)

            if(photoBoolean) {
                medication.photo_uid = imageDrawable
            } else {
                medication.icon_id = getIconIDByString(imageDrawable)
            }

            if(::scheduleIdList.isInitialized) {
                toBeAdded.forEach {
                    medication.schedules.add(it)
                }
            }

            if(medication.dpd_object?.firstOrNull() == null) {
                if (intent.hasExtra("dpd-id")) {
                    val dpdId = intent.getIntExtra("dpd-id", 0)
                    val dpdObject = DatabaseHelper.getDPDObjectById(dpdId)
                    dpdObject!!.medications.add(medication)
                } else if (dpdIdToLink != null && dpdIdToLink != 0) {
                    val dpdObject = DatabaseHelper.getDPDObjectById(dpdIdToLink as Int)
                    dpdObject!!.medications.add(medication)
                }
            }
        }
    }

    private fun deleteMedication(medication: Medications) {
        Realm.getDefaultInstance().executeTransaction {
            medication.deleted = true
        }
    }

    private fun addScheduleRecords(scheduleRecords: List<ScheduleRecord>) {
        scheduleRecords.forEach { record ->
            record.deleteScheduleImage.setOnClickListener {
                scheduleRecordsSetToDelete.add(record)
                updateScheduleList()
            }
            scheduleStack.addView(record)
        }
    }

    private fun unlinkMedication(dpdObject: DPDObjects, medication: Medications) {
        Realm.getDefaultInstance().executeTransaction {
            dpdObject.medications.remove(medication)
        }
    }

    private fun updateScheduleList() {
        scheduleStack.removeAllViews()
        if (intent.hasExtra("medication-uid")) {
            addScheduleRecords(calculateScheduleRecords(getMedicationByUid(intent.getStringExtra("medication-uid"))!!.schedules, this, scheduleRecordsSetToDelete))
        }
        addScheduleRecords(calculateScheduleRecords(toBeAdded, this, scheduleRecordsSetToDelete))
    }

    private fun startUnlinkDialog(medication: Medications?) {
        var dpdObject:DPDObjects?
        if(medication == null) {
            if(dpdIdToLink == null || dpdIdToLink == 0) return
            dpdObject = getDPDObjectById(dpdIdToLink!!)
        }
        else {
            dpdObject = medication.dpd_object?.firstOrNull() ?: return
        }


        val unlinkDialog = LayoutInflater.from(this).inflate(R.layout.delete_schedules_prompt, null)

        unlinkDialog.findViewById<TextView>(R.id.deletePrompt).text = "Are you sure you want to unlink this medication?"
        val title = SpannableString("Unlink Medication")
        title.setSpan(
            ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null)),
            0,
            title.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(unlinkDialog)
            .setTitle(title)

        val whiteTextBox = unlinkDialog!!.findViewById<TextView>(R.id.deleteSchedules)

        val whiteText = SpannableString(dpdObject!!.name)
        whiteText.setSpan(
            ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null)),
            0,
            whiteText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        whiteTextBox.text = whiteText

        val unlinkAlertDialog = dialogBuilder.show()
        unlinkDialog.dialogConfirmBtn.setOnClickListener {
            unlinkAlertDialog.dismiss()
            if(medication != null) {
                unlinkMedication(dpdObject!!, medication)
            }
            else {
                dpdIdToLink = null
            }
            unlinkMedicationButton.visibility = View.GONE
            linkMedicationButton.visibility = View.VISIBLE
        }

        unlinkDialog.dialogCancelBtn.setOnClickListener {
            unlinkAlertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data != null) {
            if (requestCode == 1) { // Schedules
                if (data.hasExtra("schedule-id-list")) {
                    scheduleIdList = data.getStringArrayListExtra("schedule-id-list")
                    scheduleIdList.forEach {
                        toBeAdded.add(getScheduleByUid(it)!!)
                    }
                    updateScheduleList()
                }
            } else if (requestCode == 2) { // Icon
                if (data.getBooleanExtra("photo-boolean", false) == false) {
                    photoBoolean = false
                    if (data.hasExtra("color-string")) {
                        colorString = data.getStringExtra("color-string")!!
                        iconButton.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor(colorString))
                    }
                    if (data.hasExtra("image-string")) {
                        imageDrawable = data.getStringExtra("image-string")!!
                        iconButton.icon = resources.getDrawable(
                            DatabaseHelper.getDrawableIconById(
                                this,
                                getIconIDByString(imageDrawable)
                            ), theme
                        )
                        iconButton.iconTint =
                            ColorStateList.valueOf(Color.parseColor("#0F0F0F"))
                    }
                } else {
                    photoBoolean = true
                    if (data.hasExtra("color-string")) {
                        colorString = data.getStringExtra("color-string")!!
                        iconButton.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor(colorString))
                    }
                    if (data.hasExtra("image-string")) {
                        imageDrawable = data.getStringExtra("image-string")!!
                        iconButton.icon = BitmapDrawable(
                            resources,
                            Bitmap.createScaledBitmap(
                                convertByteArrayToBitmap(
                                    getByteArrayById(
                                        imageDrawable
                                    )
                                ), 64, 64, false
                            )
                        )
                        iconButton.iconTint = null
                    }
                }
            } else if (requestCode == 3) { // Link with existing medication
                val responseId = data.getIntExtra("dpd-id", 0)
                if(responseId != 0) {
                    linkMedicationButton.visibility = View.GONE
                    unlinkMedicationButton.visibility = View.VISIBLE
                }
            } else if (requestCode == 4) { // Link with non-existing medication
                dpdIdToLink = data.getIntExtra("dpd-id", 0)
                if(dpdIdToLink != null && dpdIdToLink != 0) {
                    linkMedicationButton.visibility = View.GONE
                    unlinkMedicationButton.visibility = View.VISIBLE
                }
            }
        }

    }
}