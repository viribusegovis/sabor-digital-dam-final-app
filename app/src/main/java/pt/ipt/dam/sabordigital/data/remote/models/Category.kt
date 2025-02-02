package pt.ipt.dam.sabordigital.data.remote.models

import com.google.gson.annotations.SerializedName

// Data class representing a recipe category with the following attributes:
// - Unique identifier
// - Category name and description
// - Optional image URL
// - Count of recipes in this category
// Uses Gson annotations for JSON serialization/deserialization
data class Category(
    val category_id: Int,          // Unique identifier for the category
    val name: String,              // Category name
    val description: String?,      // Optional category description
    @SerializedName("image_url")   // Maps JSON field "image_url" to imageUrl property
    val imageUrl: String?,         // Optional URL for category image
    val recipeCount: Int = 0       // Number of recipes in category, defaults to 0
)
