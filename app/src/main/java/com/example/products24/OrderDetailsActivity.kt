package com.example.products24

import android.os.Bundle
import android.widget.ImageView // Если захочешь добавить кнопку назад
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.products24.data.api.AuthApi
import com.example.products24.data.model.OrderDetailsDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var rvItems: RecyclerView
    private lateinit var tvTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        // Инициализация View
        rvItems = findViewById(R.id.rvOrderItems)
        tvTotal = findViewById(R.id.tvOrderDetailsTotal)

        // Настройка RecyclerView
        rvItems.layoutManager = LinearLayoutManager(this)

        // Получаем ID заказа из Intent
        val orderId = intent.getStringExtra("ORDER_ID")

        if (!orderId.isNullOrEmpty()) {
            loadOrderInfo(orderId)
        } else {
            Toast.makeText(this, "ID заказа не найден", Toast.LENGTH_SHORT).show()
            finish() // Закрываем экран, если нет ID
        }
    }

    private fun loadOrderInfo(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)
                val response = api.getOrderById(id)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val orderDetails = response.body()

                        if (orderDetails != null) {

                            findViewById<TextView>(R.id.tvDetailCustomerName).text = "Клиент: ${orderDetails.userName}"
                            findViewById<TextView>(R.id.tvDetailPhone).text = "Тел: ${orderDetails.phoneNumber}"
                            findViewById<TextView>(R.id.tvDetailAddress).text = "Адрес: ${orderDetails.address}"
                            tvTotal.text = "Итого: ${orderDetails.totalAmount} ₽"


                            rvItems.adapter = OrderItemsAdapter(orderDetails.orderItems)
                        } else {
                            Toast.makeText(this@OrderDetailsActivity, "Данные заказа пусты", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@OrderDetailsActivity, "Ошибка сервера: $errorBody", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    Toast.makeText(this@OrderDetailsActivity, "Проблема с сетью: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}