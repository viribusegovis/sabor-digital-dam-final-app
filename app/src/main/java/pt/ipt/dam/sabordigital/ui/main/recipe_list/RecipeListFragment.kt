package pt.ipt.dam.sabordigital.ui.main.re

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.databinding.FragmentRecipeListBinding
import pt.ipt.dam.sabordigital.ui.main.recipe_details.RecipeDetailsFragment
import pt.ipt.dam.sabordigital.ui.main.recipe_list.ui.recipelist.RecipeListViewModel
import pt.ipt.dam.sabordigital.utils.RecipeAdapter

/**
 * Fragment displaying a list of recipes.
 *
 * Supports filtering via search and chip-based filters (categories and ingredients).
 * Navigates to the recipe details screen when a recipe is selected.
 */
class RecipeListFragment : Fragment() {

    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeListViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    /**
     * Inflates the fragment's view using view binding.
     *
     * @param inflater LayoutInflater to inflate views.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed.
     * @return The root view of the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Configures the UI components and observers after the view is created.
     *
     * Sets up the RecyclerView, search view, and filter chips.
     * Also handles any passed arguments to apply default filters.
     *
     * @param view The view returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()  // Initialize recipe list RecyclerView.
        setupSearchView()    // Configure the search bar.
        setupFilters()       // Setup category and ingredient chip filters.
        setupObservers()     // Listen for LiveData updates.
        handleArguments()    // Process fragment arguments to apply filters.
    }

    /**
     * Processes any arguments passed to this fragment.
     *
     * Loads available filters (categories and ingredients) and applies them.
     * If arguments contain a specific filter (category or ingredient), applies that filter.
     */
    private fun handleArguments() {
        viewModel.loadCategories()
        viewModel.loadIngredients()
        if (viewModel.hasFilters()) {
            viewModel.refreshWithCurrentFilters()
        } else {
            arguments?.let { bundle ->
                when {
                    bundle.containsKey("category_id") -> {
                        val categoryId = bundle.getInt("category_id")
                        viewModel.filterByCategory(categoryId)
                    }

                    bundle.containsKey("ingredient_id") -> {
                        val ingredientId = bundle.getInt("ingredient_id")
                        viewModel.filterByIngredient(ingredientId)
                    }
                }
            } ?: viewModel.refreshWithCurrentFilters()
        }
    }

    /**
     * Initializes the RecyclerView for displaying recipes.
     *
     * Uses a LinearLayoutManager and sets a RecipeAdapter with an item click handler
     * to navigate to the RecipeDetailsFragment.
     */
    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list.
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            navigateToRecipeDetails(recipe)
        }
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    /**
     * Navigates to the RecipeDetailsFragment to display details for a selected recipe.
     *
     * Passes the recipe details via fragment arguments.
     *
     * @param recipe The selected Recipe object.
     */
    private fun navigateToRecipeDetails(recipe: Recipe) {
        val fragment = RecipeDetailsFragment().apply {
            arguments = Bundle().apply {
                putInt("recipe_id", recipe.id)
                putString("recipe_title", recipe.title)
                putString("recipe_description", recipe.description)
                putInt("recipe_preparation_time", recipe.preparation_time)
                putInt("recipe_servings", recipe.servings)
                putString("recipe_difficulty", recipe.difficulty)
                putString("recipe_image_url", recipe.imageUrl)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Configures the SearchView for recipe search.
     *
     * Listens for query submissions and changes.
     * Initiates search queries via the ViewModel.
     */
    private fun setupSearchView() {
        binding.searchView.apply {
            queryHint = getString(R.string.search_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.searchRecipes(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // Only handle changes initiated by the user.
                    if (hasFocus()) {
                        when {
                            newText.isNullOrBlank() -> {
                                viewModel.loadRecipes()         // Reload original list if the query is empty.
                                binding.chipGroupCategories.clearCheck() // Clear category filter.
                                binding.chipGroupIngredients.clearCheck() // Clear ingredient filter.
                            }

                            else -> viewModel.searchRecipes(newText) // Perform search query.
                        }
                    }
                    return true
                }
            })
        }
    }

    /**
     * Sets up filter chips for categories and ingredients.
     *
     * Observes categories and ingredients LiveData to dynamically build chip groups.
     * Handles chip selections; selecting a category will clear ingredient selection and vice versa.
     */
    private fun setupFilters() {
        // Observe categories and create chips.
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.chipGroupCategories.removeAllViews()
            categories.forEach { category ->
                val chip = Chip(context).apply {
                    text = category.name
                    isCheckable = true
                    tag = category.category_id
                    isChecked = category.category_id == viewModel.getCurrentCategoryId()
                }
                binding.chipGroupCategories.addView(chip)
            }
        }
        // Handle category chip selection.
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                // Clear ingredient selections when a category is selected.
                binding.chipGroupIngredients.post {
                    binding.chipGroupIngredients.clearCheck()
                }
                val chip = group.findViewById<Chip>(checkedIds.first())
                viewModel.filterByCategory(chip.tag as Int)
            } else {
                if (viewModel.getCurrentCategoryId() != null) {
                    viewModel.clearFilters()
                }
            }
        }

        // Observe ingredients and create chips.
        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            binding.chipGroupIngredients.removeAllViews()
            ingredients.forEach { ingredient ->
                val chip = Chip(context).apply {
                    text = ingredient.name
                    isCheckable = true
                    tag = ingredient.ingredient_id
                    isChecked = ingredient.ingredient_id == viewModel.getCurrentIngredientId()
                }
                binding.chipGroupIngredients.addView(chip)
            }
        }
        // Handle ingredient chip selection.
        binding.chipGroupIngredients.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                // Clear category selections when an ingredient is selected.
                binding.chipGroupCategories.post {
                    binding.chipGroupCategories.clearCheck()
                }
                val chip = group.findViewById<Chip>(checkedIds.first())
                viewModel.filterByIngredient(chip.tag as Int)
            } else {
                if (viewModel.getCurrentIngredientId() != null) {
                    viewModel.clearFilters()
                }
            }
        }
    }

    /**
     * Sets up observers for LiveData objects from the ViewModel.
     *
     * Observes recipe list, loading state, and swipe-to-refresh events.
     * Updates the RecyclerView adapter and displays an empty state message if needed.
     */
    private fun setupObservers() {
        // Observe the list of recipes.
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter = RecipeAdapter(recipes) { recipe ->
                navigateToRecipeDetails(recipe)
            }
            binding.recipesRecyclerView.adapter = recipeAdapter
            // Show or hide empty state message.
            binding.emptyStateText.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe the loading state.
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        // Set up swipe-to-refresh functionality.
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            viewModel.refreshWithCurrentFilters()
        }

        // Define color scheme for the swipe-to-refresh indicator.
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.primary_dark,
            R.color.accent
        )
    }

    /**
     * Cleans up view binding when the view is destroyed to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}