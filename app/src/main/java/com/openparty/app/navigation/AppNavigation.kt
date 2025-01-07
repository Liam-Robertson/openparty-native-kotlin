package com.openparty.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.openparty.app.features.engagement.comments.feature_add_comment.presentation.AddCommentScreen
import com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_article.presentation.CouncilMeetingArticleScreen
import com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_preview.presentation.CouncilMeetingsPreviewScreen
import com.openparty.app.features.newsfeed.discussions.feature_add_discussion.presentation.AddDiscussionScreen
import com.openparty.app.features.newsfeed.discussions.feature_discussions_article.presentation.DiscussionArticleScreen
import com.openparty.app.features.newsfeed.discussions.feature_discussions_preview.presentation.DiscussionsPreviewScreen
import com.openparty.app.features.startup.account.feature_login.presentation.LoginScreen
import com.openparty.app.features.startup.account.feature_register.presentation.RegisterScreen
import com.openparty.app.features.startup.feature_screen_name_generation.presentation.ScreenNameGenerationScreen
import com.openparty.app.features.startup.feature_splash.presentation.SplashScreen
import com.openparty.app.features.startup.verification.feature_email_verification.presentation.EmailVerificationScreen
import com.openparty.app.features.startup.verification.feature_location_verification.presentation.LocationVerificationScreen
import com.openparty.app.features.startup.verification.feature_manual_verification.presentation.ManualVerificationScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavDestinations.Splash.route) {
        composable(NavDestinations.Splash.route) { SplashScreen(navController = navController) }
        composable(NavDestinations.Login.route) { LoginScreen(navController = navController) }
        composable(NavDestinations.Register.route) { RegisterScreen(navController = navController) }
        composable(NavDestinations.EmailVerification.route) { EmailVerificationScreen(navController = navController) }
        composable(NavDestinations.LocationVerification.route) { LocationVerificationScreen(navController = navController) }
        composable(NavDestinations.ScreenNameGeneration.route) { ScreenNameGenerationScreen(navController = navController) }
        composable(NavDestinations.ManualVerification.route) { ManualVerificationScreen() }
        composable(NavDestinations.DiscussionsPreview.route) { DiscussionsPreviewScreen(navController = navController) }
        composable(
            route = "discussion_article/{discussionId}",
            arguments = listOf(navArgument("discussionId") { type = NavType.StringType })
        ) { DiscussionArticleScreen(navController = navController) }
        composable(NavDestinations.CouncilMeetingsPreview.route) { CouncilMeetingsPreviewScreen(navController = navController) }
        composable(
            route = "council_meetings_article/{councilMeetingId}",
            arguments = listOf(navArgument("councilMeetingId") { type = NavType.StringType })
        ) { CouncilMeetingArticleScreen(navController = navController) }
        composable(
            route = "add_comment_screen?discussionId={discussionId}&titleText={titleText}",
            arguments = listOf(
                navArgument("discussionId") { type = NavType.StringType; defaultValue = "" },
                navArgument("titleText") { type = NavType.StringType; defaultValue = "" }
            )
        ) { AddCommentScreen(navController = navController) }
        composable(NavDestinations.AddDiscussion.route) { AddDiscussionScreen(navController = navController) }
    }
}
