package com.saveetha.educonnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class TeacherdashboardActivity : AppCompatActivity() {

    private var teacherId: Int = 0
    private var teacherName: String = ""
    private var teacherEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teacherdashboard)

        // ✅ Retrieve using the key "user_id"
        val prefs = getSharedPreferences("EduConnectPrefs", Context.MODE_PRIVATE)
        teacherId = prefs.getInt("user_id", 0)   // ✅ Corrected to "user_id"
        teacherName = prefs.getString("name", "") ?: ""
        teacherEmail = prefs.getString("email", "") ?: ""

        // ✅ Validate teacherId
        if (teacherId == 0) {
            Toast.makeText(this, "Teacher ID not found, please login again", Toast.LENGTH_LONG).show()
            val intent = Intent(this, TeacherloginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // ✅ Display teacher’s name and today’s date
        findViewById<TextView>(R.id.tvTeacherName).text = "Welcome, $teacherName"
        findViewById<TextView>(R.id.tvDate).text =
            SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())

        // ✅ Create Meeting
        findViewById<TextView>(R.id.createmeeting).setOnClickListener {
            val intent = Intent(this, createevent::class.java)
            intent.putExtra("teacher_id", teacherId)
            startActivity(intent)
        }

        // ✅ Reschedule Meeting
        findViewById<TextView>(R.id.reschedulemeeting).setOnClickListener {
            val intent = Intent(this, RescheduleMeeting::class.java)
            intent.putExtra("teacher_id", teacherId)
            startActivity(intent)
        }

        // ✅ View Parent-Teacher Meetings
        findViewById<TextView>(R.id.meetingpage).setOnClickListener {
            val intent = Intent(this, ParentsteachermeetingActivity::class.java)
            intent.putExtra("teacher_id", teacherId)
            startActivity(intent)
        }

        // ✅ Mark Attendance
        findViewById<ImageView>(R.id.markattendance).setOnClickListener {
            val intent = Intent(this, TeacherAttendanceActivity::class.java)
            intent.putExtra("teacher_id", teacherId)
            startActivity(intent)
        }

        // ✅ Shared Updates
        findViewById<TextView>(R.id.sharedupdates).setOnClickListener {
            val intent = Intent(this, SharedUpdates::class.java)
            intent.putExtra("teacher_id", teacherId)
            startActivity(intent)
        }

        // ✅ Menu Page
        findViewById<TextView>(R.id.menuBtn).setOnClickListener {
            val intent = Intent(this, menu::class.java)
            intent.putExtra("teacher_id", teacherId)
            startActivity(intent)
        }

        // ✅ Logout Page
        findViewById<TextView>(R.id.logoutBtn).setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, logout::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
