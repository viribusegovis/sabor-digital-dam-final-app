package pt.ipt.dam.sabordigital.data.remote.models

import com.google.gson.annotations.SerializedName

// Data class representing a complete recipe with:
// - Basic recipe information (id, title, description)
// - Preparation details (time, servings, difficulty)
// - Visual content (image)
// - Associated categories and ingredients
// Uses Gson annotation for JSON field mapping

data class Recipe(
    val id: Int,                   // Unique recipe identifier
    val title: String,             // Recipe title/name
    val description: String?,      // Optional recipe description
    val preparation_time: Int,     // Time needed to prepare in minutes
    val servings: Int,            // Number of servings the recipe yields
    val difficulty: String,        // Difficulty level of the recipe
    @SerializedName("image_url")   // Maps JSON "image_url" to imageUrl property
    val imageUrl: String?,         // Optional URL for recipe image
    val categories: List<Category>,     // List of categories this recipe belongs to
    val ingredients: List<Ingredient>   // List of ingredients used in the recipe
)
