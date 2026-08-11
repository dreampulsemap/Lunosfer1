package io.lunosfer.dreamap.ui.screens

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Main : Screen("main") // Container for bottom nav screens
    
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Vision : Screen("vision")
    object Messages : Screen("messages")

    /** Route şablonu {otherUserId} taşır. Navigasyon için Thread.routeFor(id) kullan. */
    object Thread : Screen("thread/{otherUserId}") {
        fun routeFor(otherUserId: String) = "thread/$otherUserId"
    }

    object CreateDream : Screen("create_dream")
    object CreateVision : Screen("create_vision")
    object Profile : Screen("profile")
    object DreamDetail : Screen("dream/{dreamId}") {
        fun createRoute(dreamId: Long) = "dream/$dreamId"
    }
}
