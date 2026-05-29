package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class Repository(private val database: AppDatabase) {
    val allPosts: Flow<List<PostEntity>> = database.postDao().getAllPosts()
    val allStories: Flow<List<StoryEntity>> = database.storyDao().getAllStories()
    val allBoards: Flow<List<BoardEntity>> = database.boardDao().getAllBoards()

    suspend fun insertPost(post: PostEntity): Long {
        return database.postDao().insertPost(post)
    }

    suspend fun updatePost(post: PostEntity) {
        database.postDao().updatePost(post)
    }

    suspend fun deletePost(post: PostEntity) {
        database.postDao().deletePost(post)
    }

    suspend fun getPostById(id: Long): PostEntity? {
        return database.postDao().getPostById(id)
    }

    suspend fun insertStory(story: StoryEntity): Long {
        return database.storyDao().insertStory(story)
    }

    suspend fun updateStory(story: StoryEntity) {
        database.storyDao().updateStory(story)
    }

    suspend fun deleteStory(id: Long) {
        database.storyDao().deleteStoryById(id)
    }

    fun getMessagesBetween(userA: String, userB: String): Flow<List<MessageEntity>> {
        return database.messageDao().getMessagesBetween(userA, userB)
    }

    suspend fun insertMessage(message: MessageEntity): Long {
        return database.messageDao().insertMessage(message)
    }

    fun getPinsForBoard(boardId: Long): Flow<List<BoardPinEntity>> {
        return database.boardDao().getPinsForBoard(boardId)
    }

    suspend fun insertBoard(board: BoardEntity): Long {
        return database.boardDao().insertBoard(board)
    }

    suspend fun insertPin(pin: BoardPinEntity): Long {
        return database.boardDao().insertPin(pin)
    }

    // JSON Helper to construct initial comments
    private fun makeCommentsJson(vararg comments: Pair<String, String>): String {
        val array = JSONArray()
        for (c in comments) {
            val obj = JSONObject()
            obj.put("authorName", c.first)
            obj.put("text", c.second)
            obj.put("timestamp", System.currentTimeMillis())
            array.put(obj)
        }
        return array.toString()
    }

    // Auto-seed initial premium social data when database is empty
    suspend fun seedIfNeeded(context: Context) {
        val firstPost = database.postDao().getPostById(1)
        if (firstPost == null) {
            // 1. Seed Boards
            val board1Id = database.boardDao().insertBoard(
                BoardEntity(
                    title = "720p Cinematography",
                    description = "Mobile capturing, color grading, and framing techniques for crisp 720p smartphone cinematic masterpieces.",
                    iconName = "videocam",
                    creatorName = "Marcus Chen"
                )
            )

            val board2Id = database.boardDao().insertBoard(
                BoardEntity(
                    title = "Street Aesthetics",
                    description = "Chasing the geometric shadows and glowing neon lights of modern nocturnal urban landscapes.",
                    iconName = "photo_camera",
                    creatorName = "Elena Rostova"
                )
            )

            val board3Id = database.boardDao().insertBoard(
                BoardEntity(
                    title = "Gourmet Bites",
                    description = "Daily recipes and macro photos of artisan pastries and aesthetic dining plates.",
                    iconName = "restaurant",
                    creatorName = "Chloe Baker"
                )
            )

            // Seed Pins for Boards
            database.boardDao().insertPin(
                BoardPinEntity(
                    boardId = board1Id,
                    authorName = "Marcus Chen",
                    authorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                    contentText = "Pro Tip: Set your camera to 720p @ 60fps instead of 4K when shooting fast kinetic drift. The rolling shutter artifact is reduced by almost 40%! Renders beautifully in the Vidsta feed.",
                    mediaUrl = "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=800"
                )
            )

            database.boardDao().insertPin(
                BoardPinEntity(
                    boardId = board1Id,
                    authorName = "Elena Rostova",
                    authorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                    contentText = "I paired standard 720p recording with the 'Retro Glow' LUT filter. It completely mimics old Kodak 200 film stock!",
                    mediaUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800"
                )
            )

            database.boardDao().insertPin(
                BoardPinEntity(
                    boardId = board2Id,
                    authorName = "Elena Rostova",
                    authorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                    contentText = "Found this beautiful alleyway in Kyoto at 11 PM. Perfect balance of magenta neon shadows.",
                    mediaUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800"
                )
            )

            // 2. Seed Stories (for active sliders)
            database.storyDao().insertStory(
                StoryEntity(
                    authorName = "Elena Rostova",
                    authorAvatar = "Elena",
                    mediaType = "image",
                    mediaUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=600"
                )
            )
            database.storyDao().insertStory(
                StoryEntity(
                    authorName = "Marcus Chen",
                    authorAvatar = "Marcus",
                    mediaType = "image",
                    mediaUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600"
                )
            )
            database.storyDao().insertStory(
                StoryEntity(
                    authorName = "Chloe Baker",
                    authorAvatar = "Chloe",
                    mediaType = "image",
                    mediaUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=600"
                )
            )
            database.storyDao().insertStory(
                StoryEntity(
                    authorName = "Vidsta Hub",
                    authorAvatar = "Vidsta",
                    mediaType = "image",
                    mediaUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600"
                )
            )

            // 3. Seed Posts
            database.postDao().insertPost(
                PostEntity(
                    authorName = "Elena Rostova",
                    authorAvatar = "Elena",
                    mediaType = "short",
                    mediaUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800",
                    caption = "Kyoto nocturnal neon alleyway stream. Truly vintage film-grade grain. #tokyodrift #cyberpunk",
                    likesCount = 890,
                    isLiked = false,
                    resolution = "720p Loop HD",
                    filterApplied = "Cyberpunk",
                    commentsJson = makeCommentsJson(
                        "Marcus Chen" to "This short loop is so clean!",
                        "Elena Rostova" to "Thanks Marcus! Shot on mobile."
                    )
                )
            )

            database.postDao().insertPost(
                PostEntity(
                    authorName = "Marcus Chen",
                    authorAvatar = "Marcus",
                    mediaType = "short",
                    mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                    caption = "Swiss Alps sunset trail in 720p. The mountains feel so real with Retro Glow! #landscape #loop",
                    likesCount = 1240,
                    isLiked = true,
                    resolution = "720p 60fps Loop",
                    filterApplied = "Retro Glow",
                    commentsJson = "[]"
                )
            )

            database.postDao().insertPost(
                PostEntity(
                    authorName = "Elena Rostova",
                    authorAvatar = "Elena",
                    mediaType = "image",
                    mediaUrl = "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=800",
                    caption = "Chasing sunsets along the gorgeous cliffs of the Amalfi coast! Perfectly preserves depth when rendering 720p visuals.",
                    likesCount = 248,
                    isLiked = false,
                    resolution = "720p Cinema HD",
                    filterApplied = "Glow Sunset",
                    commentsJson = makeCommentsJson(
                        "Marcus Chen" to "This is crazy sharp! Did you do color correction?",
                        "Elena Rostova" to "Yes! Boosted warm shadows by 15% in Vidsta Studio :)",
                        "Chloe Baker" to "Stunning composition. Adding this to travel board."
                    )
                )
            )

            database.postDao().insertPost(
                PostEntity(
                    authorName = "Marcus Chen",
                    authorAvatar = "Marcus",
                    mediaType = "video",
                    mediaUrl = "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=800",
                    caption = "Low-light exposure trial in 720p resolution. Testing our dynamic noise compression with neon cyber hues. The glow looks incredibly premium!",
                    likesCount = 512,
                    isLiked = true,
                    resolution = "720p HD (60fps)",
                    filterApplied = "Cyberpunk Purple",
                    commentsJson = makeCommentsJson(
                        "Elena Rostova" to "Whoa! The contrast is supreme. No noise at all.",
                        "Vidsta Hub" to "Excellent compression profile!"
                    )
                )
            )

            database.postDao().insertPost(
                PostEntity(
                    authorName = "Chloe Baker",
                    authorAvatar = "Chloe",
                    mediaType = "image",
                    mediaUrl = "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=800",
                    caption = "Fresh, fluffy lemon-iced blueberry soufflé pancakes for brunch! Golden brown peaks and high dynamic contrast.",
                    likesCount = 189,
                    isLiked = false,
                    resolution = "720p HD",
                    filterApplied = "Retro Soft",
                    commentsJson = makeCommentsJson(
                        "Sarah Jenkins" to "Oh my goodness recipe please!",
                        "Chloe Baker" to "Pinned it to the #GourmetBites board!"
                    )
                )
            )

            // 4. Seed Messages for Private Chat rooms (E2E Encrypted simulator)
            database.messageDao().insertMessage(
                MessageEntity(
                    senderName = "Elena Rostova",
                    receiverName = "Me",
                    content = "Hey there! Have you tested the new 720p creator studio filter tools?",
                    isEncrypted = true,
                    timestamp = System.currentTimeMillis() - 7200000
                )
            )
            database.messageDao().insertMessage(
                MessageEntity(
                    senderName = "Me",
                    receiverName = "Elena Rostova",
                    content = "Not yet, does the HDR saturation export nicely?",
                    isEncrypted = true,
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )
            database.messageDao().insertMessage(
                MessageEntity(
                    senderName = "Elena Rostova",
                    receiverName = "Me",
                    content = "Absolutely! Try applying the 'Cyberpunk' or 'Retro Glow' LUT. It renders without losing any fine pixels.",
                    isEncrypted = true,
                    timestamp = System.currentTimeMillis() - 1800000
                )
            )

            database.messageDao().insertMessage(
                MessageEntity(
                    senderName = "Marcus Chen",
                    receiverName = "Me",
                    content = "Yo! Check out the #720pCinematography board. I pinned my setup tips there.",
                    isEncrypted = true,
                    timestamp = System.currentTimeMillis() - 14400000
                )
            )
            database.messageDao().insertMessage(
                MessageEntity(
                    senderName = "Me",
                    receiverName = "Marcus Chen",
                    content = "Awesome Marcus. I'll read it. Let's build a collaborative camera log together.",
                    isEncrypted = true,
                    timestamp = System.currentTimeMillis() - 10000000
                )
            )

            database.messageDao().insertMessage(
                MessageEntity(
                    senderName = "Vidsta Secure Hub",
                    receiverName = "Me",
                    content = "Welcome to Vidsta! All messages here are digitally locked and signed side-by-side using end-to-end cryptographic keypairs.",
                    isEncrypted = true,
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
        }
    }
}
