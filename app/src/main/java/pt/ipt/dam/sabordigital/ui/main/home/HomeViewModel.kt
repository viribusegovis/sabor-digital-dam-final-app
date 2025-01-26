package pt.ipt.dam.sabordigital.ui.main.ui.home

import android.content.Context
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


    fun fetchCategories(context: Context) {
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

    fun fetchPopularIngredients(context: Context) {
        RetrofitInitializer().ingredientService().getTop10Ingredients(10)
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
