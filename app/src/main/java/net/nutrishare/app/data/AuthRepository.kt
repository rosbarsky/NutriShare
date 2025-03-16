package net.nutrishare.app.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val message: String): AuthResult()
    data class Failure(val exception: Exception): AuthResult()
}

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference.child("profile_images")
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success("Login Successful")
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    suspend fun register(email: String, password: String, username: String, profileImageUri: Uri?): AuthResult {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val user = auth.currentUser
            val imageUrl = if (profileImageUri != null) {
                uploadProfileImage(user?.uid, profileImageUri)
            } else null
            saveUserDetail(user?.uid, username, imageUrl)
            AuthResult.Success("Registration Successful")
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    private suspend fun uploadProfileImage(userId: String?, imageUri: Uri): String? {
        userId ?: return null
        return try {
            val fileRef = storageRef.child("$userId.jpg")
            fileRef.putFile(imageUri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveUserDetail(userId: String?, username: String, profileImageUrl: String?) {
        userId ?: throw Exception("User ID is null")
        val userMap = hashMapOf(
            "userId" to userId,
            "userName" to username,
            "profileImageUrl" to (profileImageUrl ?: "")
        )
        firestore.collection("USERS").document(userId).set(userMap).await()
    }

    suspend fun updateUserName(userId: String?, newName: String): AuthResult {
        return try {
            userId ?: throw Exception("User ID is null")

            firestore.collection("USERS").document(userId).update("userName", newName).await()

            AuthResult.Success("User name updated successfully")
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    suspend fun updateProfileImage(userId: String?, newImageUri: Uri): AuthResult {
        return try {
            userId ?: throw Exception("User ID is null")

            val imageUrl = uploadProfileImage(userId, newImageUri)

            firestore.collection("USERS").document(userId).update("profileImageUrl", imageUrl).await()

            AuthResult.Success("Profile image updated successfully")
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

}
