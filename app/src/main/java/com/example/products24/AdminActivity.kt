package com.example.products24

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.products24.data.api.AuthApi
import com.example.products24.data.model.OrderDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {

    private lateinit var ordersContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        ordersContainer = findViewById(R.id.adminOrdersContainer)
        val btnAddProduct = findViewById<Button>(R.id.btnGoToAddProduct)

        btnAddProduct.setOnClickListener {
            // Переход на экран создания товара (создадим его следующим шагом)
            startActivity(Intent(this, AdminAddProduct::class.java))
        }

        loadAllSystemOrders()
    }

    private fun loadAllSystemOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)
                val orders = api.getAllOrders() // Этот метод мы добавили в AuthApi

                withContext(Dispatchers.Main) {
                    displayOrders(orders)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayOrders(orders: List<OrderDto>) {
        ordersContainer.removeAllViews()

        orders.forEach { order ->
            val view = layoutInflater.inflate(R.layout.item_order, ordersContainer, false)

            val tvName = view.findViewById<TextView>(R.id.orderId)
            val tvStatus = view.findViewById<TextView>(R.id.orderStatus)
            val tvTotal = view.findViewById<TextView>(R.id.orderTotal)

            // 1. Добавляем отображение курьера (если в макете есть подходящее поле,
            // например tvCourier, если нет — можно добавить в tvName через перенос строки)
            val courierInfo = if (!order.courierName.isNullOrEmpty() && order.courierName != "Не назначен") {
                "\n🚚 Курьер: ${order.courierName}"
            } else {
                "\n⌛ Курьер: Не назначен"
            }

            tvName.text = "Клиент: ${order.userName}$courierInfo"
            tvTotal.text = "${order.totalAmount} ₽"

            tvStatus.text = when(order.status) {
                "Новый", "Pending" -> "⚡ Новый"
                "В пути", "Shipped" -> "🚚 Доставляется"
                "Доставлен", "Completed" -> "✅ Завершен"
                else -> order.status
            }

            when (order.status) {
                "Новый", "Pending" -> {
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#FFA451")) // Оранжевый
                }
                "В пути", "Shipped" -> {
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#2196F3")) // Синий
                }
                "Доставлен", "Completed" -> {
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Зеленый
                }
            }

            view.setOnClickListener {
                val intent = Intent(this, OrderDetailsActivity::class.java)
                intent.putExtra("ORDER_ID", order.orderID)
                startActivity(intent)
            }

            ordersContainer.addView(view)
        }
    }

    // Обновляем список при возвращении на экран
    override fun onResume() {
        super.onResume()
        loadAllSystemOrders()
    }
}