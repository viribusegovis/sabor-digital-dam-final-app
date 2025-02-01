package pt.ipt.dam.sabordigital.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sabordigital.data.remote.models.TokenResponse
import pt.ipt.dam.sabordigital.databinding.ActivityLoginBinding
import pt.ipt.dam.sabordigital.ui.auth.register.RegisterActivity
import pt.ipt.dam.sabordigital.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupLoginButton()
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { result ->
            result.fold(
                onSuccess = { tokenResponse ->
                    saveLoginInfo(tokenResponse)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, "Erro no login", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (!viewModel.validateCredentials(email, password)) {
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

    private fun saveLoginInfo(response: TokenResponse) {
        val sharedPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("jwt_token", "${response.tokenType} ${response.accessToken}")
            .putInt("user_id", response.user.id)
            .putString("user_name", response.user.name)
            .putString("user_email", response.user.email)
            .putString("last_login", response.user.lastLogin)
            .apply()
    }
}
