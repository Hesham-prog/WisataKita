package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wisatakita.app.data.UserPrefs
import com.wisatakita.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPrefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userPrefs = UserPrefs(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!userPrefs.isEmailTaken(email)) {
                Toast.makeText(this, "Akun tidak ditemukan. Silakan daftar terlebih dahulu.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!userPrefs.validateLogin(email, password)) {
                Toast.makeText(this, "Password salah", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userPrefs.setCurrentUser(email)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
