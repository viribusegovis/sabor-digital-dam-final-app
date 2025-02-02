package pt.ipt.dam.sabordigital.ui.main.profile

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

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ProfileViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProfileBinding.bind(view)

        setupObservers()

        // Load user data when the fragment is created.
        viewModel.loadUser(requireContext())

        // Change Password Button Click Listener.
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Logout Button Click Listener.
        binding.btnLogout.setOnClickListener {
            viewModel.logout(requireContext())
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun showChangePasswordDialog() {
        // Inflate the change password layout.
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)
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
                    // Call your ViewModel to perform the password change.
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

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.tvProfileName.text = user.name
            binding.tvProfileEmail.text = user.email
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
