package com.example.notesapp_apv_czg.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image

@Composable
fun AttachmentPreview(
    attachmentUris: List<String>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(attachmentUris.take(3)) { uri ->
            AttachmentPreviewItem(uri = uri)
        }
        if (attachmentUris.size > 3) {
            item {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+${attachmentUris.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentPreviewItem(
    uri: String,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when {
        uri.contains("audio", ignoreCase = true) || uri.endsWith(".mp3", true) || uri.endsWith(".m4a", true) || uri.endsWith(".wav", true) || uri.endsWith(".3gp", true) -> Icons.Default.Audiotrack
        uri.contains("image", ignoreCase = true) || uri.endsWith(".png", true) || uri.endsWith(".jpg", true) || uri.endsWith(".jpeg", true) || uri.endsWith(".webp", true) -> Icons.Default.Image
        else -> Icons.Default.Description
    }

    Surface(
        modifier = modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.small),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}