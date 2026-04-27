package com.keubis.zentryc.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.keubis.zentryc.R
import com.keubis.zentryc.ui.base.BaseFragment

class LoginFragment : BaseFragment() {

    // Instancia de Firebase Authentication
    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnRegister: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnRegister = view.findViewById(R.id.btnRegister)

        setupButtons()
    }

    private fun setupButtons() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validaciones básicas
            if (email.isEmpty() || password.isEmpty()) {
                showError("Rellena todos los campos")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showError("La contraseña debe tener al menos 6 caracteres")
                return@setOnClickListener
            }

            // Intenta iniciar sesión con Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Login correcto, navega al dashboard
                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                }
                .addOnFailureListener { exception ->
                    showError("Error: ${exception.message}")
                }
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showError("Rellena todos los campos")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showError("La contraseña debe tener al menos 6 caracteres")
                return@setOnClickListener
            }

            // Crea una cuenta nueva en Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    showMessage("Cuenta creada correctamente")
                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                }
                .addOnFailureListener { exception ->
                    showError("Error: ${exception.message}")
                }
        }
    }
}