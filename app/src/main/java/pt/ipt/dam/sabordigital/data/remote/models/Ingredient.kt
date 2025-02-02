package pt.ipt.dam.sabordigital.data.remote.models

import com.google.gson.annotations.SerializedName

// Data class representing a recipe ingredient with:
// - Optional ID (null for new ingredients)
// - Required name
// - Optional image URL
// - Recipe usage count
// Uses Gson annotation for JSON field mapping

data class Ingredient(
    val ingredient_id: Int? = null,     // Unique identifier, nullable for new ingredients
    var name: String,                   // Required ingredient name, mutable
    @SerializedName("image_url")        // Maps JSON "image_url" to imageUrl property
    val imageUrl: String? = null,       // Optional URL for ingredient image
    val recipeCount: Int = 0            // Number of recipes using this ingredient
)
