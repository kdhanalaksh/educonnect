package com.saveetha.educonnect

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class TeacherProfileEdit : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var nameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var genderEdit: EditText
    private lateinit var subjectEdit: EditText
    private lateinit var qualificationEdit: EditText
    private lateinit var experienceEdit: EditText
    private lateinit var updateBtn: Button

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacherprofileedit)

        // Bind UI
        nameEdit = findViewById(R.id.editName)
        emailEdit = findViewById(R.id.editEmail)
        phoneEdit = findViewById(R.id.editPhone)
        genderEdit = findViewById(R.id.editGender)
        subjectEdit = findViewById(R.id.editSubject)
        qualificationEdit = findViewById(R.id.editQualification)
        experienceEdit = findViewById(R.id.editExperience)
        updateBtn = findViewById(R.id.btnUpdateProfile)

        // Get logged-in user_id from SharedPreferences
        val sharedPref = getSharedPreferences("EduConnectPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Load current profile data into EditTexts
        loadCurrentProfile()

        // Update profile button click
        updateBtn.setOnClickListener {
            updateTeacherProfile()
        }
    }

    private fun loadCurrentProfile() {
        val url = "http://192.168.136.226/educonnect_new_backend/get_teacher_profile.php?user_id=$userId"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TeacherProfileEdit, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("TeacherProfileEdit", "Load profile error", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    Log.d("TeacherProfileEdit", "Profile Response: $body")
                    try {
                        val json = JSONObject(body ?: "{}")
                        if (json.optString("status") == "success") {
                            val teacher = json.getJSONObject("teacher")
                            runOnUiThread {
                                nameEdit.setText(teacher.optString("name", ""))
                                emailEdit.setText(teacher.optString("email", ""))
                                phoneEdit.setText(teacher.optString("contact_number", ""))
                                genderEdit.setText(teacher.optString("gender", ""))
                                subjectEdit.setText(teacher.optString("subject_id", ""))
                                qualificationEdit.setText(teacher.optString("qualifications", ""))
                                experienceEdit.setText(teacher.optString("experience", ""))
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@TeacherProfileEdit, json.optString("message", "Error loading profile"), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@TeacherProfileEdit, "Parsing error: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("TeacherProfileEdit", "JSON parse error", e)
                        }
                    }
                }
            }
        })
    }

    private fun updateTeacherProfile() {
        val name = nameEdit.text.toString().trim()
        val email = emailEdit.text.toString().trim()
        val phone = phoneEdit.text.toString().trim()
        val gender = genderEdit.text.toString().trim()
        val subjectId = subjectEdit.text.toString().trim().toIntOrNull() ?: 0
        val qualification = qualificationEdit.text.toString().trim()
        val experience = experienceEdit.text.toString().trim().toIntOrNull() ?: 0

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || gender.isEmpty() ||
            subjectId == 0 || qualification.isEmpty() || experience == 0
        ) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        // Match PHP expected keys exactly
        val json = JSONObject().apply {
            put("user_id", userId)
            put("name", name)
            put("email", email)
            put("phone", phone)
            put("gender", gender)
            put("subject_id", subjectId)
            put("qualifications", qualification)
            put("experience", experience)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://192.168.136.226/educonnect_new_backend/update_teacher_profile.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TeacherProfileEdit, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("TeacherProfileEdit", "Update failed", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = it.body?.string()
                    Log.d("TeacherProfileEdit", "Update Response: $responseBody")
                    runOnUiThread {
                        try {
                            val jsonResponse = JSONObject(responseBody ?: "{}")
                            val status = jsonResponse.optString("status")
                            val message = jsonResponse.optString("message")

                            Toast.makeText(this@TeacherProfileEdit, message, Toast.LENGTH_SHORT).show()
                            if (status == "success") finish() // Go back to profile screen
                        } catch (ex: Exception) {
                            Toast.makeText(this@TeacherProfileEdit, "Invalid response from server", Toast.LENGTH_LONG).show()
                            Log.e("TeacherProfileEdit", "JSON parse error", ex)
                        }
                    }
                }
            }
        })
    }
}
