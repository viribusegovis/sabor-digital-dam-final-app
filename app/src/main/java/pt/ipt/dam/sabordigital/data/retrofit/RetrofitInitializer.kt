package pt.ipt.dam.sabordigital.data.retrofit

import com.google.gson.GsonBuilder
import pt.ipt.dam.sabordigital.data.remote.api.AuthService
import pt.ipt.dam.sabordigital.data.remote.api.CategoryService
import pt.ipt.dam.sabordigital.data.remote.api.IngredientService
import pt.ipt.dam.sabordigital.data.remote.api.RecipeService
import pt.ipt.dam.sabordigital.data.remote.api.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Class responsible for initializing and configuring Retrofit instance
// Provides centralized API service creation with consistent configuration
// Sets up base URL, JSON parsing, and creates service interfaces

class RetrofitInitializer {
    private val host =
        "http://10.0.2.2:8080"      // Base URL for API

    private val gson = GsonBuilder()
        .setLenient()               // Configures Gson to be lenient in parsing
        .create()                   // Creates Gson instance for JSON handling

    // Main Retrofit instance configuration
    private val retrofit = Retrofit.Builder()
        .baseUrl(host)              // Sets the API base URL
        .addConverterFactory(GsonConverterFactory.create(gson))  // Adds Gson converter for JSON
        .build()                    // Creates the Retrofit instance

    // Service creation methods - each returns a configured API service interface
    fun authService() = retrofit.create(AuthService::class.java)         // Authentication service
    fun recipeService() = retrofit.create(RecipeService::class.java)     // Recipe operations
    fun categoryService() = retrofit.create(CategoryService::class.java)  // Category operations
    fun userService() = retrofit.create(UserService::class.java)         // User management
    fun ingredientService() =
        retrofit.create(IngredientService::class.java)  // Ingredient operations
}
