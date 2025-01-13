package pt.ipt.dam.sabordigital.ui.main

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

class MainViewModel : ViewModel() {
    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> = _authState

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _ingredients = MutableLiveData<List<Ingredient>>()
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun checkAuthState(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("jwt_token", null)
        _authState.value = token != null
    }

    fun logout(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("jwt_token").apply()
        _authState.value = false
    }

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
        RetrofitInitializer().ingredientService().getTop10Ingredients()
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
