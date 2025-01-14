package pt.ipt.dam.sabordigital.ui.main.re

import android.content.Intent
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
import pt.ipt.dam.sabordigital.ui.details.RecipeDetailsActivity
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
        loadInitialData()
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
        val intent = Intent(requireContext(), RecipeDetailsActivity::class.java).apply {
            putExtra("recipe_id", recipe.id)
            putExtra("recipe_title", recipe.title)
            putExtra("recipe_description", recipe.description)
            putExtra("recipe_preparation_time", recipe.preparationTime)
            putExtra("recipe_servings", recipe.servings)
            putExtra("recipe_difficulty", recipe.difficulty)
            putExtra("recipe_image_url", recipe.imageUrl)
        }
        startActivity(intent)
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
                    if (newText.isNullOrBlank()) {
                        loadInitialData()
                    }
                    return true
                }
            })
        }
    }

    private fun setupFilters() {
        // Map of category IDs to names based on the database
        val categories = mapOf(
            1 to "Pratos Principais",
            2 to "Sobremesas",
            3 to "Sopas",
            4 to "Entradas",
            5 to "Vegetariano"
        )

        categories.forEach { (id, name) ->
            val chip = Chip(context).apply {
                text = name
                isCheckable = true
                tag = id  // Store the category ID in the chip's tag
            }
            binding.chipGroupCategories.addView(chip)
        }

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                loadInitialData()
            } else {
                val chip = group.findViewById<Chip>(checkedIds.first())
                val categoryId = chip.tag as Int
                viewModel.filterByCategory(categoryId)
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
        }
    }

    private fun loadInitialData() {
        viewModel.loadRecipes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
