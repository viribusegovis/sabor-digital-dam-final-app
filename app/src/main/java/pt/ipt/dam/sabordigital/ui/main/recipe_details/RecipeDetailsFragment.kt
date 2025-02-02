package pt.ipt.dam.sabordigital.ui.main.recipe_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.IngredientListItem
import pt.ipt.dam.sabordigital.databinding.FragmentRecipeDetailsBinding
import pt.ipt.dam.sabordigital.utils.ImageHelper
import pt.ipt.dam.sabordigital.utils.IngredientAdapter
import pt.ipt.dam.sabordigital.utils.InstructionAdapter

/**
 * Fragment displaying the details of a recipe.
 *
 * This fragment shows the recipe's title, description, image,
 * difficulty, preparation time, servings, ingredients, and instructions.
 * It loads recipe data from a ViewModel based on a provided recipe ID.
 */
class RecipeDetailsFragment : Fragment() {

    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeDetailsViewModel by viewModels()

    /**
     * Inflates the fragment layout using view binding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed.
     * @return The root View of the inflated layout.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up observers for LiveData objects and loads recipe data.
     *
     * Called immediately after onCreateView, this method configures UI components
     * and triggers the loading of detailed recipe information.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()      // Setup UI observers for LiveData updates.
        loadRecipeDetails()   // Load recipe details based on recipe_id argument.
    }

    /**
     * Configures observers for various LiveData objects in the ViewModel.
     *
     * Observes recipe details, ingredients, instructions, and loading state.
     * Updates the UI components accordingly, including image loading with Glide
     * or decoding a Base64 string using ImageHelper.
     */
    private fun setupObservers() {
        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            // Set recipe title and description.
            binding.titleText.text = recipe.title
            binding.descriptionText.text = recipe.description

            // Load recipe image.
            if (!recipe.imageUrl.isNullOrEmpty()) {
                // Check if imageUrl is a Base64 string (starts with "data:" or is unusually long).
                if (recipe.imageUrl.startsWith("data:") || recipe.imageUrl.length > 500) {
                    // Decode and display Base64 image.
                    ImageHelper.setImageFromBase64(binding.recipeImage, recipe.imageUrl)
                } else {
                    // Otherwise, load image from URL using Glide.
                    Glide.with(requireContext())
                        .load(recipe.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.placehold)
                        .error(R.drawable.placehold)
                        .into(binding.recipeImage)
                }
            }

            // Set difficulty chip text.
            binding.difficultyChip.text = when (recipe.difficulty) {
                "FACIL" -> getString(R.string.stars_easy)
                "MEDIO" -> getString(R.string.stars_medium)
                "DIFICIL" -> getString(R.string.stars_hard)
                else -> getString(R.string.difficulty_unknown)
            }

            // Set preparation time and servings chip text.
            binding.timeChip.text =
                getString(R.string.recipe_prep_time_format, recipe.preparation_time)
            binding.servingsChip.text = getString(R.string.recipe_servings_format, recipe.servings)
        }

        viewModel.ingredients.observe(viewLifecycleOwner) { recipeIngredients ->
            // Configure RecyclerView for ingredients with a horizontal list.
            binding.ingredientsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = IngredientAdapter(
                    // Map each element to IngredientListItem.RecipeIngredient.
                    recipeIngredients.map { IngredientListItem.RecipeIngredient(it) }
                ) {
                    // No click handling in the details view.
                }
            }
        }

        viewModel.instructions.observe(viewLifecycleOwner) { instructions ->
            // Configure RecyclerView for instructions with a vertical list.
            binding.instructionsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = InstructionAdapter(instructions)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Show or hide the progress bar based on loading state.
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    /**
     * Loads recipe details based on passed arguments.
     *
     * Reads the recipe ID from the fragment arguments and instructs the ViewModel
     * to load the recipe details, ingredients, and instructions.
     */
    private fun loadRecipeDetails() {
        arguments?.let { args ->
            val recipeId = args.getInt("recipe_id")
            viewModel.loadRecipeDetails(recipeId)
            viewModel.loadRecipeIngredients(recipeId)
            viewModel.loadRecipeInstructions(recipeId)
        }
    }

    /**
     * Cleans up view binding when the view is destroyed to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}