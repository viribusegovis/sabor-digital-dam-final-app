package pt.ipt.dam.sabordigital.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import pt.ipt.dam.sabordigital.R

class RecipeDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = RecipeDetailsFragment()
    }

    private val viewModel: RecipeDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recipe_details, container, false)
    }
}