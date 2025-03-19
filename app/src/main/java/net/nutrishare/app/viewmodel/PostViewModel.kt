package net.nutrishare.app.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.nutrishare.app.data.PostRepository
import net.nutrishare.app.model.Comment
import net.nutrishare.app.model.Post

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    private val _operationStatus = MutableLiveData<Result<String>>()
    val operationStatus: LiveData<Result<String>> = _operationStatus

    private val _postsLiveData = MutableLiveData<List<Post>>()
    val postsLiveData: LiveData<List<Post>> = _postsLiveData

    private val _favoritePosts = MutableLiveData<List<Post>>()
    val favoritePosts: LiveData<List<Post>> = _favoritePosts

    private val _commentsLiveData = MutableLiveData<List<Comment>>()
    val commentsLiveData: LiveData<List<Comment>> = _commentsLiveData

    fun favoritePost(userId: String, post: Post) {
        viewModelScope.launch {
            try {
                repository.favoritePost(userId, post)
                _operationStatus.postValue(Result.success("Post marked as favorite"))
            } catch (e: Exception) {
                _operationStatus.postValue(Result.failure(e))
            }
        }
    }

    fun unfavoritePost(userId: String, post: Post) {
        viewModelScope.launch {
            try {
                repository.unfavoritePost(userId, post)
                _operationStatus.postValue(Result.success("Post removed from favorites"))
            } catch (e: Exception) {
                _operationStatus.postValue(Result.failure(e))
            }
        }
    }

    fun fetchFavoritePostsByUser(userId: String) {
        viewModelScope.launch {
            val posts = repository.fetchFavoritePostsByUser(userId)
            _favoritePosts.postValue(posts)
        }
    }

    fun createPost(post: Post, imageUri: Uri?) {
        viewModelScope.launch {
            try {
                repository.createPost(post, imageUri)
                _operationStatus.postValue(Result.success("Post created successfully"))
                loadAllPosts()
            } catch (e: Exception) {
                _operationStatus.postValue(Result.failure(e))
            }
        }
    }

    fun updatePost(post: Post, imageUri: Uri?) {
        viewModelScope.launch {
            try {
                repository.updatePost(post, imageUri)
                _operationStatus.postValue(Result.success("Post updated successfully"))
                loadAllPosts()
            } catch (e: Exception) {
                _operationStatus.postValue(Result.failure(e))
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                repository.deletePost(post)
                _operationStatus.postValue(Result.success("Post deleted successfully"))
                loadAllPosts()
            } catch (e: Exception) {
                _operationStatus.postValue(Result.failure(e))
            }
        }
    }

    fun loadAllPosts() {
        viewModelScope.launch {
            val posts = repository.getAllPosts()
            _postsLiveData.postValue(posts)
        }
    }

    fun addComment(comment: Comment) {
        viewModelScope.launch {
            try {
                repository.addComment(comment)
                _operationStatus.postValue(Result.success("Comment added successfully"))
                fetchCommentsByPostId(comment.postId)
            } catch (e: Exception) {
                _operationStatus.postValue(Result.failure(e))
            }
        }
    }

    fun deleteComment(commentId: String, postId: String) {
        viewModelScope.launch {
            try {
                repository.deleteComment(commentId, postId)
                _operationStatus.postValue(Result.success("Comment deleted successfully"))
                fetchCommentsByPostId(postId)
            } catch (e: Exception) {
                _operationStatus.postValue(Result.failure(e))
            }
        }
    }

    fun fetchCommentsByPostId(postId: String) {
        viewModelScope.launch {
            val comments = repository.fetchCommentsByPostId(postId)
            _commentsLiveData.postValue(comments)
        }
    }
}
