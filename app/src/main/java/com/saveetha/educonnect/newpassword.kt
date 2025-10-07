package com.saveetha.educonnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class newpassword : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newpassword)

        val email = intent.getStringExtra("email")
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Email not found. Try again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val passwordEditText = findViewById<EditText>(R.id.editpassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmpassword)
        val continueButton = findViewById<Button>(R.id.confirm_button)
        val continueToLoginButton = findViewById<Button>(R.id.continuelogin)
        val backArrow = findViewById<ImageView>(R.id.backArrowBtn)

        val tickIcon = findViewById<ImageView>(R.id.tickIcon)
        val successText = findViewById<TextView>(R.id.successText)

        // Initial state
        continueToLoginButton.visibility = View.GONE
        tickIcon.visibility = View.GONE
        successText.visibility = View.GONE

        continueButton.setOnClickListener {
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                resetPassword(email, password)
            }
        }

        continueToLoginButton.setOnClickListener {
            startActivity(Intent(this@newpassword, TeacherloginActivity::class.java))
            finish()
        }

        backArrow.setOnClickListener {
            finish()
        }
    }

    private fun resetPassword(email: String, password: String) {
        val url = "http://192.168.136.226/educonnect_new_backend/reset_password.php"

        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("confirm_password", password)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RESET_PASSWORD", "Network error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@newpassword, "Network error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("RESET_PASSWORD", "Response: $responseBody")

                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val status = jsonResponse.getBoolean("status")
                            val message = jsonResponse.getString("message")

                            Toast.makeText(this@newpassword, message, Toast.LENGTH_LONG).show()

                            if (status) {
                                // Hide password fields and show success UI
                                findViewById<EditText>(R.id.editpassword).visibility = View.GONE
                                findViewById<EditText>(R.id.confirmpassword).visibility = View.GONE
                                findViewById<Button>(R.id.confirm_button).visibility = View.GONE

                                findViewById<ImageView>(R.id.tickIcon).visibility = View.VISIBLE
                                findViewById<TextView>(R.id.successText).visibility = View.VISIBLE
                                findViewById<Button>(R.id.continuelogin).visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            Toast.makeText(this@newpassword, "Unexpected server response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@newpassword, "Failed to reset password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
