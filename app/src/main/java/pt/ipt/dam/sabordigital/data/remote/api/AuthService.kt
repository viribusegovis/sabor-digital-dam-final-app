package pt.ipt.dam.sabordigital.data.remote.api

import pt.ipt.dam.sabordigital.data.remote.models.TokenRequest
import pt.ipt.dam.sabordigital.data.remote.models.TokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    // Defines an authentication service interface for API communication
    // This interface is used with Retrofit to handle authentication
    // The login function sends user credentials and receives an auth token

    @POST("/token")  // Specifies this is a POST request to the "/token" endpoint
    fun login(
        @Body credentials: TokenRequest  // Takes a TokenRequest object as the request body
    ): Call<TokenResponse>  // Returns a Retrofit Call object wrapping TokenResponse

}