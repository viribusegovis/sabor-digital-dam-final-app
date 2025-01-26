package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.databinding.ItemRecipeBinding

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.binding.apply {
            titleText.text = recipe.title
            descriptionText.text = recipe.description

            difficultyChip.text = when (recipe.difficulty) {
                "FACIL" -> root.context.getString(R.string.difficulty_easy)
                "MEDIO" -> root.context.getString(R.string.difficulty_medium)
                "DIFICIL" -> root.context.getString(R.string.difficulty_hard)
                else -> root.context.getString(R.string.difficulty_unknown)
            }

            timeChip.text = root.context.getString(
                R.string.recipe_prep_time_format,
                recipe.preparation_time
            )

            servingsChip.text = root.context.getString(
                R.string.recipe_servings_format,
                recipe.servings
            )

            if (!recipe.imageUrl.isNullOrEmpty()) {
                Glide.with(recipeImage.context)
                    .load(recipe.imageUrl)
                    .centerCrop()
                    .into(recipeImage)
            }

            root.setOnClickListener { onItemClick(recipe) }
        }
    }

    override fun getItemCount() = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}
