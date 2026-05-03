package com.keubis.zentryc.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
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
    private lateinit var layoutSelectionBar: LinearLayout
    private lateinit var tvSelectionCount: TextView
    private lateinit var btnCancelSelection: ImageButton
    private lateinit var btnDeleteSelected: ImageButton

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    private var filterStartDate: Long? = null
    private var filterEndDate: Long? = null

    private lateinit var btnFilterCategory: MaterialButton

    private lateinit var chipCategoryActive: Chip

    private var filterCategoryId: Int? = null

    private var filterCategoryName: String? = null

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
        layoutSelectionBar = view.findViewById(R.id.layoutSelectionBar)
        tvSelectionCount = view.findViewById(R.id.tvSelectionCount)
        btnCancelSelection = view.findViewById(R.id.btnCancelSelection)
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected)
        btnFilterCategory = view.findViewById(R.id.btnFilterCategory)
        chipCategoryActive = view.findViewById(R.id.chipCategoryActive)

        setupRecyclerView()
        setupFilterButton()
        setupSelectionBar()
        observeAllTransactions()
        setupCategoryFilterButton()

    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onItemLongClick = { transactionWithCategory ->
                // En modo selección la pulsación larga muestra menú editar/eliminar
                if (!adapter.isSelectionMode) {
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
            },
            onSelectionChanged = { count ->
                // Actualiza la barra de selección
                if (count > 0) {
                    layoutSelectionBar.visibility = View.VISIBLE
                    tvSelectionCount.text = "$count seleccionado${if (count != 1) "s" else ""}"
                } else {
                    layoutSelectionBar.visibility = View.GONE
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSelectionBar() {
        // Cancela la selección
        btnCancelSelection.setOnClickListener {
            adapter.clearSelection()
            layoutSelectionBar.visibility = View.GONE
        }

        // Elimina los items seleccionados
        btnDeleteSelected.setOnClickListener {
            val selected = adapter.getSelectedItems()
            if (selected.isEmpty()) return@setOnClickListener

            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar ${selected.size} movimiento${if (selected.size != 1) "s" else ""}")
                .setMessage("¿Quieres eliminar los movimientos seleccionados?")
                .setPositiveButton("Eliminar") { _, _ ->
                    selected.forEach { viewModel.deleteExpense(it.expense) }
                    adapter.clearSelection()
                    layoutSelectionBar.visibility = View.GONE
                    showMessage("${selected.size} movimiento${if (selected.size != 1) "s" else ""} eliminado${if (selected.size != 1) "s" else ""}")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
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
        DatePickerDialog(
            requireContext(),
            { _, startYear, startMonth, startDay ->
                val startCalendar = Calendar.getInstance()
                startCalendar.set(startYear, startMonth, startDay, 0, 0, 0)
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
        chipFilterActive.text = label
        chipFilterActive.visibility = View.VISIBLE
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

    private fun openEditDialog(item: TransactionWithCategory) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_add_transaction, null)

        // Ocultamos el botón guardar del layout porque el diálogo tiene los suyos propios
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave).visibility = View.GONE

        val tabType = dialogView.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabTransactionType)
        val etAmount = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAmount)
        val etDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDescription)
        val etDate = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDate)
        val acCategory = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.acCategory)

        etAmount.setText(item.expense.amount.toString())
        etDescription.setText(item.expense.description)
        etDate.setText(dateFormat.format(item.expense.date))

        tabType.addTab(tabType.newTab().setText("Gasto"))
        tabType.addTab(tabType.newTab().setText("Ingreso"))
        val tabIndex = if (item.expense.type == "INCOME") 1 else 0
        tabType.selectTab(tabType.getTabAt(tabIndex))

        var selectedDate = item.expense.date
        var selectedCategoryId = item.expense.categoryId

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

    private fun setupCategoryFilterButton() {
        btnFilterCategory.setOnClickListener {
            showCategoryPicker()
        }
        chipCategoryActive.setOnCloseIconClickListener {
            clearCategoryFilter()
        }
    }

    private fun showCategoryPicker() {
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            if (categories.isEmpty()) {
                showError("No hay categorías disponibles")
                return@observe
            }

            val categoryNames = categories.map { it.name }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Filtrar por categoría")
                .setItems(categoryNames) { _, which ->
                    val selectedCategory = categories[which]
                    filterCategoryId = selectedCategory.id
                    filterCategoryName = selectedCategory.name
                    applyCategoryFilter(selectedCategory.id, selectedCategory.name)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun applyCategoryFilter(categoryId: Int, categoryName: String) {
        // Muestra el chip con la categoría seleccionada
        chipCategoryActive.text = categoryName
        chipCategoryActive.visibility = View.VISIBLE

        // Si hay también filtro de fecha combina ambos filtros
        if (filterStartDate != null && filterEndDate != null) {
            viewModel.getTransactionsByCategoryAndDateRange(
                categoryId,
                filterStartDate!!,
                filterEndDate!!
            ).observe(viewLifecycleOwner) { transactions ->
                adapter.submitList(transactions)
                updateEmptyState(transactions.isEmpty())
            }
        } else {
            viewModel.getTransactionsByCategory(categoryId)
                .observe(viewLifecycleOwner) { transactions ->
                    adapter.submitList(transactions)
                    updateEmptyState(transactions.isEmpty())
                }
        }
    }

    private fun clearCategoryFilter() {
        filterCategoryId = null
        filterCategoryName = null
        chipCategoryActive.visibility = View.GONE

        // Si hay filtro de fecha activo vuelve a aplicarlo sin categoría
        if (filterStartDate != null && filterEndDate != null) {
            applyFilter(filterStartDate!!, filterEndDate!!, chipFilterActive.text.toString())
        } else {
            observeAllTransactions()
        }
    }
}