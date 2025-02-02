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

class RecipeDetailsFragment : Fragment() {
    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        loadRecipeDetails()
    }

    private fun setupObservers() {
        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            binding.titleText.text = recipe.title
            binding.descriptionText.text = recipe.description

            // Load recipe image
            if (!recipe.imageUrl.isNullOrEmpty()) {
                // Check if imageUrl looks like a Base64 string (it might start with "data:" or be very long)
                if (recipe.imageUrl.startsWith("data:") || recipe.imageUrl.length > 500) {
                    // If Base64-encoded, use our helper function to decode and display it.
                    ImageHelper.setImageFromBase64(binding.recipeImage, recipe.imageUrl)
                } else {
                    // Otherwise, assume it's a URL and load with Glide.
                    Glide.with(requireContext())
                        .load(recipe.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.placehold)
                        .error(R.drawable.placehold)
                        .into(binding.recipeImage)
                }
            }

            // Set chips information
            binding.difficultyChip.text = when (recipe.difficulty) {
                "FACIL" -> getString(R.string.stars_easy)
                "MEDIO" -> getString(R.string.stars_medium)
                "DIFICIL" -> getString(R.string.stars_hard)
                else -> getString(R.string.difficulty_unknown)
            }

            binding.timeChip.text = getString(
                R.string.recipe_prep_time_format,
                recipe.preparation_time
            )

            binding.servingsChip.text = getString(
                R.string.recipe_servings_format,
                recipe.servings
            )

        }

        viewModel.ingredients.observe(viewLifecycleOwner) { recipeIngredients ->
            binding.ingredientsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = IngredientAdapter(
                    recipeIngredients.map { IngredientListItem.RecipeIngredient(it) }
                ) { ingredient ->
                    // Empty lambda since we don't need click handling in details
                }
            }
        }

        viewModel.instructions.observe(viewLifecycleOwner) { instructions ->
            binding.instructionsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = InstructionAdapter(instructions)
            }
        }


        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadRecipeDetails() {
        arguments?.let { args ->
            val recipeId = args.getInt("recipe_id")
            viewModel.loadRecipeDetails(recipeId)
            viewModel.loadRecipeIngredients(recipeId)
            viewModel.loadRecipeInstructions(recipeId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
