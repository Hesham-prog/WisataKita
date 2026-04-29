package com.wisatakita.app.data

import android.content.Context

class UserPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("wk_users", Context.MODE_PRIVATE)
    private val session = context.getSharedPreferences("wk_session", Context.MODE_PRIVATE)

    fun register(name: String, email: String, password: String) {
        prefs.edit().apply {
            putBoolean("exists_$email", true)
            putString("name_$email", name)
            putString("pass_$email", password)
            apply()
        }
    }

    fun saveProfile(email: String, age: String, gender: String, phone: String, hometown: String) {
        prefs.edit().apply {
            putString("age_$email", age)
            putString("gender_$email", gender)
            putString("phone_$email", phone)
            putString("hometown_$email", hometown)
            apply()
        }
    }

    fun isEmailTaken(email: String) = prefs.getBoolean("exists_$email", false)

    fun validateLogin(email: String, password: String): Boolean {
        if (!isEmailTaken(email)) return false
        return prefs.getString("pass_$email", "") == password
    }

    fun getName(email: String) = prefs.getString("name_$email", "") ?: ""

    fun setCurrentUser(email: String) {
        session.edit().putString("current_email", email).apply()
    }

    fun getCurrentEmail() = session.getString("current_email", "") ?: ""

    fun getCurrentName(): String {
        val email = getCurrentEmail()
        return if (email.isNotEmpty()) getName(email) else "Penjelajah"
    }

    fun logout() {
        session.edit().remove("current_email").apply()
    }
}
