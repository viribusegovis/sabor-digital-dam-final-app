package pt.ipt.dam.sabordigital.ui.main.ui.home

import android.content.Context
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

    /**
     * Inflates the fragment's view using view binding.
     *
     * @param inflater LayoutInflater to inflate views in the fragment.
     * @param container Optional parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root view of the inflated layout.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView().
     * Sets up UI components, observers, and triggers data loading.
     *
     * @param view The view returned by onCreateView().
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWelcomeMessage()
        setupRecyclerViews()
        setupObservers()
        loadData()
    }

    /**
     * Sets up a welcome message using the user name retrieved from SharedPreferences.
     * Displays a personalized greeting.
     */
    private fun setupWelcomeMessage() {
        val sharedPrefs = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val userName = sharedPrefs.getString("user_name", "") ?: ""
        binding.welcomeText.text = getString(R.string.welcome_message, userName)
    }

    /**
     * Configures the RecyclerViews for displaying ingredients and categories.
     * Sets:
     * - Horizontal LinearLayoutManager for ingredients list.
     * - GridLayoutManager with 2 columns for categories.
     */
    private fun setupRecyclerViews() {
        binding.ingredientsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.categoriesRecyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    /**
     * Sets up observers for LiveData objects from the HomeViewModel.
     * Handles:
     * - Categories list: Sets an adapter with click listener to navigate to filtered RecipeListFragment.
     * - Ingredients list: Maps each ingredient to an IngredientOnly type and sets an adapter with a click listener.
     * - Loading state: Shows or hides the progress bar and stops swipe refresh animation when loading completes.
     *
     * Also configures the swipe-to-refresh layout and its color scheme.
     */
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

    /**
     * Initiates loading of categories and popular ingredients data.
     * Displays the swipe-to-refresh indicator while loading data.
     */
    private fun loadData() {
        context?.let {
            binding.swipeRefreshLayout.isRefreshing = true
            viewModel.fetchCategories()
            viewModel.fetchPopularIngredients()
        }
    }

    /**
     * Cleans up the binding instance when the view is destroyed to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
