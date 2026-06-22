package org.golfcat.team.project

import org.golfcat.team.project.models.User

actual object SessionProvider {
    actual fun saveUser(user: User?) {}
    actual fun loadUser(): User? = null
    actual fun clear() {}
}
