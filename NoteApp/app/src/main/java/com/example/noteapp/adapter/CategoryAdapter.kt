package com.example.noteapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.Category

class CategoryAdapter(
    private var list: MutableList<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryVH>() {

    private var onDelete: ((Category) -> Unit)? = null

    fun setOnDeleteListener(listener: (Category) -> Unit) {
        onDelete = listener
    }

    fun updateData(newList: MutableList<Category>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class CategoryVH(val view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryVH(view)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = list[position]

        holder.tvName.text = item.name
        holder.view.setOnClickListener { onClick(item) }

        holder.btnDelete.setOnClickListener {
            onDelete?.invoke(item)
        }
    }

    override fun getItemCount(): Int = list.size
}