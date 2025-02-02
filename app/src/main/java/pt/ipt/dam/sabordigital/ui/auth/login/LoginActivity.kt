package pt.ipt.dam.sabordigital.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.TokenResponse
import pt.ipt.dam.sabordigital.databinding.ActivityLoginBinding
import pt.ipt.dam.sabordigital.ui.UserAgreementActivity
import pt.ipt.dam.sabordigital.ui.auth.register.RegisterActivity
import pt.ipt.dam.sabordigital.ui.main.MainActivity
import pt.ipt.dam.sabordigital.utils.PreferencesHelper

// Activity handling user login functionality
// Implements user authentication, input validation, and navigation
// Uses ViewBinding for view access and ViewModel for business logic

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding    // View binding instance
    private val viewModel: LoginViewModel by viewModels() // ViewModel for login logic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()    // Set up LiveData observers
        setupLoginButton()  // Configure login button behavior

        // Check if user has accepted the agreement
        if (!PreferencesHelper.isUserAgreementAccepted(this)) {
            startActivity(Intent(this, UserAgreementActivity::class.java))
            finish()
        }

        // Configure info icon to show about dialog
        binding.infoIcon.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.info_about))
                .setMessage(getString(R.string.about_us_info))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
    }

    // Sets up observers for login state changes
    private fun setupObservers() {
        viewModel.loginState.observe(this) { result ->
            result.fold(
                onSuccess = { tokenResponse ->
                    saveLoginInfo(tokenResponse)          // Save auth info
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onFailure = { error ->
                    binding.loginError.text =
                        error.message ?: getString(R.string.invalid_credentials)
                    binding.loginError.visibility = View.VISIBLE
                }
            )
        }
    }

    // Configures login button and register link behavior
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

    // Validates user input for email and password
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
        binding.loginError.text = ""
        binding.loginError.visibility = View.GONE
        return true
    }

    // Saves authentication information to SharedPreferences
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
