package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import pt.ipt.dam.sabordigital.databinding.ItemIngredientBinding

class IngredientAdapter(
    private val ingredients: List<Ingredient>,
    private val onItemClick: (Ingredient) -> Unit
) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemIngredientBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIngredientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.binding.apply {
            ingredientName.text = ingredient.name
            if (!ingredient.imageUrl.isNullOrEmpty()) {
                Glide.with(ingredientImage.context)
                    .load(ingredient.imageUrl)
                    .into(ingredientImage)
            }
            root.setOnClickListener { onItemClick(ingredient) }
        }
    }

    override fun getItemCount() = ingredients.size
}