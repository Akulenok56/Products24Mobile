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

            val tvName = view.findViewById<TextView>(R.id.orderId) // Используем как имя
            val tvStatus = view.findViewById<TextView>(R.id.orderStatus)
            val tvTotal = view.findViewById<TextView>(R.id.orderTotal)

            tvName.text = "Клиент: ${order.userName}"
            tvTotal.text = "${order.totalAmount} ₽"
            tvStatus.text = when(order.status) {
                "Pending" -> "Ожидает"
                "Assembling" -> "Сборка"
                "Shipping" -> "В пути"
                "Completed" -> "Доставлен"
                else -> order.status
            }
            view.setOnClickListener {
                val intent = Intent(this, OrderDetailsActivity::class.java)
                // Передаем ID заказа, чтобы на следующем экране загрузить его товары
                intent.putExtra("ORDER_ID", order.orderID.toString())
                startActivity(intent)
            }
            // Красим статус в оранжевый, если заказ новый
            if (order.status == "Pending") {
                tvStatus.setTextColor(android.graphics.Color.parseColor("#FFA451"))
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