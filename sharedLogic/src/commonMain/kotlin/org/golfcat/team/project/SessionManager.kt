package org.golfcat.team.project

import org.golfcat.team.project.models.User

expect object SessionProvider {
    fun saveUser(user: User?)
    fun loadUser(): User?
    fun clear()
}
