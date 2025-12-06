package com.example.products24

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.products24.data.model.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegActivity : AppCompatActivity() {

    private lateinit var loginEd: EditText
    private lateinit var passEd: EditText
    private lateinit var repPassEd: EditText
    private lateinit var emailEd: EditText
    private lateinit var phoneEd: EditText
    private lateinit var cityEd: EditText
    private lateinit var streetEd: EditText
    private lateinit var numStrEd: EditText
    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)


        RetrofitInstance.init(
            applicationContext,
            "http://10.0.2.2:5000/"
        )

        initViews()

        btnStart.setOnClickListener {
            registerUser()
        }
    }

    private fun initViews() {
        loginEd = findViewById(R.id.loginEd)
        passEd = findViewById(R.id.passEd)
        repPassEd = findViewById(R.id.repPassEd)
        emailEd = findViewById(R.id.emailEd)
        phoneEd = findViewById(R.id.phoneEd)
        cityEd = findViewById(R.id.cityEd)
        streetEd = findViewById(R.id.streetEd)
        numStrEd = findViewById(R.id.numStrEd)
        btnStart = findViewById(R.id.btnStart)
    }

    private fun registerUser() {
        val login = loginEd.text.toString().trim()
        val pass = passEd.text.toString().trim()
        val repass = repPassEd.text.toString().trim()
        val email = emailEd.text.toString().trim()
        val phone = phoneEd.text.toString().trim()
        val city = cityEd.text.toString().trim()
        val street = streetEd.text.toString().trim()
        val house = numStrEd.text.toString().trim()

        if (login.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != repass) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RegisterRequest(
            password = pass,
            email = email,
            phoneNumber = phone,
            city = city,
            street = street,
            houseNumber = house,
            fullName = login
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitInstance.authApi()
                val response = api.register(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {

                        Toast.makeText(this@RegActivity, "Регистрация успешна", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this@RegActivity, LoginActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(this@RegActivity, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegActivity, "Ошибка сервера: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
