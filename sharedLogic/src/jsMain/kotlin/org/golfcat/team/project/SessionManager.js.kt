package org.golfcat.team.project

import org.golfcat.team.project.models.User
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

actual object SessionProvider {
    private const val KEY_USER = "current_user"
    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveUser(user: User?) {
        if (user == null) {
            window.localStorage.removeItem(KEY_USER)
        } else {
            val userJson = json.encodeToString(user)
            window.localStorage.setItem(KEY_USER, userJson)
        }
    }

    actual fun loadUser(): User? {
        val userJson = window.localStorage.getItem(KEY_USER) ?: return null
        return try {
            json.decodeFromString<User>(userJson)
        } catch (e: Exception) {
            null
        }
    }

    actual fun clear() {
        window.localStorage.removeItem(KEY_USER)
    }
}
