package pt.ipt.dam.sabordigital.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.databinding.ActivityMainBinding
import pt.ipt.dam.sabordigital.databinding.NavigationLayoutBinding
import pt.ipt.dam.sabordigital.ui.auth.login.LoginActivity
import pt.ipt.dam.sabordigital.ui.auth.register.RegisterActivity
import pt.ipt.dam.sabordigital.ui.main.ui.home.HomeFragment

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

        setupNavigationDrawer()
        setupObservers()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { isAuthenticated ->
            updateAuthButtons(isAuthenticated)
        }
    }

    private fun updateAuthButtons(isAuthenticated: Boolean) {
        navigationBinding.apply {
            loginButton.visibility = if (isAuthenticated) View.GONE else View.VISIBLE
            registerButton.visibility = if (isAuthenticated) View.GONE else View.VISIBLE
            logoutButton.visibility = if (isAuthenticated) View.VISIBLE else View.GONE
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
