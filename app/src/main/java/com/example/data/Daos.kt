package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Long): PostEntity?
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long

    @Update
    suspend fun updateStory(story: StoryEntity)

    @Query("DELETE FROM stories WHERE id = :id")
    suspend fun deleteStoryById(id: Long)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE (senderName = :userA AND receiverName = :userB) OR (senderName = :userB AND receiverName = :userA) ORDER BY timestamp ASC")
    fun getMessagesBetween(userA: String, userB: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long
}

@Dao
interface BoardDao {
    @Query("SELECT * FROM boards ORDER BY timestamp DESC")
    fun getAllBoards(): Flow<List<BoardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: BoardEntity): Long

    @Query("SELECT * FROM board_pins WHERE boardId = :boardId ORDER BY timestamp DESC")
    fun getPinsForBoard(boardId: Long): Flow<List<BoardPinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: BoardPinEntity): Long
}
