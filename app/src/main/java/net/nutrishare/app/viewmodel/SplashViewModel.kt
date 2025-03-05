package net.nutrishare.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SplashViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _sessionCheckResult = MutableLiveData<Boolean>()
    val sessionCheckResult: LiveData<Boolean> = _sessionCheckResult

    fun checkUserSession() {
        val user = auth.currentUser
        _sessionCheckResult.value = user != null
    }
}
