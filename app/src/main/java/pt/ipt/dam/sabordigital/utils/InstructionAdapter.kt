package pt.ipt.dam.sabordigital.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.sabordigital.data.remote.models.Instruction
import pt.ipt.dam.sabordigital.databinding.ItemInstructionBinding

/**
 * Adapter for displaying a list of instructions in a RecyclerView.
 *
 * Each item shows the step number (based on its position) and the instruction text.
 *
 * @param instructions A list of Instruction objects to be displayed.
 */
class InstructionAdapter(
    private val instructions: List<Instruction>
) : RecyclerView.Adapter<InstructionAdapter.ViewHolder>() {

    /**
     * ViewHolder class that holds the binding for an instruction item.
     *
     * @property binding The view binding for the instruction item layout.
     */
    class ViewHolder(val binding: ItemInstructionBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Inflates the instruction item layout and returns a new ViewHolder instance.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view, unused here since we have only one type.
     * @return A new ViewHolder containing the inflated layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInstructionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Binds the instruction data to the corresponding ViewHolder.
     *
     * For each instruction, it sets the step number (position + 1) and the instruction text.
     *
     * @param holder The ViewHolder to update.
     * @param position The position of the instruction in the list.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instruction = instructions[position]
        holder.binding.apply {
            // Display step number; position is zero-based hence add 1.
            stepNumber.text = "${position + 1}."
            // Set the instruction text.
            instructionText.text = instruction.instruction_text
        }
    }

    /**
     * Returns the total count of instruction items.
     *
     * @return The size of the instructions list.
     */
    override fun getItemCount() = instructions.size
}
