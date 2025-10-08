package com.simats.educonnect

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class notificatoion : AppCompatActivity() {

    private lateinit var notificationList: LinearLayout
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificatoion)

        notificationList = findViewById(R.id.notification_list)

        // ✅ Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // ✅ Get studentId passed from Login/Dashboard
        val studentId = intent.getIntExtra("student_id", -1)

        if (studentId == -1) {
            Toast.makeText(this, "Student ID missing!", Toast.LENGTH_SHORT).show()
            return
        }

        // Create notification channel (for Android 8+)
        createNotificationChannel()

        fetchMeetings(studentId)
    }

    private fun fetchMeetings(studentId: Int) {
        val request = Request.Builder()
            .url("http://14.139.187.229:8081/PDD-2025(9thmonth)/EDUCONNECT-back-end-main/get_students_meetings.php?student_id=$studentId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@notificatoion, "Failed to load data", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonData = response.body?.string()
                if (jsonData != null) {
                    val jsonObject = JSONObject(jsonData)
                    if (jsonObject.getString("status") == "success") {
                        val meetingsArray = jsonObject.getJSONArray("meetings")

                        runOnUiThread {
                            notificationList.removeAllViews()
                            for (i in 0 until meetingsArray.length()) {
                                val obj = meetingsArray.getJSONObject(i)

                                val title = obj.getString("title")
                                val date = obj.getString("meeting_date")
                                val start = obj.getString("start_time")
                                val end = obj.getString("end_time")
                                val purpose = obj.getString("purpose")
                                val link = obj.optString("meeting_link", "No link")
                                val status = obj.getString("status")

                                // Show in UI
                                addNotificationView(title, date, start, end, purpose, link, status)

                                // Trigger system notification
                                showPopupNotification(title, date, start, end, purpose, link)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun addNotificationView(
        title: String,
        date: String,
        start: String,
        end: String,
        purpose: String,
        link: String,
        status: String
    ) {
        val textView = TextView(this)

        // Make "link" clickable
        val spannable = SpannableString("📌 $title\n📅 $date $start - $end\n🎯 $purpose\n🔗 $link\n⚡ Status: $status")

        if (link != "No link") {
            val startIndex = spannable.indexOf(link)
            val endIndex = startIndex + link.length
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(browserIntent)
                }
            }, startIndex, endIndex, 0)
        }

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.textSize = 16f
        textView.setPadding(20, 20, 20, 20)

        notificationList.addView(textView)
    }

    private fun showPopupNotification(
        title: String,
        date: String,
        start: String,
        end: String,
        purpose: String,
        link: String
    ) {
        // Create an Intent when user taps the notification
        val intent = Intent(this, notificatoion::class.java).apply {
            putExtra("meeting_title", title)
            putExtra("meeting_date", date)
            putExtra("meeting_start", start)
            putExtra("meeting_end", end)
            putExtra("meeting_purpose", purpose)
            putExtra("meeting_link", link)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "educonnect_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Meeting: $title")
            .setContentText("📅 $date $start - $end | 🎯 $purpose")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // ✅ Open activity on tap
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@notificatoion,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "educonnect_channel",
                "EduConnect Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for meeting notifications"
            }
            val manager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // ✅ Handle runtime permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}