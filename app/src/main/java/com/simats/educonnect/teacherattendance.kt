package com.simats.educonnect

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.core.view.setPadding

data class StudentAttendance(
    val studentId: Int,
    val present: RadioButton,
    val absent: RadioButton,
    val waiting: RadioButton
)

class TeacherAttendanceActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var submitBtn: Button
    private lateinit var studentContainer: LinearLayout
    private val studentViews = mutableListOf<StudentAttendance>() // Holds student and their RadioButtons

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacherattendance)

        studentContainer = findViewById(R.id.studentContainer) // Parent layout to add students dynamically
        submitBtn = findViewById(R.id.submitBtn)

        val teacherId = 8 // Replace with logged-in teacher ID dynamically

        fetchStudents(teacherId)

        submitBtn.setOnClickListener {
            val studentsArray = JSONArray()

            for (student in studentViews) {
                val status = when {
                    student.present.isChecked -> "Present"
                    student.absent.isChecked -> "Absent"
                    student.waiting.isChecked -> "Waiting"
                    else -> ""
                }
                if (status.isNotEmpty()) {
                    val s = JSONObject()
                    s.put("student_id", student.studentId)
                    s.put("status", status)
                    studentsArray.put(s)
                }
            }

            if (studentsArray.length() == 0) {
                Toast.makeText(this, "Select status for at least one student", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val jsonObject = JSONObject()
            jsonObject.put("class_id", 1)   // optionally set dynamically
            jsonObject.put("subject_id", 8) // change if needed
            jsonObject.put("date", today)
            jsonObject.put("students", studentsArray)

            Log.d("ATTEND_PAYLOAD", jsonObject.toString())
            sendAttendance(jsonObject)
        }
    }

    private fun fetchStudents(teacherId: Int) {
        val url = "http://14.139.187.229:8081/PDD-2025(9thmonth)/EDUCONNECT-back-end-main/get_students_attendance.php?teacher_id=$teacherId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TeacherAttendanceActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body?.string()
                try {
                    val json = JSONObject(responseStr)
                    if (json.getString("status") == "success") {
                        val students = json.getJSONArray("students")
                        runOnUiThread { addStudentsToUI(students) }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@TeacherAttendanceActivity, "No students found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@TeacherAttendanceActivity, "Error parsing response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun addStudentsToUI(students: JSONArray) {
        studentContainer.removeAllViews()
        studentViews.clear()

        // Add bottom padding so the Save button is visible after scrolling
        studentContainer.setPadding(0, 0, 0, 32)

        for (i in 0 until students.length()) {
            val student = students.getJSONObject(i)
            val studentId = student.getInt("student_user_id")
            val studentName = student.getString("student_name")

            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            container.setPadding(16)
            container.setBackgroundColor(resources.getColor(android.R.color.white))
            container.elevation = 8f
            container.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }

            val nameTv = TextView(this)
            nameTv.text = "Student: $studentName"
            nameTv.textSize = 16f
            nameTv.setPadding(8)
            container.addView(nameTv)

            val radioGroup = RadioGroup(this)
            radioGroup.orientation = RadioGroup.HORIZONTAL

            val present = RadioButton(this)
            present.text = "Present"
            radioGroup.addView(present)

            val absent = RadioButton(this)
            absent.text = "Absent"
            radioGroup.addView(absent)

            val waiting = RadioButton(this)
            waiting.text = "Waiting"
            radioGroup.addView(waiting)

            container.addView(radioGroup)
            studentContainer.addView(container)

            studentViews.add(StudentAttendance(studentId, present, absent, waiting))
        }
    }

    private fun sendAttendance(jsonObject: JSONObject) {
        val url = "http://14.139.187.229:8081/PDD-2025(9thmonth)/EDUCONNECT-back-end-main/teacherattendance.php"

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TeacherAttendanceActivity, "Request Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body?.string()
                runOnUiThread {
                    Toast.makeText(this@TeacherAttendanceActivity, responseStr, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
