package com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_preview.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.model.CouncilMeeting

@Composable
fun CouncilMeetingCard(councilMeeting: CouncilMeeting, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = councilMeeting.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                modifier = Modifier.padding(8.dp)
            )
            AsyncImage(
                model = councilMeeting.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Crop
            )
        }
    }
}
