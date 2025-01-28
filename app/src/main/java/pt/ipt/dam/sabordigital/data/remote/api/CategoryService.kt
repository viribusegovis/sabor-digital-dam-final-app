package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.Category
import retrofit2.Call
import retrofit2.http.GET

interface CategoryService {
    @GET("/categories/")
    fun getCategories(): Call<List<Category>>
}
