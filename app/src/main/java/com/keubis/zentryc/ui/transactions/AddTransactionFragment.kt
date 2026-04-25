package com.keubis.zentryc.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.keubis.zentryc.R
import com.keubis.zentryc.data.model.Category
import com.keubis.zentryc.data.model.Expense
import com.keubis.zentryc.ui.base.BaseFragment
import com.keubis.zentryc.ui.dashboard.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.widget.AutoCompleteTextView

class AddTransactionFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var tabTransactionType: TabLayout
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var acCategory: AutoCompleteTextView
    private lateinit var btnSave: MaterialButton

    private var selectedCategory: Category? = null
    private var selectedDate: Long = System.currentTimeMillis()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        initViews(view)
        setupTabs()
        setupDatePicker()
        setupCategoryDropdown()
        setupSaveButton()

        // Fecha por defecto: hoy
        etDate.setText(dateFormat.format(selectedDate))
    }

    private fun initViews(view: View) {
        tabTransactionType = view.findViewById(R.id.tabTransactionType)
        etAmount = view.findViewById(R.id.etAmount)
        etDescription = view.findViewById(R.id.etDescription)
        etDate = view.findViewById(R.id.etDate)
        acCategory = view.findViewById(R.id.acCategory)
        btnSave = view.findViewById(R.id.btnSave)
    }

    private fun setupTabs() {
        tabTransactionType.addTab(
            tabTransactionType.newTab().setText("Gasto")
        )
        tabTransactionType.addTab(
            tabTransactionType.newTab().setText("Ingreso")
        )
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    etDate.setText(dateFormat.format(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupCategoryDropdown() {
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val categoryNames = categories.map { it.name }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoryNames
            )
            acCategory.setAdapter(adapter)
            acCategory.setOnItemClickListener { _, _, position, _ ->
                selectedCategory = categories[position]
            }
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()

            // Validaciones
            if (amountText.isEmpty()) {
                showError("Introduce un importe")
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                showError("El importe debe ser mayor que 0")
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                showError("Introduce una descripción")
                return@setOnClickListener
            }

            // Tipo: pestaña 0 = Gasto, pestaña 1 = Ingreso
            val type = if (tabTransactionType.selectedTabPosition == 0) "EXPENSE" else "INCOME"

            val expense = Expense(
                amount = amount,
                description = description,
                date = selectedDate,
                type = type,
                categoryId = selectedCategory?.id
            )

            viewModel.insertExpense(expense)
            showMessage("Movimiento guardado")
            clearForm()
        }
    }

    private fun clearForm() {
        etAmount.setText("")
        etDescription.setText("")
        etDate.setText(dateFormat.format(System.currentTimeMillis()))
        selectedDate = System.currentTimeMillis()
        selectedCategory = null
        acCategory.setText("")
        tabTransactionType.selectTab(tabTransactionType.getTabAt(0))
    }
}