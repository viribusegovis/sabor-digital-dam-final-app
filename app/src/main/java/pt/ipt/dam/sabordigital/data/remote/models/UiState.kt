package pt.ipt.dam.sabordigital.data.remote.models

sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}