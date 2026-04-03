package com.example.products24

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.products24.data.model.OrderItemDto

class OrderItemsAdapter(private val items: List<OrderItemDto>) :
    RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tvOrderDetailName)
        val qty = view.findViewById<TextView>(R.id.tvOrderDetailQty)
        val price = view.findViewById<TextView>(R.id.tvOrderDetailPrice)
        val image = view.findViewById<ImageView>(R.id.ivOrderDetailImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // 1. Устанавливаем название
        holder.name.text = item.product?.name ?: "Товар удален"

        // 2. Устанавливаем количество
        holder.qty.text = "Кол-во: ${item.quantity}"

        // 3. Исправляем цену (берем unitPrice из JSON и умножаем на количество)
        // Если в твоем DTO поле называется unitPrice, используй его:
        val totalPrice = item.priceAtOrder * item.quantity
        holder.price.text = "$totalPrice ₽"

        // 4. Загружаем картинку через Glide
        val imageUrl = item.product?.imageUrl

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .centerCrop() // Чтобы картинка заполняла квадрат без пустых полей
            .transform(RoundedCorners(20)) // Скругление углов (в пикселях)
            .placeholder(R.drawable.rectangle_21) // Заглушка пока грузится
            .error(R.drawable.rectangle_21)       // Заглушка если ошибка
            .into(holder.image)
    }

    override fun getItemCount() = items.size
}