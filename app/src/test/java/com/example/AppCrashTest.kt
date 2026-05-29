package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class AppCrashTest {
    @get:Rule 
    val composeTestRule = createComposeRule()

    @Test
    fun testFeedScreenRendersAndInteracts() {
        val mockPosts = listOf(
            PostEntity(
                id = 1,
                authorName = "Elena",
                authorAvatar = "Elena",
                mediaType = "image",
                mediaUrl = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800",
                caption = "Kyoto sunset loop",
                likesCount = 42,
                isLiked = false
            )
        )
        val mockStories = listOf(
            StoryEntity(
                id = 1,
                authorName = "Chloe",
                authorAvatar = "Chloe",
                mediaType = "image",
                mediaUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=600"
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                FeedScreen(
                    posts = mockPosts,
                    stories = mockStories,
                    profileName = "Artist",
                    onLikePost = {},
                    onCommentAdd = { _, _ -> },
                    onStoryClick = {},
                    onAddStoryClick = {},
                    getComments = { emptyList() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composeTestRule.onNodeWithTag("feed_lazy_column").assertExists()
        composeTestRule.onNodeWithText("Kyoto sunset loop").assertExists()
        composeTestRule.onNodeWithText("DAILY STORIES").assertExists()
    }

    @Test
    fun testBoardsScreenRendersAndInteracts() {
        val mockBoards = listOf(
            BoardEntity(
                id = 1,
                title = "Cinematography",
                description = "720p setups",
                iconName = "videocam",
                creatorName = "Marcus"
            )
        )
        val mockPins = listOf(
            BoardPinEntity(
                id = 1,
                boardId = 1,
                authorName = "Elena",
                authorAvatar = "Elena",
                contentText = "Retro LUTs are great"
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                BoardsScreen(
                    boards = mockBoards,
                    selectedBoardId = null,
                    boardPins = mockPins,
                    onBoardSelected = {},
                    onAddPin = { _, _, _ -> },
                    onAddBoard = { _, _, _ -> },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composeTestRule.onNodeWithTag("boards_list").assertExists()
        composeTestRule.onNodeWithText("#Cinematography").assertExists()
    }

    @Test
    fun testSecureChatScreenRendersAndInteracts() {
        val mockMessages = listOf(
            MessageEntity(
                id = 1,
                senderName = "Elena",
                receiverName = "Me",
                content = "Hello there",
                isEncrypted = true
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SecureChatScreen(
                    chatMessages = mockMessages,
                    currentPartner = "Elena Rostova",
                    onPartnerSelected = {},
                    onSendMessage = { _, _ -> },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composeTestRule.onNodeWithTag("chat_messages_lazy").assertExists()
        composeTestRule.onNodeWithText("Hello there").assertExists()
        composeTestRule.onNodeWithText("SECURE PROTOCOL ACTIVE").assertExists()
    }

    @Test
    fun testProfileScreenRendersAndInteracts() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    posts = emptyList(),
                    stories = emptyList(),
                    profileName = "Aesthetic Visual Artist",
                    profileBio = "Cinematic video creator",
                    profileAvatarColor = 0xFFFF007F.toInt(),
                    profileCategory = "Aesthetic Visual Artist",
                    profileAvatarUrl = "",
                    prefNotifLikes = true,
                    prefNotifComments = true,
                    prefNotifChats = true,
                    onUpdateProfile = { _, _, _, _, _ -> },
                    onUpdateNotifications = { _, _, _ -> },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composeTestRule.onNodeWithText("EDIT PROFILE").assertExists()
        composeTestRule.onNodeWithText("Aesthetic Visual Artist").assertExists()
        composeTestRule.onNodeWithText("Cinematic video creator").assertExists()
    }

    @Test
    fun testCreatorStudioRenders() {
        composeTestRule.setContent {
            MyApplicationTheme {
                CreatorStudioDialog(
                    onDismiss = {},
                    onPublishPost = { _, _, _, _, _, _ -> },
                    onPublishStory = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("post_caption_input").assertExists()
        composeTestRule.onNodeWithTag("publish_action_button").assertExists()
    }
}
