package com.example.products24

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.products24.data.model.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEd: EditText
    private lateinit var passEd: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // инициализация Retrofit
        RetrofitInstance.init(
            applicationContext,
            "http://10.0.2.2:5000/"   // или твой реальный URL
        )

        loginEd = findViewById(R.id.loginEd)
        passEd = findViewById(R.id.passEd)
        btnLogin = findViewById(R.id.btnStart)

        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val login = loginEd.text.toString().trim()
        val pass = passEd.text.toString().trim()

        if (login.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitInstance.authApi()
                val response = api.login(LoginRequest(login, pass))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {

                        val token = response.body()!!.token

                        Session.saveToken(applicationContext, token)

                        Toast.makeText(this@LoginActivity, "Успешный вход", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(this@LoginActivity, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Ошибка сервера: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
