package com.example.noteapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryHorizontalAdapter(
    private var list: MutableList<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryHorizontalAdapter.ViewHolder>() {

    private var selectedId = -1   // -1 = All

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tvCategoryItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_horizontal, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tv.text = item.name

        if (item.id == selectedId) {
            holder.tv.setBackgroundResource(R.drawable.bg_category_item_selected)
            holder.tv.setTextColor(Color.WHITE)
        } else {
            holder.tv.setBackgroundResource(R.drawable.bg_category_item)
            holder.tv.setTextColor(Color.parseColor("#FF6600"))
        }

        holder.itemView.setOnClickListener {
            selectedId = item.id
            notifyDataSetChanged()
            onClick(item)
        }
    }

    override fun getItemCount() = list.size
    fun updateData(newList: List<Category>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

}
