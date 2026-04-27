package com.keubis.zentryc.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class StatisticsFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var pieChart: PieChart
    private lateinit var tvStatIncome: TextView
    private lateinit var tvStatExpenses: TextView

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

        setupPieChart()
        observeData()
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

    private fun observeData() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->

            // Totales para las tarjetas
            val totalIncome = transactions
                .filter { it.expense.type == "INCOME" }
                .sumOf { it.expense.amount }

            val totalExpenses = transactions
                .filter { it.expense.type == "EXPENSE" }
                .sumOf { it.expense.amount }

            tvStatIncome.text = String.format("%.2f €", totalIncome)
            tvStatExpenses.text = String.format("%.2f €", totalExpenses)

            // Datos para la gráfica — solo gastos agrupados por categoría
            val expensesByCategory = transactions
                .filter { it.expense.type == "EXPENSE" }
                .groupBy { it.category?.name ?: "Sin categoría" }
                .mapValues { entry -> entry.value.sumOf { it.expense.amount } }

            if (expensesByCategory.isEmpty()) {
                pieChart.setNoDataText("No hay gastos registrados")
                pieChart.setNoDataTextColor(Color.GRAY)
                pieChart.invalidate()
                return@observe
            }

            val entries = expensesByCategory.map { (categoryName, amount) ->
                PieEntry(amount.toFloat(), categoryName)
            }

// Color real de cada categoría
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

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            tvStatIncome.text = String.format("%.2f €", income ?: 0.0)
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { expenses ->
            tvStatExpenses.text = String.format("%.2f €", expenses ?: 0.0)
        }
    }
}