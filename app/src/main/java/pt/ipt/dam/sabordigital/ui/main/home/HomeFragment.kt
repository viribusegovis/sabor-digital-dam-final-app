package pt.ipt.dam.sabordigital.ui.main.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.IngredientListItem
import pt.ipt.dam.sabordigital.databinding.FragmentHomeBinding
import pt.ipt.dam.sabordigital.ui.main.re.RecipeListFragment
import pt.ipt.dam.sabordigital.utils.CategoryAdapter
import pt.ipt.dam.sabordigital.utils.IngredientAdapter

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupObservers()
        loadData()
    }

    private fun setupRecyclerViews() {
        binding.ingredientsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.categoriesRecyclerView.layoutManager = GridLayoutManager(context, 2)
    }


    private fun setupObservers() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.categoriesRecyclerView.adapter = CategoryAdapter(categories) { category ->
                // Navigate to RecipeListFragment with category filter
                val fragment = RecipeListFragment().apply {
                    arguments = Bundle().apply {
                        putInt("category_id", category.category_id)
                        putString("category_name", category.name)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            binding.ingredientsRecyclerView.adapter = IngredientAdapter(
                items = ingredients.map { IngredientListItem.IngredientOnly(it) }
            ) { ingredient ->
                // Navigate to RecipeListFragment with ingredient filter
                val fragment = RecipeListFragment().apply {
                    arguments = Bundle().apply {
                        ingredient.ingredient_id?.let { putInt("ingredient_id", it) }
                        putString("ingredient_name", ingredient.name)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }


        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.primary_dark,
            R.color.accent
        )

    }

    private fun loadData() {
        context?.let {
            binding.swipeRefreshLayout.isRefreshing = true
            viewModel.fetchCategories(it)
            viewModel.fetchPopularIngredients(it)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
