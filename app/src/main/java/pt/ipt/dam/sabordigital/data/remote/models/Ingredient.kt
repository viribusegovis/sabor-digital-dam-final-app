package pt.ipt.dam.sabordigital.data.remote.models

import com.google.gson.annotations.SerializedName

data class Ingredient(
    val ingredient_id: Int? = null, // null if new ingredient
    var name: String,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    val recipeCount: Int = 0
)