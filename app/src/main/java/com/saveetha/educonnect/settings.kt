package com.saveetha.educonnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Back arrow click
        val backArrow: ImageView = findViewById(R.id.backbtn)
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Right arrow to go to Teacher Profile
        val rightArrow: ImageView = findViewById(R.id.rightarrow)
        rightArrow.setOnClickListener {
            val intent = Intent(this, TeacherProfileActivity::class.java)
            startActivity(intent)
        }

        // Notification Switches
        val schoolSwitch: Switch = findViewById(R.id.school)
        val classSwitch: Switch = findViewById(R.id.class_update)
        val assignmentSwitch: Switch = findViewById(R.id.assignment_remainder)
        val emergencySwitch: Switch = findViewById(R.id.emergency)

        schoolSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "School Announcements ON" else "School Announcements OFF", Toast.LENGTH_SHORT).show()
        }
        classSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Class Updates ON" else "Class Updates OFF", Toast.LENGTH_SHORT).show()
        }
        assignmentSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Assignment Reminders ON" else "Assignment Reminders OFF", Toast.LENGTH_SHORT).show()
        }
        emergencySwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, if (isChecked) "Emergency Alerts ON" else "Emergency Alerts OFF", Toast.LENGTH_SHORT).show()
        }

        // Sign Out button - Go to Login
        val signOutButton: Button = findViewById(R.id.signout)
        signOutButton.setOnClickListener {
            val intent = Intent(this, LoginPageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
