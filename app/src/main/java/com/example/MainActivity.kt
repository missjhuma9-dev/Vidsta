package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.VidstaViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Primary tab enum for modern state-driven navigation
enum class VidstaTab(val label: String, val icon: ImageVector, val tag: String) {
    FEED("Feed", Icons.Default.Home, "tab_feed"),
    BOARDS("Boards", Icons.Default.Forum, "tab_boards"),
    SECURE_CHAT("Secure Chat", Icons.Default.Lock, "tab_secure_chat"),
    PROFILE("Profile", Icons.Default.Person, "tab_profile")
}

// Preset visual images to make uploads instant and high-quality
data class ImagePreset(val name: String, val url: String)

val PRESET_IMAGES = listOf(
    ImagePreset("🌇 Tokyo Nocturnal Lights", "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800"),
    ImagePreset("🏔️ Swiss Alps Horizon", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"),
    ImagePreset("🍵 Matcha Latte Art", "https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=800"),
    ImagePreset("🐈 Cozy Studio Cat", "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800"),
    ImagePreset("🏎️ Formula Kinetic Drift", "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=800"),
    ImagePreset("🏖️ Serene Wave Crests", "https://images.unsplash.com/photo-1505118380757-91f5f5632de0?w=800")
)

// Main layout activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VidstaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VidstaApp() {
    val viewModel: VidstaViewModel = viewModel()
    
    // Core state flows
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val boards by viewModel.boards.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val selectedChatPartner by viewModel.selectedChatPartner.collectAsStateWithLifecycle()
    val boardPins by viewModel.boardPins.collectAsStateWithLifecycle()
    val selectedBoardId by viewModel.selectedBoardId.collectAsStateWithLifecycle()
    val viewingStory by viewModel.viewingStory.collectAsStateWithLifecycle()

    // Profile settings
    val profileName by viewModel.profileName.collectAsStateWithLifecycle()
    val profileBio by viewModel.profileBio.collectAsStateWithLifecycle()
    val profileAvatarColorVal by viewModel.profileAvatarColor.collectAsStateWithLifecycle()
    val profileCategory by viewModel.profileCategory.collectAsStateWithLifecycle()
    val profileAvatarUrl by viewModel.profileAvatarUrl.collectAsStateWithLifecycle()
    val prefNotifLikes by viewModel.prefNotifLikes.collectAsStateWithLifecycle()
    val prefNotifComments by viewModel.prefNotifComments.collectAsStateWithLifecycle()
    val prefNotifChats by viewModel.prefNotifChats.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(VidstaTab.FEED) }
    var isCreatorStudioOpen by remember { mutableStateOf(false) }

    // Glow theme values (Deep Slate Charcoal vs Cyberpunk lights)
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF070B13),
                Color(0xFF0C111C),
                Color(0xFF0F1626)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                VidstaHeader(
                    currentTab = currentTab,
                    onChatIconClicked = { currentTab = VidstaTab.SECURE_CHAT }
                )
            },
            bottomBar = {
                VidstaNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            },
            floatingActionButton = {
                if (currentTab == VidstaTab.FEED || currentTab == VidstaTab.BOARDS) {
                    FloatingActionButton(
                        onClick = { isCreatorStudioOpen = true },
                        containerColor = Color(0xFFFF007F), // Glowing Magenta Hot Fab
                        contentColor = Color.White,
                        modifier = Modifier
                            .testTag("fab_creator_studio")
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Open Creator Studio"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                // Navigation Content Handler
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "tab_switch_animation"
                ) { targetTab ->
                    when (targetTab) {
                        VidstaTab.FEED -> {
                            FeedScreen(
                                posts = posts,
                                stories = stories,
                                profileName = profileName,
                                onLikePost = { viewModel.likePost(it) },
                                onCommentAdd = { postId, text -> 
                                    viewModel.addCommentToPost(postId, profileName, text) 
                                },
                                onStoryClick = { viewModel.startViewingStory(it) },
                                onAddStoryClick = { isCreatorStudioOpen = true },
                                getComments = { viewModel.getComments(it) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        VidstaTab.BOARDS -> {
                            BoardsScreen(
                                boards = boards,
                                selectedBoardId = selectedBoardId,
                                boardPins = boardPins,
                                onBoardSelected = { viewModel.selectBoard(it) },
                                onAddPin = { boardId, text, img -> viewModel.addPinToBoard(boardId, text, img) },
                                onAddBoard = { title, desc, icon -> viewModel.createBoard(title, desc, icon) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        VidstaTab.SECURE_CHAT -> {
                            SecureChatScreen(
                                chatMessages = chatMessages,
                                currentPartner = selectedChatPartner,
                                onPartnerSelected = { viewModel.selectChatPartner(it) },
                                onSendMessage = { text, isEnc -> viewModel.sendChatMessage(text, isEnc) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        VidstaTab.PROFILE -> {
                            ProfileScreen(
                                posts = posts,
                                stories = stories,
                                profileName = profileName,
                                profileBio = profileBio,
                                profileAvatarColor = profileAvatarColorVal,
                                profileCategory = profileCategory,
                                profileAvatarUrl = profileAvatarUrl,
                                prefNotifLikes = prefNotifLikes,
                                prefNotifComments = prefNotifComments,
                                prefNotifChats = prefNotifChats,
                                onUpdateProfile = { name, bio, color, cat, avUrl ->
                                    viewModel.updateProfile(name, bio, color, cat, avUrl)
                                },
                                onUpdateNotifications = { likes, comments, chats ->
                                    viewModel.updateNotificationSettings(likes, comments, chats)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Story Fullscreen Overlay View Dialog
        viewingStory?.let { story ->
            StoryOverlayDialog(
                story = story,
                onDismiss = { viewModel.stopViewingStory() }
            )
        }

        // Creator & Editor Studio Modal Sheet
        if (isCreatorStudioOpen) {
            CreatorStudioDialog(
                onDismiss = { isCreatorStudioOpen = false },
                onPublishPost = { caption, filter, resolution, mediaUrl, mediaType, isPrivate ->
                    viewModel.createPost(caption, filter, resolution, mediaUrl, mediaType, isPrivate)
                    isCreatorStudioOpen = false
                },
                onPublishStory = { mediaUrl ->
                    viewModel.createStory(mediaUrl)
                    isCreatorStudioOpen = false
                }
            )
        }
    }
}

// ------------------------------------------------------------------------
// HEADER & NAVIGATION
// ------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VidstaHeader(
    currentTab: VidstaTab,
    onChatIconClicked: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Sleek camera logo tag
                Icon(
                    imageVector = Icons.Default.FilterCenterFocus,
                    contentDescription = null,
                    tint = Color(0xFFFF007F), // Hot glowing magenta
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "V I D S T A",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 4.sp,
                    color = Color.White
                )
            }
        },
        actions = {
            if (currentTab != VidstaTab.SECURE_CHAT) {
                IconButton(
                    onClick = onChatIconClicked,
                    modifier = Modifier.testTag("action_chat")
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Open Encrypted Chat",
                            tint = Color(0xFF00F0FF) // Cyber electric cyan
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFFFE600), CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF070B13).copy(alpha = 0.9f),
            titleContentColor = Color.White
        ),
        modifier = Modifier.border(
            width = 0.5.dp,
            color = Color.White.copy(alpha = 0.08f),
            shape = RoundedCornerShape(0.dp)
        )
    )
}

@Composable
fun VidstaNavigationBar(
    currentTab: VidstaTab,
    onTabSelected: (VidstaTab) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF070B13).copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(0.dp)
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        VidstaTab.values().forEach { tab ->
            val selected = (currentTab == tab)
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) Color(0xFFFF007F) else Color.White.copy(alpha = 0.6f)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFFF007F).copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}

// ------------------------------------------------------------------------
// SOCIAL FEED SCREEN & STORY SECTION
// ------------------------------------------------------------------------

@Composable
fun FeedScreen(
    posts: List<PostEntity>,
    stories: List<StoryEntity>,
    profileName: String,
    onLikePost: (PostEntity) -> Unit,
    onCommentAdd: (Long, String) -> Unit,
    onStoryClick: (StoryEntity) -> Unit,
    onAddStoryClick: () -> Unit,
    getComments: (String) -> List<PostComment>,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf("MOMENTS") } // "MOMENTS" or "SHORTS"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("feed_lazy_column"),
        contentPadding = PaddingValues(bottom = 72.dp)
    ) {
        // Daily Stories Slider Component
        item {
            StoryTraySection(
                stories = stories,
                onStoryClick = onStoryClick,
                onAddStoryClick = onAddStoryClick
            )
        }

        // Sub tab navigation selector row matching high-quality interactive UI norms
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { selectedSubTab = "MOMENTS" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSubTab == "MOMENTS") Color(0xFFFF007F) else Color.Transparent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Dashboard, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Moments Feed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { selectedSubTab = "SHORTS" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedSubTab == "SHORTS") Color(0xFF00F0FF) else Color.Transparent,
                        contentColor = if (selectedSubTab == "SHORTS") Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.FlashOn, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Vidsta Shorts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Display targeted media entities matching selected layout
        val filteredPosts = if (selectedSubTab == "MOMENTS") {
            posts.filter { it.mediaType != "short" }
        } else {
            posts.filter { it.mediaType == "short" }
        }

        if (filteredPosts.isEmpty()) {
            item {
                if (selectedSubTab == "MOMENTS") {
                    EmptyStateCard(
                        title = "No Moments Posted",
                        desc = "Tap + to capture and broadcast your daily high-quality highlights!"
                    )
                } else {
                    EmptyStateCard(
                        title = "No Shorts Created Yet",
                        desc = "Tap + -> SHORT to publish a vertical cinematic 720p loop!"
                    )
                }
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                if (selectedSubTab == "MOMENTS") {
                    PostCardItem(
                        post = post,
                        profileName = profileName,
                        onLike = { onLikePost(post) },
                        onCommentSubmit = { text -> onCommentAdd(post.id, text) },
                        comments = getComments(post.commentsJson)
                    )
                } else {
                    ShortCardItem(
                        post = post,
                        profileName = profileName,
                        onLike = { onLikePost(post) },
                        onCommentSubmit = { text -> onCommentAdd(post.id, text) },
                        comments = getComments(post.commentsJson)
                    )
                }
            }
        }
    }
}

// Horizontal tray representing ongoing daily stories
@Composable
fun StoryTraySection(
    stories: List<StoryEntity>,
    onStoryClick: (StoryEntity) -> Unit,
    onAddStoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Color.White.copy(alpha = 0.02f))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "DAILY STORIES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // "Add Story" Slot First
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onAddStoryClick() }
                        .testTag("add_story_action")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .border(2.dp, Color(0xFFFF007F), CircleShape)
                            .padding(3.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "New Story",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "New Story",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Real Story list items
            items(stories) { story ->
                val gradientRing = if (story.isViewed) {
                    Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.2f)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF00F0FF)))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onStoryClick(story) }
                        .testTag("story_bubble_${story.id}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .drawBehind {
                                drawCircle(
                                    brush = gradientRing,
                                    radius = size.minDimension / 2f,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )
                            }
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Render a beautiful icon avatar derived from user name
                            val initials = if (story.authorName.length >= 2) {
                                story.authorName.substring(0, 2).uppercase()
                            } else {
                                "JD"
                            }
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = story.authorName,
                        fontSize = 12.sp,
                        color = if (story.isViewed) Color.White.copy(alpha = 0.5f) else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(68.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Beautiful individual card post item
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PostCardItem(
    post: PostEntity,
    profileName: String,
    onLike: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    comments: List<PostComment>
) {
    var doubleTapHeartVisible by remember { mutableStateOf(false) }
    var commentsSectionOpen by remember { mutableStateOf(false) }
    var commentInputText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val authorDisplayName = if (post.authorName == "Me") profileName else post.authorName

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("post_card_${post.id}")
    ) {
        Column {
            // Post card header details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Circular user avatar simulator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFFF007F).copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color(0xFFFF007F).copy(alpha = 0.5f), CircleShape)
                ) {
                    Text(
                        text = authorDisplayName.take(2).uppercase(),
                        color = Color(0xFFFF007F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = authorDisplayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "A moments ago",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (post.isPrivate) {
                            Surface(
                                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                contentColor = Color(0xFFFFD700),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Private",
                                        modifier = Modifier.size(9.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Private", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFF00FF88).copy(alpha = 0.15f),
                                contentColor = Color(0xFF00FF88),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Published",
                                        modifier = Modifier.size(9.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Published", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Premium Quality Quality Indicator Tag
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFF007F), Color(0xFF8000FF))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = post.resolution,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Post Visual Image Box with Filters Simulated
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFF0F172A))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                onLike()
                                doubleTapHeartVisible = true
                                coroutineScope.launch {
                                    delay(900)
                                    doubleTapHeartVisible = false
                                }
                            }
                        )
                    }
            ) {
                // Async image fallback helper
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Visual Story Element",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = android.R.drawable.ic_menu_gallery)
                )

                // Simulated LIVE Filters Applied (Colortint blocks matching user filter choice)
                when (post.filterApplied) {
                    "Cyberpunk" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFF007F).copy(alpha = 0.15f))
                        )
                    }
                    "Retro Glow" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFFA500).copy(alpha = 0.12f))
                        )
                    }
                    "Emerald Hue" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF00FF7F).copy(alpha = 0.15f))
                        )
                    }
                    "Chrome Flare" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF00FFFF).copy(alpha = 0.12f))
                        )
                    }
                    "720p Cinematic" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF008080).copy(alpha = 0.18f))
                        )
                    }
                }

                // If media is a video, draw video playback overlay simulator
                if (post.mediaType == "video") {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f))
                    ) {
                        // Top watermark "Premium Video Player"
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "720p HD STREAMING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F0FF)
                            )
                        }

                        // Play/Pause Overlay indicator
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = "Video playing visual",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.Center)
                        )

                        // Bottom progress/volume controllers
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "0:14 / 0:30",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Simulated slide line
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.5.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.46f)
                                        .height(3.dp)
                                        .background(Color(0xFF00F0FF), RoundedCornerShape(1.5.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Audio status",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Filter Watermark Tag
                if (post.filterApplied != "Normal" && post.filterApplied.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ ${post.filterApplied.uppercase()}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF007F)
                        )
                    }
                }

                // Animated Heart Double-Tap Burst
                androidx.compose.animation.AnimatedVisibility(
                    visible = doubleTapHeartVisible,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Liked!",
                        tint = Color(0xFFFF007F),
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            // Post Details & Action Panel
            Column(modifier = Modifier.padding(12.dp)) {
                // Likes & Options Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onLike,
                            modifier = Modifier.testTag("post_like_button_${post.id}")
                        ) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like Post",
                                tint = if (post.isLiked) Color(0xFFFF007F) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "${post.likesCount} likes",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(onClick = { commentsSectionOpen = !commentsSectionOpen }) {
                            Icon(
                                imageVector = Icons.Default.Comment,
                                contentDescription = "Show/Hide Comments",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "${comments.size}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }

                    Row {
                        IconButton(onClick = { /* Share Simulation */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share post",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Caption Title
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = post.caption,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }

                // Inline quick comments listing section
                if (comments.isNotEmpty()) {
                    Text(
                        text = "View all ${comments.size} comments",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { commentsSectionOpen = !commentsSectionOpen }
                            .padding(vertical = 4.dp)
                    )

                    // Render top 2 comments directly
                    comments.take(2).forEach { comm ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = comm.authorName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = comm.text,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                // Collapsible Full Comments Thread Panel
                AnimatedVisibility(
                    visible = commentsSectionOpen,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "COMMENTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF007F)
                        )

                        // All comments
                        comments.forEach { comm ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = comm.authorName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00F0FF),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = comm.text,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }

                        // Send comment field
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = commentInputText,
                                onValueChange = { commentInputText = it },
                                placeholder = { Text("Write encrypted reply...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF007F),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = TextStyle(fontSize = 12.sp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("comment_field_${post.id}")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    if (commentInputText.isNotBlank()) {
                                        onCommentSubmit(commentInputText)
                                        commentInputText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("comment_submit_${post.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Submit Comment",
                                    tint = Color(0xFFFF007F),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShortCardItem(
    post: PostEntity,
    profileName: String,
    onLike: () -> Unit,
    onCommentSubmit: (String) -> Unit,
    comments: List<PostComment>
) {
    var commentsSectionOpen by remember { mutableStateOf(false) }
    var commentInputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val authorDisplayName = if (post.authorName == "Me") profileName else post.authorName

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1524)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .border(
                width = 1.dp,
                color = Color(0xFF00F0FF).copy(alpha = 0.25f), // Glowing Cyan border for shorts!
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                // Background Unsplash Vertical Image Loop simulator
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = "Vertical Short Loop",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark vignetting gradients to make text and metrics stand out clearly
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Central subtle Pulsing Play indicator overlay
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Playing short loop",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.Center)
                        )
                    }
                    // Short badge label
                    Box(
                        modifier = Modifier
                            .padding(top = 90.dp)
                            .background(Color(0xFF00F0FF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("720p CINEMATIC LOOP", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Top visual watermark
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "V I D S T A  S H O R T",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                // LUT filter style tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("LUT: ${post.filterApplied}", fontSize = 9.sp, color = Color(0xFF00F0FF), fontWeight = FontWeight.SemiBold)
                }

                // Bottom Left Details Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Avatar with vibrant ring
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.sweepGradient(listOf(Color(0xFFFF007F), Color(0xFF00F0FF), Color(0xFFFF007F))),
                                    CircleShape
                                )
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F141F))
                        ) {
                            Text(
                                text = authorDisplayName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "@$authorDisplayName",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = post.caption,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Vertical Floating Action Metrics on the Bottom Right
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Like button overlay
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onLike,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like Short",
                                tint = if (post.isLiked) Color(0xFFFF007F) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "${post.likesCount}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Comments button overlay
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = { commentsSectionOpen = !commentsSectionOpen },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Show Comments",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "${comments.size}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Expandable Comment tray matching standard feed card
            if (commentsSectionOpen) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Encrypted Thread Comments (${comments.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00F0FF),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    comments.forEach { comment ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "${comment.authorName}: ",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                            Text(
                                text = comment.text,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = commentInputText,
                            onValueChange = { commentInputText = it },
                            placeholder = { Text("Comment securely...", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            textStyle = TextStyle(fontSize = 11.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00F0FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (commentInputText.isNotBlank()) {
                                    onCommentSubmit(commentInputText)
                                    commentInputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF00F0FF), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// INSTANT PHOTO & VIDEO CREATOR STUDIO
// ------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorStudioDialog(
    onDismiss: () -> Unit,
    onPublishPost: (caption: String, filter: String, resolution: String, mediaUrl: String, mediaType: String, isPrivate: Boolean) -> Unit,
    onPublishStory: (mediaUrl: String) -> Unit
) {
    var captionText by remember { mutableStateOf("") }
    var isPrivatePost by remember { mutableStateOf(false) }
    
    // Default Selected Preset image is the first Unsplash option
    var selectedPresetIndex by remember { mutableStateOf(0) }
    var selectedCreateType by remember { mutableStateOf("MOMENT") } // "MOMENT", "SHORT", "STORY"
    var isVideoType by remember { mutableStateOf(false) } // Photo vs Video
    
    // Sliders simulated changes
    var brightnessStrength by remember { mutableStateOf(0.5f) }
    var contrastStrength by remember { mutableStateOf(0.5f) }
    var saturationStrength by remember { mutableStateOf(0.5f) }
    
    // Selected Filter preset
    var selectedFilter by remember { mutableStateOf("Normal") }
    val filtersList = listOf("Normal", "Cyberpunk", "Retro Glow", "Emerald Hue", "Chrome Flare", "720p Cinematic")
    
    // Resolution Preset Choice
    var selectedResolution by remember { mutableStateOf("720p HD Premium") }
    val resolutionOptions = listOf("720p HD Premium", "1080p HQ Full Frame", "4K Mastering")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141F)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(top = 16.dp)
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header of Modal
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "V I D S T A  S T U D I O",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF007F),
                        letterSpacing = 2.sp
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Creator",
                            tint = Color.White
                        )
                    }
                }

                // Type Tab: Moment vs Short vs Story
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { selectedCreateType = "MOMENT" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCreateType == "MOMENT") Color(0xFFFF007F) else Color.Transparent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_create_post"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("POST MOMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { selectedCreateType = "SHORT" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCreateType == "SHORT") Color(0xFF00F0FF) else Color.Transparent,
                            contentColor = if (selectedCreateType == "SHORT") Color.Black else Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_create_short"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("POST SHORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { selectedCreateType = "STORY" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedCreateType == "STORY") Color(0xFFFFD700) else Color.Transparent,
                            contentColor = if (selectedCreateType == "STORY") Color.Black else Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_create_story"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("ADD STORY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Mediatype Selector (Only for general Feeds MOMENT)
                if (selectedCreateType == "MOMENT") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isVideoType = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isVideoType) Color(0xFF00F0FF) else Color(0xFF1E293B),
                                contentColor = if (!isVideoType) Color.Black else Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Camera Photo")
                        }

                        Button(
                            onClick = { isVideoType = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVideoType) Color(0xFF00F0FF) else Color(0xFF1E293B),
                                contentColor = if (isVideoType) Color.Black else Color.White
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Videocam, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Premium Video")
                        }
                    }
                }

                // 1. Select Preset Scene Graphic
                Text(
                    text = "SELECT CREATIVE BACKDROP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(PRESET_IMAGES.size) { index ->
                        val item = PRESET_IMAGES[index]
                        val isSelected = index == selectedPresetIndex
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFF007F) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { selectedPresetIndex = index }
                                .width(94.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = Color(0xFFFF007F),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AsyncImage(
                                    model = item.url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                )
                                Text(
                                    text = item.name.split(" ").last(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // LIVE PREVIEW BLOCK WITH SIMULATED FILTERS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF070B13), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = PRESET_IMAGES[selectedPresetIndex].url,
                        contentDescription = "Visual Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Filter Tints
                    when (selectedFilter) {
                        "Cyberpunk" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFF007F).copy(alpha = 0.15f)))
                        "Retro Glow" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFA500).copy(alpha = 0.12f)))
                        "Emerald Hue" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF00FF7F).copy(alpha = 0.15f)))
                        "Chrome Flare" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF00FFFF).copy(alpha = 0.12f)))
                        "720p Cinematic" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF008080).copy(alpha = 0.18f)))
                    }

                    // Brightness simulator overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.White.copy(
                                    alpha = if (brightnessStrength > 0.5f) (brightnessStrength - 0.5f) * 0.3f else 0f
                                )
                            )
                            .background(
                                Color.Black.copy(
                                    alpha = if (brightnessStrength < 0.5f) (0.5f - brightnessStrength) * 0.6f else 0f
                                )
                            )
                    )

                    // Video shutter overlay
                    if (isVideoType && selectedCreateType == "MOMENT") {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                        )
                    }

                    // Filter applied tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(30.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Filter: $selectedFilter",
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Filter Presets Selection Row
                Text(
                    text = "APPLY CINEMATIC LUT FILTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    items(filtersList) { filter ->
                        val isSelected = filter == selectedFilter
                        AssistChip(
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) Color(0xFFFF007F) else Color.Transparent,
                                labelColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                // 3. Sliders: Brightness, Contrast, Saturation
                Text(
                    text = "MANUAL IMAGE EXPOSURE CORRECTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Brightness slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Brightness", fontSize = 12.sp, color = Color.White, modifier = Modifier.width(80.dp))
                        Slider(
                            value = brightnessStrength,
                            onValueChange = { brightnessStrength = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(String.format("%.1f", brightnessStrength * 2f), fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }

                    // Contrast simulator slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Contrast", fontSize = 12.sp, color = Color.White, modifier = Modifier.width(80.dp))
                        Slider(
                            value = contrastStrength,
                            onValueChange = { contrastStrength = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(String.format("%.1f", contrastStrength * 2f), fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }

                    // Saturation simulator slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Saturation", fontSize = 12.sp, color = Color.White, modifier = Modifier.width(80.dp))
                        Slider(
                            value = saturationStrength,
                            onValueChange = { saturationStrength = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(String.format("%.1f", saturationStrength * 2f), fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }

                // 4. Resolution Switch Choice (Explicitly shows 720p option is default premium optimized)
                if (selectedCreateType == "MOMENT") {
                    Text(
                        text = "EXPORT ENCODING SPEC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        resolutionOptions.forEach { resol ->
                            val isSelected = resol == selectedResolution
                            val specColor = if (resol.contains("720p")) Color(0xFF00F0FF) else Color.White.copy(alpha = 0.6f)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFFF007F).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.04f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedResolution = resol }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) Color(0xFFFF007F) else Color.White.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(resol.split(" ").first(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = specColor)
                                    Text(
                                        text = if (resol.contains("720p")) "Premium Fast" else "Heavy",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                } else if (selectedCreateType == "SHORT") {
                    Text(
                        text = "EXPORT ENCODING SPEC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF00F0FF).copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, null, tint = Color(0xFF00F0FF), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("720p Loop HD (Optimized)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text("Seamless looping rendering preset with cinematic vintage flow enabled.", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Short Caption Detail
                if (selectedCreateType != "STORY") {
                    Text(
                        text = "WRITE YOUR EXPRESSION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        placeholder = { Text("What details are you capturing today? #720pStudio") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF007F),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("post_caption_input")
                    )
                }

                // Publication Privacy Section
                if (selectedCreateType != "STORY") {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "PUBLICATION PRIVACY MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isPrivatePost = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isPrivatePost) Color(0xFF00FF88).copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (!isPrivatePost) Color(0xFF00FF88) else Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = if (!isPrivatePost) Color(0xFF00FF88) else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Published (Public)",
                                fontSize = 11.sp,
                                color = if (!isPrivatePost) Color(0xFF00FF88) else Color.White.copy(alpha = 0.6f)
                            )
                        }

                        Button(
                            onClick = { isPrivatePost = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPrivatePost) Color(0xFFFFD700).copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isPrivatePost) Color(0xFFFFD700) else Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isPrivatePost) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Private (Only Me)",
                                fontSize = 11.sp,
                                color = if (isPrivatePost) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Final Publish trigger
                Button(
                    onClick = {
                        val mediaUrl = PRESET_IMAGES[selectedPresetIndex].url
                        when (selectedCreateType) {
                            "STORY" -> {
                                onPublishStory(mediaUrl)
                            }
                            "SHORT" -> {
                                onPublishPost(captionText, selectedFilter, "720p Loop HD", mediaUrl, "short", isPrivatePost)
                            }
                            else -> {
                                val userMediaType = if (isVideoType) "video" else "image"
                                onPublishPost(captionText, selectedFilter, selectedResolution, mediaUrl, userMediaType, isPrivatePost)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (selectedCreateType) {
                            "SHORT" -> Color(0xFF00F0FF)
                            "STORY" -> Color(0xFFFFD700)
                            else -> Color(0xFFFF007F)
                        },
                        contentColor = if (selectedCreateType == "MOMENT") Color.White else Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("publish_action_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (selectedCreateType) {
                            "STORY" -> "PUBLISH DAILY STORY"
                            "SHORT" -> "SHARE SEAMLESS SHORT VIDEO"
                            else -> "SHARE PREMIUM MOMENT"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// Fullscreen Story modal slider viewer with active auto progression line
@Composable
fun StoryOverlayDialog(
    story: StoryEntity,
    onDismiss: () -> Unit
) {
    var progressCount by remember { mutableStateOf(0f) }
    
    // Auto increment progress effect
    LaunchedEffect(key1 = story) {
        progressCount = 0f
        while (progressCount < 1f) {
            delay(40)
            progressCount += 0.01f
        }
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            // Fullscreen story image
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = "Full Story Visual",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top action bar overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Progress timeline line bar indicator
                LinearProgressIndicator(
                    progress = { progressCount },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color(0xFF00F0FF),
                    trackColor = Color.White.copy(alpha = 0.25f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Creator avatar details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF00F0FF).copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, Color(0xFF00F0FF), CircleShape)
                        ) {
                            Text(
                                text = story.authorName.take(2).uppercase(),
                                color = Color(0xFF00F0FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = story.authorName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE STORY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF007F),
                            modifier = Modifier
                                .background(Color(0xFFFF007F).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Dismiss Story", tint = Color.White)
                    }
                }
            }

            // Friendly dismiss tip label at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(30.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Tap anywhere to skip",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ------------------------------------------------------------------------
// COLLABORATIVE COMMUNITY BOARDS TAB
// ------------------------------------------------------------------------

@Composable
fun BoardsScreen(
    boards: List<BoardEntity>,
    selectedBoardId: Long?,
    boardPins: List<BoardPinEntity>,
    onBoardSelected: (Long?) -> Unit,
    onAddPin: (Long, String, String?) -> Unit,
    onAddBoard: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isNewBoardDialogOpen by remember { mutableStateOf(false) }
    var selectPinPresetIndex by remember { mutableStateOf(0) }
    var pinText by remember { mutableStateOf("") }

    if (selectedBoardId != null) {
        // Render Active Board detail collaboration logs
        val activeBoard = boards.find { it.id == selectedBoardId }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Small Back Button
            TextButton(
                onClick = { onBoardSelected(null) },
                modifier = Modifier.testTag("back_to_boards_list")
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF00F0FF))
                Spacer(modifier = Modifier.width(6.dp))
                Text("BACK TO BOARDS", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold)
            }

            activeBoard?.let { board ->
                Text(
                    text = "#${board.title}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Founded by ${board.creatorName}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                Text(
                    text = board.description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Write Pin Form Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161E2E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "COLLABORATE - PIN NEW WORK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF007F)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            PRESET_IMAGES.take(3).forEachIndexed { idx, preset ->
                                val isSelected = idx == selectPinPresetIndex
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = Color(0xFFFF007F),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { selectPinPresetIndex = idx }
                                ) {
                                    AsyncImage(preset.url, null, contentScale = ContentScale.Crop)
                                }
                            }
                            Text(
                                text = "Choose Pin Backdrop preset",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = pinText,
                                onValueChange = { pinText = it },
                                placeholder = { Text("What are we pinning to the collaborative board?") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pin_input_field"),
                                textStyle = TextStyle(fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFF007F),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (pinText.isNotBlank()) {
                                        onAddPin(
                                            board.id,
                                            pinText,
                                            PRESET_IMAGES[selectPinPresetIndex].url
                                        )
                                        pinText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFFF007F), CircleShape)
                                    .testTag("pin_submit_btn")
                            ) {
                                Icon(Icons.Default.PushPin, "Pin work", tint = Color.White)
                            }
                        }
                    }
                }

                // Pins thread Lazy list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("board_pins_lazy_column")
                ) {
                    if (boardPins.isEmpty()) {
                        item {
                            EmptyStateCard(title = "No pins yet", desc = "Be the first who contributes a tip or image!")
                        }
                    } else {
                        items(boardPins) { pin ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111721)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Color(0xFF00F0FF), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = pin.authorName.take(1).uppercase(),
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = pin.authorName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.PushPin, null, tint = Color(0xFFFF007F), modifier = Modifier.size(12.dp))
                                    }

                                    Text(
                                        text = pin.contentText,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )

                                    pin.mediaUrl?.let { mUrl ->
                                        AsyncImage(
                                            model = mUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Render Boards catalog index page
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "COMMUNITY BOARDS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Collaborate on projects and visual tutorials",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                Button(
                    onClick = { isNewBoardDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    modifier = Modifier.testTag("action_new_board")
                ) {
                    Icon(Icons.Default.GroupAdd, null, tint = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text("create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("boards_list")
            ) {
                items(boards) { board ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBoardSelected(board.id) }
                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .testTag("board_card_${board.id}")
                    ) {
                        Row(modifier = Modifier.padding(14.dp)) {
                            // Categorized Icon holder
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFFF007F).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            ) {
                                // Dynamic board icon representer
                                val boardIcon = when (board.iconName) {
                                    "videocam" -> Icons.Default.Videocam
                                    "restaurant" -> Icons.Default.Restaurant
                                    else -> Icons.Default.PhotoCamera
                                }
                                Icon(
                                    imageVector = boardIcon,
                                    contentDescription = null,
                                    tint = Color(0xFFFF007F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "#${board.title}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = board.description,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, null, tint = Color(0xFF00F0FF), modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Founded by ${board.creatorName}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00F0FF)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "View Pins",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }

    // New Board Creator dialog
    if (isNewBoardDialogOpen) {
        var nTitle by remember { mutableStateOf("") }
        var nDesc by remember { mutableStateOf("") }
        val iconsList = listOf("photo_camera", "videocam", "restaurant")
        var selectedIconIndex by remember { mutableStateOf(0) }

        AlertDialog(
            onDismissRequest = { isNewBoardDialogOpen = false },
            title = { Text("CREATIVE FORUM BOARD FOUNDER", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF007F)) },
            containerColor = Color(0xFF0F1420),
            confirmButton = {
                Button(
                    onClick = {
                        if (nTitle.isNotBlank()) {
                            onAddBoard(nTitle, nDesc, iconsList[selectedIconIndex])
                            isNewBoardDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F))
                ) {
                    Text("START BOARD")
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewBoardDialogOpen = false }) {
                    Text("CANCEL", color = Color.White)
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = nTitle,
                        onValueChange = { nTitle = it },
                        placeholder = { Text("e.g. TravelHues") },
                        label = { Text("Board Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_board_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nDesc,
                        onValueChange = { nDesc = it },
                        placeholder = { Text("Explain what community members collaborate on...") },
                        label = { Text("Brief Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_board_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(12.dp))

                    Text("SELECT BOARD ICON", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        iconsList.forEachIndexed { idx, iName ->
                            val isSelected = idx == selectedIconIndex
                            val iconToDraw = when (iName) {
                                "videocam" -> Icons.Default.Videocam
                                "restaurant" -> Icons.Default.Restaurant
                                else -> Icons.Default.PhotoCamera
                            }
                            IconButton(
                                onClick = { selectedIconIndex = idx },
                                modifier = Modifier
                                    .background(
                                        if (isSelected) Color(0xFFFF007F) else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(iconToDraw, null, tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        )
    }
}

// ------------------------------------------------------------------------
// END-TO-END ENCRYPTED SECURE MESSAGING TAB
// ------------------------------------------------------------------------

@Composable
fun SecureChatScreen(
    chatMessages: List<MessageEntity>,
    currentPartner: String,
    onPartnerSelected: (String) -> Unit,
    onSendMessage: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputMsgText by remember { mutableStateOf("") }
    var e2eeEnabled by remember { mutableStateOf(true) } // encryption switcher simulator

    val chatPartners = listOf("Elena Rostova", "Marcus Chen", "Chloe Baker", "Vidsta Secure Hub")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Cryptographic status header badge bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1D2D)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .border(
                    width = 0.5.dp,
                    color = Color(0xFF00F0FF).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LockClock,
                    contentDescription = null,
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SECURE PROTOCOL ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00F0FF)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF00FF7F), CircleShape)
                        )
                    }
                    Text(
                        text = "Encrypted side-by-side using local asymmetric ECDSA key verification.",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Switch to toggle encryption
                Switch(
                    checked = e2eeEnabled,
                    onCheckedChange = { e2eeEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00F0FF),
                        checkedTrackColor = Color(0xFF00F0FF).copy(alpha = 0.3f)
                    )
                )
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // Contacts Selector rail on left (shows active contact partners)
            Column(
                modifier = Modifier
                    .width(76.dp)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                chatPartners.forEach { partner ->
                    val isSelected = partner == currentPartner
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onPartnerSelected(partner) }
                            .testTag("chat_partner_$partner")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (isSelected) Color(0xFFFF007F) else Color.White.copy(alpha = 0.1f),
                                    CircleShape
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = Color(0xFFFF007F),
                                    shape = CircleShape
                                )
                        ) {
                            Text(
                                text = partner.take(2).uppercase(),
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = partner.split(" ").first(),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Real conversational messages list panel
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26).copy(alpha = 0.7f)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Chat header info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentPartner,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (e2eeEnabled) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Verified Key",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "LOCKED",
                                fontSize = 9.sp,
                                color = Color(0xFF00F0FF),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "UNSECURED",
                                fontSize = 9.sp,
                                color = Color(0xFFFF007F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Conversation lazy list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("chat_messages_lazy"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val sentByMe = msg.senderName == "Me"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (sentByMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = if (sentByMe) {
                                                Brush.horizontalGradient(listOf(Color(0xFFFF007F), Color(0xFF8A2BE2)))
                                            } else {
                                                Brush.horizontalGradient(listOf(Color(0xFF262E3D), Color(0xFF1E2633)))
                                            },
                                            shape = RoundedCornerShape(
                                                topStart = 14.dp,
                                                topEnd = 14.dp,
                                                bottomStart = if (sentByMe) 14.dp else 0.dp,
                                                bottomEnd = if (sentByMe) 0.dp else 14.dp
                                            )
                                        )
                                        .border(
                                            width = 0.5.dp,
                                            color = if (sentByMe) Color(0xFFFF007F).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(
                                                topStart = 14.dp,
                                                topEnd = 14.dp,
                                                bottomStart = if (sentByMe) 14.dp else 0.dp,
                                                bottomEnd = if (sentByMe) 0.dp else 14.dp
                                            )
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .widthIn(max = 180.dp)
                                ) {
                                    Column {
                                        // Plain vs Cryptographic sign indicator
                                        if (msg.isEncrypted && e2eeEnabled) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EnhancedEncryption,
                                                    contentDescription = null,
                                                    tint = Color(0xFF00F0FF),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(Modifier.width(3.dp))
                                                Text(
                                                    text = "AES_256",
                                                    fontSize = 8.sp,
                                                    color = Color(0xFF00F0FF),
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Text(
                                            text = msg.content,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Quick Send panel box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputMsgText,
                            onValueChange = { inputMsgText = it },
                            placeholder = { Text("Encrypted text...") },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("chat_input_field"),
                            textStyle = TextStyle(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00F0FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            )
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                if (inputMsgText.isNotBlank()) {
                                    onSendMessage(inputMsgText, e2eeEnabled)
                                    inputMsgText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF00F0FF), CircleShape)
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send secure line",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// USER PROFILE SCREEN
// ------------------------------------------------------------------------

@Composable
fun ProfileScreen(
    posts: List<PostEntity>,
    stories: List<StoryEntity>,
    profileName: String,
    profileBio: String,
    profileAvatarColor: Int,
    profileCategory: String,
    profileAvatarUrl: String,
    prefNotifLikes: Boolean,
    prefNotifComments: Boolean,
    prefNotifChats: Boolean,
    onUpdateProfile: (name: String, bio: String, color: Int, category: String, avatarUrl: String) -> Unit,
    onUpdateNotifications: (likes: Boolean, comments: Boolean, chats: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val myPosts = posts.filter { it.authorName == "Me" }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPrefsDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(profileName) }
    var editBio by remember { mutableStateOf(profileBio) }
    var selectedColor by remember { mutableStateOf(profileAvatarColor) }
    var editCategory by remember { mutableStateOf(profileCategory) }
    var editAvatarUrl by remember { mutableStateOf(profileAvatarUrl) }

    // Synchronize editing variables whenever dialog opens
    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            editName = profileName
            editBio = profileBio
            selectedColor = profileAvatarColor
            editCategory = profileCategory
            editAvatarUrl = profileAvatarUrl
        }
    }

    val primaryColor = remember(profileAvatarColor) { Color(profileAvatarColor) }
    val gradientBrush = remember(primaryColor) {
        Brush.sweepGradient(listOf(primaryColor, Color(0xFFFF007F), primaryColor))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Brief Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Custom Profile Graphic Glow using selected color profile
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .background(gradientBrush, CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F141F))
                    ) {
                        if (profileAvatarUrl.isNotEmpty()) {
                            AsyncImage(
                                model = profileAvatarUrl,
                                contentDescription = "Profile Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = profileName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    // Media counters row
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.weight(1f)
                    ) {
                        CounterBox(count = "${posts.size}", label = "Posts")
                        CounterBox(count = "${stories.size}", label = "Stories")
                        CounterBox(count = "4.2K", label = "Followers")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profileName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Specialty Category Badge indicator
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF007F).copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                            .border(0.5.dp, Color(0xFFFF007F).copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = profileCategory.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF007F),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Text(
                    text = profileBio,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("EDIT PROFILE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showPrefsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PREFERENCES", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid Title of Pinned uploads
        Text(
            text = "MY RECENT CAPTURES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF007F),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (myPosts.isEmpty()) {
            EmptyStateCard(
                title = "No custom captures shared",
                desc = "Tap Feed -> + Share to write and upload dynamic premium moments here!"
            )
        } else {
            // Visual dynamic grid layout helper using simple Column Rows
            val chunks = myPosts.chunked(2)
            chunks.forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { mPost ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box {
                                AsyncImage(
                                    model = mPost.mediaUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Overlay quality badge indicators
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(mPost.resolution, fontSize = 8.sp, color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold)
                                }

                                if (mPost.filterApplied != "Normal" && mPost.filterApplied.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                            .background(Color(0xFFFF007F), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(mPost.filterApplied, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Profile Settings Notifications Dialog Panel
    if (showPrefsDialog) {
        Dialog(onDismissRequest = { showPrefsDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1422)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(Icons.Default.Notifications, null, tint = Color(0xFF00F0FF), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NOTIFICATIONS & SETTINGS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00F0FF),
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Manage your real-time secure alerts, communications, and social feed notifications on Vidsta.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Likes toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Likes Alerts", fontSize = 12.sp, color = Color.White)
                        }
                        Switch(
                            checked = prefNotifLikes,
                            onCheckedChange = { onUpdateNotifications(it, prefNotifComments, prefNotifChats) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                        )
                    }

                    // Comments toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Message, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Comment Toggles", fontSize = 12.sp, color = Color.White)
                        }
                        Switch(
                            checked = prefNotifComments,
                            onCheckedChange = { onUpdateNotifications(prefNotifLikes, it, prefNotifChats) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                        )
                    }

                    // Chats toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("E2EE Chat Notifications", fontSize = 12.sp, color = Color.White)
                        }
                        Switch(
                            checked = prefNotifChats,
                            onCheckedChange = { onUpdateNotifications(prefNotifLikes, prefNotifComments, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showPrefsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DONE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Profile Customisation Edit Dialog Panel
    if (showEditDialog) {
        Dialog(
            onDismissRequest = { showEditDialog = false }
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1422)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color(0xFFFF007F).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "CUSTOMISE PROFILE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF007F),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Profile Name Input Box
                    Text(
                        text = "DISPLAY NAME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF007F),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_name")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bio Input Box
                    Text(
                        text = "EXPRESSIVE BIO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF007F),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_bio")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Studio Category Selection
                    Text(
                        text = "STUDIO SPECIALTY CATEGORY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val categories = listOf("Aesthetic Visual Artist", "Cinematic Videographer", "Creative Director", "Cyberpunk Storyteller")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = editCategory == cat
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) Color(0xFFFF007F).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) Color(0xFFFF007F) else Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { editCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 10.sp, color = if (isSel) Color(0xFFFF007F) else Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom DP Avatar presets
                    Text(
                        text = "CHOOSE PORTRAIT PICTURE (DP)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val dpPresets = listOf(
                        "" to "Default text avatar",
                        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150" to "Woman",
                        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150" to "Man",
                        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150" to "Glow Accent",
                        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150" to "Visualist"
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dpPresets.forEach { (url, label) ->
                            val isSelected = editAvatarUrl == url
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFF007F) else Color.White.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    )
                                    .clickable { editAvatarUrl = url },
                                contentAlignment = Alignment.Center
                            ) {
                                if (url.isNotEmpty()) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = editName.take(2).uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Or enter custom Web URL directly:",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = editAvatarUrl,
                        onValueChange = { editAvatarUrl = it },
                        placeholder = { Text("https://example.com/photo.jpg", fontSize = 11.sp, color = Color.White.copy(alpha = 0.3f)) },
                        maxLines = 1,
                        textStyle = TextStyle(fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF007F),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Colour scheme Picker (representing profile glows)
                    Text(
                        text = "GLOW THEME PROFILE ACCENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val colorPresets = listOf(
                        0xFFFF007F.toInt() to "Magenta", // Magenta Hot Pink
                        0xFF00F0FF.toInt() to "Cyan",    // Cyan Blue
                        0xFFFFD700.toInt() to "Gold",    // Gold
                        0xFF00FF88.toInt() to "Emerald", // Emerald Green
                        0xFF8B5CF6.toInt() to "Iris"     // Purple Iris
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colorPresets.forEach { (colorVal, name) ->
                            val isSelected = selectedColor == colorVal
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(colorVal), CircleShape)
                                    .clickable { selectedColor = colorVal }
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dialog Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showEditDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (editName.isNotBlank()) {
                                    onUpdateProfile(editName, editBio, selectedColor, editCategory, editAvatarUrl)
                                    showEditDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007F)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CounterBox(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
    }
}

// ------------------------------------------------------------------------
// EXTRA REUSABLE COMPOSABLES
// ------------------------------------------------------------------------

@Composable
fun EmptyStateCard(title: String, desc: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
