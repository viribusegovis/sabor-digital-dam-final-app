package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.databinding.ActivityDetailsRecipeBinding

class RecipeAdapter(
    private val recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ActivityDetailsRecipeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ActivityDetailsRecipeBinding.inflate(
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
                "FACIL" -> "Fácil"
                "MEDIO" -> "Médio"
                "DIFICIL" -> "Difícil"
                else -> "Desconhecido"
            }
            timeChip.text = "${recipe.preparationTime} min"
            servingsChip.text = "${recipe.servings} pessoas"
            root.setOnClickListener { onItemClick(recipe) }
        }
    }

    override fun getItemCount() = recipes.size
}
