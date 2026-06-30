package com.wisatakita.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wisatakita.app.data.UserPrefs
import com.wisatakita.app.databinding.ActivityProfileSetupBinding

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSetupBinding
    private lateinit var userPrefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPrefs = UserPrefs(this)
        val email = intent.getStringExtra("EMAIL") ?: ""

        binding.btnSaveProfile.bounceClick()
        binding.btnSaveProfile.setOnClickListener {
            val age = binding.etAge.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val hometown = binding.etHometown.text.toString().trim()

            val gender = when (binding.rgGender.checkedRadioButtonId) {
                R.id.rbMale -> "Laki-laki"
                R.id.rbFemale -> "Perempuan"
                else -> ""
            }

            if (age.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Usia dan gender wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userPrefs.saveProfile(email, age, gender, phone, hometown)
            userPrefs.setCurrentUser(email)

            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}
