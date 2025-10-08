package com.simats.educonnect
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater 
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

// ---------------------- Adapter Class ----------------------
class StudentMeetingAdapter(
    private val context: Context,
    private val meetings: MutableList<JSONObject>
) : RecyclerView.Adapter<StudentMeetingAdapter.MeetingViewHolder>() {

    class MeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val tvPurpose: TextView = itemView.findViewById(R.id.tvPurpose)
        val tvLink: TextView = itemView.findViewById(R.id.tvLink)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_meetings, parent, false)
        return MeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val meeting = meetings[position]

        holder.tvTitle.text = meeting.optString("title", "Untitled Meeting")
        holder.tvDateTime.text = "${meeting.optString("meeting_date", "N/A")} | ${meeting.optString("start_time", "00:00")} - ${meeting.optString("end_time", "00:00")}"
        holder.tvPurpose.text = "Purpose: ${meeting.optString("purpose", "N/A")}"

        val link = meeting.optString("meeting_link", "")
        if (link.isNotEmpty() && link != "NULL") {
            holder.tvLink.text = link
            holder.tvLink.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Invalid link", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.tvLink.text = "No link provided"
            holder.tvLink.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = meetings.size

    fun updateData(newMeetings: List<JSONObject>) {
        meetings.clear()
        meetings.addAll(newMeetings)
        notifyDataSetChanged()
    }
}

// ---------------------- Activity Class ----------------------
class activitystudent_meeting : AppCompatActivity() {

    private val client = OkHttpClient()
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoMeetings: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentMeetingAdapter
    private var studentId: Int = -1

    private val CHANNEL_ID = "student_meeting_channel"
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var countDownTimer: CountDownTimer
    private var isThirtySecondAlerted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activitystudent_meeting)

        progressBar = findViewById(R.id.progressBar)
        tvNoMeetings = findViewById(R.id.tvNoMeetings)
        recyclerView = findViewById(R.id.metRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = StudentMeetingAdapter(this, mutableListOf())
        recyclerView.adapter = adapter

        createNotificationChannel()

        studentId = intent.getIntExtra("student_id", -1)

        if (studentId == -1) {
            val prefs = getSharedPreferences("EduConnectPrefs", MODE_PRIVATE)
            studentId = prefs.getInt("student_id", -1)
        }

        if (studentId != -1) {
            fetchMeetings(studentId)
        } else {
            Toast.makeText(this, "Invalid student ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Student Meeting Notifications"
            val descriptionText = "Notifications for student meeting alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun fetchMeetings(studentId: Int) {
        progressBar.visibility = View.VISIBLE
        tvNoMeetings.visibility = View.GONE

        val request = Request.Builder()
            .url("http://14.139.187.229:8081/PDD-2025(9thmonth)/EDUCONNECT-back-end-main/get_students_meetings.php?student_id=$studentId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@activitystudent_meeting, "Failed to fetch meetings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    Log.d("StudentMeeting", "Response: $body")
                    if (!it.isSuccessful || body == null) {
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this@activitystudent_meeting, "Error loading meetings", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val json = JSONObject(body)
                    if (json.optString("status") == "success") {
                        val meetings = json.optJSONArray("meetings")

                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            if (meetings == null || meetings.length() == 0) {
                                tvNoMeetings.visibility = View.VISIBLE
                            } else {
                                val meetingList = mutableListOf<JSONObject>()
                                for (i in 0 until meetings.length()) {
                                    meetingList.add(meetings.getJSONObject(i))
                                }
                                adapter.updateData(meetingList)

                                // ✅ Show notification and start timer for buzzer alerts
                                showNotification("Meeting Alert", "You have ${meetingList.size} upcoming meeting(s).")
                                startMeetingTimer()
                            }
                        }
                    } else {
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            tvNoMeetings.visibility = View.VISIBLE
                        }
                    }
                }
            }
        })
    }

    private fun startMeetingTimer() {
        isThirtySecondAlerted = false
        countDownTimer = object : CountDownTimer(60000, 1000) { // 1 minute with 1-second interval
            override fun onTick(millisUntilFinished: Long) {
                if (!isThirtySecondAlerted && millisUntilFinished <= 30000) {
                    isThirtySecondAlerted = true
                    showNotification("Meeting Alert", "Only 30 seconds left!")
                    playBuzzer()
                }
            }

            override fun onFinish() {
                showNotification("Meeting Alert", "1 minute over!")
                playBuzzer()
            }
        }.start()
    }

    private fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    private fun playBuzzer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.buzzer)
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}
