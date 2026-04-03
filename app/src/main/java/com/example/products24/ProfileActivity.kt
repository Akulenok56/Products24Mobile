package com.example.products24

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.products24.data.api.AuthApi
import com.example.products24.data.model.OrderHistoryDto
import com.example.products24.data.model.UserProfileResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var ordersContainer: LinearLayout
    private lateinit var tvPhone: TextView // Объяви в начале класса

    // Регистратор для выбора изображения из галереи
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                uploadAvatar(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Инициализация View
        ivAvatar = findViewById(R.id.ivAvatar)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        ordersContainer = findViewById(R.id.ordersContainer)

        tvPhone = findViewById(R.id.tvPhone)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Слушатели событий
        btnBack.setOnClickListener { finish() }

        btnLogout.setOnClickListener {
            val sharedPref = getSharedPreferences("auth", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java) // Укажи свою Activity логина
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        ivAvatar.setOnClickListener {
            openGallery()
        }

        loadProfile()
        loadOrderHistory()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun loadProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Используем твой RetrofitInstance (или RetrofitClient)
                val response = RetrofitInstance.create(AuthApi::class.java).getProfile()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        user?.let {
                            tvFullName.text = it.fullName
                            tvEmail.text = it.email
                            tvPhone.text = it.phoneNumber ?: "Номер не указан"

                            if (!it.photoUrl.isNullOrEmpty()) {
                                Glide.with(this@ProfileActivity)
                                    .load(it.photoUrl)
                                    .placeholder(R.drawable.basket)
                                    .centerCrop()
                                    .into(ivAvatar)
                            }
                        }
                    } else if (response.code() == 401) {
                        // Токен невалиден — на выход
                        finish()
                    } else {
                        // ОБЯЗАТЕЛЬНЫЙ КОРЕННОЙ ELSE
                        // Здесь обрабатываем все остальные ошибки (500, 404 и т.д.)
                        Toast.makeText(this@ProfileActivity, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error: ${e.message}")
            }
        }
    }

    private fun loadOrderHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.create(AuthApi::class.java).getMyOrders()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        displayOrders(response.body() ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun displayOrders(orders: List<OrderHistoryDto>) {
        ordersContainer.removeAllViews()
        for (order in orders) {
            val view = layoutInflater.inflate(R.layout.item_order, ordersContainer, false)
            view.findViewById<TextView>(R.id.orderId).text = "Заказ №${order.orderID.take(8)}"
            view.findViewById<TextView>(R.id.orderDate).text = order.orderDate.split("T")[0]
            view.findViewById<TextView>(R.id.orderTotal).text = "${order.totalAmount} ₽"
            view.findViewById<TextView>(R.id.orderStatus).text = order.status
            ordersContainer.addView(view)
        }
    }

    private fun uploadAvatar(uri: Uri) {
        val file = getFileFromUri(uri) ?: return
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.create(AuthApi::class.java).uploadAvatar(body)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        loadProfile()
                        Toast.makeText(this@ProfileActivity, "Аватар обновлен!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Upload error", e)
            }
        }
    }

    // Вспомогательная функция для конвертации Uri в File
    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val file = File(cacheDir, "temp_avatar.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}