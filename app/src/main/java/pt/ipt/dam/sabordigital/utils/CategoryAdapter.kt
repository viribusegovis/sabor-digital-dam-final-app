package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Category
import pt.ipt.dam.sabordigital.databinding.ItemCategoryBinding


/**
 * RecyclerView.Adapter for displaying a list of categories.
 *
 * @property categories The list of Category objects to display.
 * @property onItemClick A lambda function invoked when a category is clicked.
 */
class CategoryAdapter(
    private val categories: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    /**
     * ViewHolder class for CategoryAdapter.
     *
     * Wraps the [ItemCategoryBinding] which contains the UI elements for a category item.
     *
     * @param binding View binding for the category item layout.
     */
    class ViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Inflates the item layout and returns a new ViewHolder instance.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder instance with the inflated binding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Binds data to the UI elements of the ViewHolder.
     *
     * Populates the category name and image. Supports both Base64-encoded images and URL images.
     * Also sets an OnClickListener to invoke the provided lambda when a category is clicked.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.apply {
            // Set the category name in the TextView.
            categoryName.text = category.name

            // Load the category image.
            if (!category.imageUrl.isNullOrEmpty()) {
                // Check if the image URL is Base64 encoded or very long.
                if (category.imageUrl.startsWith("data:") || category.imageUrl.length > 500) {
                    // Use helper function to set Base64 image.
                    ImageHelper.setImageFromBase64(categoryImage, category.imageUrl)
                } else {
                    // Otherwise, load image from URL using Glide.
                    Glide.with(root.context)
                        .load(category.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.placehold)
                        .error(R.drawable.placehold)
                        .into(categoryImage)
                }
            } else {
                // Set a placeholder image if no URL is provided.
                categoryImage.setImageResource(R.drawable.placehold)
            }
            // Set click listener to trigger onItemClick when an item is tapped.
            root.setOnClickListener { onItemClick(category) }
        }
    }

    /**
     * Returns the total number of category items.
     */
    override fun getItemCount() = categories.size
}