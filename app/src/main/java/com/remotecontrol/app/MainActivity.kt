package com.remotecontrol.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var balanceText: TextView
    private lateinit var referCodeText: TextView
    private lateinit var referCountText: TextView
    private lateinit var allowButton: Button
    private lateinit var referButton: Button
    private lateinit var cashoutBkash: Button
    private lateinit var cashoutNagad: Button

    private val db = FirebaseDatabase.getInstance().reference
    private val myDeviceId by lazy {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        balanceText = findViewById(R.id.balanceText)
        referCodeText = findViewById(R.id.referCodeText)
        referCountText = findViewById(R.id.referCountText)
        allowButton = findViewById(R.id.allowButton)
        referButton = findViewById(R.id.referButton)
        cashoutBkash = findViewById(R.id.cashoutBkash)
        cashoutNagad = findViewById(R.id.cashoutNagad)

        // Refer code = device id এর শেষ ৮ digit
        val myReferCode = myDeviceId.takeLast(8).uppercase()
        referCodeText.text = "Refer Code: $myReferCode"

        // Firebase থেকে balance পড়ো
        db.child("users").child(myDeviceId).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val balance = snapshot.child("balance").getValue(Long::class.java) ?: 0L
                    val referCount = snapshot.child("referCount").getValue(Long::class.java) ?: 0L
                    balanceText.text = "৳ $balance"
                    referCountText.text = "Total Refer: $referCount"
                }

                override fun onCancelled(error: DatabaseError) {}
            }
        )

        // Refer বাটন
        referButton.setOnClickListener {
            val intent = Intent(this, ReferActivity::class.java)
            intent.putExtra("referCode", myReferCode)
            startActivity(intent)
        }

        // Allow বাটন → ControlActivity তে যায়
        allowButton.setOnClickListener {
            val intent = Intent(this, ControlActivity::class.java)
            startActivity(intent)
        }

        // bKash cashout (ডেমো)
        cashoutBkash.setOnClickListener {
            Toast.makeText(
                this,
                "Admin approval required. Support এ যোগাযোগ করুন।",
                Toast.LENGTH_LONG
            ).show()
        }

        // Nagad cashout (ডেমো)
        cashoutNagad.setOnClickListener {
            Toast.makeText(
                this,
                "Admin approval required. Support এ যোগাযোগ করুন।",
                Toast.LENGTH_LONG
            ).show()
        }

        // প্রথমবার user হলে Firebase এ তৈরি করো
        db.child("users").child(myDeviceId).get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                val data = mapOf(
                    "balance" to 0L,
                    "referCount" to 0L,
                    "referCode" to myReferCode,
                    "joinedAt" to System.currentTimeMillis()
                )
                db.child("users").child(myDeviceId).setValue(data)
            }
        }
    }
}
