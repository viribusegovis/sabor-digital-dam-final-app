package pt.ipt.dam.sabordigital.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import pt.ipt.dam.sabordigital.data.remote.models.RecipeIngredient
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import pt.ipt.dam.sabordigital.databinding.ItemRecipeCreationIngredientBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Adapter for managing recipe ingredients during recipe creation.
 *
 * This adapter displays a list of RecipeIngredient items, allows for editing amount/unit,
 * validates inputs, and provides a searchable dialog to add new ingredients.
 */
class RecipeCreationRecipeIngredientAdapter :
    RecyclerView.Adapter<RecipeCreationRecipeIngredientAdapter.ViewHolder>() {

    // List holding the current RecipeIngredient objects.
    private val recipeIngredients = mutableListOf<RecipeIngredient>()

    // Map tracking validation errors for each ingredient field, keyed by item position.
    private val errors = mutableMapOf<Int, MutableMap<String, Boolean>>()

    /**
     * ViewHolder class for a recipe ingredient item.
     *
     * @property binding The view binding for the recipe ingredient item layout.
     */
    inner class ViewHolder(val binding: ItemRecipeCreationIngredientBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Inflates the ingredient item layout and returns a new ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeCreationIngredientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Binds the RecipeIngredient data to the ViewHolder.
     *
     * Sets the ingredient name (non-editable), amount, and unit fields;
     * attaches a listener on the remove button to delete the item;
     * and sets text change listeners on the amount and unit fields to update data and validation.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipeIngredient = recipeIngredients[position]
        holder.binding.apply {
            // Populate the ingredient name and disable editing (selection is done externally).
            etIngredientName.setText(recipeIngredient.ingredient.name)
            etIngredientName.isEnabled = false
            etIngredientName.inputType = android.text.InputType.TYPE_NULL

            // Set the amount and unit values (if available).
            etAmount.setText(if (recipeIngredient.amount != null) recipeIngredient.amount.toString() else "")
            etUnit.setText(recipeIngredient.unit)

            // Remove button deletes the ingredient from the list.
            btnRemove.setOnClickListener {
                removeIngredient(position)
            }

            // Validate and update RecipeIngredient.amount when the text changes.
            etAmount.doAfterTextChanged { text ->
                if (!text.isNullOrBlank()) {
                    text.toString().toFloatOrNull()?.let {
                        recipeIngredient.amount = it
                        validateField(position, "amount", true)
                    } ?: run {
                        validateField(position, "amount", false)
                    }
                } else {
                    recipeIngredient.amount = null
                    validateField(position, "amount", false)
                }
                updateErrorState(holder, position)
            }

            // Validate and update RecipeIngredient.unit when the text changes.
            etUnit.doAfterTextChanged { text ->
                recipeIngredient.unit = text.toString()
                validateField(position, "unit", !text.isNullOrBlank())
                updateErrorState(holder, position)
            }
        }
    }

    /**
     * Displays a searchable dialog to allow the user to select an ingredient.
     *
     * Inflates a search dialog layout containing a search input and RecyclerView.
     * Applies a debounce to the search input to avoid too many search requests,
     * and uses Retrofit to search for ingredients matching the query.
     * When an ingredient is clicked, it is added to the list.
     *
     * @param context The Context used to inflate the dialog.
     */
    fun showIngredientSearch(context: Context) {
        // Inflate the custom layout for ingredient search.
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_searchable_ingredient, null)
        val searchInput = dialogView.findViewById<TextInputEditText>(R.id.etSearch)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvIngredients)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)

        // Build the search dialog.
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.select_ingredient)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
            .create()

        // Create an adapter for displaying search results.
        val searchAdapter = IngredientSearchAdapter { selectedIngredient ->
            // When an ingredient is selected, add it to the list.
            addIngredient(selectedIngredient)
            dialog.dismiss()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = searchAdapter

        // Use a coroutine to debounce the search query.
        var searchJob: Job? = null
        searchInput.doOnTextChanged { text, _, _, _ ->
            searchJob?.cancel()
            searchJob = CoroutineScope(Dispatchers.Main).launch {
                delay(300) // Debounce time of 300ms.
                val query = text?.toString() ?: ""
                if (query.length >= 2) {
                    progressBar.visibility = android.view.View.VISIBLE
                    try {
                        // Perform the ingredient search using Retrofit.
                        RetrofitInitializer().ingredientService().searchIngredients(query)
                            .enqueue(object : Callback<List<Ingredient>> {
                                override fun onResponse(
                                    call: Call<List<Ingredient>>,
                                    response: Response<List<Ingredient>>
                                ) {
                                    if (response.isSuccessful) {
                                        val ingredients = response.body() ?: emptyList()
                                        searchAdapter.updateIngredients(ingredients)
                                    }
                                    progressBar.visibility = android.view.View.GONE
                                }

                                override fun onFailure(call: Call<List<Ingredient>>, t: Throwable) {
                                    progressBar.visibility = android.view.View.GONE
                                }
                            })
                    } catch (e: Exception) {
                        progressBar.visibility = android.view.View.GONE
                    }
                } else {
                    // Clear search results if query is too short.
                    searchAdapter.updateIngredients(emptyList())
                }
            }
        }
        dialog.show()
    }

    /**
     * Validates a specific field of an ingredient item.
     *
     * @param position The position of the item.
     * @param field The field name ("amount" or "unit").
     * @param isValid True if the field value is valid, false otherwise.
     */
    private fun validateField(position: Int, field: String, isValid: Boolean) {
        if (!errors.containsKey(position)) {
            errors[position] = mutableMapOf()
        }
        errors[position]?.put(field, isValid)
    }

    /**
     * Updates the error UI of a recipe ingredient item.
     *
     * Sets error messages on the TextInputLayout for amount and unit if the corresponding validation fails.
     *
     * @param holder The ViewHolder corresponding to the item.
     * @param position The item position in the list.
     */
    private fun updateErrorState(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val positionErrors = errors[position] ?: return

            tilAmount.error = if (positionErrors["amount"] == false)
                holder.itemView.context.getString(R.string.error_amount_required) else null

            tilUnit.error = if (positionErrors["unit"] == false)
                holder.itemView.context.getString(R.string.error_unit_required) else null
        }
    }

    /**
     * Validates all ingredients in the list.
     *
     * Checks that each ingredient has a non-null amount and a non-blank unit.
     * Returns false if any ingredient or the list itself is invalid, and refreshes the UI.
     *
     * @return True if all ingredients are valid, false otherwise.
     */
    fun validateAllIngredients(): Boolean {
        var isValid = true
        if (recipeIngredients.isEmpty()) {
            isValid = false
        }

        recipeIngredients.forEachIndexed { index, ingredient ->
            val amountValid = ingredient.amount != null
            val unitValid = !ingredient.unit.isNullOrBlank()

            validateField(index, "amount", amountValid)
            validateField(index, "unit", unitValid)

            if (!amountValid || !unitValid) {
                isValid = false
            }
        }
        notifyDataSetChanged() // Refresh error states in the UI.
        return isValid
    }

    /**
     * Returns the total number of recipe ingredient items.
     *
     * @return The size of the recipeIngredients list.
     */
    override fun getItemCount() = recipeIngredients.size

    /**
     * Removes a recipe ingredient at the specified position from the list.
     *
     * Also removes any associated validation errors and notifies the adapter.
     *
     * @param position The position of the ingredient to remove.
     */
    fun removeIngredient(position: Int) {
        recipeIngredients.removeAt(position)
        errors.remove(position)
        notifyItemRemoved(position)
    }

    /**
     * Adds a new RecipeIngredient to the list using the selected ingredient.
     *
     * Initializes the new item with a null amount and an empty unit.
     *
     * @param ingredient The Ingredient object selected by the user.
     */
    private fun addIngredient(ingredient: Ingredient) {
        recipeIngredients.add(
            RecipeIngredient(
                ingredient_id = ingredient.ingredient_id,
                amount = null,    // User will fill in later.
                unit = "",        // User will fill in later.
                ingredient = ingredient
            )
        )
        notifyItemInserted(recipeIngredients.size - 1)
    }

    /**
     * Retrieves an immutable list of RecipeIngredient items.
     *
     * @return A copy of the recipeIngredients list.
     */
    fun getIngredients() = recipeIngredients.toList()
}
