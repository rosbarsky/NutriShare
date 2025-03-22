package net.nutrishare.app.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import net.nutrishare.app.database.CommentDao
import net.nutrishare.app.database.PostDao
import net.nutrishare.app.model.Comment
import net.nutrishare.app.model.Post
import net.nutrishare.app.utils.SessionManager

class PostRepository(
    private val postDao: PostDao,
    private val firestore: FirebaseFirestore,
    private val storageRef: StorageReference,
    private val commentDao:CommentDao?
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
        syncPostsFromFirestore()
        return postDao.getAllPosts()
    }

    suspend fun getAllPostsByUserId(userId: String): List<Post> {
        return postDao.getPostsByUserId(userId)
    }

    private suspend fun syncPostsFromFirestore() {
        try {

            val firestorePosts = firestore.collection("POSTS")
                .get()
                .await()
                .toObjects(Post::class.java)

            val favoritePosts = firestore.collection("FAVOURITES")
                .document("${SessionManager.getUser()?.userId}")
                .collection("POSTS")
                .get()
                .await()
                .documents.map { it.id }

            val mergedPosts = firestorePosts.map { post ->
                post.copy(isFavorite = favoritePosts.contains(post.postId))
            }

            postDao.insertPosts(mergedPosts)

        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    suspend fun favoritePost(userId: String, post: Post) {
        val updatedPost = post.copy(isFavorite = true)
        postDao.updatePost(updatedPost)
        syncFavoriteStatusToFirestore(userId, post.postId, true)
    }

    suspend fun unfavoritePost(userId: String, post: Post) {
        val updatedPost = post.copy(isFavorite = false)
        postDao.updatePost(updatedPost)
        syncFavoriteStatusToFirestore(userId, post.postId, false)
    }

    private suspend fun syncFavoriteStatusToFirestore(userId: String, postId: String, isFavorite: Boolean) {
        val userFavoritesRef = firestore.collection("FAVOURITES").document(userId).collection("POSTS")
        try {
            if (isFavorite) {
                userFavoritesRef.document(postId).set(mapOf("timestamp" to System.currentTimeMillis())).await()
            } else {
                userFavoritesRef.document(postId).delete().await()
            }
        } catch (e: Exception) {
            // Log the error or handle retries as needed
        }
    }

    suspend fun fetchFavoritePostsByUser(userId: String): List<Post> {
        return try {
            val favoritePostIds = firestore.collection("FAVOURITES")
                .document(userId)
                .collection("POSTS")
                .get()
                .await()
                .documents.map { it.id }

            postDao.getAllPosts().filter { it.postId in favoritePostIds }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment) {
        commentDao?.insertComment(comment)
        syncCommentToFirestore(comment)
    }

    suspend fun deleteComment(commentId: String, postId: String) {
        commentDao?.deleteCommentById(commentId)
        deleteCommentFromFirestore(commentId, postId)
    }

    suspend fun fetchCommentsByPostId(postId: String): List<Comment> {
        return try {
            val comments = firestore.collection("POSTS")
                .document(postId)
                .collection("COMMENTS")
                .orderBy("timestamp")
                .get()
                .await()
                .toObjects(Comment::class.java)

            commentDao?.insertComments(comments)
            comments
        } catch (e: Exception) {
            commentDao!!.getCommentsByPostId(postId)
        }
    }

    suspend fun syncCommentToFirestore(comment: Comment) {
        try {
            firestore.collection("POSTS")
                .document(comment.postId)
                .collection("COMMENTS")
                .document(comment.commentId)
                .set(comment)
                .await()
        } catch (e: Exception) {
            // Handle sync error if needed
        }
    }

    private suspend fun deleteCommentFromFirestore(commentId: String, postId: String) {
        try {
            firestore.collection("POSTS")
                .document(postId)
                .collection("COMMENTS")
                .document(commentId)
                .delete()
                .await()
        } catch (e: Exception) {
            // Handle delete error if needed
        }
    }
}
