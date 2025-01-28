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

class RecipeListFragment : Fragment() {
    private var _binding: FragmentRecipeListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeListViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupFilters()
        setupObservers()
        handleArguments()
    }

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

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            navigateToRecipeDetails(recipe)
        }
        binding.recipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recipeAdapter
        }
    }

    private fun navigateToRecipeDetails(recipe: Recipe) {
        // Navigate to RecipeListFragment with ingredient filter
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

    private fun setupSearchView() {
        binding.searchView.apply {
            queryHint = getString(R.string.search_hint)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.searchRecipes(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (hasFocus()) { // Only handle user-initiated changes
                        when {
                            newText.isNullOrBlank() -> {
                                viewModel.loadRecipes()
                                binding.chipGroupCategories.clearCheck()
                                binding.chipGroupIngredients.clearCheck()
                            }

                            else -> viewModel.searchRecipes(newText)
                        }
                    }
                    return true
                }
            })
        }
    }

    private fun setupFilters() {
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

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                // Clear ingredients only when actively selecting a category
                binding.chipGroupIngredients.post {
                    binding.chipGroupIngredients.clearCheck()
                }
                val chip = group.findViewById<Chip>(checkedIds.first())
                viewModel.filterByCategory(chip.tag as Int)
            } else {
                // Only clear if there was an active category filter
                if (viewModel.getCurrentCategoryId() != null) {
                    viewModel.clearFilters()
                }
            }
        }

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

        binding.chipGroupIngredients.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                // Clear categories only when actively selecting an ingredient
                binding.chipGroupCategories.post {
                    binding.chipGroupCategories.clearCheck()
                }
                val chip = group.findViewById<Chip>(checkedIds.first())
                viewModel.filterByIngredient(chip.tag as Int)
            } else {
                // Only clear if there was an active ingredient filter
                if (viewModel.getCurrentIngredientId() != null) {
                    viewModel.clearFilters()
                }
            }
        }
    }


    private fun setupObservers() {
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipeAdapter = RecipeAdapter(recipes) { recipe ->
                navigateToRecipeDetails(recipe)
            }
            binding.recipesRecyclerView.adapter = recipeAdapter
            binding.emptyStateText.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            viewModel.refreshWithCurrentFilters()
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.primary_dark,
            R.color.accent
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
