package org.golfcat.team.project

import android.content.Context
import org.golfcat.team.project.models.User
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

actual object SessionProvider {
    private const val PREFS_NAME = "golf_team_prefs"
    private const val KEY_USER = "current_user"

    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveUser(user: User?) {
        val context = ActivityProvider.currentActivity ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (user == null) {
            prefs.edit().remove(KEY_USER).apply()
        } else {
            val userJson = json.encodeToString(user)
            prefs.edit().putString(KEY_USER, userJson).apply()
        }
    }

    actual fun loadUser(): User? {
        val context = ActivityProvider.currentActivity ?: return null
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            json.decodeFromString<User>(userJson)
        } catch (e: Exception) {
            null
        }
    }

    actual fun clear() {
        saveUser(null)
    }
}
