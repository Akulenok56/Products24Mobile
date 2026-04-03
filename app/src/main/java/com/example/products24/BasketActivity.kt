package com.example.products24

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.products24.data.api.AuthApi
import com.example.products24.data.model.CartItemDto
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class BasketActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var totalText: TextView
    private lateinit var btnPay: Button

    private var cartItems: List<CartItemDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_basket)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnPay = findViewById(R.id.btnPay)
        val btnBack = findViewById<Button>(R.id.btnBack)
        totalText = findViewById(R.id.total)

        btnBack.setOnClickListener { finish() }

        btnPay.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                showPayDialog()
            } else {
                Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show()
            }
        }

        val scroll = findViewById<ScrollView>(R.id.scroll)
        container = scroll.getChildAt(0) as LinearLayout

        loadCart()
    }

    private fun loadCart() {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)
                val response = api.getCartItems()

                if (response.isSuccessful) {
                    cartItems = response.body() ?: emptyList()
                    showCartItems(cartItems)
                    updateTotalAmount()
                } else {
                    Toast.makeText(this@BasketActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@BasketActivity, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCartItems(items: List<CartItemDto>) {
        container.removeAllViews()

        // Отключаем кнопку оплаты, если товаров нет
        btnPay.isEnabled = items.isNotEmpty()

        for (item in items) {
            val view = layoutInflater.inflate(R.layout.item_basket, container, false)

            val name = view.findViewById<TextView>(R.id.name)
            val count = view.findViewById<TextView>(R.id.count)
            val price = view.findViewById<TextView>(R.id.price)
            val image = view.findViewById<ImageView>(R.id.imageView)
            val btnPlus = view.findViewById<ImageView>(R.id.btnPlus)
            val btnMinus = view.findViewById<ImageView>(R.id.btnMinus)

            name.text = item.productName
            count.text = item.quantity.toString()
            price.text = "${item.price * item.quantity} ₽"

            Glide.with(this).load(item.imageUrl).into(image)

            btnPlus.setOnClickListener { updateIncrease(item.cartItemID) }
            btnMinus.setOnClickListener { updateDecrease(item.cartItemID) }

            container.addView(view)
        }
    }

    private fun showPayDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_pay, null)

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener { dialog.dismiss() }

        // Оплата при получении
        view.findViewById<Button>(R.id.btnPay).setOnClickListener {
            dialog.dismiss()
            performCheckout()
        }

        // Перейти к оплате картой
        view.findViewById<Button>(R.id.btnPayCard).setOnClickListener {
            dialog.dismiss()
            showCardDialog()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showCardDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_cardinfo, null)

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener { dialog.dismiss() }

        view.findViewById<Button>(R.id.btnPayCard).setOnClickListener {
            dialog.dismiss()
            performCheckout()
        }

        dialog.setContentView(view) // Важно!
        dialog.show()               // Важно!
    }

    private fun performCheckout() {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)
                val response = api.checkout()

                if (response.isSuccessful) {
                    val intent = Intent(this@BasketActivity, OrderSuccses::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@BasketActivity, "Ошибка оформления: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BasketActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateIncrease(cartItemId: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)
                val response = api.increase(cartItemId)
                if (response.isSuccessful) loadCart()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateDecrease(cartItemId: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)
                val response = api.decrease(cartItemId)
                if (response.isSuccessful) loadCart()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateTotalAmount() {
        val total = cartItems.sumOf { it.price * it.quantity }
        totalText.text = "$total ₽"
    }
}