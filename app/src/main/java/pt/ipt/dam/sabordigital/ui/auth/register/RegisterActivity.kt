package pt.ipt.dam.sabordigital.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.TokenResponse
import pt.ipt.dam.sabordigital.databinding.ActivityRegisterBinding
import pt.ipt.dam.sabordigital.ui.auth.login.LoginActivity
import pt.ipt.dam.sabordigital.ui.main.MainActivity

// Activity handling user registration functionality
// Manages user registration form, validation, and navigation
// Uses ViewBinding for view access and ViewModel for business logic

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding   // View binding instance
    private val viewModel: RegisterViewModel by viewModels() // ViewModel for registration logic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()  // Set up LiveData observers
        setupButtons()    // Configure button behaviors
    }

    /**
     * Sets up LiveData observers for registration state
     * Handles successful registration by saving user info and navigating to MainActivity
     * Shows error toast on registration failure
     */
    private fun setupObservers() {
        viewModel.loginState.observe(this) { result ->
            result.fold(
                onSuccess = { tokenResponse ->
                    saveLoginInfo(tokenResponse)          // Save auth info
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onFailure = {
                    Toast.makeText(this, "Erro no login", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    /**
     * Configures click listeners for registration button and login link
     * Registration button validates inputs before attempting registration
     * Login link navigates user back to login screen
     */
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

    /**
     * Validates user input for registration form
     *
     * @param name User's full name
     * @param email User's email address
     * @param password User's chosen password
     * @return Boolean indicating if all inputs are valid
     *
     * Validation rules:
     * - Name must not be empty
     * - Email must not be empty and must be valid format
     * - Password must not be empty and must be at least 8 characters
     */
    private fun validateInputs(name: String, email: String, password: String): Boolean {
        if (!viewModel.validateCredentials(name = name, email = email, password = password)) {
            if (name.isEmpty()) {
                binding.nameInput.error = getString(R.string.name_required)
            }
            if (email.isEmpty()) {
                binding.emailInput.error = getString(R.string.email_required)
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailInput.error = getString(R.string.email_invalid)
            }
            if (password.isEmpty()) {
                binding.passwordInput.error = getString(R.string.password_required)
            }
            if (password.length < 8) {
                binding.passwordInput.error = getString(R.string.password_size)
            }
            return false
        }
        return true
    }

    /**
     * Saves user authentication information to SharedPreferences
     *
     * @param response TokenResponse containing user and authentication details
     *
     * Saved information includes:
     * - JWT token with type
     * - User ID
     * - User name
     * - User email
     * - Last login timestamp
     */
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
