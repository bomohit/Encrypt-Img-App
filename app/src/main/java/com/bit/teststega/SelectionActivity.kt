package com.bit.teststega

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selection_main)

        val encrypt = findViewById<Button>(R.id.selectEncrypt)
        val decrypt = findViewById<Button>(R.id.selectDecrypt)

        encrypt.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        decrypt.setOnClickListener {
            val intent = Intent(this, DecodeActivity::class.java)
            startActivity(intent)
        }
    }
}