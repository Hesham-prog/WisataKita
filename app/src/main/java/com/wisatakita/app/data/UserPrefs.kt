package com.wisatakita.app.data

import android.content.Context
import com.wisatakita.app.R
import com.wisatakita.app.data.db.AppDatabase
import com.wisatakita.app.data.db.UserEntity

class UserPrefs(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val session = context.getSharedPreferences("wk_session", Context.MODE_PRIVATE)

    fun register(name: String, email: String, password: String) {
        userDao.insert(UserEntity(email = email, name = name, password = password))
    }

    fun signInWithGoogle(name: String, email: String) {
        if (!isEmailTaken(email)) {
            userDao.insert(
                UserEntity(
                    email = email,
                    name = name.ifBlank { context.getString(R.string.home_default_user) },
                    password = "google_sign_in"
                )
            )
        }
        setCurrentUser(email)
    }

    fun saveProfile(email: String, age: String, gender: String, phone: String, hometown: String) {
        val existing = userDao.findByEmail(email) ?: return
        userDao.update(existing.copy(age = age, gender = gender, phone = phone, hometown = hometown))
    }

    fun isEmailTaken(email: String): Boolean = userDao.countByEmail(email) > 0

    fun validateLogin(email: String, password: String): Boolean {
        val user = userDao.findByEmail(email) ?: return false
        return user.password == password
    }

    fun getName(email: String): String = userDao.findByEmail(email)?.name ?: ""

    fun setCurrentUser(email: String) {
        session.edit().putString("current_email", email).apply()
    }

    fun getCurrentEmail(): String = session.getString("current_email", "") ?: ""

    fun getCurrentName(): String {
        val email = getCurrentEmail()
        return if (email.isNotEmpty()) getName(email) else context.getString(R.string.home_default_user)
    }

    fun logout() {
        session.edit().remove("current_email").apply()
    }
}
