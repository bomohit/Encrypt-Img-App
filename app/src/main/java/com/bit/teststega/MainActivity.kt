package com.bit.teststega

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log.d
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextDecoding
import com.ayush.imagesteganographylibrary.Text.TextEncoding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.*


class MainActivity : AppCompatActivity(), TextEncodingCallback, TextDecodingCallback {

    private val pickImage = 100
    private var imageUri: Uri? = null
    private var msg: EditText? = null
    private var key: EditText? = null
    private var original_image : Bitmap? = null
    private var encodeImg : Bitmap? = null
    private var v : View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        msg = findViewById(R.id.textMessage)
        key = findViewById(R.id.textKey)
        val lay = findViewById<ConstraintLayout>(R.id.pageLayout)
        val float = findViewById<FloatingActionButton>(R.id.floatingActionButton)

        float.setOnClickListener {
            onBackPressed()
        }

        checkAndRequestPermissions()

        lay.setOnClickListener {
            closeKeyBoard(it)
        }

     //select image
        val selectImg = findViewById<Button>(R.id.buttonSelectImage)
        selectImg.setOnClickListener {

            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        // ENCODE PRESSED
        val selectEncode = findViewById<Button>(R.id.buttonEncode)
        selectEncode.setOnClickListener {
            v = it
            d("bomoh", "Encode Start ${msg!!.text} , ${key!!.text}")
            if (imageUri != null) {
                if (!msg!!.text.toString().isNullOrEmpty() && !key!!.text.toString().isNullOrEmpty()) {
                    Snackbar.make(it, "ENCRYPTING ..", Snackbar.LENGTH_SHORT).show()
                    //ImageSteganography Object instantiation
                    val imageSteganography = ImageSteganography(
                            msg!!.text.toString(),
                            key!!.text.toString(),
                            original_image
                    )
                    //TextEncoding object Instantiation
                    val textEncoding = TextEncoding(this, this)
                    //Executing the encoding
                    textEncoding.execute(imageSteganography)

                } else {
                    Snackbar.make(it, "ENTER MESSAGE AND SECRET KEY", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        // SAVE PRESSED
        val selectSave = findViewById<Button>(R.id.buttonSave)
        selectSave.setOnClickListener {
            saveToInternalStorage(encodeImg!!)
        }

        //DECODE PRESSED
        val selectDecode = findViewById<Button>(R.id.buttonDecode)
        selectDecode.isVisible = false
        selectDecode.setOnClickListener {
            d("bomoh", "decode Start")
            if (encodeImg != null) {
                //Making the ImageSteganography object
                val imageSteganography = ImageSteganography(
                        key!!.text.toString(),
                        encodeImg
                )

                //Making the TextDecoding object
                val textDecoding = TextDecoding(this, this)

                //Execute Task
                textDecoding.execute(imageSteganography)

            }

        }
    }

    override fun onStartTextEncoding() {
        TODO("Not yet implemented")
    }

    override fun onCompleteTextEncoding(result: ImageSteganography?) {
        if (result != null && result.isEncoded) {
            encodeImg = result.encoded_image
            val img = findViewById<ImageView>(R.id.imageView)
            img.setImageBitmap(encodeImg)
            Snackbar.make(v!!, "COMPLETE", Snackbar.LENGTH_SHORT).show()
            d("bomoh", "encode On COmplete")
            d("bomoh", "img : $encodeImg")
        }

        else if (result != null) {
            val textMsg = findViewById<TextView>(R.id.textMessage)
            if (!result.isDecoded)
//                textView.setText("No message found");
                textMsg.text = ("No message found")
            else {
                d("bomoh", "img : $encodeImg")
                if (!result.isSecretKeyWrong) {
//                    textView.setText("Decoded")
//                    message.setText("" + result.message)
                    textMsg.text = "${result.message}"
                    d("bomoh", "correct secretkey")
                } else {
//                    textView.setText("Wrong secret key")
                    d("bomoh", "wrong secretkey || ${result.message}")
                    textMsg.text = "Wrong secret key"
                }
            }
        } else {
//            textView.setText("Select Image First")
            d("bomoh", "Select Image First")
            Snackbar.make(v!!, "FAILED", Snackbar.LENGTH_SHORT).show()
        }
    }


    private fun saveToInternalStorage(bitmapImage: Bitmap) {
        val fOut: OutputStream
        val file = File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                ), "Encoded" + ".png"
        ) // the File to save ,

        try {
            fOut = FileOutputStream(file)
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fOut) // saving the Bitmap to a file

            fOut.flush() // Not really required
            fOut.close() // do not forget to close the stream
            Toast.makeText(applicationContext, "IMAGE SAVED", Toast.LENGTH_SHORT).show()

            d("bomoh", "saved to internal")
//            whether_encoded.post(Runnable {
//                Toast.makeText(applicationContext, "done Encode", Toast.LENGTH_SHORT).show()
//            })
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null
            ) { path, uri ->
                // code to execute when scanning is complete
                d("bomoh", "scan complete")
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
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

    private fun closeKeyBoard(v: View) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }


}