package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.databinding.ItemRecipeCreationInstructionBinding

class RecipeCreationInstructionAdapter :
    RecyclerView.Adapter<RecipeCreationInstructionAdapter.ViewHolder>() {
    private val instructions = mutableListOf<String>()
    private val errors = mutableMapOf<Int, Boolean>()

    inner class ViewHolder(val binding: ItemRecipeCreationInstructionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeCreationInstructionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instruction = instructions[position]
        holder.binding.apply {
            tvStepNumber.text = "${position + 1}."
            etInstruction.setText(instruction)

            btnRemove.setOnClickListener {
                removeInstruction(position)
            }

            etInstruction.doAfterTextChanged { text ->
                instructions[position] = text.toString()
                validateInstruction(position, !text.isNullOrBlank())
                updateErrorState(holder, position)
            }
        }
    }

    private fun validateInstruction(position: Int, isValid: Boolean) {
        errors[position] = isValid
    }

    private fun updateErrorState(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            tilInstruction.error = if (errors[position] == false)
                holder.itemView.context.getString(R.string.error_instruction_required)
            else null
        }
    }

    fun validateAllInstructions(): Boolean {
        var isValid = true
        instructions.forEachIndexed { index, instruction ->
            val instructionValid = instruction.isNotBlank()
            validateInstruction(index, instructionValid)
            if (!instructionValid) {
                isValid = false
            }
        }
        notifyDataSetChanged() // Update all error states
        return isValid
    }

    override fun getItemCount() = instructions.size

    fun addInstruction() {
        instructions.add("")
        notifyItemInserted(instructions.size - 1)
    }

    fun removeInstruction(position: Int) {
        instructions.removeAt(position)
        errors.remove(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, instructions.size)
    }

    fun getInstructions() = instructions.toList()
}
