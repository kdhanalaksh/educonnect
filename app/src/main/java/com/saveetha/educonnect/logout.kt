package com.saveetha.educonnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class logout : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logout)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get buttons
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // Logout → Navigate to Login Page and clear session
        btnLogout.setOnClickListener {
            // Clear SharedPreferences (so user is really logged out)
            val sharedPref = getSharedPreferences("EduConnectPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            // Navigate to LoginActivity
            val intent = Intent(this, LoginPageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Cancel → Navigate back to Teacher Dashboard
        btnCancel.setOnClickListener {
            val intent = Intent(this, TeacherdashboardActivity ::class.java)
            startActivity(intent)
            finish()
        }
    }
}
