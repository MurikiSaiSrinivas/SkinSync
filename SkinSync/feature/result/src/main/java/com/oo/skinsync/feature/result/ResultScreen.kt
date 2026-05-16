package com.oo.skinsync.feature.result

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oo.skinsync.designsystem.Spacing
import com.oo.skinsync.domain.ColorRec
import com.oo.skinsync.domain.Suggestion
import com.oo.skinsync.domain.UiState

@Composable
fun ResultScreen(viewModel: ResultViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saveMessage by viewModel.saveMessage.collectAsStateWithLifecycle()
    var location by remember { mutableStateOf("") }
    var linkQuery by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(Spacing.lg)) {
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Photoshoot location") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        val context = LocalContext.current
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.padding(top = Spacing.sm)) {
            Button(onClick = { viewModel.generate(location) }) { Text("Get my palette") }
            (state as? UiState.Success)?.let { success ->
                Button(onClick = { viewModel.saveCurrent() }) { Text("Save look") }
                Button(onClick = {
                    ShareCardRenderer.share(context, location, success.data)
                }) { Text("Share") }
            }
        }
        saveMessage?.let {
            Text(it, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = Spacing.xs))
        }

        Box(Modifier.fillMaxSize(), Alignment.TopCenter) {
            when (val s = state) {
                is UiState.Empty -> Hint("Enter a location to get color ideas.")
                is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is UiState.Error -> Hint(s.message)
                is UiState.Success -> Palette(s.data) { linkQuery = it }
            }
        }
    }

    linkQuery?.let { q ->
        RetailerDialog(
            query = q,
            links = viewModel.linksFor(q),
            onDismiss = { linkQuery = null },
        )
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, modifier = Modifier.padding(top = Spacing.xl), style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun Palette(suggestion: Suggestion, onQuery: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.padding(top = Spacing.md)) {
        item {
            Text(
                "Your type: ${suggestion.seasonalType}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(suggestion.palette) { rec -> ColorCard(rec, onQuery) }
    }
}

@Composable
private fun ColorCard(rec: ColorRec, onQuery: (String) -> Unit) {
    val swatch = remember(rec.hexColor) {
        runCatching { Color(android.graphics.Color.parseColor(rec.hexColor)) }
            .getOrDefault(Color.Gray)
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Box(
                    Modifier.size(36.dp).background(swatch, CircleShape)
                        .semantics { contentDescription = "${rec.name} ${rec.hexColor}" },
                )
                Column {
                    Text(rec.name, style = MaterialTheme.typography.titleMedium)
                    // B8: hex text so the color is identifiable without color vision.
                    Text(rec.hexColor, style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(rec.reason, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                rec.outfitQueries.take(3).forEach { q ->
                    AssistChip(onClick = { onQuery(q) }, label = { Text(q) })
                }
            }
        }
    }
}

@Composable
private fun RetailerDialog(
    query: String,
    links: List<com.oo.skinsync.domain.ShoppingLink>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Shop \"$query\"") },
        text = {
            Column {
                links.forEach { link ->
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                        onDismiss()
                    }) { Text(link.retailer) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
