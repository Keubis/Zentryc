package com.keubis.zentryc.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keubis.zentryc.R
import com.keubis.zentryc.ui.base.BaseFragment
import com.keubis.zentryc.ui.transactions.TransactionAdapter
import androidx.navigation.fragment.findNavController

class DashboardFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvBalance: TextView
    private lateinit var recyclerRecent: RecyclerView
    private lateinit var adapter: TransactionAdapter

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
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter { }
        recyclerRecent.layoutManager = LinearLayoutManager(requireContext())
        recyclerRecent.adapter = adapter
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

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            val income = viewModel.totalIncome.value ?: 0.0
            val expenses = viewModel.totalExpenses.value ?: 0.0
            val balance = income - expenses
            tvBalance.text = String.format("%.2f €", balance)
            tvBalance.setTextColor(
                if (balance >= 0)
                    requireContext().getColor(android.R.color.holo_green_dark)
                else
                    requireContext().getColor(android.R.color.holo_red_dark)
            )
            // Mostrar los últimos 5 movimientos
            val recent = transactions.take(5)
            adapter.submitList(recent)
        }
    }
}