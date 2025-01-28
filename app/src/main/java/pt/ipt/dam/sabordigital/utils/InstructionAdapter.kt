package pt.ipt.dam.sabordigital.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sabordigital.data.remote.models.Instruction
import pt.ipt.dam.sabordigital.databinding.ItemInstructionBinding

class InstructionAdapter(
    private val instructions: List<Instruction>
) : RecyclerView.Adapter<InstructionAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInstructionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInstructionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instruction = instructions[position]
        holder.binding.apply {
            stepNumber.text = "${position + 1}."
            instructionText.text = instruction.instruction_text
        }
    }

    override fun getItemCount() = instructions.size
}
