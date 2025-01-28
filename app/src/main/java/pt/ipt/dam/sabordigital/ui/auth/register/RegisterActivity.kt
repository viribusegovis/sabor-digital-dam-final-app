package pt.ipt.dam.sabordigital.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sabordigital.databinding.ActivityRegisterBinding
import pt.ipt.dam.sabordigital.ui.auth.login.LoginActivity
import pt.ipt.dam.sabordigital.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupButtons()
    }

    private fun setupObservers() {
        viewModel.registerState.observe(this) { result ->
            result.fold(
                onSuccess = { token ->
                    saveToken("Bearer ${token}")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, "Erro no registo", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun setupButtons() {
        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (validateInputs(name, email, password)) {
                viewModel.register(name, email, password)
            }
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(name: String, email: String, password: String): Boolean {
        if (!viewModel.validateCredentials(name, email, password)) {
            if (name.isEmpty()) {
                binding.nameInput.error = "Nome é obrigatório"
            }
            if (email.isEmpty()) {
                binding.emailInput.error = "Email é obrigatório"
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailInput.error = "Email inválido"
            }
            if (password.isEmpty()) {
                binding.passwordInput.error = "Password é obrigatória"
            }
            return false
        }
        return true
    }

    private fun saveToken(token: String) {
        val sharedPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        sharedPrefs.edit().putString("jwt_token", token).apply()
    }

}