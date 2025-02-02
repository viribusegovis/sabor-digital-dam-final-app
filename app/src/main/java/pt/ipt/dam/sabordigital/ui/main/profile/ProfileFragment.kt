package pt.ipt.dam.sabordigital.ui.main.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.databinding.FragmentProfileBinding
import pt.ipt.dam.sabordigital.ui.auth.login.LoginActivity

/**
 * Fragment displaying user profile details.
 * Provides functionality for password change, account deletion, and logout.
 */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ProfileViewModel>()

    /**
     * Called immediately after onCreateView().
     *
     * Binds views, sets up observers, loads user data, and handles click events.
     *
     * @param view The view returned by onCreateView().
     * @param savedInstanceState If the fragment is being re-created from a previous state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind the view to the binding object.
        _binding = FragmentProfileBinding.bind(view)

        // Set up LiveData observers.
        setupObservers()

        // Load user data.
        viewModel.loadUser(requireContext())

        // Set click listener for Change Password button.
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Set click listener for Logout button.
        binding.btnLogout.setOnClickListener {
            viewModel.logout(requireContext())
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        // Set up Delete Account button with confirmation dialog.
        setupDeleteAccountButton()
    }

    /**
     * Displays a dialog for changing the user password.
     *
     * The dialog collects the new password and its confirmation.
     * If the new password is valid and the fields match, it calls the ViewModel's changePassword function.
     * Appropriate feedback is displayed via toast messages.
     */
    private fun showChangePasswordDialog() {
        // Inflate the change password dialog layout.
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)
        // Retrieve references to the password input fields.
        val etNewPassword = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        // Build the dialog.
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.change_password_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.submit)) { dialog, _ ->
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                    // Call ViewModel to update the password.
                    viewModel.changePassword(requireContext(), newPassword) { success ->
                        if (success) {
                            Toast.makeText(
                                context,
                                getString(R.string.password_changed),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.password_change_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.passwords_do_not_match),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Sets up a click listener for the Delete Account button.
     *
     * Displays a confirmation dialog before invoking account deletion via the ViewModel.
     */
    private fun setupDeleteAccountButton() {
        binding.btnDeleteAccount.setOnClickListener {
            // Display a confirmation alert dialog.
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_account))
                .setMessage(getString(R.string.confirm_delete_account))
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    viewModel.deleteAccount(requireContext())
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    /**
     * Sets up observers to listen for changes in user data.
     *
     * Once user data is available, updates the UI components accordingly.
     */
    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.tvProfileName.text = user.name
            binding.tvProfileEmail.text = user.email
        }
    }

    /**
     * Called when the fragment's view is about to be destroyed.
     *
     * Clears the binding reference to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}