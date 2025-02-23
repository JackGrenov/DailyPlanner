package com.example.daily_planner.ui

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daily_planner.data.Note
import com.example.daily_planner.data.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel,
    onNavigateToDay: (LocalDate) -> Unit,
    onNavigateToTags: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val notes by viewModel.notesForSelectedDate.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var selectedDayForNote by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ежедневник") },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Светлая тема" else "Темная тема"
                        )
                    }
                    IconButton(onClick = onNavigateToTags) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск по тегам")
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
            DateHeader(
                currentDate = selectedDate,
                onPreviousDay = { viewModel.selectDate(selectedDate.minusDays(7)) },
                onNextDay = { viewModel.selectDate(selectedDate.plusDays(7)) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
                items(7) { dayIndex ->
                    val currentDate = startOfWeek.plusDays(dayIndex.toLong())
                    DayCard(
                        date = currentDate,
                        notes = notes.filter { it.date == currentDate },
                        onAddNote = {
                            selectedDayForNote = currentDate
                            showAddNoteDialog = true
                        },
                        onDeleteNote = { note -> viewModel.deleteNote(note) },
                        onClick = { onNavigateToDay(currentDate) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showAddNoteDialog && selectedDayForNote != null) {
            AddNoteDialog(
                onDismiss = { 
                    showAddNoteDialog = false
                    selectedDayForNote = null
                },
                onNoteAdded = { content ->
                    selectedDayForNote?.let { date ->
                        viewModel.addNoteForDate(content, date)
                    }
                    showAddNoteDialog = false
                    selectedDayForNote = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayCard(
    date: LocalDate,
    notes: List<Note>,
    onAddNote: () -> Unit,
    onDeleteNote: (Note) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                Column {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("dd MMMM", Locale("ru"))),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = onAddNote) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить заметку")
                }
            }

            if (notes.isEmpty()) {
                Text(
                    text = "Нет заметок",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                notes.take(2).forEach { note ->
                    NoteItem(note = note, onDelete = { onDeleteNote(note) })
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (notes.size > 2) {
                    Text(
                        text = "Ещё ${notes.size - 2} заметок...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun DateHeader(
    currentDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(Icons.Default.KeyboardArrowLeft, "Предыдущая неделя")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val startOfWeek = currentDate.minusDays(currentDate.dayOfWeek.value.toLong() - 1)
            val endOfWeek = startOfWeek.plusDays(6)
            Text(
                text = "${startOfWeek.format(DateTimeFormatter.ofPattern("dd.MM"))} - ${endOfWeek.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        IconButton(onClick = onNextDay) {
            Icon(Icons.Default.KeyboardArrowRight, "Следующая неделя")
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onNoteAdded: (String) -> Unit
) {
    var noteContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить заметку") },
        text = {
            TextField(
                value = noteContent,
                onValueChange = { noteContent = it },
                placeholder = { Text("Введите текст заметки") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onNoteAdded(noteContent) },
                enabled = noteContent.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlannerScreenPreview() {
    val previewViewModel = PlannerViewModel(FakeNoteRepository())
    MaterialTheme {
        PlannerScreen(
            viewModel = previewViewModel,
            onNavigateToDay = {},
            onNavigateToTags = {},
            modifier = Modifier
        )
    }
}

private class FakeNoteRepository : NoteRepository {
    override fun getNotesByDate(date: LocalDate): Flow<List<Note>> = flow {
        emit(listOf(
            Note(
                id = 1,
                date = LocalDate.now(),
                content = "Пример заметки 1"
            ),
            Note(
                id = 2,
                date = LocalDate.now(),
                content = "Пример заметки 2"
            )
        ))
    }

    override fun getNotesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Note>> = flow {
        emit(emptyList())
    }

    override fun getAllNotes(): Flow<List<Note>> = flow {
        emit(listOf(
            Note(
                id = 1,
                date = LocalDate.now(),
                content = "Пример заметки 1"
            ),
            Note(
                id = 2,
                date = LocalDate.now(),
                content = "Пример заметки 2"
            )
        ))
    }

    override suspend fun insertNote(note: Note) {}
    override suspend fun deleteNote(note: Note) {}
    override suspend fun updateNote(note: Note) {}
} 