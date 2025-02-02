package pt.ipt.dam.sabordigital.data.remote.models

import com.google.gson.annotations.SerializedName

data class Category(
    val category_id: Int,
    val name: String,
    val description: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    val recipeCount: Int = 0
)