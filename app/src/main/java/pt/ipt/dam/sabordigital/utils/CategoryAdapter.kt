package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Category
import pt.ipt.dam.sabordigital.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.apply {
            categoryName.text = category.name
            if (!category.imageUrl.isNullOrEmpty()) {
                if (category.imageUrl.startsWith("data:") || category.imageUrl.length > 500) {
                    // If Base64-encoded, use our helper function.
                    ImageHelper.setImageFromBase64(categoryImage, category.imageUrl)
                } else {
                    // Otherwise, use Glide.
                    Glide.with(root.context)
                        .load(category.imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.placehold)
                        .error(R.drawable.placehold)
                        .into(categoryImage)
                }
            } else {
                // Optionally, set a placeholder if no image URL is provided.
                categoryImage.setImageResource(R.drawable.placehold)
            }
            root.setOnClickListener { onItemClick(category) }
        }
    }

    override fun getItemCount() = categories.size
}