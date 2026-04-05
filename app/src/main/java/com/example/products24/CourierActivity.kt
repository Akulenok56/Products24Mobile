package com.example.products24

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

            val tvCustomer = view.findViewById<TextView>(R.id.courOrderCustomer)
            val tvAddress = view.findViewById<TextView>(R.id.courOrderAddress)
            val btnAction = view.findViewById<Button>(R.id.btnAcceptOrder) // Твоя кнопка из XML

            tvCustomer.text = "Заказ: ${order.userName}"
            tvAddress.text = order.addressDelivery ?: "Адрес не указан"

            // ПРОВЕРКА СТАТУСА
            if (order.status == "В пути" || order.status == "Shipped") {
                // Если собран, меняем текст и, например, цвет кнопки
                btnAction.text = "Информация о доставке"
                btnAction.setBackgroundColor(android.graphics.Color.parseColor("#FFA451")) // Синий
            } else {
                // Если новый
                btnAction.text = "Принять и собрать"
                btnAction.setBackgroundColor(android.graphics.Color.parseColor("#FFA451")) // Твой оранжевый
            }

            // Клик теперь можно вешать и на саму кнопку, и на всю карточку
            val clickListener = android.view.View.OnClickListener {
                if (order.status == "В пути" || order.status == "Shipped") {
                    showDeliverySheet(order)
                } else {
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

        // 1. ЗАЩИТА ОТ КРАША: Если items пришел null, используем пустой список
        val items = order.items ?: emptyList()

        if (items.isEmpty()) {
            Toast.makeText(this, "Список товаров пуст", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Изначально выключаем кнопку, если товары есть
        btnDone.isEnabled = false

        items.forEach { item ->
            val itemView = layoutInflater.inflate(R.layout.item_order_assembly, itemsContainer, false)
            val cb = itemView.findViewById<CheckBox>(R.id.checkCollected)
            val tv = itemView.findViewById<TextView>(R.id.tvItemName)

            tv.text = "${item.productName} x${item.quantity}"

            // Сбрасываем состояние из модели (на случай переоткрытия диалога)
            cb.isChecked = item.isChecked

            cb.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                // Кнопка станет активной только когда ВСЕ чекбоксы нажаты
                btnDone.isEnabled = items.all { it.isChecked }
            }
            itemsContainer.addView(itemView)
        }

        // Проверяем состояние кнопки сразу после загрузки (вдруг уже всё отмечено)
        btnDone.isEnabled = items.all { it.isChecked }

        btnDone.setOnClickListener {
            lifecycleScope.launch {
                try {
                    // Обращаемся к правильному объекту RetrofitInstance
                    // И вызываем метод authApi(), который создали в объекте
                    val response = RetrofitInstance.authApi().markAsShipped(order.orderID)

                    if (response.isSuccessful) {
                        dialog.dismiss()
                        showDeliverySheet(order) // Переходим к следующему этапу
                    } else {
                        Toast.makeText(this@CourierActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CourierActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showDeliverySheet(order: OrderDto) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_delivery, null)

        val tvName = view.findViewById<TextView>(R.id.tvDeliveryCustomerName)
        val tvPhone = view.findViewById<TextView>(R.id.tvDeliveryPhone)
        val tvAddress = view.findViewById<TextView>(R.id.tvDeliveryAddress)
        val btnComplete = view.findViewById<Button>(R.id.btnCompleteOrder)
        val btnCall = view.findViewById<ImageButton>(R.id.btnCallCustomer)

        // Заполняем данные (они уже есть в OrderDto)
        tvName.text = order.userName
        tvPhone.text = order.phoneNumber
        tvAddress.text = order.addressDelivery

        // Кнопка позвонить
        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${order.phoneNumber}")
            startActivity(intent)
        }

        // Финальная кнопка "Завершить заказ"
        btnComplete.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.authApi().markAsComplete(order.orderID)
                    if (response.isSuccessful) {
                        dialog.dismiss()
                        Toast.makeText(this@CourierActivity, "Заказ завершен!", Toast.LENGTH_SHORT).show()
                        loadPendingOrders() // Не забудь обновить список на главном экране курьера
                    }
                } catch (e: Exception) {
                    Log.e("Courier", "Error completing order", e)
                }
            }
        }

        dialog.setContentView(view)
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