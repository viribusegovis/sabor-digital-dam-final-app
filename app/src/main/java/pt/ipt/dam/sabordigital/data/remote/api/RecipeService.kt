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
    // Service interface for handling all recipe-related API operations
    // This comprehensive interface handles:
    // 1. Basic recipe CRUD operations
    // 2. Recipe filtering by category and ingredient
    // 3. Recipe search functionality
    // 4. Fetching recipe components (ingredients and instructions)
    // 5. User-specific recipe operations

    // All operations are asynchronous using Retrofit's Call wrapper
    @GET("/recipes/")                 // GET endpoint for fetching all recipes
    fun getAllRecipes(): Call<List<Recipe>>  // Returns list of all available recipes

    @GET("/recipes/{id}")            // GET endpoint for specific recipe details
    fun getRecipeDetails(
        @Path("id") id: Int          // Path parameter for recipe ID
    ): Call<Recipe>                  // Returns detailed information for a single recipe

    @GET("/recipes/author/")         // GET endpoint for user's own recipes
    fun getMyRecipes(
        @Header("Authorization") token: String  // Auth token required for user identification
    ): Call<List<Recipe>>           // Returns list of recipes created by authenticated user

    @GET("/recipes/category/{categoryId}")  // GET endpoint for category-filtered recipes
    fun getRecipesByCategory(
        @Path("categoryId") categoryId: Int    // Path parameter for category filtering
    ): Call<List<Recipe>>                     // Returns recipes in specified category

    @GET("/recipes/ingredient/{ingredientId}")  // GET endpoint for ingredient-filtered recipes
    fun getRecipesByIngredient(
        @Path("ingredientId") ingredientId: Int  // Path parameter for ingredient filtering
    ): Call<List<Recipe>>                       // Returns recipes containing specified ingredient

    @GET("/recipes/search")          // GET endpoint for recipe search
    fun searchRecipes(
        @Query("query") query: String  // Query parameter for search term
    ): Call<List<Recipe>>             // Returns recipes matching search criteria

    @GET("/recipes/{recipeId}/ingredients")  // GET endpoint for recipe ingredients
    fun getRecipeIngredients(
        @Path("recipeId") recipeId: Int      // Path parameter for recipe ID
    ): Call<List<RecipeIngredient>>          // Returns list of ingredients for specific recipe

    @GET("/recipes/{recipeId}/instructions")  // GET endpoint for recipe instructions
    fun getRecipeInstructions(
        @Path("recipeId") recipeId: Int       // Path parameter for recipe ID
    ): Call<List<Instruction>>                // Returns list of instructions for specific recipe

    @POST("/recipes/")               // POST endpoint for creating new recipe
    fun createRecipe(
        @Header("Authorization") token: String,  // Auth token required for creation
        @Body recipe: RecipeCreate             // Recipe data in request body
    ): Call<RecipeCreate>                      // Returns created recipe data

}
