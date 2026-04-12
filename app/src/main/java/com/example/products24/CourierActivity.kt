package com.example.products24

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.products24.data.model.OrderDto
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CourierActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        // Ключ и инициализация MapKit должны быть в самом первом Activity
        MapKitFactory.setApiKey("1ffd18d9-a519-422f-8c73-4d6de53ec747")
        MapKitFactory.initialize(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier)

        container = findViewById(R.id.courierOrdersContainer)

        loadPendingOrders()
    }

    // Обновляем список каждый раз, когда курьер возвращается на этот экран
    override fun onResume() {
        super.onResume()
        loadPendingOrders()
    }

    private fun loadPendingOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitInstance.authApi()
                val orders = api.getPendingOrders()
                withContext(Dispatchers.Main) {
                    displayOrders(orders)
                }
            } catch (e: Exception) {
                Log.e("Courier", "Error loading orders", e)
            }
        }
    }

    private fun displayOrders(orders: List<OrderDto>) {
        container.removeAllViews()
        orders.forEach { order ->
            val view = layoutInflater.inflate(R.layout.item_order_courier, container, false)
            val tvCustomer = view.findViewById<TextView>(R.id.courOrderCustomer)
            val tvAddress = view.findViewById<TextView>(R.id.courOrderAddress)
            val btnAction = view.findViewById<Button>(R.id.btnAcceptOrder)

            tvCustomer.text = "Заказ: ${order.userName}"
            tvAddress.text = order.addressDelivery ?: "Адрес не указан"

            // Если заказ уже собран и готов к доставке
            if (order.status == "В пути" || order.status == "Shipped") {
                btnAction.text = "Открыть карту"
                btnAction.setBackgroundColor(Color.parseColor("#FFA451"))
            } else {
                // Если заказ только поступил на сборку
                btnAction.text = "Принять и собрать"
                btnAction.setBackgroundColor(Color.parseColor("#FFA451"))
            }

            val clickListener = android.view.View.OnClickListener {
                if (order.status == "В пути" || order.status == "Shipped") {
                    // Переход на новый полноэкранный Activity с картой
                    openDeliveryScreen(order)
                } else {
                    // Показ BottomSheet для сборки товара
                    showAssemblySheet(order)
                }
            }

            view.setOnClickListener(clickListener)
            btnAction.setOnClickListener(clickListener)
            container.addView(view)
        }
    }

    private fun showAssemblySheet(order: OrderDto) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_assembly, null)
        val itemsContainer = view.findViewById<LinearLayout>(R.id.assemblyItemsContainer)
        val btnDone = view.findViewById<Button>(R.id.btnOrderAssembled)

        val items = order.items ?: emptyList()
        if (items.isEmpty()) {
            Toast.makeText(this, "Список товаров пуст", Toast.LENGTH_SHORT).show()
            return
        }

        btnDone.isEnabled = false

        items.forEach { item ->
            val itemView = layoutInflater.inflate(R.layout.item_order_assembly, itemsContainer, false)
            val cb = itemView.findViewById<CheckBox>(R.id.checkCollected)
            val tv = itemView.findViewById<TextView>(R.id.tvItemName)

            tv.text = "${item.productName} x${item.quantity}"
            cb.isChecked = item.isChecked

            cb.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                btnDone.isEnabled = items.all { it.isChecked }
            }
            itemsContainer.addView(itemView)
        }

        btnDone.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.authApi().markAsShipped(order.orderID)
                    if (response.isSuccessful) {
                        dialog.dismiss()
                        loadPendingOrders()
                        Toast.makeText(this@CourierActivity, "Заказ собран! Теперь откройте карту.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CourierActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun openDeliveryScreen(order: OrderDto) {
        val intent = Intent(this, DeliveryActivity::class.java)
        intent.putExtra("ORDER_DATA", order)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}