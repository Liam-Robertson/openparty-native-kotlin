package com.openparty.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationFooter(
    navController: androidx.navigation.NavController,
    currentRoute: String?
) {
    val discussionsColor = if (currentRoute == NavDestinations.DiscussionsPreview.route) Color.White else Color.Gray
    val newsfeedColor = if (currentRoute == NavDestinations.CouncilMeetingsPreview.route) Color.White else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.Black)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .clickable {
                    navController.navigate(NavDestinations.DiscussionsPreview.route) {
                        popUpTo(NavDestinations.DiscussionsPreview.route) { inclusive = true }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Chat,
                contentDescription = null,
                tint = discussionsColor,
                modifier = Modifier.size(24.dp)
            )
            Text("Discussions", color = discussionsColor, fontSize = 12.sp)
        }

        Column(
            modifier = Modifier
                .clickable {
                    navController.navigate(NavDestinations.CouncilMeetingsPreview.route) {
                        popUpTo(NavDestinations.CouncilMeetingsPreview.route) { inclusive = true }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Newspaper,
                contentDescription = null,
                tint = newsfeedColor,
                modifier = Modifier.size(24.dp)
            )
            Text("Newsfeed", color = newsfeedColor, fontSize = 12.sp)
        }
    }
}
