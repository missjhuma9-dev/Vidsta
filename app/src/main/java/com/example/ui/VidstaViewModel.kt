package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class VidstaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repository
    private val sharedPrefs = application.getSharedPreferences("vidsta_profile_prefs", android.content.Context.MODE_PRIVATE)

    // User Profile persistent states
    private val _profileName = MutableStateFlow(sharedPrefs.getString("profile_name", "Aesthetic Visual Artist") ?: "Aesthetic Visual Artist")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileBio = MutableStateFlow(
        sharedPrefs.getString("profile_bio", "Recording snapshots & premium 720p cinematic loops with vintage color profiles on Vidsta. Let's share experiences!")
            ?: "Recording snapshots & premium 720p cinematic loops with vintage color profiles on Vidsta. Let's share experiences!"
    )
    val profileBio: StateFlow<String> = _profileBio.asStateFlow()

    // 0xFFFF007F is glowing magenta, 0xFF00F0FF is custom cyan, 0xFFFFE600 is warm yellow, etc.
    private val _profileAvatarColor = MutableStateFlow(sharedPrefs.getInt("profile_avatar_color", 0xFFFF007F.toInt()))
    val profileAvatarColor: StateFlow<Int> = _profileAvatarColor.asStateFlow()

    private val _profileCategory = MutableStateFlow(sharedPrefs.getString("profile_category", "Aesthetic Visual Artist") ?: "Aesthetic Visual Artist")
    val profileCategory: StateFlow<String> = _profileCategory.asStateFlow()

    private val _profileAvatarUrl = MutableStateFlow(sharedPrefs.getString("profile_avatar_url", "") ?: "")
    val profileAvatarUrl: StateFlow<String> = _profileAvatarUrl.asStateFlow()

    // Notification toggles
    private val _prefNotifLikes = MutableStateFlow(sharedPrefs.getBoolean("notif_likes", true))
    val prefNotifLikes: StateFlow<Boolean> = _prefNotifLikes.asStateFlow()

    private val _prefNotifComments = MutableStateFlow(sharedPrefs.getBoolean("notif_comments", true))
    val prefNotifComments: StateFlow<Boolean> = _prefNotifComments.asStateFlow()

    private val _prefNotifChats = MutableStateFlow(sharedPrefs.getBoolean("notif_chats", true))
    val prefNotifChats: StateFlow<Boolean> = _prefNotifChats.asStateFlow()

    fun updateProfile(name: String, bio: String, colorVal: Int, category: String, avatarUrl: String) {
        _profileName.value = name
        _profileBio.value = bio
        _profileAvatarColor.value = colorVal
        _profileCategory.value = category
        _profileAvatarUrl.value = avatarUrl
        sharedPrefs.edit().apply {
            putString("profile_name", name)
            putString("profile_bio", bio)
            putInt("profile_avatar_color", colorVal)
            putString("profile_category", category)
            putString("profile_avatar_url", avatarUrl)
            apply()
        }
    }

    fun updateNotificationSettings(likes: Boolean, comments: Boolean, chats: Boolean) {
        _prefNotifLikes.value = likes
        _prefNotifComments.value = comments
        _prefNotifChats.value = chats
        sharedPrefs.edit().apply {
            putBoolean("notif_likes", likes)
            putBoolean("notif_comments", comments)
            putBoolean("notif_chats", chats)
            apply()
        }
    }

    // Initialize Database & Repository
    init {
        val database = AppDatabase.getDatabase(application)
        repository = Repository(database)
        viewModelScope.launch {
            repository.seedIfNeeded(application)
        }
    }

    // Main Flows
    val posts: StateFlow<List<PostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.allStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val boards: StateFlow<List<BoardEntity>> = repository.allBoards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat management
    private val _selectedChatPartner = MutableStateFlow<String>("Elena Rostova")
    val selectedChatPartner: StateFlow<String> = _selectedChatPartner.asStateFlow()

    val chatMessages: StateFlow<List<MessageEntity>> = _selectedChatPartner
        .flatMapLatest { partner ->
            repository.getMessagesBetween("Me", partner)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Board Pins management
    private val _selectedBoardId = MutableStateFlow<Long?>(null)
    val selectedBoardId: StateFlow<Long?> = _selectedBoardId.asStateFlow()

    val boardPins: StateFlow<List<BoardPinEntity>> = _selectedBoardId
        .flatMapLatest { boardId ->
            if (boardId != null) {
                repository.getPinsForBoard(boardId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Story Viewer State
    private val _viewingStory = MutableStateFlow<StoryEntity?>(null)
    val viewingStory: StateFlow<StoryEntity?> = _viewingStory.asStateFlow()

    // Methods
    fun selectChatPartner(partner: String) {
        _selectedChatPartner.value = partner
    }

    fun selectBoard(boardId: Long?) {
        _selectedBoardId.value = boardId
    }

    fun startViewingStory(story: StoryEntity) {
        _viewingStory.value = story
        viewModelScope.launch {
            repository.updateStory(story.copy(isViewed = true))
        }
    }

    fun stopViewingStory() {
        _viewingStory.value = null
    }

    fun likePost(post: PostEntity) {
        viewModelScope.launch {
            val isLikedNow = !post.isLiked
            val newLikeCount = if (isLikedNow) post.likesCount + 1 else post.likesCount - 1
            repository.updatePost(
                post.copy(
                    isLiked = isLikedNow,
                    likesCount = maxOf(0, newLikeCount)
                )
            )
        }
    }

    fun addCommentToPost(postId: Long, author: String, text: String) {
        viewModelScope.launch {
            val post = repository.getPostById(postId) ?: return@launch
            val commentsArray = try {
                JSONArray(post.commentsJson)
            } catch (e: Exception) {
                JSONArray()
            }

            val newComment = JSONObject().apply {
                put("authorName", author)
                put("text", text)
                put("timestamp", System.currentTimeMillis())
            }
            commentsArray.put(newComment)

            repository.updatePost(post.copy(commentsJson = commentsArray.toString()))
        }
    }

    fun createPost(
        caption: String,
        filter: String,
        resolution: String,
        mediaUrl: String,
        mediaType: String, // "image", "video", or "short"
        isPrivate: Boolean = false
    ) {
        viewModelScope.launch {
            val newPost = PostEntity(
                authorName = "Me",
                authorAvatar = "Me",
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                caption = caption,
                resolution = resolution,
                filterApplied = filter,
                likesCount = 0,
                isLiked = false,
                commentsJson = "[]",
                isPrivate = isPrivate
            )
            repository.insertPost(newPost)
        }
    }

    fun createStory(mediaUrl: String) {
        viewModelScope.launch {
            val newStory = StoryEntity(
                authorName = "Me",
                authorAvatar = "Me",
                mediaType = "image",
                mediaUrl = mediaUrl
            )
            repository.insertStory(newStory)
        }
    }

    fun sendChatMessage(text: String, isEncrypted: Boolean = true) {
        val receiver = _selectedChatPartner.value
        if (text.isBlank()) return
        viewModelScope.launch {
            val newMsg = MessageEntity(
                senderName = "Me",
                receiverName = receiver,
                content = text,
                isEncrypted = isEncrypted
            )
            repository.insertMessage(newMsg)
        }
    }

    fun createBoard(title: String, description: String, iconName: String) {
        viewModelScope.launch {
            val newBoard = BoardEntity(
                title = title,
                description = description,
                iconName = iconName,
                creatorName = "Me"
            )
            repository.insertBoard(newBoard)
        }
    }

    fun addPinToBoard(boardId: Long, text: String, mediaUrl: String?) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val newPin = BoardPinEntity(
                boardId = boardId,
                authorName = "Me",
                authorAvatar = "Me",
                contentText = text,
                mediaUrl = mediaUrl
            )
            repository.insertPin(newPin)
        }
    }

    // Helper to retrieve comments as objects
    fun getComments(commentsJson: String): List<PostComment> {
        val list = mutableListOf<PostComment>()
        try {
            val array = JSONArray(commentsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PostComment(
                        authorName = obj.optString("authorName", "Anonymous"),
                        text = obj.optString("text", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
