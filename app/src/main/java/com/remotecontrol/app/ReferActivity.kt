package com.remotecontrol.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refer)

        val referCode = intent.getStringExtra("referCode") ?: "UNKNOWN"

        val codeText = findViewById<TextView>(R.id.myCodeText)
        val shareBtn = findViewById<Button>(R.id.shareButton)
        val copyBtn = findViewById<Button>(R.id.copyButton)
        val backBtn = findViewById<Button>(R.id.backButton)

        codeText.text = referCode

        copyBtn.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Refer Code", referCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Code copied!", Toast.LENGTH_SHORT).show()
        }

        shareBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Join this app! Use my refer code: $referCode"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        backBtn.setOnClickListener {
            finish()
        }
    }
}
