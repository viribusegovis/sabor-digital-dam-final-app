package pt.ipt.dam.sabordigital.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.databinding.ActivityMainBinding
import pt.ipt.dam.sabordigital.databinding.NavigationLayoutBinding
import pt.ipt.dam.sabordigital.ui.auth.login.LoginActivity
import pt.ipt.dam.sabordigital.ui.auth.register.RegisterActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationBinding: NavigationLayoutBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        drawerLayout = binding.drawerLayout
        navigationBinding =
            NavigationLayoutBinding.bind(binding.root.findViewById(R.id.navigationLayout))

        setContentView(binding.root)
        setupObservers()
        setupNavigationDrawer()
        viewModel.checkAuthState(this)
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { isAuthenticated ->
            binding.authDebug.setOnClickListener {
                Toast.makeText(this, "Token Valid: " + isAuthenticated, Toast.LENGTH_LONG).show()
            }
            if (isAuthenticated) {
                navigationBinding.loginButton.visibility = View.GONE
                navigationBinding.registerButton.visibility = View.GONE
                navigationBinding.logoutButton.visibility = View.VISIBLE
            } else {
                navigationBinding.loginButton.visibility = View.VISIBLE
                navigationBinding.registerButton.visibility = View.VISIBLE
                navigationBinding.logoutButton.visibility = View.GONE
            }
        }
    }

    private fun setupNavigationDrawer() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        navigationBinding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        navigationBinding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        navigationBinding.logoutButton.setOnClickListener {
            viewModel.logout(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            drawerLayout.closeDrawer(GravityCompat.START)
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
