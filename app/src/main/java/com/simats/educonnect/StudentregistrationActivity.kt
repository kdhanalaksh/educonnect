package com.simats.educonnect

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.simats.educonnect.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class StudentregistrationActivity : AppCompatActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var inputContact: EditText
    private lateinit var radioGender: RadioGroup
    private lateinit var btnSubmit: Button

    private val client = OkHttpClient()
    private val backendUrl = "http://14.139.187.229:8081/PDD-2025(9thmonth)/EDUCONNECT-back-end-main/register.php"  // change to your PHP file path

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.studentregistration) // replace with your XML file name

        // Bind views
        inputName = findViewById(R.id.inputName)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        spinnerRole = findViewById(R.id.spinnerRole)
        inputContact = findViewById(R.id.inputContact)
        radioGender = findViewById(R.id.radioGender)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Example role spinner items
        val roles = listOf("student", "teacher", "admin")
        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        // On button click → send request
        btnSubmit.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = inputName.text.toString().trim()
        val email = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString().trim()
        val role = spinnerRole.selectedItem.toString()
        val contact = inputContact.text.toString().trim()
        val genderId = radioGender.checkedRadioButtonId
        val gender = if (genderId != -1) findViewById<RadioButton>(genderId).text.toString() else ""

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty() || contact.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create JSON body
        val json = JSONObject()
        json.put("name", name)
        json.put("email", email)
        json.put("password", password)
        json.put("role", role)
        json.put("contact_number", contact)
        json.put("gender", gender)
        json.put("profile_picture", JSONObject.NULL) // optional

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())

        val request = Request.Builder()
            .url(backendUrl)
            .post(body)
            .build()

        // Send async
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@StudentregistrationActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonObj = JSONObject(responseBody ?: "")
                        val status = jsonObj.optString("status")
                        val message = jsonObj.optString("message")

                        Toast.makeText(this@StudentregistrationActivity, message, Toast.LENGTH_LONG).show()

                        if (status == "success") {
                            // Navigate to login screen
                            val intent = Intent(this@StudentregistrationActivity, StudentLoginActivity::class.java)
                            startActivity(intent)
                            finish() // close registration screen
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this@StudentregistrationActivity, "Unexpected response", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
