package com.saveetha.educonnect

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import android.view.LayoutInflater
import android.view.ViewGroup


// ---------------- Data Class ----------------
data class Meeting(
    val title: String,
    val timeRange: String,
    val purpose: String,
    val status: String,
    val link: String?
)

// ---------------- Adapter ----------------
class MeetingAdapter(private val meetings: List<Meeting>) :
    RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder>() {

    class MeetingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvPurpose: TextView = view.findViewById(R.id.tvPurpose)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvLink: TextView = view.findViewById(R.id.tvLink)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_meeting, parent, false)
        return MeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val meeting = meetings[position]
        holder.tvTitle.text = meeting.title
        holder.tvTime.text = meeting.timeRange
        holder.tvPurpose.text = meeting.purpose
        holder.tvStatus.text = meeting.status

        if (meeting.link.isNullOrEmpty()) {
            holder.tvLink.visibility = View.GONE
        } else {
            holder.tvLink.visibility = View.VISIBLE
            holder.tvLink.text = "Join: ${meeting.link}"
            holder.tvLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meeting.link))
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = meetings.size
}

// ---------------- Activity ----------------
class SharedUpdates : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MeetingAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoMeetings: TextView
    private var meetingList = ArrayList<Meeting>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sharedupdates)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvNoMeetings = findViewById(R.id.tvNoMeetings)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MeetingAdapter(meetingList)
        recyclerView.adapter = adapter

        val sharedPref = getSharedPreferences("EduConnectPrefs", Context.MODE_PRIVATE)
        val teacherId = sharedPref.getInt("user_id", -1)
        if (teacherId != -1) {
            fetchMeetings(teacherId)
        } else {
            Toast.makeText(this, "Teacher not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchMeetings(teacherId: Int) {
        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvNoMeetings.visibility = View.GONE
        }

        val url =
            "http://192.168.136.226/educonnect_new_backend/teacher_shared_updates.php?teacher_id=$teacherId"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@SharedUpdates,
                        "Failed to fetch updates: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("SharedUpdates", "Network error", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    try {
                        val json = JSONObject(body ?: "{}")
                        if (json.optString("status") == "success") {
                            val meetingsJson = json.getJSONArray("meetings")
                            meetingList.clear()
                            for (i in 0 until meetingsJson.length()) {
                                val item = meetingsJson.getJSONObject(i)
                                val meeting = Meeting(
                                    item.optString("title"),
                                    item.optString("time_range"),
                                    item.optString("purpose"),
                                    item.optString("status"),
                                    item.optString("meeting_link")
                                )
                                meetingList.add(meeting)
                            }
                            runOnUiThread {
                                progressBar.visibility = View.GONE
                                if (meetingList.isEmpty()) {
                                    tvNoMeetings.visibility = View.VISIBLE
                                    recyclerView.visibility = View.GONE
                                } else {
                                    tvNoMeetings.visibility = View.GONE
                                    recyclerView.visibility = View.VISIBLE
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        } else {
                            runOnUiThread {
                                progressBar.visibility = View.GONE
                                tvNoMeetings.visibility = View.VISIBLE
                                tvNoMeetings.text = json.optString("message")
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            tvNoMeetings.visibility = View.VISIBLE
                            tvNoMeetings.text = "Parsing error: ${e.message}"
                            Log.e("SharedUpdates", "JSON parse error", e)
                        }
                    }
                }
            }
        })
    }
}
