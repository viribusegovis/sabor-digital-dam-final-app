package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface IngredientService {
    @GET("/ingredients/top/{limit}")
    fun getTopIngredients(
        @Path("limit") limit: Int
    ): Call<List<Ingredient>>

    @GET("/ingredients/")
    fun getAllIngredients(): Call<List<Ingredient>>
}
