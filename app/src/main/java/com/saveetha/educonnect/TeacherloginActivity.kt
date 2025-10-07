package com.saveetha.educonnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TeacherloginActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teacherlogin)

        val teacherLoginBtn = findViewById<Button>(R.id.loginbutton)
        val teacherBackBtn = findViewById<ImageView>(R.id.teacherbackbtn)
        val teacherForgetBtn = findViewById<TextView>(R.id.teacherforgetbtn)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)

        teacherLoginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                loginTeacher(email, password)
            }
        }

        teacherForgetBtn.setOnClickListener {
            startActivity(Intent(this, VerifyEmailActivity::class.java))
        }

        teacherBackBtn.setOnClickListener {
            startActivity(Intent(this, LoginPageActivity::class.java))
            finish()
        }
    }

    private fun loginTeacher(email: String, password: String) {
        val url = "http://192.168.136.226/educonnect_new_backend/login.php"

        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .add("role", "teacher")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@TeacherloginActivity,
                        "Network error: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && !jsonString.isNullOrEmpty()) {
                        try {
                            val jsonObject = JSONObject(jsonString)
                            val status = jsonObject.optString("status")
                            val message = jsonObject.optString("message")

                            if (status == "success") {
                                val userObj = jsonObject.optJSONObject("user")
                                val teacherId = userObj?.optInt("id") ?: 0
                                val teacherName = userObj?.optString("name") ?: ""
                                val teacherEmail = userObj?.optString("email") ?: ""

                                if (teacherId != 0) {
                                    // ✅ Save login info
                                    val prefs = getSharedPreferences("EduConnectPrefs", Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putInt("user_id", teacherId)
                                        .putString("name", teacherName)
                                        .putString("email", teacherEmail)
                                        .apply()

                                    Toast.makeText(
                                        this@TeacherloginActivity,
                                        "Welcome $teacherName!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // ✅ Navigate to Teacher Dashboard
                                    val intent = Intent(this@TeacherloginActivity, TeacherdashboardActivity::class.java)
                                    // Safer flags (prevents going back to login)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this@TeacherloginActivity, "Invalid user data", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@TeacherloginActivity,
                                    message.ifEmpty { "Login failed" },
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Navigate to error page on failure
                                val intent = Intent(this@TeacherloginActivity, teacherloginerrorpage::class.java)
                                startActivity(intent)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@TeacherloginActivity, "Response parsing error", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@TeacherloginActivity, "Server error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
