package com.keubis.zentryc.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.keubis.zentryc.R
import com.keubis.zentryc.data.model.Expense
import com.keubis.zentryc.data.model.TransactionWithCategory
import com.keubis.zentryc.ui.base.BaseFragment
import com.keubis.zentryc.ui.dashboard.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionsFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnFilter: MaterialButton
    private lateinit var chipFilterActive: Chip

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    private var filterStartDate: Long? = null
    private var filterEndDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        recyclerView = view.findViewById(R.id.recyclerTransactions)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        btnFilter = view.findViewById(R.id.btnFilter)
        chipFilterActive = view.findViewById(R.id.chipFilterActive)

        setupRecyclerView()
        setupFilterButton()
        observeAllTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter { transactionWithCategory ->
            // Menú con opciones al pulsar largo
            val options = arrayOf("✏️ Editar", "🗑️ Eliminar")
            AlertDialog.Builder(requireContext())
                .setTitle(transactionWithCategory.expense.description)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openEditDialog(transactionWithCategory)
                        1 -> confirmDelete(transactionWithCategory)
                    }
                }
                .show()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }
    private fun openEditDialog(item: TransactionWithCategory) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_add_transaction, null)

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave).visibility = View.GONE

        val tabType = dialogView.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabTransactionType)
        val etAmount = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAmount)
        val etDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDescription)
        val etDate = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDate)
        val acCategory = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.acCategory)

        // Rellena con los datos actuales
        etAmount.setText(item.expense.amount.toString())
        etDescription.setText(item.expense.description)
        etDate.setText(dateFormat.format(item.expense.date))

        tabType.addTab(tabType.newTab().setText("Gasto"))
        tabType.addTab(tabType.newTab().setText("Ingreso"))
        val tabIndex = if (item.expense.type == "INCOME") 1 else 0
        tabType.selectTab(tabType.getTabAt(tabIndex))

        var selectedDate = item.expense.date
        var selectedCategoryId = item.expense.categoryId

        // Selector de fecha
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.timeInMillis
                etDate.setText(dateFormat.format(selectedDate))
            },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Dropdown de categorías
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val names = categories.map { it.name }
            val adapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                names
            )
            acCategory.setAdapter(adapter)
            acCategory.setText(item.category?.name ?: "", false)
            acCategory.setOnItemClickListener { _, _, position, _ ->
                selectedCategoryId = categories[position].id
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar movimiento")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val amountText = etAmount.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (amountText.isEmpty() || description.isEmpty()) {
                    showError("Rellena todos los campos")
                    return@setPositiveButton
                }

                val amount = amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    showError("Importe no válido")
                    return@setPositiveButton
                }

                val type = if (tabType.selectedTabPosition == 1) "INCOME" else "EXPENSE"

                val updated = item.expense.copy(
                    amount = amount,
                    description = description,
                    date = selectedDate,
                    type = type,
                    categoryId = selectedCategoryId
                )
                viewModel.updateExpense(updated)
                showMessage("Movimiento actualizado")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(item: TransactionWithCategory) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar movimiento")
            .setMessage("¿Quieres eliminar '${item.expense.description}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteExpense(item.expense)
                showMessage("Movimiento eliminado")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupFilterButton() {
        btnFilter.setOnClickListener {
            showDateRangePicker()
        }

        chipFilterActive.setOnCloseIconClickListener {
            clearFilter()
        }
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()

        // Primero selecciona fecha inicio
        DatePickerDialog(
            requireContext(),
            { _, startYear, startMonth, startDay ->
                val startCalendar = Calendar.getInstance()
                startCalendar.set(startYear, startMonth, startDay, 0, 0, 0)

                // Luego selecciona fecha fin
                DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDay ->
                        val endCalendar = Calendar.getInstance()
                        endCalendar.set(endYear, endMonth, endDay, 23, 59, 59)

                        filterStartDate = startCalendar.timeInMillis
                        filterEndDate = endCalendar.timeInMillis

                        applyFilter(
                            filterStartDate!!,
                            filterEndDate!!,
                            "${dateFormat.format(startCalendar.time)} - ${dateFormat.format(endCalendar.time)}"
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun applyFilter(startDate: Long, endDate: Long, label: String) {
        // Muestra el chip con el rango activo
        chipFilterActive.text = label
        chipFilterActive.visibility = View.VISIBLE

        // Observa solo el rango seleccionado
        viewModel.getTransactionsByDateRange(startDate, endDate)
            .observe(viewLifecycleOwner) { transactions ->
                adapter.submitList(transactions)
                updateEmptyState(transactions.isEmpty())
            }
    }

    private fun clearFilter() {
        filterStartDate = null
        filterEndDate = null
        chipFilterActive.visibility = View.GONE
        observeAllTransactions()
    }

    private fun observeAllTransactions() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
            updateEmptyState(transactions.isEmpty())
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}