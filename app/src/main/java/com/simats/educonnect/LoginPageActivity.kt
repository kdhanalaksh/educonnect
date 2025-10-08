package com.simats.educonnect

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class LoginPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)  // Your actual main login layout XML

        val teacherLoginBtn = findViewById<ImageView>(R.id.teacherlogin)
        val studentLoginBtn = findViewById<ImageView>(R.id.studentparent)
        val loginbackbtn = findViewById<ImageView>(R.id.loginbackbutton)

        teacherLoginBtn.setOnClickListener {
            val intent = Intent(this, TeacherloginActivity::class.java)
            startActivity(intent)
        }

        studentLoginBtn.setOnClickListener {
            // Correct usage: Launch the StudentLoginActivity (not XML layout)
            val intent = Intent(this, StudentLoginActivity::class.java)
            startActivity(intent)
        }

        loginbackbtn.setOnClickListener {
            // Navigate back to MainActivity (or previous activity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
