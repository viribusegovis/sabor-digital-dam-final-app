package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.Category
import retrofit2.Call
import retrofit2.http.GET

interface CategoryService {
    // Defines a service interface for handling category-related API operations
    // This interface is used with Retrofit to:
    // - Fetch all available recipe categories from the backend
    // - Return them as a list that can be used for category filtering and selection
    // - Handle the async nature of the network request through Retrofit's Call wrapper

    @GET("/categories/")  // Specifies a GET request to fetch categories from "/categories/" endpoint
    fun getCategories(): Call<List<Category>>  // Returns a list of Category objects wrapped in a Retrofit Call

    // This interface is used with Retrofit to:
    // - Fetch all available recipe categories from the backend
    // - Return them as a list that can be used for category filtering and selection
    // - Handle the async nature of the network request through Retrofit's Call wrapper
}
