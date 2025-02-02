package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient

/**
 * Adapter for displaying search results of ingredients.
 *
 * This RecyclerView adapter shows a list of ingredients for search functionality.
 * When an ingredient is clicked, it invokes a callback provided by the caller.
 *
 * @param onItemClick A lambda function that is triggered when an ingredient is clicked.
 */
class IngredientSearchAdapter(private val onItemClick: (Ingredient) -> Unit) :
    RecyclerView.Adapter<IngredientSearchAdapter.ViewHolder>() {

    // Mutable list holding the current ingredients in the adapter.
    private val ingredients = mutableListOf<Ingredient>()

    /**
     * ViewHolder for an ingredient search item.
     *
     * It holds a reference to the root view and the TextView displaying the ingredient name.
     *
     * @param rootView The root view of the item layout.
     */
    inner class ViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView) {
        // TextView for displaying the ingredient's name.
        val tvName: TextView = rootView.findViewById(R.id.tvIngredientName)
    }

    /**
     * Inflates the ingredient search item layout and returns a ViewHolder instance.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The type of view for the new item.
     * @return A new ViewHolder instance for this adapter.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_search, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds the ingredient data to the ViewHolder.
     *
     * Sets the ingredient name in the TextView and attaches a click listener that calls the
     * provided onItemClick lambda with the clicked ingredient.
     *
     * @param holder The ViewHolder for the current item.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.tvName.text = ingredient.name
        holder.itemView.setOnClickListener {
            onItemClick(ingredient)
        }
    }

    /**
     * Returns the total number of ingredients in the adapter.
     *
     * @return The number of ingredients.
     */
    override fun getItemCount() = ingredients.size

    /**
     * Updates the adapter's dataset with new ingredients.
     *
     * Clears the current ingredient list, adds all items from the new list, and notifies the adapter.
     *
     * @param newIngredients The new list of ingredients.
     */
    fun updateIngredients(newIngredients: List<Ingredient>) {
        ingredients.clear()
        ingredients.addAll(newIngredients)
        notifyDataSetChanged()
    }
}
