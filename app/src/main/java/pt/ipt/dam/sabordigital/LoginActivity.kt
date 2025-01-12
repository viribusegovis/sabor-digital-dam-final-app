package pt.ipt.dam.sabordigital

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sabordigital.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLoginButton()
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (validarCredenciais(email, password)) {
                realizarLogin(email, password)
            }
        }
    }

    private fun realizarLogin(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInitializer().authService().login(email, password)
                if (response.isSuccessful) {
                    val token = response.body()?.accessToken
                    guardarToken(token)
                    navegarParaHome()
                } else {
                    mostrarErro("Credenciais inválidas")
                }
            } catch (e: Exception) {
                mostrarErro("Erro ao realizar login")
            }
        }
    }

    private fun guardarToken(token: String?) {
        val sharedPrefs = EncryptedSharedPreferences.create(
            "auth_prefs",
            "master_key",
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        sharedPrefs.edit().putString("access_token", token).apply()
    }
}

