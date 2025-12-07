package com.example.lacakair.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lacakair.data.Post
import com.example.lacakair.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _postCount = MutableStateFlow(0)
    val postCount: StateFlow<Int> = _postCount

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("ProfileViewModel", "Loading profile for user: $userId")

                // Load user data
                val userDoc = firestore.collection("users").document(userId).get().await()
                if (userDoc.exists()) {
                    val user = User(
                        id = userDoc.id,
                        email = userDoc.getString("email") ?: "",
                        name = userDoc.getString("name") ?: ""
                    )
                    _user.value = user
                    Log.d("ProfileViewModel", "User loaded: ${user.name}")
                }

                // Load user posts
                loadUserPosts(userId)

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile", e)
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    private fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Loading posts for user: $userId")

                firestore.collection("posts")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("ProfileViewModel", "Error loading user posts", error)
                            error.printStackTrace()
                            _isLoading.value = false
                            return@addSnapshotListener
                        }

                        snapshot?.let {
                            val postsList = it.documents.mapNotNull { doc ->
                                try {
                                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                                } catch (e: Exception) {
                                    Log.e("ProfileViewModel", "Error parsing post ${doc.id}", e)
                                    null
                                }
                            }.sortedByDescending { it.getTimestampLong() }

                            _userPosts.value = postsList
                            _postCount.value = postsList.size
                            _isLoading.value = false
                            Log.d("ProfileViewModel", "Loaded ${postsList.size} posts for user")
                        }
                    }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception in loadUserPosts", e)
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val postRef = firestore.collection("posts").document(postId)

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    @Suppress("UNCHECKED_CAST")
                    val likes = (snapshot.get("likes") as? ArrayList<*>)?.filterIsInstance<String>() ?: emptyList()

                    val newLikes = if (likes.contains(userId)) {
                        likes - userId
                    } else {
                        likes + userId
                    }

                    transaction.update(postRef, "likes", newLikes)
                }.await()

                Log.d("ProfileViewModel", "Like toggled for post: $postId")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error toggling like", e)
                e.printStackTrace()
            }
        }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    onError("User tidak login")
                    return@launch
                }

                // Verify post belongs to user
                val postDoc = firestore.collection("posts").document(postId).get().await()
                val postUserId = postDoc.getString("userId")

                if (postUserId != userId) {
                    onError("Anda tidak memiliki izin untuk menghapus post ini")
                    return@launch
                }

                // Delete post
                firestore.collection("posts").document(postId).delete().await()
                Log.d("ProfileViewModel", "Post deleted: $postId")
                onSuccess()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error deleting post", e)
                onError(e.message ?: "Gagal menghapus post")
            }
        }
    }
}

