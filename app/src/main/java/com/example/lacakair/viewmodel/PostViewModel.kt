package com.example.lacakair.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lacakair.data.Post
import com.example.lacakair.util.ImageUploader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostViewModel : ViewModel() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("PostViewModel", "Starting to load posts...")

                firestore.collection("posts")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("PostViewModel", "Error loading posts", error)
                            error.printStackTrace()
                            _isLoading.value = false
                            return@addSnapshotListener
                        }

                        snapshot?.let {
                            Log.d("PostViewModel", "Received ${it.documents.size} documents")
                            val postsList = it.documents.mapNotNull { doc ->
                                try {
                                    val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                                    Log.d("PostViewModel", "Loaded post: ${post?.userName} - ${post?.caption}")
                                    post
                                } catch (e: Exception) {
                                    Log.e("PostViewModel", "Error parsing document ${doc.id}", e)
                                    e.printStackTrace()
                                    null
                                }
                            }.sortedByDescending { it.getTimestampLong() }

                            _posts.value = postsList
                            _isLoading.value = false
                            Log.d("PostViewModel", "Total posts loaded: ${postsList.size}")
                        }
                    }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception in loadPosts", e)
                e.printStackTrace()
                _isLoading.value = false
            }
        }
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

                Log.d("PostViewModel", "Like toggled for post: $postId")
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error toggling like", e)
                e.printStackTrace()
            }
        }
    }

    fun createPostWithImage(
        context: Context,
        imageUri: Uri,
        caption: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    onError("User tidak login")
                    return@launch
                }

                Log.d("PostViewModel", "Starting upload for image: $imageUri")

                // Upload image ke ImgBB (gratis)
                val uploadResult = ImageUploader.uploadImage(context, imageUri)

                if (uploadResult.isFailure) {
                    onError(uploadResult.exceptionOrNull()?.message ?: "Upload gagal")
                    return@launch
                }

                val imageUrl = uploadResult.getOrNull()
                if (imageUrl == null) {
                    onError("Gagal mendapatkan URL gambar")
                    return@launch
                }

                Log.d("PostViewModel", "Image uploaded: $imageUrl")

                // Get user name
                val userDoc = firestore.collection("users").document(userId).get().await()
                val userName = userDoc.getString("name") ?: "Unknown"

                // Create post document
                val post = hashMapOf(
                    "userId" to userId,
                    "userName" to userName,
                    "imageUrl" to imageUrl,
                    "caption" to caption,
                    "likes" to emptyList<String>(),
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("posts").add(post).await()
                Log.d("PostViewModel", "Post created successfully")
                onSuccess()
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error creating post", e)
                onError(e.message ?: "Gagal membuat post")
            }
        }
    }

    fun createPost(imageUrl: String, caption: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                // Get user name
                val userDoc = firestore.collection("users").document(userId).get().await()
                val userName = userDoc.getString("name") ?: "Unknown"

                val post = hashMapOf(
                    "userId" to userId,
                    "userName" to userName,
                    "imageUrl" to imageUrl,
                    "caption" to caption,
                    "likes" to emptyList<String>(),
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("posts").add(post).await()
                Log.d("PostViewModel", "Post created successfully")
                onSuccess()
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error creating post", e)
                onError(e.message ?: "Gagal membuat post")
            }
        }
    }
}
