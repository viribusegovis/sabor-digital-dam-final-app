package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import pt.ipt.dam.sabordigital.data.remote.models.IngredientListItem
import pt.ipt.dam.sabordigital.databinding.ItemIngredientBinding
import pt.ipt.dam.sabordigital.databinding.ItemRecipeIngredientBinding

class IngredientAdapter(
    private val items: List<IngredientListItem>,
    private val onItemClick: (Ingredient) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_INGREDIENT = 0
        private const val VIEW_TYPE_RECIPE_INGREDIENT = 1
    }

    class IngredientViewHolder(val binding: ItemIngredientBinding) :
        RecyclerView.ViewHolder(binding.root)

    class RecipeIngredientViewHolder(val binding: ItemRecipeIngredientBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is IngredientListItem.IngredientOnly -> VIEW_TYPE_INGREDIENT
            is IngredientListItem.RecipeIngredient -> VIEW_TYPE_RECIPE_INGREDIENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_INGREDIENT -> {
                val binding = ItemIngredientBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                IngredientViewHolder(binding)
            }

            VIEW_TYPE_RECIPE_INGREDIENT -> {
                val binding = ItemRecipeIngredientBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                RecipeIngredientViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is IngredientListItem.IngredientOnly -> {
                val ingredientHolder = holder as IngredientViewHolder
                val ingredient = item.ingredient
                ingredientHolder.binding.apply {
                    ingredientName.text = ingredient.name
                    if (!ingredient.imageUrl.isNullOrEmpty()) {
                        Glide.with(ingredientImage.context)
                            .load(ingredient.imageUrl)
                            .into(ingredientImage)
                    }
                    root.setOnClickListener { onItemClick(ingredient) }
                }
            }

            is IngredientListItem.RecipeIngredient -> {
                val recipeIngredientHolder = holder as RecipeIngredientViewHolder
                val recipeIngredient = item.recipeIngredient
                recipeIngredientHolder.binding.apply {
                    ingredientName.text = recipeIngredient.ingredient.name
                    ingredientAmount.text =
                        recipeIngredient.amount.toString() + " " + recipeIngredient.unit
                }
            }

        }
    }

    override fun getItemCount() = items.size
}
