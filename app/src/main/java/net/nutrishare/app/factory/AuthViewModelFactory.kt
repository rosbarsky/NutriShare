package net.nutrishare.app.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.nutrishare.app.data.AuthRepository
import net.nutrishare.app.viewmodel.AuthViewModel

class AuthViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
