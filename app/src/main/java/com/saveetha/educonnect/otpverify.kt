package com.saveetha.educonnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class otpverify : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpverify)

        val email = intent.getStringExtra("email")
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Missing email. Please try again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val otpEditText = findViewById<EditText>(R.id.otpEditText)
        val verifyButton = findViewById<Button>(R.id.verifyCodeButton)
        val resendText = findViewById<TextView>(R.id.resendCodeText)
        val backLayout = findViewById<LinearLayout>(R.id.backToLogin)

        backLayout.setOnClickListener {
            startActivity(Intent(this, VerifyEmailActivity::class.java))
            finish()
        }

        resendText.setOnClickListener {
            Toast.makeText(this, "Resend OTP not implemented yet", Toast.LENGTH_SHORT).show()
        }

        verifyButton.setOnClickListener {
            val otp = otpEditText.text.toString().trim()

            if (otp.isEmpty()) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
            } else {
                verifyOtp(email, otp)
            }
        }
    }

    private fun verifyOtp(email: String, otp: String) {
        val url = "http://192.168.136.226/educonnect_new_backend/verifyotp.php"

        val json = JSONObject().apply {
            put("email", email)
            put("otp", otp)
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
                Log.e("VERIFY_OTP", "Request failed: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@otpverify, "Network error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("VERIFY_OTP", "Response: $responseBody")

                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val status = jsonResponse.getBoolean("status")
                            val message = jsonResponse.getString("message")

                            Toast.makeText(this@otpverify, message, Toast.LENGTH_LONG).show()

                            if (status) {
                                // ✅ OTP verified — navigate to newpassword screen
                                val intent = Intent(this@otpverify, newpassword::class.java)
                                intent.putExtra("email", email) // Pass email to next screen
                                startActivity(intent)
                                finish()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@otpverify, "Invalid server response", Toast.LENGTH_SHORT).show()
                            Log.e("VERIFY_OTP", "JSON parse error: ${e.message}")
                        }
                    } else {
                        Toast.makeText(this@otpverify, "Error verifying OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
