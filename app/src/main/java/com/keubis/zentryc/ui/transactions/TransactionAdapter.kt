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
    private val onItemLongClick: (TransactionWithCategory) -> Unit,
    private val onSelectionChanged: (Int) -> Unit = {}
) : ListAdapter<TransactionWithCategory, TransactionAdapter.TransactionViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    // Set de ids seleccionados
    private val selectedIds = mutableSetOf<Int>()

    // Indica si el modo selección está activo
    var isSelectionMode = false
        private set

    // Devuelve los items seleccionados
    fun getSelectedItems(): List<TransactionWithCategory> {
        return currentList.filter { selectedIds.contains(it.expense.id) }
    }

    // Limpia la selección y desactiva el modo selección
    fun clearSelection() {
        selectedIds.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

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

            // Resalta el item si está seleccionado
            val isSelected = selectedIds.contains(item.expense.id)
            itemView.setBackgroundColor(
                if (isSelected)
                    android.graphics.Color.parseColor("#E3F2FD")
                else
                    itemView.context.getColor(android.R.color.white)
            )

            // Pulsación normal — en modo selección selecciona/deselecciona
            itemView.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(item)
                }
            }

            // Pulsación larga — activa modo selección o muestra menú contextual
            itemView.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(item)
                } else {
                    onItemLongClick(item)
                }
                true
            }
        }

        private fun toggleSelection(item: TransactionWithCategory) {
            if (selectedIds.contains(item.expense.id)) {
                selectedIds.remove(item.expense.id)
                // Si no quedan seleccionados desactiva el modo selección
                if (selectedIds.isEmpty()) isSelectionMode = false
            } else {
                selectedIds.add(item.expense.id)
            }
            notifyItemChanged(adapterPosition)
            // Notifica cuántos items hay seleccionados
            onSelectionChanged(selectedIds.size)
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