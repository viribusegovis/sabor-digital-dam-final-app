package pt.ipt.dam.sabordigital.data.remote.models

data class UserCreate(
    val email: String,
    val password: String,
    val name: String
)