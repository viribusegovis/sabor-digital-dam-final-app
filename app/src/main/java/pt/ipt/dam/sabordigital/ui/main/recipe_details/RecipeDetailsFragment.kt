package pt.ipt.dam.sabordigital.ui.main.recipe_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.databinding.FragmentRecipeDetailsBinding

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
                /*Glide.with(requireContext())
                    .load(recipe.imageUrl)
                    .centerCrop()
                    .into(recipeImage)*/
            }

            // Set chips information
            binding.difficultyChip.text = when (recipe.difficulty) {
                "FACIL" -> getString(R.string.difficulty_easy)
                "MEDIO" -> getString(R.string.difficulty_medium)
                "DIFICIL" -> getString(R.string.difficulty_hard)
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

        viewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            // TODO: Setup ingredients adapter
        }

        viewModel.instructions.observe(viewLifecycleOwner) { instructions ->
            // TODO: Setup instructions adapter
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
