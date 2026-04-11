package com.example.products24

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.products24.data.api.AuthApi
import com.example.products24.data.model.OrderDto
import com.example.products24.data.model.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {

    private lateinit var mainContainer: LinearLayout
    private lateinit var titleLabel: TextView
    private lateinit var btnOrders: Button
    private lateinit var btnProducts: Button
    private lateinit var btnUsers: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Инициализация View
        mainContainer = findViewById(R.id.adminOrdersContainer)
        titleLabel = findViewById(R.id.orderListLabel)
        btnOrders = findViewById(R.id.btnShowOrders)
        btnProducts = findViewById(R.id.btnShowProducts)
        btnUsers = findViewById(R.id.btnShowUsers)

        val btnAddProduct = findViewById<Button>(R.id.btnGoToAddProduct)

        // Переход на создание товара
        btnAddProduct.setOnClickListener {
            startActivity(Intent(this, AdminAddProduct::class.java))
        }

        // Логика переключения вкладок
        btnOrders.setOnClickListener {
            updateTabUI(btnOrders)
            loadAllSystemOrders()
        }

        btnProducts.setOnClickListener {
            updateTabUI(btnProducts)
            loadAllProducts()
        }

        btnUsers.setOnClickListener {
            updateTabUI(btnUsers)
            loadAllUsers()
        }

        // По умолчанию загружаем заказы
        loadAllSystemOrders()
    }

    // --- УПРАВЛЕНИЕ ЗАКАЗАМИ ---
    private fun loadAllSystemOrders() {
        titleLabel.text = "Все заказы системы"
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val orders = RetrofitInstance.create(AuthApi::class.java).getAllOrders()
                withContext(Dispatchers.Main) { displayOrders(orders) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { showToast("Ошибка заказов: ${e.message}") }
            }
        }
    }

    private fun displayOrders(orders: List<OrderDto>) {
        mainContainer.removeAllViews()
        orders.forEach { order ->
            val view = layoutInflater.inflate(R.layout.item_order, mainContainer, false)
            val tvName = view.findViewById<TextView>(R.id.orderId)
            val tvStatus = view.findViewById<TextView>(R.id.orderStatus)
            val tvTotal = view.findViewById<TextView>(R.id.orderTotal)

            val courierInfo = if (!order.courierName.isNullOrEmpty() && order.courierName != "Не назначен") {
                "\n🚚 Курьер: ${order.courierName}"
            } else "\n⌛ Курьер: Не назначен"

            tvName.text = "Клиент: ${order.userName}$courierInfo"
            tvTotal.text = "${order.totalAmount} ₽"
            tvStatus.text = order.status

            view.setOnClickListener {
                val intent = Intent(this, OrderDetailsActivity::class.java)
                intent.putExtra("ORDER_ID", order.orderID)
                startActivity(intent)
            }
            mainContainer.addView(view)
        }
    }

    // --- УПРАВЛЕНИЕ ТОВАРАМИ (CRUD) ---
    private fun loadAllProducts() {
        titleLabel.text = "Управление товарами"
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val products = RetrofitInstance.create(AuthApi::class.java).getProducts()
                withContext(Dispatchers.Main) {
                    mainContainer.removeAllViews()
                    products.forEach { product ->
                        val view = layoutInflater.inflate(R.layout.item_product_admin, mainContainer, false)

                        view.findViewById<TextView>(R.id.adminProductName).text = product.name
                        view.findViewById<TextView>(R.id.adminProductPrice).text = "${product.price} ₽"

                        // Клик для редактирования
                        view.setOnClickListener {
                            val intent = Intent(this@AdminActivity, AdminAddProduct::class.java)
                            intent.putExtra("PRODUCT_ID", product.productID.toString())
                            startActivity(intent)
                        }

                        // Удаление товара
                        view.findViewById<ImageButton>(R.id.btnDeleteProduct).setOnClickListener {
                            deleteProduct(product.productID.toString())
                        }

                        mainContainer.addView(view)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { showToast("Ошибка товаров") }
            }
        }
    }

    private fun deleteProduct(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = RetrofitInstance.create(AuthApi::class.java).deleteProduct(id)
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful) {
                        showToast("Товар удален")
                        loadAllProducts()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { showToast("Ошибка при удалении") }
            }
        }
    }

    // --- УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ (Admin CRUD) ---
    private fun loadAllUsers() {
        titleLabel.text = "Управление пользователями"
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val users = RetrofitInstance.create(AuthApi::class.java).getAdminAllUsers()
                withContext(Dispatchers.Main) {
                    mainContainer.removeAllViews()
                    users.forEach { user ->
                        val view = layoutInflater.inflate(R.layout.item_user_admin, mainContainer, false)

                        view.findViewById<TextView>(R.id.adminUserName).text = user.fullName
                        val tvRole = view.findViewById<TextView>(R.id.adminUserRole)
                        tvRole.text = "Роль: ${user.role}"

                        val btnRole = view.findViewById<Button>(R.id.btnChangeRole)
                        btnRole.text = if (user.role == "Courier") "В юзеры" else "В курьеры"

                        // Смена роли
                        btnRole.setOnClickListener {
                            val newRole = if (user.role == "Courier") "User" else "Courier"
                            changeUserRole(user.userId, newRole)
                        }

                        // Удаление пользователя
                        view.findViewById<ImageButton>(R.id.btnDeleteUser).setOnClickListener {
                            deleteUser(user.userId)
                        }

                        mainContainer.addView(view)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { showToast("Ошибка пользователей") }
            }
        }
    }

    private fun changeUserRole(userId: String, role: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = RetrofitInstance.create(AuthApi::class.java).updateRole(userId, role)
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful) loadAllUsers()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { showToast("Не удалось изменить роль") }
            }
        }
    }

    private fun deleteUser(userId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = RetrofitInstance.create(AuthApi::class.java).deleteUser(userId)
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful) {
                        showToast("Пользователь удален")
                        loadAllUsers()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { showToast("Ошибка удаления пользователя") }
            }
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ---
    private fun updateTabUI(activeButton: Button) {
        val inactiveColor = Color.parseColor("#938DB5")
        val activeColor = Color.parseColor("#FFA451")

        btnOrders.setTextColor(inactiveColor)
        btnProducts.setTextColor(inactiveColor)
        btnUsers.setTextColor(inactiveColor)

        activeButton.setTextColor(activeColor)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // При возвращении обновляем текущую выбранную вкладку
        val currentTitle = titleLabel.text.toString()
        when {
            currentTitle.contains("заказы", ignoreCase = true) -> loadAllSystemOrders()
            currentTitle.contains("товарами", ignoreCase = true) -> loadAllProducts()
            currentTitle.contains("пользователями", ignoreCase = true) -> loadAllUsers()
        }
    }
}