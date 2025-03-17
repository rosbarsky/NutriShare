package net.nutrishare.app.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import net.nutrishare.app.database.PostDao
import net.nutrishare.app.model.Post

class PostRepository(
    private val postDao: PostDao,
    private val firestore: FirebaseFirestore,
    private val storageRef: StorageReference
) {

    suspend fun createPost(post: Post, imageUri: Uri?) {

        val updatedPost = if (imageUri != null) {
            val imageUrl = uploadPostImage(post.postId, imageUri)
            post.copy(postImage = imageUrl)
        } else {
            post
        }
        postDao.insertPost(updatedPost)
        syncPostToFirestore(updatedPost)
    }

    suspend fun updatePost(post: Post, imageUri: Uri?) {
        val updatedPost = if (imageUri != null) {
            val imageUrl = uploadPostImage(post.postId, imageUri)
            post.copy(postImage = imageUrl)
        } else {
            post
        }
        postDao.updatePost(updatedPost)
        syncPostToFirestore(updatedPost)
    }

    suspend fun deletePost(post: Post) {
        postDao.deletePost(post)
        deletePostFromFirestore(post.postId)
    }

    suspend fun getAllPosts(): List<Post> {
        return postDao.getAllPosts()
    }

    private suspend fun uploadPostImage(postId: String, imageUri: Uri): String {
        return try {
            val fileRef = storageRef.child("post_images/$postId.jpg")
            fileRef.putFile(imageUri).await()
            fileRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Image upload failed: ${e.message}")
        }
    }

    private suspend fun syncPostToFirestore(post: Post) {
        try {
            firestore.collection("POSTS")
                .document(post.postId)
                .set(post)
                .await()
        } catch (e: Exception) {
            // Log error or handle retries as needed
        }
    }

    private suspend fun deletePostFromFirestore(postId: String) {
        try {
            firestore.collection("POSTS")
                .document(postId)
                .delete()
                .await()
        } catch (e: Exception) {
            // Log error or handle retries as needed
        }
    }
}
