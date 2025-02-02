package pt.ipt.dam.sabordigital.data.remote.models

// Data class representing a single recipe instruction step with:
// - Unique instruction identifier
// - Reference to parent recipe
// - Step ordering number
// - Actual instruction text

data class Instruction(
    val instruction_id: Int,      // Unique identifier for the instruction
    val recipe_id: Int,          // ID of the recipe this instruction belongs to
    val step_number: Int,        // Order/sequence number of this instruction step
    val instruction_text: String  // The actual instruction text content
)
