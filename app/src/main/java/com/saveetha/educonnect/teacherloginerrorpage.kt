package com.saveetha.educonnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class teacherloginerrorpage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_teacherloginerrorpage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.teacherloginerrorpage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Correct IDs from your XML
        val backArrowBtn = findViewById<ImageView>(R.id.backArrowBtn)
        val tryAgainButton = findViewById<Button>(R.id.tryAgainButton)
        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordText)

        // 🔁 Back Arrow → TeacherloginActivity
        backArrowBtn.setOnClickListener {
            startActivity(Intent(this, TeacherloginActivity::class.java))
            finish()
        }

        // 🔁 Try Again Button → TeacherloginActivity
        tryAgainButton.setOnClickListener {
            startActivity(Intent(this, TeacherloginActivity::class.java))
            finish()
        }

        // 🔁 Forgot Password Text → VerifyEmailActivity
        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, VerifyEmailActivity::class.java))
        }
    }
}
