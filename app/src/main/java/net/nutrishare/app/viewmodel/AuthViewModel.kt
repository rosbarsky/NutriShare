package net.nutrishare.app.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.nutrishare.app.data.AuthRepository
import net.nutrishare.app.data.AuthResult

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authResult = MutableLiveData<AuthResult>()
    val authResult: LiveData<AuthResult> = _authResult

    fun logout() {
        viewModelScope.launch {
            _authResult.value = authRepository.logout()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = authRepository.login(email, password)
        }
    }

    fun register(email: String, password: String, username: String, profileImageUri: Uri?) {
        viewModelScope.launch {
            _authResult.value = authRepository.register(email, password, username, profileImageUri)
        }
    }

    fun updateUserName(userId: String?, newName: String) {
        viewModelScope.launch {
            _authResult.value = authRepository.updateUserName(userId, newName)
        }
    }

    fun updateProfileImage(userId: String?, newImageUri: Uri) {
        viewModelScope.launch {
            _authResult.value = authRepository.updateProfileImage(userId, newImageUri)
        }
    }

}


