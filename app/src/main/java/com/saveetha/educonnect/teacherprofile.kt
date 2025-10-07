package com.saveetha.educonnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TeacherProfileActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var subjectTextView: TextView
    private lateinit var qualificationsTextView: TextView
    private lateinit var experienceTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var editProfileButton: Button

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacherprofile)

        // Bind views
        nameTextView = findViewById(R.id.profileName)
        emailTextView = findViewById(R.id.profileEmail)
        phoneTextView = findViewById(R.id.profilePhone)
        genderTextView = findViewById(R.id.profileGender)
        subjectTextView = findViewById(R.id.profileSubject)
        qualificationsTextView = findViewById(R.id.profileQualifications)
        experienceTextView = findViewById(R.id.profileExperience)
        profileImageView = findViewById(R.id.profileImage)
        editProfileButton = findViewById(R.id.btnEditProfile)

        // Read user_id from SharedPreferences
        val sharedPref = getSharedPreferences("EduConnectPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        // Navigate to edit profile
        editProfileButton.setOnClickListener {
            val intent = Intent(this, TeacherProfileEdit::class.java)
            startActivity(intent)
        }
    }

    // Reload profile whenever activity resumes (after editing)
    override fun onResume() {
        super.onResume()
        if (userId != -1) {
            fetchTeacherProfile(userId)
        }
    }

    private fun fetchTeacherProfile(userId: Int) {
        val url = "http://192.168.136.226/educonnect_new_backend/get_teacher_profile.php"
        val request = Request.Builder()
            .url("$url?user_id=$userId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TeacherProfileActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseData = it.body?.string()
                    Log.d("TeacherProfile", "Response: $responseData")

                    if (!it.isSuccessful || responseData.isNullOrEmpty()) {
                        runOnUiThread {
                            Toast.makeText(this@TeacherProfileActivity, "Server error", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    try {
                        val json = JSONObject(responseData)
                        if (json.getString("status") == "success") {
                            val teacher = json.getJSONObject("teacher")
                            runOnUiThread {
                                nameTextView.text = "Name: ${teacher.optString("name", "N/A")}"
                                emailTextView.text = "Email: ${teacher.optString("email", "N/A")}"
                                phoneTextView.text = "Phone: ${teacher.optString("contact_number", "N/A")}"
                                genderTextView.text = "Gender: ${teacher.optString("gender", "N/A")}"
                                subjectTextView.text = "Subject: ${teacher.optString("subject_name", "N/A")}"
                                qualificationsTextView.text = "Qualifications: ${teacher.optString("qualifications", "N/A")}"
                                experienceTextView.text = "Experience: ${teacher.optString("experience", "0")} years"
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@TeacherProfileActivity, json.optString("message", "Profile not found"), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@TeacherProfileActivity, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
