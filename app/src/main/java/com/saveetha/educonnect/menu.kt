package com.saveetha.educonnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class menu : AppCompatActivity() {

    private var teacherId: Int = 0
    private var teacherName: String = ""
    private var teacherEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // 🔹 First try to get teacher data from Intent
        teacherId = intent.getIntExtra("teacher_id", 0)
        teacherName = intent.getStringExtra("name") ?: ""
        teacherEmail = intent.getStringExtra("email") ?: ""

        // 🔹 If Intent is empty, fallback to SharedPreferences
        if (teacherId == 0 || teacherName.isEmpty()) {
            val prefs = getSharedPreferences("educonnect_prefs", Context.MODE_PRIVATE)
            teacherId = prefs.getInt("user_id", 0)
            teacherName = prefs.getString("name", "") ?: ""
            teacherEmail = prefs.getString("email", "") ?: ""
        }

        // 🔹 Optional: Show welcome toast
        if (teacherName.isNotEmpty()) {
            Toast.makeText(this, "Welcome $teacherName", Toast.LENGTH_SHORT).show()
        }

        // 🔹 Home: Go to Teacher Dashboard
        findViewById<LinearLayout>(R.id.home).setOnClickListener {
            val intent = Intent(this, TeacherdashboardActivity::class.java)
            intent.putExtra("teacher_id", teacherId)
            intent.putExtra("name", teacherName)
            intent.putExtra("email", teacherEmail)
            startActivity(intent)
        }

        // 🔹 Profile: Open a profile screen
        findViewById<LinearLayout>(R.id.profile).setOnClickListener {
            val intent = Intent(this, TeacherProfileActivity::class.java)
            intent.putExtra("teacher_id", teacherId)
            startActivity(intent)
        }

        // 🔹 Settings: Navigate to settings screen and pass teacher data
        findViewById<LinearLayout>(R.id.settings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("teacher_id", teacherId)
            intent.putExtra("name", teacherName)
            intent.putExtra("email", teacherEmail)
            startActivity(intent)
        }

        // 🔹 Logout: Clear session & go back to login
        findViewById<LinearLayout>(R.id.logout).setOnClickListener {
            // Clear SharedPreferences
            val prefs = getSharedPreferences("educonnect_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginPageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
