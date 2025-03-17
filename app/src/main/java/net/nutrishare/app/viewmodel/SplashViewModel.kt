package net.nutrishare.app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import net.nutrishare.app.model.User
import net.nutrishare.app.utils.SessionManager

class SplashViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _sessionCheckResult = MutableLiveData<Boolean>()
    val sessionCheckResult: LiveData<Boolean> = _sessionCheckResult

    suspend fun checkUserSession() {
        val user = auth.currentUser
        if (user != null){
            fetchUserDetails(user.uid)
        }
        _sessionCheckResult.value = user != null
    }

    private suspend fun fetchUserDetails(userId: String){
        try {
            val documentSnapshot = firestore.collection("USERS").document(userId).get().await()
            if (documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    SessionManager.setUser(it)
                }
            }
        } catch (e: Exception) {
            Log.d("TEST999", e.localizedMessage)
        }
    }
}
