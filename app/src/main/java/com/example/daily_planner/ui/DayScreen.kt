package com.example.daily_planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.daily_planner.data.Note
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import com.example.daily_planner.ui.parseStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    date: LocalDate,
    notes: List<Note>,
    onNavigateBack: () -> Unit,
    onAddNote: () -> Unit,
    onDeleteNote: (Note) -> Unit,
    onNoteClick: (Note) -> Unit,
    onNewNote: () -> Unit,
    onNavigateToTags: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy г.", Locale("ru"))),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru"))}, День ${date.dayOfYear}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToTags) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск по тегам")
                    }
                    IconButton(onClick = onNewNote) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить заметку")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет заметок на этот день",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(notes) { note ->
                        DayNoteItem(
                            note = note,
                            onDelete = { onDeleteNote(note) },
                            onClick = { onNoteClick(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayNoteItem(
    note: Note,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val styles = parseStyles(note.contentStyle)
                val annotatedString = buildAnnotatedString {
                    append(note.content)
                    styles.forEach { style ->
                        if (style.isBold) {
                            addStyle(
                                SpanStyle(fontWeight = FontWeight.Bold),
                                style.start,
                                style.end
                            )
                        }
                        if (style.isItalic) {
                            addStyle(
                                SpanStyle(fontStyle = FontStyle.Italic),
                                style.start,
                                style.end
                            )
                        }
                        if (style.isUnderline) {
                            addStyle(
                                SpanStyle(textDecoration = TextDecoration.Underline),
                                style.start,
                                style.end
                            )
                        }
                        style.color?.let { colorString ->
                            try {
                                val color = Color(android.graphics.Color.parseColor(colorString))
                                addStyle(
                                    SpanStyle(color = color),
                                    style.start,
                                    style.end
                                )
                            } catch (e: Exception) {
                                // Игнорируем некорректные цвета
                            }
                        }
                    }
                }
                
                Text(
                    text = annotatedString,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
} 