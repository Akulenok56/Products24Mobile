package com.example.products24

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.products24.data.api.AuthApi
import com.example.products24.data.model.ProductDto
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var allProducts: List<ProductDto> = emptyList()
    private lateinit var btnToBasket: ImageButton
    private lateinit var leftContainer: LinearLayout
    private lateinit var downContainer: LinearLayout
    private var activeCategory: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitInstance.init(this, "http://10.0.2.2:5162/")

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnToBasket = findViewById(R.id.basketBtn)
        btnToBasket.setOnClickListener {
            startActivity(Intent(this, BasketActivity::class.java))
        }

        val scrollLeft = findViewById<HorizontalScrollView>(R.id.scroll1)
        leftContainer = scrollLeft.getChildAt(0) as LinearLayout

        val scrollDown = findViewById<HorizontalScrollView>(R.id.downCont)
        downContainer = scrollDown.getChildAt(0) as LinearLayout

        val categoryScroll = findViewById<HorizontalScrollView>(R.id.scroll)
        val categoryLayout = categoryScroll.getChildAt(0) as LinearLayout

        for (i in 0 until categoryLayout.childCount) {
            val categoryTextView = categoryLayout.getChildAt(i) as TextView
            if (i == 0) {
                activeCategory = categoryTextView
                categoryTextView.setTextColor(0xFF000000.toInt())
            }

            categoryTextView.setOnClickListener {
                activeCategory?.setTextColor(0xFF938DB5.toInt())
                categoryTextView.setTextColor(0xFF000000.toInt())
                activeCategory = categoryTextView

                val filtered = allProducts.filter {
                    it.description.equals(categoryTextView.text.toString(), ignoreCase = true)
                }
                showProductsDown(filtered)
            }
        }

        val searchEd = findViewById<EditText>(R.id.searchEd)
        searchEd.setOnEditorActionListener { v, actionId, event ->
            val query = searchEd.text.toString()
            if (query.isNotEmpty()) {
                showSearchDialog(query)
            }
            true
        }

        loadAllProducts()
        loadProducts()
    }

    private fun loadAllProducts() {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)
                val products = api.getProducts()
                products.forEach { addProductCardLeft(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)
                val products = api.getProducts()
                allProducts = products

                activeCategory?.let { firstCategory ->
                    val filtered = allProducts.filter {
                        it.description.equals(firstCategory.text.toString(), ignoreCase = true)
                    }
                    showProductsDown(filtered)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showProductsDown(list: List<ProductDto>) {
        downContainer.removeAllViews()
        list.forEach { addProductCardDown(it) }
    }

    private fun addProductCardLeft(product: ProductDto) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_product, leftContainer, false)

        val name = view.findViewById<TextView>(R.id.pnameHoney)
        val price = view.findViewById<TextView>(R.id.ppriceHoneyLime)
        val image = view.findViewById<ImageView>(R.id.pimageHoneyLime)

        name.text = product.name
        price.text = product.price.toString()

        Glide.with(this)
            .load(product.imageUrl)
            .into(image)

        leftContainer.addView(view)
    }

    private fun addProductCardDown(product: ProductDto) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_product, downContainer, false)

        val name = view.findViewById<TextView>(R.id.pnameHoney)
        val price = view.findViewById<TextView>(R.id.ppriceHoneyLime)
        val image = view.findViewById<ImageView>(R.id.pimageHoneyLime)

        name.text = product.name
        price.text = product.price.toString()

        Glide.with(this)
            .load(product.imageUrl)
            .into(image)

        downContainer.addView(view)
    }


    private fun showSearchDialog(query: String) {
        val filteredProducts = allProducts.filter {
            it.name.contains(query, ignoreCase = true) && it.stockQuantity > 0
        }

        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_search, null)

        val container = view.findViewById<LinearLayout>(R.id.searchContainer)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener { dialog.dismiss() }

        container.removeAllViews()

        if (filteredProducts.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "Ничего не найдено"
            tvEmpty.textSize = 16f
            tvEmpty.setPadding(16, 16, 16, 16)
            container.addView(tvEmpty)
        } else {
            filteredProducts.forEach { product ->
                val itemView = layoutInflater.inflate(R.layout.item_product_search, container, false)

                val name = itemView.findViewById<TextView>(R.id.pnameSearch)
                val price = itemView.findViewById<TextView>(R.id.ppriceSearch)
                val image = itemView.findViewById<ImageView>(R.id.pimageSearch)

                name.text = product.name
                price.text = product.price.toString()

                Glide.with(this)
                    .load(product.imageUrl)
                    .into(image)

                container.addView(itemView)
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
