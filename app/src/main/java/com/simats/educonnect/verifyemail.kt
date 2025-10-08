package com.simats.educonnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class VerifyEmailActivity : AppCompatActivity() {

    // Configure OkHttpClient with timeout settings
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifyemail)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val verifyButton = findViewById<Button>(R.id.verifyEmailButton)
        val backButton = findViewById<Button>(R.id.backButton) // Make sure your XML has this ID

        // Back button navigates to TeacherLoginActivity
        backButton.setOnClickListener {
            val intent = Intent(this, TeacherloginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // On Verify Email button click
        verifyButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                sendOtpRequest(email)
            }
        }
    }

    private fun sendOtpRequest(email: String) {
        val url = "http://14.139.187.229:8081/PDD-2025(9thmonth)/EDUCONNECT-back-end-main/sendotp.php"

        val json = JSONObject().apply {
            put("email", email)
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
                Log.e("OTP_ERROR", "Network error: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@VerifyEmailActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("OTP_RESPONSE", responseBody ?: "No response")

                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@VerifyEmailActivity, "OTP Sent Successfully", Toast.LENGTH_SHORT).show()

                        // ✅ Navigate to OTP verification page with email
                        val intent = Intent(this@VerifyEmailActivity, otpverify::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@VerifyEmailActivity, "Error: $responseBody", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
