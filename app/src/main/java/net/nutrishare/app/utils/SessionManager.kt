package net.nutrishare.app.utils

import net.nutrishare.app.model.User


object SessionManager {
    var currentUser: User? = null

    fun setUser(user: User) {
        currentUser = user
    }

    fun getUser(): User? {
        return currentUser
    }

    fun clearUser() {
        currentUser = null
    }
}