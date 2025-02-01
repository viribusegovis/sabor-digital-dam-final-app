package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient

class IngredientSearchAdapter(private val onItemClick: (Ingredient) -> Unit) :
    RecyclerView.Adapter<IngredientSearchAdapter.ViewHolder>() {

    private val ingredients = mutableListOf<Ingredient>()

    inner class ViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView) {
        val tvName: TextView = rootView.findViewById(R.id.tvIngredientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.tvName.text = ingredient.name
        holder.itemView.setOnClickListener {
            onItemClick(ingredient)
        }
    }

    override fun getItemCount() = ingredients.size

    fun updateIngredients(newIngredients: List<Ingredient>) {
        ingredients.clear()
        ingredients.addAll(newIngredients)
        notifyDataSetChanged()
    }
}