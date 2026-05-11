package hr.team16.booksy.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("booksy_prefs", Context.MODE_PRIVATE)

    fun saveSession(token: String, role: String, email: String, userId: Long) {
        prefs.edit()
            .putString("token", token)
            .putString("role", role)
            .putString("email", email)
            .putLong("userId", userId)
            .apply()
    }

    fun getToken(): String? = prefs.getString("token", null)
    fun getRole(): String? = prefs.getString("role", null)
    fun getEmail(): String? = prefs.getString("email", null)
    fun getUserId(): Long = prefs.getLong("userId", -1)

    fun isLoggedIn(): Boolean = getToken() != null

    fun getBearerToken(): String = "Bearer ${getToken()}"

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}