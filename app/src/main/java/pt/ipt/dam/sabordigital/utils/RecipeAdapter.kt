package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.databinding.ItemRecipeBinding

/**
 * Adapter for displaying recipes in a RecyclerView.
 *
 * Each item shows the recipe title, description, difficulty, preparation time, servings, and an image.
 * Tapping on an item invokes the provided onItemClick callback with the corresponding Recipe.
 *
 * @param recipes The list of Recipe objects to display.
 * @param onItemClick A lambda function invoked when a recipe item is clicked.
 */
class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    /**
     * ViewHolder class holding the binding for a recipe item.
     *
     * @property binding The view binding for the recipe item layout.
     */
    class ViewHolder(val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Inflates the recipe item layout and returns a new ViewHolder.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder instance with the inflated layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Binds the data for the recipe at the given position to the ViewHolder.
     *
     * Configures the title, description, difficulty chip, time, servings, and image.
     * If an image URL exists, it is loaded using either a Base64 helper function or Glide,
     * based on the contents of the URL.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the recipe in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.binding.apply {
            // Set the recipe title and description.
            titleText.text = recipe.title
            descriptionText.text = recipe.description

            // Set the difficulty chip text based on the recipe difficulty.
            difficultyChip.text = when (recipe.difficulty) {
                "FACIL" -> root.context.getString(R.string.stars_easy)
                "MEDIO" -> root.context.getString(R.string.stars_medium)
                "DIFICIL" -> root.context.getString(R.string.stars_hard)
                else -> root.context.getString(R.string.difficulty_unknown)
            }

            // Format and set the preparation time.
            timeChip.text = root.context.getString(
                R.string.recipe_prep_time_format,
                recipe.preparation_time
            )

            // Format and set the servings information.
            servingsChip.text = root.context.getString(
                R.string.recipe_servings_format,
                recipe.servings
            )

            // Attempt to load the recipe image.
            if (!recipe.imageUrl.isNullOrEmpty()) {
                if (recipe.imageUrl.startsWith("data:") || recipe.imageUrl.length > 500) {
                    // If the image URL is Base64-encoded, use the helper function to decode and set it.
                    ImageHelper.setImageFromBase64(recipeImage, recipe.imageUrl)
                } else {
                    // Otherwise, load the image from the URL using Glide.
                    Glide.with(root.context)
                        .load(recipe.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.placehold)
                        .error(R.drawable.placehold)
                        .into(recipeImage)
                }
            } else {
                // Set a placeholder image if no URL is provided.
                recipeImage.setImageResource(R.drawable.placehold)
            }

            // Set a click listener that invokes the onItemClick callback when the recipe item is tapped.
            root.setOnClickListener { onItemClick(recipe) }
        }
    }

    /**
     * Returns the total number of recipes in the list.
     *
     * @return The size of the recipes list.
     */
    override fun getItemCount() = recipes.size
}
