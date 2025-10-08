package com.simats.educonnect

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.simats.educonnect.network.AppConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.util.*

class RescheduleMeeting : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var idEdit: EditText
    private lateinit var teacherIdEdit: EditText
    private lateinit var dateEdit: EditText
    private lateinit var startTimeEdit: EditText
    private lateinit var endTimeEdit: EditText
    private lateinit var rescheduleBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reschedulemeeting)

        idEdit = findViewById(R.id.idEdit)   // ✅ matches XML
        teacherIdEdit = findViewById(R.id.teacherIdEdit)
        dateEdit = findViewById(R.id.dateEdit)
        startTimeEdit = findViewById(R.id.startTimeEdit)
        endTimeEdit = findViewById(R.id.endTimeEdit)
        rescheduleBtn = findViewById(R.id.rescheduleBtn)

        // 📅 Date picker
        dateEdit.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    dateEdit.setText(dateStr)
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }

        // ⏰ Start time picker
        startTimeEdit.setOnClickListener {
            val c = Calendar.getInstance()
            val tpd = TimePickerDialog(this,
                { _, hour, minute ->
                    val timeStr = String.format("%02d:%02d:00", hour, minute)
                    startTimeEdit.setText(timeStr)
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            )
            tpd.show()
        }

        // ⏰ End time picker
        endTimeEdit.setOnClickListener {
            val c = Calendar.getInstance()
            val tpd = TimePickerDialog(this,
                { _, hour, minute ->
                    val timeStr = String.format("%02d:%02d:00", hour, minute)
                    endTimeEdit.setText(timeStr)
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            )
            tpd.show()
        }

        // 🚀 Send request
        rescheduleBtn.setOnClickListener {
            val id = idEdit.text.toString().trim()
            val teacherId = teacherIdEdit.text.toString().trim()
            val date = dateEdit.text.toString().trim()
            val startTime = startTimeEdit.text.toString().trim()
            val endTime = endTimeEdit.text.toString().trim()

            if (id.isEmpty() || teacherId.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                sendRescheduleRequest(id, teacherId, date, startTime, endTime)
            }
        }
    }

    private fun sendRescheduleRequest(id: String, teacherId: String, date: String, startTime: String, endTime: String) {
        val jsonObject = JSONObject()
        jsonObject.put("id", id.toInt()) // ✅ use correct column name from DB
        jsonObject.put("teacher_id", teacherId.toInt())
        jsonObject.put("meeting_date", date)
        jsonObject.put("start_time", startTime)
        jsonObject.put("end_time", endTime)

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(JSON, jsonObject.toString())

        val request = Request.Builder()
            .url( AppConfig.BASE_URL + "reschedulemeeting.php")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RescheduleMeeting, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    Toast.makeText(this@RescheduleMeeting, "Response: $responseData", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
