package pt.ipt.dam.sabordigital.ui.main.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.ipt.dam.sabordigital.data.remote.models.Category
import pt.ipt.dam.sabordigital.data.remote.models.Ingredient
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _ingredients = MutableLiveData<List<Ingredient>>()
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    /**
     * Fetches the list of recipe categories from the API.
     *
     * This method initiates a network call to retrieve categories using Retrofit.
     * Upon receiving a successful response, it updates the [_categories] LiveData.
     * The [_loading] LiveData is updated to false when the API call completes,
     * regardless of success or failure.
     */
    fun fetchCategories() {
        _loading.value = true
        RetrofitInitializer().categoryService().getCategories()
            .enqueue(object : Callback<List<Category>> {
                override fun onResponse(
                    call: Call<List<Category>>,
                    response: Response<List<Category>>
                ) {
                    if (response.isSuccessful) {
                        _categories.value = response.body()
                    }
                    _loading.value = false
                }

                override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                    _loading.value = false
                }
            })
    }

    /**
     * Fetches the list of popular ingredients from the API.
     *
     * This method retrieves the top 10 ingredients by initiating a network call via Retrofit.
     * On success, it updates the [_ingredients] LiveData with the retrieved list.
     * Regardless of the outcome, the [_loading] state is set to false after the API call.
     */
    fun fetchPopularIngredients() {
        _loading.value = true
        RetrofitInitializer().ingredientService().getTopIngredients(10)
            .enqueue(object : Callback<List<Ingredient>> {
                override fun onResponse(
                    call: Call<List<Ingredient>>,
                    response: Response<List<Ingredient>>
                ) {
                    if (response.isSuccessful) {
                        _ingredients.value = response.body()
                    }
                    _loading.value = false
                }

                override fun onFailure(call: Call<List<Ingredient>>, t: Throwable) {
                    _loading.value = false
                }
            })
    }
}
