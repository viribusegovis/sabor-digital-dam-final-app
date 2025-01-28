package pt.ipt.dam.sabordigital.data.remote.models

data class Instruction(
    val instruction_id: Int,
    val recipe_id: Int,
    val step_number: Int,
    val instruction_text: String
)
