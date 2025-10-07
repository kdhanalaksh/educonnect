package com.saveetha.educonnect

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class studentedit : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var editId: EditText
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editContact: EditText
    private lateinit var editTeacherId: EditText
    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var rbOther: RadioButton
    private lateinit var saveButton: TextView
    private lateinit var backButton: TextView
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_studentedit)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bind views (use camelCase IDs from your XML)
        editId = findViewById(R.id.editId)
        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editContact = findViewById(R.id.editContact)
        editTeacherId = findViewById(R.id.editTeacherId)
        rbMale = findViewById(R.id.rbMale)
        rbFemale = findViewById(R.id.rbFemale)
        rbOther = findViewById(R.id.rbOther)
        saveButton = findViewById(R.id.save_button)
        backButton = findViewById(R.id.back_button)
        deleteButton = findViewById(R.id.btnDeleteAccount)

        // Save button click
        saveButton.setOnClickListener {
            saveStudentProfile()
        }

        // Back button click
        backButton.setOnClickListener {
            finish()
        }

        // Delete account click (if needed)
        deleteButton.setOnClickListener {
            Toast.makeText(this, "Delete account logic goes here", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStudentProfile() {
        val url = "http://192.168.136.226/educonnect_new_backend/update_profile_student.php"

        val gender = when {
            rbMale.isChecked -> "male"
            rbFemale.isChecked -> "female"
            else -> "other"
        }

        // Build JSON request
        val json = JSONObject().apply {
            put("student_id", editId.text.toString().trim())   // ✅ matches backend
            put("name", editName.text.toString().trim())
            put("email", editEmail.text.toString().trim())
            put("contact_number", editContact.text.toString().trim())
            put("gender", gender)
            put("teacher_id", editTeacherId.text.toString().trim())
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@studentedit, "Update Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { res ->
                    val jsonResponse = JSONObject(res)
                    runOnUiThread {
                        if (jsonResponse.optString("status") == "success") {
                            Toast.makeText(this@studentedit, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            val msg = jsonResponse.optString("message", "Unknown error")
                            Toast.makeText(this@studentedit, "Update Failed: $msg", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
