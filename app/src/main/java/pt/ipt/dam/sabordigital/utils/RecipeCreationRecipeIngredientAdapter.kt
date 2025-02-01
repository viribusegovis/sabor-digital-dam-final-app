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

class RecipeCreationRecipeIngredientAdapter :
    RecyclerView.Adapter<RecipeCreationRecipeIngredientAdapter.ViewHolder>() {

    // List to hold recipe ingredients
    private val recipeIngredients = mutableListOf<RecipeIngredient>()
    private val errors = mutableMapOf<Int, MutableMap<String, Boolean>>()

    inner class ViewHolder(val binding: ItemRecipeCreationIngredientBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeCreationIngredientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipeIngredient = recipeIngredients[position]
        holder.binding.apply {
            // Set the ingredient name (if not chosen yet, this will be blank)
            etIngredientName.setText(recipeIngredient.ingredient.name)
            // Disable editing on the ingredient name field so that its value comes only from selection.
            etIngredientName.isEnabled = false
            etIngredientName.inputType = android.text.InputType.TYPE_NULL

            etAmount.setText(if (recipeIngredient.amount != null) recipeIngredient.amount.toString() else "")
            etUnit.setText(recipeIngredient.unit)

            btnRemove.setOnClickListener {
                removeIngredient(position)
            }

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

            etUnit.doAfterTextChanged { text ->
                recipeIngredient.unit = text.toString()
                validateField(position, "unit", !text.isNullOrBlank())
                updateErrorState(holder, position)
            }
        }

    }

    // This method is called when the “Add Ingredient” button is clicked.
// It shows the search dialog.
    fun showIngredientSearch(context: Context) {
        // Inflate the search dialog layout (see dialog_searchable_ingredient.xml below)
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_searchable_ingredient, null)
        val searchInput = dialogView.findViewById<TextInputEditText>(R.id.etSearch)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvIngredients)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)

        // Build and create the dialog.
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.select_ingredient)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
            .create()

        // Create an adapter for the search results.
        val searchAdapter = IngredientSearchAdapter { selectedIngredient ->
            // When a search result is clicked, add a new RecipeIngredient using the picked ingredient.
            addIngredient(selectedIngredient)
            dialog.dismiss()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = searchAdapter

        // Use a coroutine to debounce the search input.
        var searchJob: Job? = null
        searchInput.doOnTextChanged { text, _, _, _ ->
            searchJob?.cancel()
            searchJob = CoroutineScope(Dispatchers.Main).launch {
                delay(300) // 300ms debounce time
                val query = text?.toString() ?: ""
                if (query.length >= 2) {
                    progressBar.visibility = android.view.View.VISIBLE
                    try {
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
                    searchAdapter.updateIngredients(emptyList())
                }
            }
        }
        dialog.show()
    }

    private fun validateField(position: Int, field: String, isValid: Boolean) {
        if (!errors.containsKey(position)) {
            errors[position] = mutableMapOf()
        }
        errors[position]?.put(field, isValid)
    }

    private fun updateErrorState(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val positionErrors = errors[position] ?: return

            tilAmount.error = if (positionErrors["amount"] == false)
                holder.itemView.context.getString(R.string.error_amount_required) else null

            tilUnit.error = if (positionErrors["unit"] == false)
                holder.itemView.context.getString(R.string.error_unit_required) else null
        }
    }

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
        notifyDataSetChanged() // Update all error states
        return isValid
    }

    override fun getItemCount() = recipeIngredients.size

    fun removeIngredient(position: Int) {
        recipeIngredients.removeAt(position)
        errors.remove(position)
        notifyItemRemoved(position)
    }

    // Helper: Adds a new RecipeIngredient to the list using the selected ingredient.
    private fun addIngredient(ingredient: Ingredient) {
        recipeIngredients.add(
            RecipeIngredient(
                ingredient_id = ingredient.ingredient_id,
                amount = null,    // empty amount (user later fills)
                unit = "",        // empty unit
                ingredient = ingredient
            )
        )
        notifyItemInserted(recipeIngredients.size - 1)
    }

    fun getIngredients() = recipeIngredients.toList()
}