package com.openparty.app.core.shared.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TitleText(text: String) {
    Text(
        text = text,
        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, // Use Material Design headline style
        modifier = Modifier.padding(bottom = 16.dp)
    )
}
