package com.example.products24

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.products24.data.api.AuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AdminAddProduct : AppCompatActivity() {

    private lateinit var ivProductPreview: ImageView
    private var selectedImageUri: Uri? = null
    private val categories = arrayOf("Бургеры", "Пицца", "Напитки", "Десерты", "Салаты","Молочная продукция", "Выпечка", "Мясо")
    private var selectedCategory: String = ""

    // Регистратор для открытия галереи
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivProductPreview.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_add_product)

        // Исправляем отступы (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация View
        ivProductPreview = findViewById(R.id.ivProductPreview)
        val btnSelectPhoto = findViewById<Button>(R.id.btnSelectPhoto)
        val btnAddProduct = findViewById<Button>(R.id.btnAddProduct)

        val edName = findViewById<EditText>(R.id.edProductName)
        val spinner = findViewById<Spinner>(R.id.spProductCategory)
        val edPrice = findViewById<EditText>(R.id.edProductPrice)
        val edCalories = findViewById<EditText>(R.id.edProductCalories)
        val edProtein = findViewById<EditText>(R.id.edProtein)
        val edFat = findViewById<EditText>(R.id.edFat)
        val edCarbs = findViewById<EditText>(R.id.edCarbs)
        val edDescription = findViewById<EditText>(R.id.edProductDescription)
        val edStock = findViewById<EditText>(R.id.edStockQuantity)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        // Кнопка выбора фото
        btnSelectPhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = categories[0] // По умолчанию первая
            }
        }

        // Кнопка сохранения
        btnAddProduct.setOnClickListener {
            val name = edName.text.toString()
            val price = edPrice.text.toString()

            if (name.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "Название и цена обязательны!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            sendProductToBackend(
                name = name,
                category = selectedCategory,
                price = price.toDouble(),
                calories = edCalories.text.toString().toIntOrNull() ?: 0,
                protein = edProtein.text.toString().toDoubleOrNull() ?: 0.0,
                fat = edFat.text.toString().toDoubleOrNull() ?: 0.0,
                carbs = edCarbs.text.toString().toDoubleOrNull() ?: 0.0,
                stockQuantity = edStock.text.toString().toIntOrNull() ?: 0,
                description = edDescription.text.toString(),
                imageUri = selectedImageUri
            )
        }
    }

    private fun sendProductToBackend(
        name: String, category: String, price: Double,
        calories: Int, protein: Double, fat: Double,
        carbs: Double, description: String, imageUri: Uri?,
        stockQuantity: Int
    ) {
        if (imageUri == null) {
            Toast.makeText(this, "Выберите изображение!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitInstance.create(AuthApi::class.java)

                // 1. Картинка
                val imagePart = prepareFilePart("image", imageUri)

                // 2. Текстовые поля и числа (все через InvariantCulture/US для точки)
                val namePart = createPartFromString(name)
                val categoryPart = createPartFromString(category)
                val descPart = createPartFromString(description)

                val priceString = String.format(java.util.Locale.US, "%.2f", price)
                val pricePart = createPartFromString(priceString)

                val stockPart = createPartFromString(stockQuantity.toString())

                // Новые поля для КБЖУ
                val calPart = createPartFromString(calories.toString())
                val protPart = createPartFromString(String.format(java.util.Locale.US, "%.2f", protein))
                val fatPart = createPartFromString(String.format(java.util.Locale.US, "%.2f", fat))
                val carbPart = createPartFromString(String.format(java.util.Locale.US, "%.2f", carbs))

                // 3. Отправка (убедись, что аргументы идут в том же порядке, что в AuthApi)
                val response = api.addProductWithImage(
                    namePart,
                    categoryPart,
                    descPart,
                    pricePart,
                    stockPart,
                    calPart,
                    protPart,
                    fatPart,
                    carbPart,
                    imagePart
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AdminAddProduct, "Продукт $name добавлен!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Неизвестная ошибка"
                        Toast.makeText(this@AdminAddProduct, "Ошибка сервера: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminAddProduct, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val inputStream = contentResolver.openInputStream(fileUri)
        val byteArray = inputStream?.readBytes() ?: byteArrayOf()

        val requestFile = RequestBody.create(
            "image/*".toMediaTypeOrNull(),
            byteArray
        )

        // "image" — это имя ключа, которое твой сервер ищет в form.Files["image"]
        return MultipartBody.Part.createFormData(partName, "product_image.jpg", requestFile)
    }

    // Вспомогательная функция для текста
    private fun createPartFromString(string: String): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), string)
    }
}