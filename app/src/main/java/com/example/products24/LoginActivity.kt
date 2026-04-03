package com.example.products24

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.products24.data.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEd: EditText
    private lateinit var passEd: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnToReg: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Инициализация Retrofit (лучше делать один раз в Application классе, но оставляем тут)
        RetrofitInstance.init(applicationContext, "http://10.0.2.2:5162/")

        loginEd = findViewById(R.id.loginEd)
        passEd = findViewById(R.id.passEd)
        btnLogin = findViewById(R.id.btnStart)
        btnToReg = findViewById(R.id.btnToReg)

        btnToReg.setOnClickListener {
            startActivity(Intent(this, RegActivity::class.java))
        }

        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val email = loginEd.text.toString().trim()
        val pass = passEd.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Введите почту и пароль", Toast.LENGTH_SHORT).show()
            return
        }

        // Используем lifecycleScope вместо CoroutineScope для автоматической очистки при закрытии Activity
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitInstance.authApi()
                val response = api.login(LoginRequest(email, pass))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginData = response.body()!!

                        // 1. Сохраняем токен
                        Session.saveToken(applicationContext, loginData.token)

                        // 2. Сохраняем роль локально (пригодится для проверки внутри приложения)
                        val sharedPref = getSharedPreferences("auth", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("user_role", loginData.role).apply()

                        Toast.makeText(this@LoginActivity, "Успешный вход: ${loginData.role}", Toast.LENGTH_SHORT).show()

                        // 3. Навигация в зависимости от роли
                        navigateToRoleScreen(loginData.role)

                    } else {
                        Toast.makeText(this@LoginActivity, "Неверная почта или пароль", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToRoleScreen(role: String) {
        val intent = when (role.lowercase()) {
            "admin" -> Intent(this, AdminActivity::class.java)
            "courier" -> Intent(this, CourierActivity::class.java)
            else -> Intent(this, MainActivity::class.java) // По умолчанию — обычный клиент
        }


        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}