package pt.ipt.dam.sabordigital.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.databinding.ItemRecipeCreationInstructionBinding

/**
 * Adapter for managing the list of instructions during recipe creation.
 *
 * Each instruction is represented as a String in an editable text field.
 * The adapter maintains error state for each instruction and provides methods
 * to add, remove, validate, and retrieve the current list of instructions.
 */
class RecipeCreationInstructionAdapter :
    RecyclerView.Adapter<RecipeCreationInstructionAdapter.ViewHolder>() {

    // Mutable list holding the instructions entered by the user.
    private val instructions = mutableListOf<String>()

    // Map to keep track of validation errors for each instruction, keyed by position.
    private val errors = mutableMapOf<Int, Boolean>()

    /**
     * ViewHolder class for instruction items.
     *
     * Holds the binding for the instruction item layout, which contains:
     *  - A TextView displaying the step number.
     *  - An EditText for entering the instruction text.
     *  - A Button to remove the instruction.
     */
    inner class ViewHolder(val binding: ItemRecipeCreationInstructionBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Inflates the item layout for an instruction and returns a new ViewHolder.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder instance wrapping the inflated layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeCreationInstructionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Binds the instruction data to the corresponding ViewHolder.
     *
     * Sets the step number, instruction text, and click listeners for removal and text changes.
     *
     * @param holder The ViewHolder for the current item.
     * @param position The zero-based position of the instruction in the list.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instruction = instructions[position]
        holder.binding.apply {
            // Display the step number as position + 1.
            tvStepNumber.text = "${position + 1}."
            // Set the current instruction text in the EditText.
            etInstruction.setText(instruction)

            // Set up the remove button to remove this instruction.
            btnRemove.setOnClickListener {
                removeInstruction(position)
            }

            // Set up text change listener to update the instruction and its validation state.
            etInstruction.doAfterTextChanged { text ->
                instructions[position] = text.toString()
                // Validate: instruction must not be blank.
                validateInstruction(position, !text.isNullOrBlank())
                // Update the error message in the UI.
                updateErrorState(holder, position)
            }
        }
    }

    /**
     * Validates the instruction at the given position.
     *
     * @param position The position of the instruction.
     * @param isValid True if the instruction is valid, false otherwise.
     */
    private fun validateInstruction(position: Int, isValid: Boolean) {
        errors[position] = isValid
    }

    /**
     * Updates the error state for an instruction item.
     *
     * Sets an error message on the TextInputLayout if the instruction is invalid; otherwise, clears the error.
     *
     * @param holder The ViewHolder for the instruction item.
     * @param position The position of the instruction.
     */
    private fun updateErrorState(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            tilInstruction.error = if (errors[position] == false)
                holder.itemView.context.getString(R.string.error_instruction_required)
            else null
        }
    }

    /**
     * Validates all instructions in the list.
     *
     * Iterates over each instruction, updating its validation state; if any instruction is blank or the list is empty,
     * this function returns false. It notifies the adapter to refresh the UI with updated error messages.
     *
     * @return True if all instructions are valid; false otherwise.
     */
    fun validateAllInstructions(): Boolean {
        var isValid = true
        if (instructions.isEmpty()) {
            isValid = false
        }
        instructions.forEachIndexed { index, instruction ->
            val instructionValid = instruction.isNotBlank()
            validateInstruction(index, instructionValid)
            if (!instructionValid) {
                isValid = false
            }
        }
        notifyDataSetChanged() // Refresh UI to show error states.
        return isValid
    }

    /**
     * Returns the total number of instruction items currently held.
     *
     * @return The number of instructions.
     */
    override fun getItemCount() = instructions.size

    /**
     * Adds a new blank instruction to the list.
     *
     * Notifies the adapter that an item has been inserted.
     */
    fun addInstruction() {
        instructions.add("")
        notifyItemInserted(instructions.size - 1)
    }

    /**
     * Removes the instruction at the given position.
     *
     * Updates the error state and notifies the adapter of item removal and range change.
     *
     * @param position The position of the instruction to remove.
     */
    fun removeInstruction(position: Int) {
        instructions.removeAt(position)
        errors.remove(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, instructions.size)
    }

    /**
     * Retrieves the current list of instructions.
     *
     * @return An immutable copy of the instructions list.
     */
    fun getInstructions() = instructions.toList()
}
