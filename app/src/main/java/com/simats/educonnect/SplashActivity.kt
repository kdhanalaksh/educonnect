package com.simats.educonnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("EduConnectPrefs", Context.MODE_PRIVATE)
        val savedId = prefs.getInt("user_id", 0)

        if (savedId != 0) {
            // Already logged in → go to dashboard
            startActivity(Intent(this, TeacherdashboardActivity::class.java))
        } else {
            // Not logged in → go to login
            startActivity(Intent(this, TeacherloginActivity::class.java))
        }
        finish() // close splash
    }
}
