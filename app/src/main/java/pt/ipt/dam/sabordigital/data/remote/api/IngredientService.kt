package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import retrofit2.Call
import retrofit2.http.GET

interface IngredientService {
    @GET("/ingredients/top")
    fun getTop10Ingredients(): Call<List<Ingredient>>
}
