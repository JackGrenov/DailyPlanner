package com.example.daily_planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.daily_planner.data.Note
import com.example.daily_planner.ui.components.Tag
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter
import java.util.*

private val json = Json { 
    ignoreUnknownKeys = true 
    isLenient = true
    encodeDefaults = true
    prettyPrint = false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    notes: List<Note>,
    onNavigateBack: () -> Unit,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTag by remember { mutableStateOf<Tag?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Собираем все уникальные теги из заметок
    val allTags = remember(notes) {
        notes.flatMap { note ->
            try {
                json.decodeFromString<List<Tag>>(note.tags)
            } catch (e: Exception) {
                emptyList()
            }
        }.distinctBy { it.name }
    }

    // Фильтруем заметки по выбранному тегу
    val filteredNotes = remember(selectedTag, notes, searchQuery) {
        if (selectedTag == null && searchQuery.isEmpty()) {
            emptyList()
        } else {
            notes.filter { note ->
                val noteTags = try {
                    json.decodeFromString<List<Tag>>(note.tags)
                } catch (e: Exception) {
                    emptyList()
                }
                
                val matchesTag = selectedTag?.let { tag ->
                    noteTags.any { it.name == tag.name }
                } ?: true
                
                val matchesSearch = if (searchQuery.isNotEmpty()) {
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    noteTags.any { it.name.contains(searchQuery, ignoreCase = true) }
                } else {
                    true
                }
                
                matchesTag && matchesSearch
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск по тегам") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Поле поиска
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Поиск по заметкам и тегам") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Поиск")
                },
                singleLine = true
            )

            // Список тегов
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allTags) { tag ->
                    val isSelected = selectedTag?.name == tag.name
                    val tagColor = try {
                        Color(android.graphics.Color.parseColor(tag.color))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) tagColor else tagColor.copy(alpha = 0.2f),
                        modifier = Modifier.clickable {
                            selectedTag = if (isSelected) null else tag
                        }
                    ) {
                        Text(
                            text = tag.name,
                            color = if (isSelected) Color.White else tagColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Список отфильтрованных заметок
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredNotes.isEmpty() && (selectedTag != null || searchQuery.isNotEmpty())) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Заметки не найдены",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredNotes.sortedByDescending { it.updatedAt }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Дата
            Text(
                text = note.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Текст заметки
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Теги
            val tags = try {
                json.decodeFromString<List<Tag>>(note.tags)
            } catch (e: Exception) {
                emptyList()
            }
            
            if (tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tags.forEach { tag ->
                        val tagColor = try {
                            Color(android.graphics.Color.parseColor(tag.color))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tagColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = tag.name,
                                color = tagColor,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 