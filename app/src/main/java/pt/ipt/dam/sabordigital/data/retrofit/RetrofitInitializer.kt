package pt.ipt.dam.sabordigital.data.retrofit

import com.google.gson.GsonBuilder
import pt.ipt.dam.sabordigital.data.remote.api.AuthService
import pt.ipt.dam.sabordigital.data.remote.api.CategoryService
import pt.ipt.dam.sabordigital.data.remote.api.IngredientService
import pt.ipt.dam.sabordigital.data.remote.api.RecipeService
import pt.ipt.dam.sabordigital.data.remote.api.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInitializer {
    private val host =
        "http://10.0.2.2:8080"
    private val gson = GsonBuilder().setLenient().create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(host)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    fun authService() = retrofit.create(AuthService::class.java)
    fun recipeService() = retrofit.create(RecipeService::class.java)
    fun categoryService() = retrofit.create(CategoryService::class.java)
    fun userService() = retrofit.create(UserService::class.java)
    fun ingredientService() = retrofit.create(IngredientService::class.java)
}
