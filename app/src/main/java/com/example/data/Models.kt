package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorAvatar: String, // "avatar1", "avatar2" etc.
    val mediaType: String, // "image" or "video"
    val mediaUrl: String, // can be resource name or web URL
    val caption: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val resolution: String = "720p HD", // high-quality marker
    val filterApplied: String = "Normal", // "Retro Glow", "Cyberpunk", "Chrome", etc.
    val timestamp: Long = System.currentTimeMillis(),
    val commentsJson: String = "[]", // Serialized list of comments
    val isPrivate: Boolean = false
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorAvatar: String,
    val mediaType: String, // "image" or "video"
    val mediaUrl: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val receiverName: String,
    val content: String,
    val isEncrypted: Boolean = true, // Vidsta messaging defaults to E2EE
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "boards")
data class BoardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val iconName: String, // Material Icon name
    val creatorName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "board_pins")
data class BoardPinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boardId: Long,
    val authorName: String,
    val authorAvatar: String,
    val contentText: String,
    val mediaUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// UI Representational helper classes
data class PostComment(
    val authorName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
