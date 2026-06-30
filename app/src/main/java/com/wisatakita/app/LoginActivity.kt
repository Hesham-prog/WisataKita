package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.wisatakita.app.data.UserPrefs
import com.wisatakita.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPrefs: UserPrefs
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageUtil.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userPrefs = UserPrefs(this)
        credentialManager = CredentialManager.create(this)

        setupLanguageToggle()
        setupMicroInteractions()
        setupAuthActions()
        binding.cardLoginForm.fadeInSlide()
    }

    private fun setupLanguageToggle() {
        val current = LanguageUtil.currentLanguage(this)
        binding.groupLanguage.check(
            if (current == LanguageUtil.ENGLISH) R.id.btnLangEn else R.id.btnLangId
        )
        binding.groupLanguage.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val selected = if (checkedId == R.id.btnLangEn) LanguageUtil.ENGLISH else LanguageUtil.INDONESIAN
            if (selected != LanguageUtil.currentLanguage(this)) {
                LanguageUtil.setLanguage(this, selected)
                Toast.makeText(this, R.string.lang_change_restart, Toast.LENGTH_SHORT).show()
                recreate()
            }
        }
    }

    private fun setupMicroInteractions() {
        binding.btnLangId.bounceClick()
        binding.btnLangEn.bounceClick()
        binding.btnLogin.bounceClick()
        binding.btnGoogleLogin.bounceClick()
        binding.tvRegister.bounceClick()
    }

    private fun setupAuthActions() {
        binding.btnLogin.setOnClickListener {
            HapticUtil.click(it)
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.login_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!userPrefs.isEmailTaken(email)) {
                Toast.makeText(this, R.string.login_account_missing, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!userPrefs.validateLogin(email, password)) {
                Toast.makeText(this, R.string.login_wrong_password, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userPrefs.setCurrentUser(email)
            openMain()
        }

        binding.btnGoogleLogin.setOnClickListener {
            HapticUtil.click(it)
            startGoogleSignIn()
        }

        binding.tvRegister.setOnClickListener {
            HapticUtil.click(it)
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun startGoogleSignIn() {
        val clientId = getString(R.string.google_client_id)
        if (clientId.isBlank() || clientId == "YOUR_GOOGLE_WEB_CLIENT_ID_HERE") {
            Toast.makeText(this, R.string.login_google_config_missing, Toast.LENGTH_LONG).show()
            return
        }

        binding.btnGoogleLogin.isEnabled = false
        lifecycleScope.launch {
            runCatching {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(this@LoginActivity, request)
                val credential = result.credential
                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val email = googleCredential.id
                    val name = googleCredential.displayName ?: email.substringBefore("@")
                    userPrefs.signInWithGoogle(name, email)
                    openMain()
                } else {
                    Toast.makeText(this@LoginActivity, R.string.login_google_failed, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { error ->
                val message = when (error) {
                    is GetCredentialException,
                    is GoogleIdTokenParsingException -> R.string.login_google_failed
                    else -> R.string.error_generic
                }
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
            }
            binding.btnGoogleLogin.isEnabled = true
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
