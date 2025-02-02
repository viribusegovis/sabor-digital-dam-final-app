package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IngredientService {
    // Service interface for handling all ingredient-related API operations
    // This interface provides three main functionalities:
    // 1. Fetching top N most used/popular ingredients
    // 2. Retrieving the complete ingredient database
    // 3. Searching ingredients based on user input
    // All operations are asynchronous using Retrofit's Call wrapper

    @GET("/ingredients/top/{limit}")  // GET endpoint for retrieving top N ingredients
    fun getTopIngredients(
        @Path("limit") limit: Int     // Dynamic path parameter specifying how many top ingredients to fetch
    ): Call<List<Ingredient>>         // Returns a list of most commonly used ingredients
    

    @GET("/ingredients/search")       // GET endpoint for ingredient search functionality
    fun searchIngredients(
        @Query("query") query: String // Query parameter for search term
    ): Call<List<Ingredient>>         // Returns list of ingredients matching the search query


}
