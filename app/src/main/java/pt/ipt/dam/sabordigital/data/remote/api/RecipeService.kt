package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface RecipeService {
    @GET("/recipes")
    fun getRecipes(@Header("Authorization") token: String): Call<List<Recipe>>

    @GET("/recipes/{id}")
    fun getRecipeDetails(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Recipe>
}
