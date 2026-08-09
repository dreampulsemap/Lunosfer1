package io.lunosfer.dreamap.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.lunosfer.dreamap.R
import io.lunosfer.dreamap.supabase.supabaseClient
import io.lunosfer.dreamap.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus

@Composable
fun MainScreen(navController: NavHostController = rememberNavController()) {
    val sessionStatus by supabaseClient.auth.sessionStatus.collectAsState(initial = SessionStatus.Initializing)
    val isLoggedIn = sessionStatus is SessionStatus.Authenticated

    if (sessionStatus is SessionStatus.Initializing) {
        Box(modifier = Modifier.fillMaxSize().background(Void950), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AstralGold)
        }
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showTopBottomBars = currentRoute != Screen.Auth.route

    Scaffold(
        topBar = {
            if (showTopBottomBars) {
                TopBar(
                    isLoggedIn = isLoggedIn,
                    onLoginClick = { navController.navigate(Screen.Auth.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
        },
        bottomBar = {
            if (showTopBottomBars && isLoggedIn) {
                BottomNavBar(navController)
            }
        },
        containerColor = Void950
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Auth.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0)
                    }
                })
            }
            composable(Screen.Home.route) { HomeScreen(onDreamClick = { id -> navController.navigate(Screen.DreamDetail.createRoute(id)) }) }
            composable(Screen.Explore.route) { ExploreScreen() }
            composable(Screen.Vision.route) { VisionScreen() }
            composable(Screen.Messages.route) { MessagesScreen() }
            composable(Screen.CreateDream.route) { CreateDreamScreen(navController) }
            composable(Screen.CreateVision.route) { PlaceholderScreen(stringResource(R.string.nav_new_vision)) }
            composable("dream/{dreamId}", arguments = listOf(androidx.navigation.navArgument("dreamId") { type = androidx.navigation.NavType.LongType })) { backStackEntry -> val dreamId = backStackEntry.arguments?.getLong("dreamId") ?: return@composable; DreamDetailScreen(dreamId = dreamId, onBack = { navController.popBackStack() }) }
            composable(Screen.Profile.route) {
                ProfileScreen(onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0)
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(isLoggedIn: Boolean, onLoginClick: () -> Unit, onProfileClick: (() -> Unit)? = null) {
    var showAuraPopup by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Void950,
            titleContentColor = AstralGold
        ),
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "LUNOSFER",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(listOf(AstralGold, AstralAmber)),
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        fontSize = 20.sp
                    )
                )
            }
        },
        navigationIcon = {
            if (isLoggedIn) {
                Row(modifier = Modifier.padding(start = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Mana pill
                    Surface(
                        shape = CircleShape,
                        color = AetherCyan.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AetherCyan),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = AetherCyan, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("0", color = AetherCyan, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    // Aura pill
                    Surface(
                        shape = CircleShape,
                        color = AstralGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AstralGold),
                        modifier = Modifier.height(28.dp),
                        onClick = { showAuraPopup = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = AstralGold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("0", color = AstralGold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (showAuraPopup) {
                        DropdownMenu(expanded = showAuraPopup, onDismissRequest = { showAuraPopup = false }) {
                            DropdownMenuItem(text = { Text("Your Auras: 0") }, onClick = { showAuraPopup = false })
                            DropdownMenuItem(text = { Text("Buy Aura") }, onClick = { showAuraPopup = false })
                        }
                    }
                }
            }
        },
        actions = {
            if (isLoggedIn) {
                IconButton(onClick = { /* TODO */ }) {
                    BadgedBox(badge = {}) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Void800)
                        .clickable { onProfileClick?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = AstralGold, modifier = Modifier.size(20.dp))
                }
                var showMoreMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                    DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                        DropdownMenuItem(text = { Text("Settings") }, onClick = { showMoreMenu = false })
                        DropdownMenuItem(text = { Text("Help & Feedback") }, onClick = { showMoreMenu = false })
                    }
                }
            } else {
                TextButton(onClick = onLoginClick, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.Filled.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.nav_login), color = Color.White)
                }
            }
        }
    )
}

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showCreateMenu by remember { mutableStateOf(false) }
    var unreadCount by remember { mutableIntStateOf(0) }

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = AstralGold,
        selectedTextColor = AstralGold,
        unselectedIconColor = Color(0xFF64748B),
        unselectedTextColor = Color(0xFF64748B),
        indicatorColor = Color.Transparent
    )

    // FAB'ı NavigationBar dışında, üzerine overlay olarak çiz
    Box {
        NavigationBar(
            containerColor = Void900,
            contentColor = Color.White,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = currentRoute == Screen.Home.route,
                onClick = { navController.navigate(Screen.Home.route) },
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_home), style = MaterialTheme.typography.labelSmall) },
                colors = navItemColors
            )
            NavigationBarItem(
                selected = currentRoute == Screen.Explore.route,
                onClick = { navController.navigate(Screen.Explore.route) },
                icon = { Icon(Icons.Filled.Explore, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_explore), style = MaterialTheme.typography.labelSmall) },
                colors = navItemColors
            )
            // FAB için boş placeholder — ortadaki slot'u ayırt etmek için
            NavigationBarItem(
                selected = false,
                onClick = {},
                icon = { Spacer(Modifier.size(56.dp)) },
                label = {},
                colors = navItemColors,
                enabled = false
            )
            NavigationBarItem(
                selected = currentRoute == Screen.Vision.route,
                onClick = { navController.navigate(Screen.Vision.route) },
                icon = { Icon(Icons.Filled.TrackChanges, contentDescription = null) },
                label = { Text(stringResource(R.string.nav_vision), style = MaterialTheme.typography.labelSmall) },
                colors = navItemColors
            )
            NavigationBarItem(
                selected = currentRoute == Screen.Messages.route,
                onClick = { navController.navigate(Screen.Messages.route) },
                icon = {
                    BadgedBox(badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = ShadowWorkRose) {
                                Text(unreadCount.toString())
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Message, contentDescription = null)
                    }
                },
                label = { Text(stringResource(R.string.nav_messages), style = MaterialTheme.typography.labelSmall) },
                colors = navItemColors
            )
        }

        // FAB — NavigationBar'ın üstünde, ortada yüzen buton
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AstralGold, AetherCyan)))
                    .clickable { showCreateMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            DropdownMenu(expanded = showCreateMenu, onDismissRequest = { showCreateMenu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.nav_new_dream)) },
                    onClick = { showCreateMenu = false; navController.navigate(Screen.CreateDream.route) },
                    leadingIcon = { Icon(Icons.Filled.NightsStay, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.nav_new_vision)) },
                    onClick = { showCreateMenu = false; navController.navigate(Screen.CreateVision.route) },
                    leadingIcon = { Icon(Icons.Filled.TrackChanges, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().background(Void950), contentAlignment = Alignment.Center) {
        Text(title, color = AstralGold, style = MaterialTheme.typography.headlineMedium)
    }
}
