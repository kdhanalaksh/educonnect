package com.saveetha.educonnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
 
class ParentsteachermeetingActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var mediaPlayer: MediaPlayer
    private var isThirtySecondNotified = false
    private val CHANNEL_ID = "meeting_timer_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parentsteachermeeting)

        createNotificationChannel()

        val backBtn = findViewById<ImageButton>(R.id.backBtn)
        val meetingIdEdit = findViewById<EditText>(R.id.meetingIdEdit)
        val teacherIdEdit = findViewById<EditText>(R.id.teacherIdEdit)
        val joinBtn = findViewById<Button>(R.id.joinBtn)

        backBtn.setOnClickListener { finish() }

        joinBtn.setOnClickListener {
            val meetingId = meetingIdEdit.text.toString().trim()
            val teacherId = teacherIdEdit.text.toString().trim()

            if (meetingId.isEmpty() || teacherId.isEmpty()) {
                Toast.makeText(this, "Please enter Meeting ID and Teacher ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fetchMeetingLink(meetingId, teacherId)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Meeting Timer Notifications"
            val descriptionText = "Notifications for meeting timer alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun fetchMeetingLink(meetingId: String, teacherId: String) {
        val url = "http://192.168.136.226/educonnect_new_backend/meeting.php?id=$meetingId&teacher_id=$teacherId"

        Log.d("PTM_DEBUG", "Requesting: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ParentsteachermeetingActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("PTM_DEBUG", "Network error", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                Log.d("PTM_DEBUG", "Response code: ${response.code}, body: $body")

                if (!response.isSuccessful || body.isNullOrEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this@ParentsteachermeetingActivity, "Invalid response from server", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    val responseObject = JSONObject(body)
                    if (responseObject.getString("status") == "success") {
                        val data = responseObject.getJSONObject("data")
                        val meetingLink = data.getString("meeting_link")

                        runOnUiThread {
                            openMeeting(meetingLink)
                            startTimer() // Start the 1-minute timer
                        }
                    } else {
                        val msg = responseObject.optString("message", "Meeting not found")
                        runOnUiThread {
                            Toast.makeText(this@ParentsteachermeetingActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@ParentsteachermeetingActivity, "Error parsing data", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("PTM_DEBUG", "JSON parse error: $body", e)
                }
            }
        })
    }

    private fun openMeeting(meetingLink: String) {
        try {
            Log.d("PTM_DEBUG", "Opening meeting link: $meetingLink")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meetingLink))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid Meeting Link", Toast.LENGTH_SHORT).show()
            Log.e("PTM_DEBUG", "Invalid link: $meetingLink", e)
        }
    }

    private fun startTimer() {
        isThirtySecondNotified = false
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isThirtySecondNotified && millisUntilFinished <= 30000) {
                    isThirtySecondNotified = true
                    showNotification("Meeting Alert", "Only 30 seconds left!")
                }
            }

            override fun onFinish() {
                mediaPlayer = MediaPlayer.create(this@ParentsteachermeetingActivity, R.raw.buzzer)
                mediaPlayer.start()
                showNotification("Meeting Alert", "1 minute over! Buzzer playing.")
            }
        }.start()
    }

    private fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app icon here
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
