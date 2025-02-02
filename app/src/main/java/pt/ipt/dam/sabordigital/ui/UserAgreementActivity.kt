package pt.ipt.dam.sabordigital.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.ipt.dam.sabordigital.databinding.ActivityUserAgreementBinding
import pt.ipt.dam.sabordigital.ui.auth.login.LoginActivity
import pt.ipt.dam.sabordigital.utils.PreferencesHelper


/**
 * Activity that displays the User Agreement.
 *
 * The user must either accept or decline the terms. If declined, the application will close.
 * If accepted, the agreement is saved in SharedPreferences and the user is redirected to the login screen.
 */
class UserAgreementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAgreementBinding

    /**
     * Called when the activity is first created.
     *
     * Inflates the layout, and sets up click listeners for the "Accept" and "Decline" buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAgreementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure the "Decline" button with a confirmation dialog.
        binding.btnDecline.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Tem a certeza que pretende recusar os Termos? A aplicação será encerrada.")
                .setPositiveButton("Sim") { _, _ ->
                    finish() // Closes the activity (and hence the application)
                }
                .setNegativeButton("Não", null)
                .show()
        }

        // Configure the "Accept" button to save agreement status and redirect to LoginActivity.
        binding.btnAccept.setOnClickListener {
            // Save acceptance flag in SharedPreferences.
            PreferencesHelper.setUserAgreementAccepted(this, true)

            // Redirect to LoginActivity.
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}