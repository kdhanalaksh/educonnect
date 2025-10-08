package com.simats.educonnect

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StudentdashboardActivity : AppCompatActivity() {

    private lateinit var studentNameText: TextView
    private lateinit var createMeeting: TextView
    private lateinit var rescheduleMeeting: TextView
    private lateinit var notify: TextView
    private lateinit var meetingPage: TextView

    private var studentId: Int = -1   // store student id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.studentdashboard)

        studentNameText = findViewById(R.id.tvStudentName)
        createMeeting = findViewById(R.id.createmeeting)
        rescheduleMeeting = findViewById(R.id.reschedulemeeting)
        notify = findViewById(R.id.notify)
        meetingPage = findViewById(R.id.meetingpage)

        // ✅ Fetch student_id and student_name from intent (coming from login)
        studentId = intent.getIntExtra("student_id", -1)
        val studentName = intent.getStringExtra("student_name") ?: "Student"

        studentNameText.text = studentName
        Toast.makeText(this, "Welcome $studentName", Toast.LENGTH_SHORT).show()

        // Navigation click listeners
        createMeeting.setOnClickListener {
            startActivity(Intent(this, activitystudent_meeting::class.java))
        }

        rescheduleMeeting.setOnClickListener {
            startActivity(Intent(this, studentprofile::class.java))
        }

        notify.setOnClickListener {
            val intent = Intent(this, notificatoion::class.java)
            intent.putExtra("student_id", studentId)   // ✅ pass student id
            intent.putExtra("student_name", studentName) // optional, for greeting
            startActivity(intent)
        }

        meetingPage.setOnClickListener {
            startActivity(Intent(this, studentlogout::class.java))
        }
    }
}
