package com.pillpals.pillbuddies.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import com.pillpals.pillbuddies.R
import io.realm.Realm

class DrugGallery: AppCompatActivity() {

    public lateinit var bottomOptions: BottomOptions
    public lateinit var photoList : FlexboxLayout
    public lateinit var snapButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        setContentView(R.layout.activity_drug_gallery)
        val list: MutableList<ImageView> = ArrayList()

        bottomOptions = findViewById(R.id.bottomOptions)
        photoList = findViewById(R.id.photoList)
        snapButton = findViewById(R.id.snapButton)

        bottomOptions.leftButton.text = "Select"
        bottomOptions.rightButton.text = "Cancel"

        snapButton.setOnClickListener {
            dispatchTakePictureIntent()
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
            val imageView = ImageView(this)

            imageView.layoutParams = LinearLayout.LayoutParams(100, 100)
            imageView.setImageBitmap(imageBitmap)
            photoList.addView(imageView)
        }
    }
}