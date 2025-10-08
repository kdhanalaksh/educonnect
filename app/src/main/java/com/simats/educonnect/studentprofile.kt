package com.simats.educonnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class studentprofile : AppCompatActivity() {

    // Views
    private lateinit var profilePicture: ImageView
    private lateinit var tvId: TextView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvContact: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvTeacherId: TextView
    private lateinit var btnEdit: Button

    private val client = OkHttpClient()
    private var studentData: JSONObject? = null   // store fetched data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_studentprofile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        profilePicture = findViewById(R.id.profilePicture)
        tvId = findViewById(R.id.tvId)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvContact = findViewById(R.id.tvContact)
        tvGender = findViewById(R.id.tvGender)
        tvTeacherId = findViewById(R.id.tvTeacherId)
        btnEdit = findViewById(R.id.btnEdit)

        val studentId = 1
        fetchStudentProfile(studentId)

        // Handle Edit button click
        btnEdit.setOnClickListener {
            studentData?.let {
                val intent = Intent(this@studentprofile, studentedit::class.java)
                intent.putExtra("studentData", it.toString())  // send JSON as string
                startActivity(intent)
            } ?: Toast.makeText(this, "Data not loaded yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchStudentProfile(studentId: Int) {
        val url = "http://14.139.187.229:8081/PDD-2025(9thmonth)/EDUCONNECT-back-end-main/studentprofile.php"
        val json = JSONObject()
        json.put("student_id", studentId)

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@studentprofile, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { res ->
                    val jsonResponse = JSONObject(res)
                    if (jsonResponse.getString("status") == "success") {
                        studentData = jsonResponse.getJSONObject("student")  // save data
                        val student = studentData!!

                        runOnUiThread {
                            tvId.text = "ID: ${student.getInt("id")}"
                            tvName.text = "Name: ${student.getString("name")}"
                            tvEmail.text = "Email: ${student.getString("email")}"
                            tvContact.text = "Contact: ${student.getString("contact_number")}"
                            tvGender.text = "Gender: ${student.getString("gender")}"
                            tvTeacherId.text = "Teacher ID: ${student.getInt("teacher_id")}"

                            Glide.with(this@studentprofile)
                                .load("https://yourdomain.com/uploads/${student.getString("profile_picture")}")
                                .placeholder(R.drawable.profiles)
                                .into(profilePicture)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@studentprofile, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
