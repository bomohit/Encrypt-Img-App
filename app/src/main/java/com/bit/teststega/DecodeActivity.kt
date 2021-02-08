package com.bit.teststega

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Log.d
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextDecoding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class DecodeActivity : AppCompatActivity(), TextDecodingCallback {

    private val pickImage = 100
    private var imageUri: Uri? = null
    private var msg: EditText? = null
    private var key: EditText? = null
    private var original_image : Bitmap? = null
    private var v : View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        msg = findViewById(R.id.textMessage)
        key = findViewById(R.id.textKey)
        msg!!.isCursorVisible = false
        msg!!.isFocusableInTouchMode = false
        val float = findViewById<FloatingActionButton>(R.id.floatingActionButton)

        float.setOnClickListener {
            onBackPressed()
        }

        checkAndRequestPermissions()

        val selectImg = findViewById<Button>(R.id.buttonSelectImage)
        selectImg.setOnClickListener {

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        val selectEncode = findViewById<Button>(R.id.buttonEncode)
        val selectSave = findViewById<Button>(R.id.buttonSave)
        selectEncode.isVisible = false
        selectSave.isVisible = false
        //DECODE PRESSED
        val selectDecode = findViewById<Button>(R.id.buttonDecode)
        selectDecode.isVisible = true
        selectDecode.setOnClickListener {
            d("bomoh", "decode Start")
            v = it
            if (original_image != null && !key!!.text.toString().isNullOrEmpty()) {
                Snackbar.make(it, "DECRYPTING ..", Snackbar.LENGTH_SHORT).show()
                //Making the ImageSteganography object
                val imageSteganography = ImageSteganography(
                    key!!.text.toString(),
                    original_image
                )

                //Making the TextDecoding object
                val textDecoding = TextDecoding(this, this)

                //Execute Task
                textDecoding.execute(imageSteganography)

            } else {
                Snackbar.make(it, "SELECT IMAGE FIRST AND ENTER SECRET KEY", Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    override fun onStartTextEncoding() {
        TODO("Not yet implemented")
    }

    override fun onCompleteTextEncoding(result: ImageSteganography?) {
        if (result != null) {
            val textMsg = findViewById<TextView>(R.id.textMessage)
            if (!result.isDecoded)
//                textView.setText("No message found");
                textMsg.text = ("No message found")
            else {
                if (!result.isSecretKeyWrong) {
//                    textView.setText("Decoded")
//                    message.setText("" + result.message)
                    textMsg.text = "${result.message}"
                    Snackbar.make(v!!, "COMPLETE", Snackbar.LENGTH_SHORT).show()
                    d("bomoh", "correct secretkey")
                } else {
//                    textView.setText("Wrong secret key")
                    Snackbar.make(v!!, "WRONG SECRET KEY", Snackbar.LENGTH_SHORT).show()
                    d("bomoh", "wrong secretkey || ${result.message}")
                    textMsg.text = "Wrong secret key"
                }
            }
        } else {
//            textView.setText("Select Image First")
            d("bomoh", "Select Image First")
            Snackbar.make(v!!, "SELECT IMAGE FIRST", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage) {
            val img = findViewById<ImageView>(R.id.imageView)
            imageUri = data?.data
            original_image = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            d("bomoh", "image uri: $imageUri")
            d("bomoh", "data: $data")
            img.setImageBitmap(original_image)
        }
    }


    private fun checkAndRequestPermissions() {
        val permissionWriteStorage = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 1)
        }
    }

}