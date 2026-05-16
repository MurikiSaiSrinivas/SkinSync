package com.oo.skinsync.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oo.skinsync.designsystem.Spacing
import com.oo.skinsync.domain.SavedLook
import com.oo.skinsync.domain.UiState

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().padding(Spacing.lg), Alignment.TopCenter) {
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            is UiState.Empty -> Text("No saved looks yet.", style = MaterialTheme.typography.bodyLarge)
            is UiState.Error -> Text(s.message, style = MaterialTheme.typography.bodyLarge)
            is UiState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                items(s.data, key = { it.id }) { look ->
                    LookRow(
                        look = look,
                        onDelete = { viewModel.delete(look.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(look) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LookRow(look: SavedLook, onDelete: () -> Unit, onToggleFavorite: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    look.location.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (look.favorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (look.favorite) "Unfavorite" else "Favorite",
                    )
                }
            }
            Text(look.suggestion.seasonalType, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                look.suggestion.palette.forEach { rec ->
                    val c = runCatching { Color(android.graphics.Color.parseColor(rec.hexColor)) }
                        .getOrDefault(Color.Gray)
                    Box(Modifier.size(28.dp).background(c, CircleShape))
                }
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}
