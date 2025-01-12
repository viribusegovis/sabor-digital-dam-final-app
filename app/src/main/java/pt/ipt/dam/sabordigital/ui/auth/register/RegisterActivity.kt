package pt.ipt.dam.sabordigital.ui.auth.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sabordigital.databinding.ActivityLoginBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
