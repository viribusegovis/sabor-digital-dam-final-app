package pt.ipt.dam.sabordigital.data.remote.models

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)
