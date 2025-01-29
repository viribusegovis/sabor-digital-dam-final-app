package pt.ipt.dam.sabordigital.ui.main.recipe_create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Category
import pt.ipt.dam.sabordigital.data.remote.models.RecipeCreate
import pt.ipt.dam.sabordigital.databinding.FragmentRecipeCreationBinding
import pt.ipt.dam.sabordigital.ui.main.MainActivity
import pt.ipt.dam.sabordigital.utils.RecipeCreationInstructionAdapter
import pt.ipt.dam.sabordigital.utils.RecipeCreationRecipeIngredientAdapter

class RecipeCreationFragment : Fragment() {
    private var _binding: FragmentRecipeCreationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeCreationViewModel by viewModels()
    private val ingredientAdapter = RecipeCreationRecipeIngredientAdapter()
    private val instructionAdapter = RecipeCreationInstructionAdapter()

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideMainFab()
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.showMainFab()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeCreationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupImagePicker()
        setupObservers()
        setupDifficultyDropdown()
        setupSubmitButton()
        viewModel.loadCategories() // Load categories when view is created
    }

    private fun setupRecyclerViews() {
        binding.rvIngredients.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ingredientAdapter
        }

        binding.rvInstructions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = instructionAdapter
        }

        binding.btnAddIngredient.setOnClickListener {
            ingredientAdapter.addIngredient()
        }

        binding.btnAddStep.setOnClickListener {
            instructionAdapter.addInstruction()
        }
    }

    private fun setupImagePicker() {
        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
    }

    private fun setupDifficultyDropdown() {
        val difficulties = arrayOf(
            getString(R.string.difficulty_easy),
            getString(R.string.difficulty_medium),
            getString(R.string.difficulty_hard)
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            difficulties
        )
        binding.actvDifficulty.inputType = InputType.TYPE_NULL  // Add this line
        binding.actvDifficulty.setAdapter(adapter)
    }


    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !isLoading
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.chipGroupCategories.removeAllViews()
            categories.forEach { category ->
                val chip = Chip(context).apply {
                    text = category.name
                    isCheckable = true
                    tag = category
                }
                binding.chipGroupCategories.addView(chip)
            }
        }

        viewModel.selectedImage.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.recipeImage.setImageURI(it)
                binding.btnAddImage.hide()
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                val recipe = createRecipeFromForm()
                viewModel.createRecipe(recipe)
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate title
        if (binding.etTitle.text.isNullOrBlank()) {
            binding.tilTitle.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilTitle.error = null
        }

        // Validate description
        if (binding.etDescription.text.isNullOrBlank()) {
            binding.tilDescription.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilDescription.error = null
        }

        // Validate prep time
        if (binding.etPrepTime.text.isNullOrBlank()) {
            binding.tilPrepTime.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilPrepTime.error = null
        }

        // Validate servings
        if (binding.etServings.text.isNullOrBlank()) {
            binding.tilServings.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilServings.error = null
        }

        // Validate ingredients
        if (ingredientAdapter.getIngredients().isEmpty()) {
            // Show error message for ingredients
            isValid = false
        }

        // Validate instructions
        if (instructionAdapter.getInstructions().isEmpty()) {
            // Show error message for instructions
            isValid = false
        }

        return isValid
    }

    private fun createRecipeFromForm(): RecipeCreate {

        val categories = binding.chipGroupCategories.checkedChipIds.map { chipId ->
            binding.chipGroupCategories.findViewById<Chip>(chipId).tag as Category
        }

        return RecipeCreate(
            title = binding.etTitle.text.toString(),
            description = binding.etDescription.text.toString(),
            preparation_time = binding.etPrepTime.text.toString().toIntOrNull() ?: 0,
            servings = binding.etServings.text.toString().toIntOrNull() ?: 0,
            difficulty = binding.actvDifficulty.text.toString(),
            ingredients = ingredientAdapter.getIngredients(),
            instructions = instructionAdapter.getInstructions(),
            categories = categories
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                viewModel.setSelectedImage(uri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
