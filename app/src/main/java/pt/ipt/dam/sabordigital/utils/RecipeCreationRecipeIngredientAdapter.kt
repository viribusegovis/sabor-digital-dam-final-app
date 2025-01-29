package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import pt.ipt.dam.sabordigital.data.remote.models.RecipeIngredient
import pt.ipt.dam.sabordigital.databinding.ItemRecipeCreationIngredientBinding

class RecipeCreationRecipeIngredientAdapter :
    RecyclerView.Adapter<RecipeCreationRecipeIngredientAdapter.ViewHolder>() {
    private val ingredients = mutableListOf<RecipeIngredient>()

    inner class ViewHolder(val binding: ItemRecipeCreationIngredientBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeCreationIngredientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipeIngredient = ingredients[position]
        holder.binding.apply {
            etIngredientName.setText(recipeIngredient.ingredient.name)
            etAmount.setText(recipeIngredient.amount.toString())
            etUnit.setText(recipeIngredient.unit)

            btnRemove.setOnClickListener {
                removeIngredient(position)
            }

            // Add text change listeners to update the ingredient object
            etIngredientName.doAfterTextChanged { text ->
                recipeIngredient.ingredient.name = text.toString()
            }
            etAmount.doAfterTextChanged { text ->
                text?.toString()?.let {
                    recipeIngredient.amount = it.toFloat()
                }
            }
            etUnit.doAfterTextChanged { text ->
                recipeIngredient.unit = text.toString()
            }
        }
    }

    override fun getItemCount() = ingredients.size

    fun addIngredient() {
        ingredients.add(
            RecipeIngredient(
                null,
                null,
                0.0f,
                "",
                Ingredient(null, "", "")
            )
        )
        notifyItemInserted(ingredients.size - 1)
    }

    fun removeIngredient(position: Int) {
        ingredients.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getIngredients() = ingredients.toList()
}
