package com.remotecontrol.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ControlActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var roomInput: EditText
    private lateinit var screenView: ImageView
    private lateinit var startShareBtn: Button
    private lateinit var connectBtn: Button
    private lateinit var enableTouchBtn: Button

    private val db = FirebaseDatabase.getInstance().reference
    private var currentRoom: String = ""
    private var myDeviceId: String = ""

    private val projectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        statusText = findViewById(R.id.statusText)
        roomInput = findViewById(R.id.roomCodeInput)
        screenView = findViewById(R.id.screenView)
        startShareBtn = findViewById(R.id.startShareButton)
        connectBtn = findViewById(R.id.connectButton)
        enableTouchBtn = findViewById(R.id.enableTouchButton)

        myDeviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        currentRoom = myDeviceId.takeLast(6).uppercase()
        roomInput.setText(currentRoom)
        statusText.text = "Your Room: $currentRoom"

        startShareBtn.setOnClickListener {
            val intent = projectionManager.createScreenCaptureIntent()
            startActivityForResult(intent, 1001)
        }

        enableTouchBtn.setOnClickListener {
            Toast.makeText(
                this,
                "Settings → Accessibility → Remote Control → Enable",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        connectBtn.setOnClickListener {
            val code = roomInput.text.toString().trim().uppercase()
            if (code.isEmpty()) {
                Toast.makeText(this, "Enter room code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentRoom = code
            statusText.text = "Connecting to $code..."
            listenForFrames()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra("resultCode", resultCode)
                putExtra("data", data)
                putExtra("room", currentRoom)
            }
            startForegroundService(serviceIntent)
            statusText.text = "Sharing to room: $currentRoom"
            Toast.makeText(this, "Screen sharing started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listenForFrames() {
        db.child("rooms").child(currentRoom).child("frame")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val base64 = snapshot.getValue(String::class.java) ?: return
                    try {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) {
                            runOnUiThread {
                                screenView.setImageBitmap(bmp)
                                statusText.text = "Live: $currentRoom"
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
