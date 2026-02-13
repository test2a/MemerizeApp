package app.test2a.memerize.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.test2a.memerize.utils.InAppLogger

@Composable
fun InAppLogOverlay() {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var expanded by remember { mutableStateOf(false) }
    val logs = remember { mutableStateOf(InAppLogger.getLogs()) }
    val context = LocalContext.current

    // Update logs every second
    LaunchedEffect(Unit) {
        while (true) {
            logs.value = InAppLogger.getLogs()
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Black.copy(alpha = if (expanded) 0.85f else 0.5f))
            .clickable { expanded = !expanded }
            .padding(4.dp)
            .then(if (expanded) Modifier.fillMaxHeight(0.5f) else Modifier)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (expanded) "App Log (tap to collapse)" else "App Log (tap to expand)",
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    clipboardManager.setText(AnnotatedString(logs.value))
                }) {
                    Text("Copy")
                }
                Button(onClick = { InAppLogger.clear() }) {
                    Text("Clear")
                }
            }
            if (expanded) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 300.dp)
                        .verticalScroll(rememberScrollState())
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(4.dp)
                ) {
                    Text(
                        text = logs.value,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
