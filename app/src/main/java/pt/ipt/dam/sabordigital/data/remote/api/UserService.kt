package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.User
import pt.ipt.dam.sabordigital.data.remote.models.UserCreate
import pt.ipt.dam.sabordigital.data.remote.models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


data class PasswordChange(
    val password: String
)

interface UserService {

    @POST("users/register")
    fun register(@Body request: UserCreate): Call<UserResponse>

    @GET("users/me")
    fun getUser(
        @Header("Authorization") token: String,
    ): Call<User>

    @POST("users/password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body password: PasswordChange
    ): Call<Void>

}