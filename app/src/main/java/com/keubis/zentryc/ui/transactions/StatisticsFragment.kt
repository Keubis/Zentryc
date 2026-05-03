package com.keubis.zentryc.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.keubis.zentryc.R
import com.keubis.zentryc.ui.base.BaseFragment
import com.keubis.zentryc.ui.dashboard.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatisticsFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var pieChart: PieChart
    private lateinit var tvStatIncome: TextView
    private lateinit var tvStatExpenses: TextView
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
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        pieChart = view.findViewById(R.id.pieChart)
        tvStatIncome = view.findViewById(R.id.tvStatIncome)
        tvStatExpenses = view.findViewById(R.id.tvStatExpenses)
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth)
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)

        setupPieChart()
        setupMonthSelector()
        loadMonthData()
    }

    private fun setupPieChart() {
        pieChart.apply {
            isDrawHoleEnabled = true
            holeRadius = 40f
            setHoleColor(Color.WHITE)
            description.isEnabled = false
            isRotationEnabled = true
            legend.isEnabled = true
            setUsePercentValues(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }
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
        // Actualiza el texto del mes
        val monthName = monthFormat.format(calendar.time)
        tvCurrentMonth.text = monthName.replaceFirstChar { it.uppercase() }

        // Calcula inicio y fin del mes
        val startOfMonth = calendar.clone() as Calendar
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
        startOfMonth.set(Calendar.MINUTE, 0)
        startOfMonth.set(Calendar.SECOND, 0)
        startOfMonth.set(Calendar.MILLISECOND, 0)

        val endOfMonth = calendar.clone() as Calendar
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23)
        endOfMonth.set(Calendar.MINUTE, 59)
        endOfMonth.set(Calendar.SECOND, 59)

        val start = startOfMonth.timeInMillis
        val end = endOfMonth.timeInMillis

        // Observa las transacciones del mes seleccionado
        viewModel.getTransactionsByDateRange(start, end).observe(viewLifecycleOwner) { transactions ->

            // Totales del mes
            val totalIncome = transactions
                .filter { it.expense.type == "INCOME" }
                .sumOf { it.expense.amount }

            val totalExpenses = transactions
                .filter { it.expense.type == "EXPENSE" }
                .sumOf { it.expense.amount }

            tvStatIncome.text = String.format("%.2f €", totalIncome)
            tvStatExpenses.text = String.format("%.2f €", totalExpenses)

            // Datos para la gráfica — solo gastos del mes agrupados por categoría
            val expensesByCategory = transactions
                .filter { it.expense.type == "EXPENSE" }
                .groupBy { it.category?.name ?: "Sin categoría" }
                .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

            if (expensesByCategory.isEmpty()) {
                pieChart.setNoDataText("No hay gastos este mes")
                pieChart.setNoDataTextColor(Color.GRAY)
                pieChart.clear()
                pieChart.invalidate()
                return@observe
            }

            val entries = expensesByCategory.map { (category, amount) ->
                PieEntry(amount.toFloat(), category)
            }

            // Usa el color real de cada categoría
            val colors = expensesByCategory.keys.map { categoryName ->
                val category = transactions
                    .firstOrNull { it.category?.name == categoryName }
                    ?.category
                try {
                    Color.parseColor(category?.colorHex ?: "#95A5A6")
                } catch (e: Exception) {
                    Color.GRAY
                }
            }

            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors
                valueTextSize = 12f
                valueTextColor = Color.WHITE
                valueFormatter = PercentFormatter(pieChart)
            }

            pieChart.data = PieData(dataSet)
            pieChart.invalidate()
        }
    }
}