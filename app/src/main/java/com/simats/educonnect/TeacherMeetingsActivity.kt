package com.simats.educonnect


import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class TeacherMeetingsActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var meetingsListView: ListView
    private lateinit var meetingsTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_meetings)

        meetingsListView = findViewById(R.id.meetingsListView)
        meetingsTitle = findViewById(R.id.meetingsTitle)

        // Teacher ID (e.g., from login/session)
        val teacherId = 8  // Replace with dynamic ID

        fetchMeetings(teacherId)
    }

    private fun fetchMeetings(teacherId: Int) {
        val json = JSONObject().put("teacher_id", teacherId)
        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url("http://14.139.187.229:8081/PDD-2025(9thmonth)/EDUCONNECT-back-end-main/get_meetings.php") // update IP
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TeacherMeetingsActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resStr = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonRes = JSONObject(resStr ?: "")
                        if (jsonRes.getString("status") == "success") {
                            val meetings = jsonRes.getJSONArray("meetings")
                            val listItems = ArrayList<String>()

                            for (i in 0 until meetings.length()) {
                                val m = meetings.getJSONObject(i)
                                val title = m.getString("title")
                                val date = m.getString("meeting_date")
                                val time = m.getString("start_time") + " - " + m.getString("end_time")
                                val purpose = m.getString("purpose")
                                listItems.add("$title\n$date $time\nPurpose: $purpose")
                            }

                            val adapter = ArrayAdapter(this@TeacherMeetingsActivity,
                                android.R.layout.simple_list_item_1, listItems)
                            meetingsListView.adapter = adapter

                        } else {
                            Toast.makeText(this@TeacherMeetingsActivity, "No meetings found", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@TeacherMeetingsActivity, "Error parsing", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
