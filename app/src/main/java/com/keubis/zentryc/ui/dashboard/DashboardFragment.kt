package com.keubis.zentryc.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keubis.zentryc.R
import com.keubis.zentryc.ui.base.BaseFragment
import com.keubis.zentryc.ui.transactions.TransactionAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvBalance: TextView
    private lateinit var recyclerRecent: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnPreviousMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton

    private val calendar = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses)
        tvBalance = view.findViewById(R.id.tvBalance)
        recyclerRecent = view.findViewById(R.id.recyclerRecent)
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth)
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)

        // Botón cerrar sesión — limpia Room y cierra sesión de Firebase
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogout)
            .setOnClickListener {
                // Limpia los datos locales antes de cerrar sesión
                viewModel.clearLocalData()
                // Cierra sesión en Firebase
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                // Navega al login
                findNavController().navigate(R.id.loginFragment)
            }

        setupRecyclerView()
        setupMonthSelector()
        loadMonthData()
    }

    private fun setupRecyclerView() {

        adapter = TransactionAdapter(
            onItemLongClick = { },
            onSelectionChanged = { }
        )
        recyclerRecent.layoutManager = LinearLayoutManager(requireContext())
        recyclerRecent.adapter = adapter
    }

    private fun setupMonthSelector() {
        btnPreviousMonth.setOnClickListener {
            // Retrocede un mes
            calendar.add(Calendar.MONTH, -1)
            loadMonthData()
        }

        btnNextMonth.setOnClickListener {
            // Avanza un mes
            calendar.add(Calendar.MONTH, 1)
            loadMonthData()
        }
    }

    private fun loadMonthData() {
        // Actualiza el texto del mes con primera letra en mayúscula
        val monthName = monthFormat.format(calendar.time)
        tvCurrentMonth.text = monthName.replaceFirstChar { it.uppercase() }

        // Calcula el inicio del mes
        val startOfMonth = calendar.clone() as Calendar
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
        startOfMonth.set(Calendar.MINUTE, 0)
        startOfMonth.set(Calendar.SECOND, 0)
        startOfMonth.set(Calendar.MILLISECOND, 0)

        // Calcula el fin del mes
        val endOfMonth = calendar.clone() as Calendar
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23)
        endOfMonth.set(Calendar.MINUTE, 59)
        endOfMonth.set(Calendar.SECOND, 59)

        val start = startOfMonth.timeInMillis
        val end = endOfMonth.timeInMillis

        // Observa ingresos del mes
        viewModel.getIncomeByDateRange(start, end).observe(viewLifecycleOwner) { income ->
            val amount = income ?: 0.0
            tvTotalIncome.text = String.format("%.2f €", amount)
            updateBalance()
        }

        // Observa gastos del mes
        viewModel.getExpensesByDateRange(start, end).observe(viewLifecycleOwner) { expenses ->
            val amount = expenses ?: 0.0
            tvTotalExpenses.text = String.format("%.2f €", amount)
            updateBalance()
        }

        // Observa movimientos recientes del mes
        viewModel.getTransactionsByDateRange(start, end).observe(viewLifecycleOwner) { transactions ->
            // Muestra solo los últimos 5 movimientos del mes
            adapter.submitList(transactions.take(5))
        }
    }

    private fun updateBalance() {
        // Calcula el balance a partir de los textos actuales
        val income = tvTotalIncome.text.toString()
            .replace(" €", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        val expenses = tvTotalExpenses.text.toString()
            .replace(" €", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        val balance = income - expenses

        tvBalance.text = String.format("%.2f €", balance)
        tvBalance.setTextColor(
            if (balance >= 0)
                requireContext().getColor(android.R.color.holo_green_dark)
            else
                requireContext().getColor(android.R.color.holo_red_dark)
        )
    }
}