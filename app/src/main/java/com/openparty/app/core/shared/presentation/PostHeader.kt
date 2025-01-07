package com.openparty.app.core.shared.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TopContainer(
    onBackClicked: () -> Unit,
    onPostClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("X", modifier = Modifier.clickable { onBackClicked() })
        Text("Post", modifier = Modifier.clickable { onPostClicked() })
    }
}
