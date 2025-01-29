package pt.ipt.dam.sabordigital.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sabordigital.databinding.ItemRecipeCreationInstructionBinding

class RecipeCreationInstructionAdapter :
    RecyclerView.Adapter<RecipeCreationInstructionAdapter.ViewHolder>() {
    private val instructions = mutableListOf<String>()

    inner class ViewHolder(val binding: ItemRecipeCreationInstructionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeCreationInstructionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
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
            }
        }
    }

    override fun getItemCount() = instructions.size

    fun addInstruction() {
        instructions.add("")
        notifyItemInserted(instructions.size - 1)
    }

    fun removeInstruction(position: Int) {
        instructions.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, instructions.size)
    }

    fun getInstructions() = instructions.toList()
}
