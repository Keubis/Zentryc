package com.keubis.zentryc.ui.transactions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.keubis.zentryc.R
import com.keubis.zentryc.data.model.TransactionWithCategory
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val onItemLongClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionWithCategory, TransactionAdapter.TransactionViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val viewTypeIndicator: View = itemView.findViewById(R.id.viewTypeIndicator)

        fun bind(item: TransactionWithCategory) {
            tvDescription.text = item.expense.description
            tvCategory.text = item.category?.name ?: "Sin categoría"
            tvDate.text = dateFormat.format(item.expense.date)

            val isIncome = item.expense.type == "INCOME"
            val color = if (isIncome)
                itemView.context.getColor(android.R.color.holo_green_dark)
            else
                itemView.context.getColor(android.R.color.holo_red_dark)

            val sign = if (isIncome) "+" else "-"
            tvAmount.text = String.format("%s%.2f €", sign, item.expense.amount)
            tvAmount.setTextColor(color)
            viewTypeIndicator.setBackgroundColor(color)

            itemView.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TransactionWithCategory>() {
        override fun areItemsTheSame(
            oldItem: TransactionWithCategory,
            newItem: TransactionWithCategory
        ) = oldItem.expense.id == newItem.expense.id

        override fun areContentsTheSame(
            oldItem: TransactionWithCategory,
            newItem: TransactionWithCategory
        ) = oldItem == newItem
    }
}