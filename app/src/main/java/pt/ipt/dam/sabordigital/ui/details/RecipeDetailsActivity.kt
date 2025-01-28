package pt.ipt.dam.sabordigital.ui.details

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.databinding.ActivityDetailsRecipeBinding

class RecipeDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsRecipeBinding
    private val viewModel: RecipeDetailsViewModel by viewModels()

    private fun updateDifficultyUI(difficulty: Int) {
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3
        )

        stars.forEachIndexed { index, star ->
            star.setImageResource(
                if (viewModel.shouldShowFilledStar(index, difficulty))
                    R.drawable.ic_star_filled
                else
                    R.drawable.ic_star
            )
        }

        binding.difficultyChip.text = viewModel.getDifficultyText(difficulty)
    }
}
