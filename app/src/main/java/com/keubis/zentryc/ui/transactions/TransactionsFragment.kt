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
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar movimiento")
                .setMessage("¿Quieres eliminar '${transactionWithCategory.expense.description}'?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.deleteExpense(transactionWithCategory.expense)
                    showMessage("Movimiento eliminado")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
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