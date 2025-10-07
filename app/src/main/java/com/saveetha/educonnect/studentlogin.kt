package com.saveetha.educonnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class StudentLoginActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var backBtn: ImageView
    private lateinit var loginButton: Button
    private lateinit var forgetPassword: TextView
    private lateinit var signUpLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studentlogin)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        backBtn = findViewById(R.id.studentloginbackbtn)
        loginButton = findViewById(R.id.loginButtonstudent)
        forgetPassword = findViewById(R.id.forgetpasswordstudent)
        signUpLink = findViewById(R.id.signinstudent)

        backBtn.setOnClickListener { finish() }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(email, password)
        }

        signUpLink.setOnClickListener {
            startActivity(Intent(this, StudentregistrationActivity::class.java))
        }

        forgetPassword.setOnClickListener {
            Toast.makeText(this, "Forget password clicked - implement reset flow", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser(email: String, password: String) {
        val url = "http://192.168.136.226/educonnect_new_backend/login.php"

        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .add("role", "student")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@StudentLoginActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
                                val studentId = userObj?.optInt("id", -1) ?: -1
                                val studentName = userObj?.optString("name") ?: ""
                                val studentEmail = userObj?.optString("email") ?: ""

                                if (studentId != -1) {
                                    // Save in SharedPreferences
                                    val prefs = getSharedPreferences("EduConnectPrefs", Context.MODE_PRIVATE)
                                    prefs.edit().putInt("student_id", studentId).apply()

                                    Toast.makeText(this@StudentLoginActivity, "Welcome $studentName!", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@StudentLoginActivity, StudentdashboardActivity::class.java)
                                    intent.putExtra("student_name", studentName) // ✅ Key matches dashboard
                                    intent.putExtra("student_email", studentEmail)
                                    intent.putExtra("student_id", studentId)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this@StudentLoginActivity, "Invalid student data received", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@StudentLoginActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@StudentLoginActivity, "Response parse error", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@StudentLoginActivity, "Server error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
