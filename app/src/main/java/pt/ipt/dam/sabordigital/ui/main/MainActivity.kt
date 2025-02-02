package pt.ipt.dam.sabordigital.ui.main

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.databinding.ActivityMainBinding
import pt.ipt.dam.sabordigital.ui.main.profile.ProfileFragment
import pt.ipt.dam.sabordigital.ui.main.re.RecipeListFragment
import pt.ipt.dam.sabordigital.ui.main.recipe_create.RecipeCreationFragment
import pt.ipt.dam.sabordigital.ui.main.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        viewModel.checkAuthState(binding.root.context)
        setupNavigationDrawer()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }

    private fun setupNavigationDrawer() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        // Access through main binding
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                    true
                }

                R.id.nav_search -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, RecipeListFragment())
                        .commit()
                    true
                }


                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ProfileFragment())
                        .commit()
                    true
                }


                else -> false
            }
        }

        binding.addRecipeFab.setOnClickListener() {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RecipeCreationFragment())
                .commit()
        }

    }

    fun hideMainFab() {
        binding.addRecipeFab.hide()
    }

    fun showMainFab() {
        binding.addRecipeFab.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}
