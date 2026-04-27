package com.keubis.zentryc.ui.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.keubis.zentryc.R
import com.keubis.zentryc.data.model.Category

class CategoryAdapter(
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val viewCategoryColor: View = itemView.findViewById(R.id.viewCategoryColor)
        private val btnDeleteCategory: ImageButton = itemView.findViewById(R.id.btnDeleteCategory)

        fun bind(category: Category) {
            tvCategoryName.text = category.name

            // Aplica el color de la categoría al círculo
            try {
                viewCategoryColor.setBackgroundColor(Color.parseColor(category.colorHex))
            } catch (e: Exception) {
                viewCategoryColor.setBackgroundColor(Color.GRAY)
            }

            btnDeleteCategory.setOnClickListener {
                onDeleteClick(category)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Category, newItem: Category) =
            oldItem == newItem
    }
}