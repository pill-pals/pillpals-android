package com.pillpals.pillbuddies.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.flexbox.FlexboxLayout
import com.pillpals.pillbuddies.R
import com.pillpals.pillbuddies.data.model.Photos
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.convertBitmapToByteArray
import com.pillpals.pillbuddies.helpers.DatabaseHelper.Companion.convertByteArrayToBitmap
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import java.util.*
import kotlin.collections.ArrayList

class DrugGallery: AppCompatActivity() {

    public lateinit var bottomOptions: BottomOptions
    public lateinit var photoList : FlexboxLayout
    public lateinit var snapButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_drug_gallery)
        //val list: MutableList<ImageView> = ArrayList()

        bottomOptions = findViewById(R.id.bottomOptions)
        photoList = findViewById(R.id.photoList)
        snapButton = findViewById(R.id.snapButton)

        bottomOptions.leftButton.text = "Select"
        bottomOptions.rightButton.text = "Back"

        photoList.removeAllViews()
        populateGallery(readAllData(Photos::class.java) as RealmResults<out Photos>)

        snapButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        /*bottomOptions.leftButton.setOnClickListener{
            val resultIntent = Intent(this, EditMedicationIcon::class.java)
            resultIntent.putExtra("image-string", imageDrawable)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }*/

        bottomOptions.rightButton.setOnClickListener{
            finish()
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
            Realm.getDefaultInstance().executeTransaction{
                var newPhoto = it.createObject(Photos::class.java, UUID.randomUUID().toString())
                newPhoto.icon = convertBitmapToByteArray(imageBitmap)
            }

            photoList.removeAllViews()
            populateGallery(readAllData(Photos::class.java) as RealmResults<out Photos>)
        }
    }

    private fun populateGallery(photos: RealmResults<out Photos>){
        for(photo in photos){
            val newBmp = convertByteArrayToBitmap(photo.icon)
            val galleryIcon = GalleryIconCard(this)

            galleryIcon.image.setImageBitmap(newBmp)
            galleryIcon.setOnClickListener {
                //galleryIcon.active = true
            }
            photoList.addView(galleryIcon)
        }
    }

    private fun readAllData(realmClass: Class<out RealmObject>): RealmResults<out RealmObject> {
        return Realm.getDefaultInstance().where(realmClass).findAll()
    }
}