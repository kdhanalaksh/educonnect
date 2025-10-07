package com.saveetha.educonnect

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class createevent : AppCompatActivity() {

    private lateinit var etMeetingTitle: EditText
    private lateinit var etMeetingDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var etPurpose: EditText
    private lateinit var etMeetingLink: EditText
    private lateinit var cbNotifyParents: CheckBox
    private lateinit var saveButton: TextView
    private lateinit var backButton: TextView

    private var teacherId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createevent)

        etMeetingTitle = findViewById(R.id.etMeetingTitle)
        etMeetingDate = findViewById(R.id.etMeetingDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        etPurpose = findViewById(R.id.etPurpose)
        etMeetingLink = findViewById(R.id.etMeetingLink)
        cbNotifyParents = findViewById(R.id.cbNotifyParents)
        saveButton = findViewById(R.id.save_button)
        backButton = findViewById(R.id.back_button)

        // ✅ Retrieve teacherId from Intent extras
        teacherId = intent.getIntExtra("teacher_id", 0)

        if (teacherId == 0) {
            Toast.makeText(this, "Teacher ID not found. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        etMeetingDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etMeetingDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        etStartTime.setOnClickListener { showTimePicker(etStartTime) }
        etEndTime.setOnClickListener { showTimePicker(etEndTime) }

        saveButton.setOnClickListener { saveMeeting() }
        backButton.setOnClickListener { finish() }
    }

    private fun showTimePicker(editText: EditText) {
        val c = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            editText.setText(String.format("%02d:%02d:00", hour, minute))
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }

    private fun saveMeeting() {
        val title = etMeetingTitle.text.toString().trim()
        val date = etMeetingDate.text.toString().trim()
        val startTime = etStartTime.text.toString().trim()
        val endTime = etEndTime.text.toString().trim()
        val purpose = etPurpose.text.toString().trim()
        val meetingLink = etMeetingLink.text.toString().trim()
        val notifyParents = if (cbNotifyParents.isChecked) 1 else 0

        if (title.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || purpose.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "http://192.168.136.226/educonnect_new_backend/teachercreatemeeting.php"

        val jsonBody = JSONObject().apply {
            put("teacher_id", teacherId)
            put("title", title)
            put("meeting_date", date)
            put("start_time", startTime)
            put("end_time", endTime)
            put("purpose", purpose)
            put("notify_parents", notifyParents)
            put("meeting_link", meetingLink)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
}
