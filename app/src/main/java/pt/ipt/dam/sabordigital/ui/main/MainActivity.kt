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

/**
 * MainActivity serves as the central container for navigation between fragments.
 *
 * It sets up the navigation drawer, bottom navigation, and floating action button
 * for recipe creation. The activity monitors the authentication state and handles
 * fragment transactions based on user interactions.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private val viewModel: MainViewModel by viewModels()

    /**
     * Called when the activity is first created.
     *
     * Inflates the layout, sets up the navigation drawer and bottom navigation,
     * and checks the user's authentication state. If there is no saved instance state,
     * loads the HomeFragment as the initial view.
     *
     * @param savedInstanceState A Bundle containing the activity's previously frozen state (if any).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.checkAuthState(binding.root.context)
        setupNavigationDrawer()

        // Load HomeFragment on first launch.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }

    /**
     * Sets up the navigation drawer and bottom navigation.
     *
     * Configures the action bar to display a menu icon, handles bottom navigation item selections
     * to swap the current fragment, and sets the floating action button listener to open
     * the RecipeCreationFragment.
     */
    private fun setupNavigationDrawer() {
        // Enable up navigation and set the menu icon.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        // Bottom navigation item selection.
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

        // Floating action button to create a new recipe.
        binding.addRecipeFab.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, RecipeCreationFragment())
                .commit()
        }
    }

    /**
     * Hides the floating action button.
     *
     * Called to reduce distractions when navigating to fragments where the FAB is not needed.
     */
    fun hideMainFab() {
        binding.addRecipeFab.hide()
    }

    /**
     * Shows the floating action button.
     *
     * Called when returning to fragments where the FAB should be visible.
     */
    fun showMainFab() {
        binding.addRecipeFab.show()
    }

    /**
     * Handles action bar item clicks.
     *
     * Overrides the behavior for home button, opening the navigation drawer when clicked.
     *
     * @param item The selected menu item.
     * @return True if the click was handled; otherwise, delegates to the superclass.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Open the navigation drawer.
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}