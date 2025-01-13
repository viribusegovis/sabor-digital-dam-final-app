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
import pt.ipt.dam.sabordigital.utils.CategoryAdapter
import pt.ipt.dam.sabordigital.utils.IngredientAdapter

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
        setupRecyclerViews()
        setupObservers()
        setupNavigationDrawer()
        viewModel.checkAuthState(this)
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { isAuthenticated ->
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
        viewModel.authState.observe(this) { isAuthenticated ->
            if (isAuthenticated) {
                viewModel.fetchCategories(this)
                viewModel.fetchPopularIngredients(this)
            }
        }

        viewModel.categories.observe(this) { categories ->
            binding.categoriesRecyclerView.adapter = CategoryAdapter(categories) { category ->
                // Handle category click
            }
        }

        viewModel.ingredients.observe(this) { ingredients ->
            binding.ingredientsRecyclerView.adapter = IngredientAdapter(ingredients) { ingredient ->
                // Handle ingredient click
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

    private fun setupRecyclerViews() {
        // Horizontal layout for ingredients
        binding.ingredientsRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // Grid layout for categories (2 columns)
        binding.categoriesRecyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}
