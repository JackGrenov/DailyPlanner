package com.example.daily_planner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.example.daily_planner.ui.components.NoteMeta
import com.example.daily_planner.ui.components.Tag

private val json = Json { 
    ignoreUnknownKeys = true 
    isLenient = true
    encodeDefaults = true
    prettyPrint = false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    date: LocalDate,
    initialContent: String = "",
    initialStyles: String = "",
    initialTags: String = "",
    initialMood: String = "",
    initialPriority: Int = 0,
    initialWeather: String = "",
    initialLocation: String = "",
    onNavigateBack: () -> Unit,
    onSave: (String, String, String, String, Int, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var noteContent by remember { mutableStateOf(initialContent) }
    var noteStyles by remember { mutableStateOf(initialStyles) }
    var noteTags by remember { mutableStateOf(initialTags) }
    var noteMood by remember { mutableStateOf(initialMood) }
    var notePriority by remember { mutableStateOf(initialPriority) }
    var noteWeather by remember { mutableStateOf(initialWeather) }
    var noteLocation by remember { mutableStateOf(initialLocation) }
    
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showMoodPicker by remember { mutableStateOf(false) }
    var showWeatherPicker by remember { mutableStateOf(false) }

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
                            text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru")),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (noteContent != initialContent || noteStyles != initialStyles ||
                            noteTags != initialTags || noteMood != initialMood ||
                            notePriority != initialPriority || noteWeather != initialWeather ||
                            noteLocation != initialLocation) {
                            showDiscardDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка добавления тега
                    IconButton(onClick = { showTagDialog = true }) {
                        Icon(Icons.Default.Label, contentDescription = "Добавить тег")
                    }
                    // Кнопка выбора настроения
                    IconButton(onClick = { showMoodPicker = true }) {
                        Icon(Icons.Default.EmojiEmotions, contentDescription = "Настроение")
                    }
                    // Кнопка выбора погоды
                    IconButton(onClick = { showWeatherPicker = true }) {
                        Icon(Icons.Default.WbSunny, contentDescription = "Погода")
                    }
                    // Кнопка приоритета
                    IconButton(
                        onClick = { 
                            notePriority = (notePriority + 1) % 3
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Приоритет",
                            tint = if (notePriority > 0) Color(0xFFFFC107) else LocalContentColor.current
                        )
                    }
                    // Кнопка сохранения
                    IconButton(
                        onClick = { 
                            onSave(
                                noteContent,
                                noteStyles,
                                noteTags,
                                noteMood,
                                notePriority,
                                noteWeather,
                                noteLocation
                            )
                            onNavigateBack()
                        },
                        enabled = noteContent.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
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
            // Метаданные заметки
            NoteMeta(
                tags = noteTags,
                mood = noteMood,
                priority = notePriority,
                weather = noteWeather,
                location = noteLocation,
                onTagClick = { /* Обработка клика по тегу */ }
            )
            
            // Редактор текста
            RichTextEditor(
                value = noteContent,
                initialStyles = noteStyles,
                onValueChange = { content, styles ->
                    noteContent = content
                    noteStyles = styles
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // Диалог выбора настроения
        if (showMoodPicker) {
            AlertDialog(
                onDismissRequest = { showMoodPicker = false },
                title = { Text("Выберите настроение") },
                text = {
                    Column {
                        listOf(
                            "happy" to "Отличное",
                            "neutral" to "Нормальное",
                            "sad" to "Грустное"
                        ).forEach { (mood, name) ->
                            TextButton(
                                onClick = {
                                    noteMood = mood
                                    showMoodPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(name)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Диалог выбора погоды
        if (showWeatherPicker) {
            AlertDialog(
                onDismissRequest = { showWeatherPicker = false },
                title = { Text("Выберите погоду") },
                text = {
                    Column {
                        listOf(
                            "sunny" to "Солнечно",
                            "cloudy" to "Облачно",
                            "rainy" to "Дождливо"
                        ).forEach { (weather, name) ->
                            TextButton(
                                onClick = {
                                    noteWeather = weather
                                    showWeatherPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(name)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Диалог добавления тега
        if (showTagDialog) {
            var tagName by remember { mutableStateOf("") }
            var tagColor by remember { mutableStateOf("#000000") }
            
            AlertDialog(
                onDismissRequest = { showTagDialog = false },
                title = { Text("Добавить тег") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tagName,
                            onValueChange = { tagName = it },
                            label = { Text("Название тега") }
                        )
                        
                        // Выбор цвета тега
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                listOf(
                                    "#F44336", "#E91E63", "#9C27B0", "#673AB7",
                                    "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
                                    "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
                                    "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
                                )
                            ) { color ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(color)))
                                        .clickable { tagColor = color }
                                        .border(
                                            width = 2.dp,
                                            color = if (tagColor == color) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tagName.isNotBlank()) {
                                val newTag = Tag(tagName, tagColor)
                                val currentTags = try {
                                    json.decodeFromString<List<Tag>>(noteTags)
                                } catch (e: Exception) {
                                    emptyList()
                                }
                                noteTags = try {
                                    json.encodeToString(currentTags + newTag)
                                } catch (e: Exception) {
                                    ""
                                }
                                tagName = ""
                                showTagDialog = false
                            }
                        }
                    ) {
                        Text("Добавить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTagDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = { Text("Отменить изменения?") },
                text = { Text("Несохраненные изменения будут потеряны.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDiscardDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("Отменить изменения")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Продолжить редактирование")
                    }
                }
            )
        }
    }
} 