package com.pillpals.pillpals.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.pillpals.pillpals.R
import com.pillpals.pillpals.helpers.DatabaseHelper
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import io.realm.Realm
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.google.android.flexbox.FlexboxLayout
import com.pillpals.pillpals.data.model.Photos
import io.realm.RealmObject
import io.realm.RealmResults
import java.util.*


class EditMedicationIcon : AppCompatActivity() {

    public lateinit var colorLists : LinearLayout
    public lateinit var bottomOptions: BottomOptions
    public lateinit var lightColorList : LinearLayout
    public lateinit var mediumColorList : LinearLayout
    public lateinit var heavyColorList : LinearLayout
    public lateinit var shadesColorList : LinearLayout
    public lateinit var firstIconList: LinearLayout
    public lateinit var photoButton: Button
    public lateinit var photoList: FlexboxLayout
    var colorString = "#FFFFFF"
    var imageDrawable = "ic_pill_v5"
    var selectedPhoto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_edit_medication_icon)
        //val list: MutableList<String> = ArrayList()


        if(intent.hasExtra("color-string")) {
            colorString = intent.getStringExtra("color-string")
        }
        if(intent.hasExtra("image-string")) {
            imageDrawable = intent.getStringExtra("image-string")
        }
        
        colorLists = findViewById(R.id.colorLists)
        bottomOptions = findViewById(R.id.bottomOptions)
        bottomOptions.leftButton.text = "Apply"
        bottomOptions.rightButton.text = "Cancel"
        lightColorList = findViewById(R.id.lightColorList)
        mediumColorList = findViewById(R.id.mediumColorList)
        heavyColorList = findViewById(R.id.heavyColorList)
        shadesColorList = findViewById(R.id.shadesColorList)

        firstIconList = findViewById(R.id.firstIconList)
        photoList = findViewById(R.id.photoList)
        photoButton = findViewById(R.id.photoButton)

        photoList.removeAllViews()
        populateGallery(readAllData(Photos::class.java) as RealmResults<out Photos>)
        addBorderToCards()
        for (i in 0 until lightColorList.getChildCount()) {
            val borderCard = lightColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView

            card.setOnClickListener {
                colorString = String.format("#%06X", 0xFFFFFF and (it as CardView).cardBackgroundColor.defaultColor)
                addBorderToCards()
            }
        }
        for (i in 0 until mediumColorList.getChildCount()) {
            val borderCard = mediumColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView

            card.setOnClickListener {
                colorString = String.format("#%06X", 0xFFFFFF and (it as CardView).cardBackgroundColor.defaultColor)
                addBorderToCards()
            }
        }
        for (i in 0 until heavyColorList.getChildCount()) {
            val borderCard = heavyColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView

            card.setOnClickListener {
                colorString = String.format("#%06X", 0xFFFFFF and (it as CardView).cardBackgroundColor.defaultColor)
                addBorderToCards()
            }
        }
        for (i in 0 until shadesColorList.getChildCount()) {
            val borderCard = shadesColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView

            card.setOnClickListener {
                colorString = String.format("#%06X", 0xFFFFFF and (it as CardView).cardBackgroundColor.defaultColor)
                addBorderToCards()
            }
        }

        for (i in 0 until firstIconList.getChildCount()) {
            val borderCard = firstIconList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val image = card.getChildAt(0) as ImageView

            card.setOnClickListener {
                imageDrawable = image.tag as String
                addBorderToCards()
            }
        }

        photoButton.setOnClickListener {
            if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
                //val addIntent = Intent(this, DrugGallery::class.java)
                //startActivityForResult(addIntent, 1)

                val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA), 200)
                }
                //dispatchTakePictureIntent()

            }else{
                Toast.makeText(applicationContext, "Your device does not have a camera", Toast.LENGTH_SHORT).show()
            }
        }

        bottomOptions.leftButton.setOnClickListener{
            val resultIntent = Intent(this, EditMedicationIcon::class.java)
            resultIntent.putExtra("color-string", colorString)
            resultIntent.putExtra("image-string", imageDrawable)
            resultIntent.putExtra("photo-boolean", selectedPhoto)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        bottomOptions.rightButton.setOnClickListener{
            finish()
        }
    }

    fun addBorderToCards() {
        for (i in 0 until lightColorList.getChildCount()) {
            val borderCard = lightColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val cardColorString = String.format("#%06X", 0xFFFFFF and card.cardBackgroundColor.defaultColor)
            if (cardColorString == colorString) {
                borderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            else {
                borderCard.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }
        for (i in 0 until mediumColorList.getChildCount()) {
            val borderCard = mediumColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val cardColorString = String.format("#%06X", 0xFFFFFF and card.cardBackgroundColor.defaultColor)
            if (cardColorString == colorString) {
                borderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            else {
                borderCard.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }
        for (i in 0 until heavyColorList.getChildCount()) {
            val borderCard = heavyColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val cardColorString = String.format("#%06X", 0xFFFFFF and card.cardBackgroundColor.defaultColor)
            if (cardColorString == colorString) {
                borderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            else {
                borderCard.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }
        for (i in 0 until shadesColorList.getChildCount()) {
            val borderCard = shadesColorList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val cardColorString = String.format("#%06X", 0xFFFFFF and card.cardBackgroundColor.defaultColor)
            if (cardColorString == colorString) {
                borderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            else {
                borderCard.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }

        for (i in 0 until firstIconList.getChildCount()) {
            val borderCard = firstIconList.getChildAt(i) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val image = card.getChildAt(0) as ImageView
            val cardImageDrawable = image.tag
            if (cardImageDrawable == imageDrawable) {
                borderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                selectedPhoto = false
            }
            else {
                borderCard.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }

        for (i in 0 until photoList.getChildCount()){
            val container = photoList.getChildAt(i) as CardView
            container.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            container.setCardElevation(0f)

            val borderCard = container.getChildAt(0) as CardView
            val card = borderCard.getChildAt(0) as CardView
            val image = card.getChildAt(0) as ImageView
            val cardImageDrawable = image.tag
            if (cardImageDrawable == imageDrawable) {
                borderCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                selectedPhoto = true
            }
            else {
                borderCard.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, 1)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val imageBitmap = data!!.extras!!.get("data") as Bitmap
            val croppedImageBitmap = if(imageBitmap.height > imageBitmap.width){
                Bitmap.createBitmap(imageBitmap, 0, (imageBitmap.height - imageBitmap.width)/2, imageBitmap.width, imageBitmap.width)
            }else{
                Bitmap.createBitmap(imageBitmap, (imageBitmap.width - imageBitmap.height)/2, 0, imageBitmap.height, imageBitmap.height)
            }
            Realm.getDefaultInstance().executeTransaction{
                var newPhoto = it.createObject(Photos::class.java, UUID.randomUUID().toString())
                newPhoto.icon = DatabaseHelper.convertBitmapToByteArray(croppedImageBitmap)
                imageDrawable = newPhoto.uid
            }

            photoList.removeAllViews()
            populateGallery(readAllData(Photos::class.java) as RealmResults<out Photos>)
            addBorderToCards()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 200 && !grantResults.contains(PackageManager.PERMISSION_DENIED)){
            dispatchTakePictureIntent()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun populateGallery(photos: RealmResults<out Photos>){
        for(photo in photos){
            val newBmp = Bitmap.createScaledBitmap(DatabaseHelper.convertByteArrayToBitmap(photo.icon), 64,64,false)
            val galleryIcon = GalleryIconCard(this)

            galleryIcon.image.setBackground(BitmapDrawable(resources, newBmp))
            galleryIcon.image.tag = photo.uid
            galleryIcon.setOnClickListener {
                imageDrawable = galleryIcon.image.tag as String
                addBorderToCards()
            }
            photoList.addView(galleryIcon)
        }
    }

    private fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
        return Realm.getDefaultInstance().where(realmClass).findAll()
    }
}
