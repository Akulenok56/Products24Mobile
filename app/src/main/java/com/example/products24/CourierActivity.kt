package com.example.products24

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.products24.data.api.AuthApi
import com.example.products24.data.model.OrderDto
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CourierActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier) // Создай простой layout с ScrollView и LinearLayout внутри

        container = findViewById(R.id.courierOrdersContainer)
        loadPendingOrders()
    }

    private fun loadPendingOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)
                val orders = api.getPendingOrders()
                withContext(Dispatchers.Main) {
                    displayOrders(orders)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun displayOrders(orders: List<OrderDto>) {
        container.removeAllViews()
        orders.forEach { order ->
            val view = layoutInflater.inflate(R.layout.item_order_courier, container, false)
            view.findViewById<TextView>(R.id.courOrderCustomer).text = "Заказ: ${order.userName}"
            view.findViewById<TextView>(R.id.courOrderAddress).text = order.address

            view.findViewById<Button>(R.id.btnAcceptOrder).setOnClickListener {
                showAssemblySheet(order)
            }
            container.addView(view)
        }
    }

    private fun showAssemblySheet(order: OrderDto) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        // Явно указываем inflate для BottomSheet
        val view = layoutInflater.inflate(R.layout.bottom_sheet_assembly, null)

        val itemsContainer = view.findViewById<LinearLayout>(R.id.assemblyItemsContainer)
        val btnDone = view.findViewById<Button>(R.id.btnOrderAssembled)

        order.items.forEach { item ->
            val itemView = layoutInflater.inflate(R.layout.item_order_assembly, itemsContainer, false)
            val cb = itemView.findViewById<CheckBox>(R.id.checkCollected)
            val tv = itemView.findViewById<TextView>(R.id.tvItemName)

            tv.text = "${item.productName} x${item.quantity}"
            cb.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                // Кнопка станет активной только когда ВСЕ чекбоксы нажаты
                btnDone.isEnabled = order.items.all { it.isChecked }
            }
            itemsContainer.addView(itemView)
        }

        btnDone.setOnClickListener {
            markAsShipped(order.orderID, dialog)
        }

        dialog.setContentView(view) // Теперь ошибки быть не должно
        dialog.show()
    }

    private fun markAsShipped(orderId: String, dialog: BottomSheetDialog) {
        lifecycleScope.launch(Dispatchers.IO) {
            val api = RetrofitInstance.create(AuthApi::class.java)
            val response = api.markAsAssembled(orderId)
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    loadPendingOrders() // Обновляем список
                    Toast.makeText(this@CourierActivity, "Заказ в пути!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}