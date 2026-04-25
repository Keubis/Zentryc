package com.keubis.zentryc.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.keubis.zentryc.R
import com.keubis.zentryc.ui.base.BaseFragment
import com.keubis.zentryc.ui.dashboard.DashboardViewModel

class TransactionsFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: TransactionAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView

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

        setupRecyclerView()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter { transactionWithCategory ->
            // Pulsación larga = opción de eliminar
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

    private fun observeTransactions() {
        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
            tvEmpty.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (transactions.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}