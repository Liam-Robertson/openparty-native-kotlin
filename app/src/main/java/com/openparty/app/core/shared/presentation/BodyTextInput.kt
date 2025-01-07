package com.openparty.app.core.shared.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun BodyTextInput(
    textFieldValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    placeholderText: String
) {
    TextField(
        value = textFieldValue,
        onValueChange = onTextChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholderText,
                color = Color.Gray
            )
        },
        textStyle = TextStyle(color = Color.White)
    )
}
