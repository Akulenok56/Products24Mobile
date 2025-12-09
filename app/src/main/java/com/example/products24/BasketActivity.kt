package com.example.products24

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog


class BasketActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_basket)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnPay = findViewById<Button>(R.id.btnPay)

        btnPay.setOnClickListener {
            showPayDialog()
        }
    }

    private fun showPayDialog() {
        val dialog = BottomSheetDialog(
            this,
            R.style.BottomSheetDialogTheme
        )

        val view = layoutInflater.inflate(R.layout.bottom_sheet_pay, null)

        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val btnPay = view.findViewById<Button>(R.id.btnPay)
        val btnPayCard = view.findViewById<Button>(R.id.btnPayCard)

        btnClose.setOnClickListener { dialog.dismiss() }
        btnPay.setOnClickListener { dialog.dismiss() }

        btnPayCard.setOnClickListener {

        }

        dialog.setContentView(view)
        dialog.show()
    }
}