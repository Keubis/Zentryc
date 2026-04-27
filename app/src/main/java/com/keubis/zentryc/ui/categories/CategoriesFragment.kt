package com.keubis.zentryc.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.keubis.zentryc.R
import com.keubis.zentryc.data.model.Category
import com.keubis.zentryc.ui.base.BaseFragment
import com.keubis.zentryc.ui.dashboard.DashboardViewModel

class CategoriesFragment : BaseFragment() {

    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: CategoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddCategory: FloatingActionButton

    // Colores predefinidos para nuevas categorías
    private val availableColors = listOf(
        "#FF5733", "#3380FF", "#9B59B6",
        "#2ECC71", "#F39C12", "#1ABC9C",
        "#E74C3C", "#3498DB", "#95A5A6"
    )
    private var colorIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        recyclerView = view.findViewById(R.id.recyclerCategories)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        fabAddCategory = view.findViewById(R.id.fabAddCategory)

        setupRecyclerView()
        observeCategories()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter { category ->
            // Confirmación antes de eliminar
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar categoría")
                .setMessage("¿Quieres eliminar '${category.name}'? Los movimientos con esta categoría quedarán sin categoría.")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.deleteCategory(category)
                    showMessage("Categoría eliminada")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeCategories() {
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)
            tvEmpty.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (categories.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setupFab() {
        fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        // Infla un layout simple con un campo de texto
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_category, null)

        AlertDialog.Builder(requireContext())
            .setTitle("Nueva categoría")
            .setView(dialogView)
            .setPositiveButton("Añadir") { _, _ ->
                val etName = dialogView.findViewById<TextInputEditText>(R.id.etCategoryName)
                val name = etName.text.toString().trim()

                if (name.isEmpty()) {
                    showError("Introduce un nombre")
                    return@setPositiveButton
                }

                // Asigna un color rotando entre los disponibles
                val color = availableColors[colorIndex % availableColors.size]
                colorIndex++

                val category = Category(
                    name = name,
                    iconName = "other",
                    colorHex = color
                )
                viewModel.insertCategory(category)
                showMessage("Categoría añadida")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}