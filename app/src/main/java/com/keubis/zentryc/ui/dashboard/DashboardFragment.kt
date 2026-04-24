package com.keubis.zentryc.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.keubis.zentryc.R
import com.keubis.zentryc.ui.base.BaseFragment

class DashboardFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvBalance: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        tvTotalIncome = view.findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses)
        tvBalance = view.findViewById(R.id.tvBalance)

        observeData()
    }

    private fun observeData() {
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            val amount = income ?: 0.0
            tvTotalIncome.text = String.format("%.2f €", amount)
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { expenses ->
            val amount = expenses ?: 0.0
            tvTotalExpenses.text = String.format("%.2f €", amount)
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            viewModel.totalExpenses.observe(viewLifecycleOwner) { expenses ->
                val balance = (income ?: 0.0) - (expenses ?: 0.0)
                tvBalance.text = String.format("%.2f €", balance)
                tvBalance.setTextColor(
                    if (balance >= 0)
                        requireContext().getColor(android.R.color.holo_green_dark)
                    else
                        requireContext().getColor(android.R.color.holo_red_dark)
                )
            }
        }
    }
}