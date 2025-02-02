package pt.ipt.dam.sabordigital.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import pt.ipt.dam.sabordigital.data.remote.models.IngredientListItem
import pt.ipt.dam.sabordigital.databinding.ItemIngredientBinding
import pt.ipt.dam.sabordigital.databinding.ItemRecipeIngredientBinding

/**
 * Adapter for displaying ingredients in a RecyclerView.
 *
 * Supports two view types:
 *  - IngredientOnly: Displays a basic ingredient with its image and name.
 *  - RecipeIngredient: Displays a recipe ingredient with additional information (amount and unit).
 *
 * @param items The list of ingredient items to display.
 * @param onItemClick A lambda function invoked when an ingredient is clicked (applies only to IngredientOnly view type).
 */
class IngredientAdapter(
    private val items: List<IngredientListItem>,
    private val onItemClick: (Ingredient) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_INGREDIENT = 0
        private const val VIEW_TYPE_RECIPE_INGREDIENT = 1
    }

    /**
     * ViewHolder for a basic ingredient item.
     *
     * @property binding The view binding for the ingredient item layout.
     */
    class IngredientViewHolder(val binding: ItemIngredientBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * ViewHolder for a recipe ingredient item.
     *
     * @property binding The view binding for the recipe ingredient item layout.
     */
    class RecipeIngredientViewHolder(val binding: ItemRecipeIngredientBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Returns the view type of the item at the given position.
     *
     * @param position The position of the item in the list.
     * @return VIEW_TYPE_INGREDIENT or VIEW_TYPE_RECIPE_INGREDIENT depending on the type of item.
     */
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is IngredientListItem.IngredientOnly -> VIEW_TYPE_INGREDIENT
            is IngredientListItem.RecipeIngredient -> VIEW_TYPE_RECIPE_INGREDIENT
        }
    }

    /**
     * Creates a new ViewHolder based on the view type.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The type of view to create.
     * @return A new ViewHolder for the corresponding view type.
     */
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

    /**
     * Binds the data for the item at the given position to its corresponding ViewHolder.
     *
     * For IngredientOnly, it loads the ingredient image (decoding Base64 if necessary) and sets the ingredient name.
     * For RecipeIngredient, it displays the ingredient name, amount, and unit.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is IngredientListItem.IngredientOnly -> {
                val ingredientHolder = holder as IngredientViewHolder
                val ingredient = item.ingredient
                ingredientHolder.binding.apply {
                    // Set the ingredient name.
                    ingredientName.text = ingredient.name
                    // Load the ingredient image.
                    if (!ingredient.imageUrl.isNullOrEmpty()) {
                        if (ingredient.imageUrl.startsWith("data:") || ingredient.imageUrl.length > 500) {
                            // Use helper function to decode Base64 image.
                            ImageHelper.setImageFromBase64(ingredientImage, ingredient.imageUrl)
                        } else {
                            // Use Glide to load image from URL.
                            Glide.with(root.context)
                                .load(ingredient.imageUrl)
                                .centerCrop()
                                .placeholder(R.drawable.placehold)
                                .error(R.drawable.placehold)
                                .into(ingredientImage)
                        }
                    } else {
                        // Set a placeholder image if no image URL is provided.
                        ingredientImage.setImageResource(R.drawable.placehold)
                    }
                    // Set click listener for the ingredient.
                    root.setOnClickListener { onItemClick(ingredient) }
                }
            }

            is IngredientListItem.RecipeIngredient -> {
                val recipeIngredientHolder = holder as RecipeIngredientViewHolder
                val recipeIngredient = item.recipeIngredient
                recipeIngredientHolder.binding.apply {
                    // Display the ingredient name.
                    ingredientName.text = recipeIngredient.ingredient.name
                    // Display the amount and unit.
                    ingredientAmount.text =
                        recipeIngredient.amount.toString() + " " + recipeIngredient.unit
                }
            }
        }
    }

    /**
     * Returns the total number of items in the adapter.
     */
    override fun getItemCount() = items.size
}
