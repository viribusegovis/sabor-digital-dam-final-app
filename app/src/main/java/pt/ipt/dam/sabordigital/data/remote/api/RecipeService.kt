package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.Instruction
import pt.ipt.dam.sabordigital.data.remote.models.Recipe
import pt.ipt.dam.sabordigital.data.remote.models.RecipeCreate
import pt.ipt.dam.sabordigital.data.remote.models.RecipeIngredient
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeService {
    @GET("/recipes/")
    fun getAllRecipes(): Call<List<Recipe>>

    @GET("/recipes/{id}")
    fun getRecipeDetails(
        @Path("id") id: Int
    ): Call<Recipe>

    @GET("/recipes/author/")
    fun getMyRecipes(
        @Header("Authorization") token: String
    ): Call<List<Recipe>>

    @GET("/recipes/category/{categoryId}")
    fun getRecipesByCategory(
        @Path("categoryId") categoryId: Int
    ): Call<List<Recipe>>

    @GET("/recipes/ingredient/{ingredientId}")
    fun getRecipesByIngredient(
        @Path("ingredientId") ingredientId: Int
    ): Call<List<Recipe>>

    @GET("/recipes/search")
    fun searchRecipes(
        @Query("query") query: String
    ): Call<List<Recipe>>

    @GET("/recipes/{recipeId}/ingredients")
    fun getRecipeIngredients(
        @Path("recipeId") recipeId: Int
    ): Call<List<RecipeIngredient>>

    @GET("/recipes/{recipeId}/instructions")
    fun getRecipeInstructions(
        @Path("recipeId") recipeId: Int
    ): Call<List<Instruction>>

    @POST("/recipes/")
    fun createRecipe(
        @Header("Authorization") token: String,
        @Body recipe: RecipeCreate
    ): Call<RecipeCreate>

}
