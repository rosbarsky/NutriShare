package net.nutrishare.app.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.nutrishare.app.data.PostRepository
import net.nutrishare.app.model.Post

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    private val _operationStatus = MutableLiveData<Result<String>>()
    val operationStatus: LiveData<Result<String>> = _operationStatus

    private val _postsLiveData = MutableLiveData<List<Post>>()
    val postsLiveData: LiveData<List<Post>> = _postsLiveData

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
}
