package com.example.daily_planner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.daily_planner.data.Note
import java.net.URLEncoder
import java.net.URLDecoder
import java.time.LocalDate
import java.nio.charset.StandardCharsets

@Composable
fun PlannerNavigation(viewModel: PlannerViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "planner") {
        composable("planner") {
            PlannerScreen(
                viewModel = viewModel,
                onNavigateToDay = { date ->
                    navController.navigate("day/${date}")
                },
                onNavigateToTags = {
                    navController.navigate("tags")
                },
                modifier = Modifier
            )
        }
        
        composable("tags") {
            val notes by viewModel.allNotes.collectAsState()
            TagsScreen(
                notes = notes,
                onNavigateBack = { navController.popBackStack() },
                onNoteClick = { note ->
                    navController.popBackStack()
                    navController.navigate("day/${note.date}")
                }
            )
        }
        
        composable(
            route = "day/{date}",
            arguments = listOf(
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
            val date = LocalDate.parse(dateString)
            val notes by viewModel.notesForSelectedDate.collectAsState()
            var showAddNoteDialog by remember { mutableStateOf(false) }
            
            DayScreen(
                date = date,
                notes = notes.filter { it.date == date },
                onNavigateBack = { navController.popBackStack() },
                onAddNote = { showAddNoteDialog = true },
                onDeleteNote = { note -> viewModel.deleteNote(note) },
                onNoteClick = { note ->
                    try {
                        val encodedContent = URLEncoder.encode(note.content, StandardCharsets.UTF_8.toString())
                        val encodedStyles = URLEncoder.encode(note.contentStyle, StandardCharsets.UTF_8.toString())
                        navController.navigate("note/edit/${date}?content=${encodedContent}&styles=${encodedStyles}&noteId=${note.id}")
                    } catch (e: Exception) {
                        // В случае ошибки кодирования просто переходим к редактированию без контента
                        navController.navigate("note/edit/${date}?noteId=${note.id}")
                    }
                },
                onNewNote = {
                    navController.navigate("note/edit/${date}")
                },
                onNavigateToTags = {
                    navController.navigate("tags")
                },
                modifier = Modifier
            )
            
            if (showAddNoteDialog) {
                AddNoteDialog(
                    onDismiss = { showAddNoteDialog = false },
                    onNoteAdded = { content ->
                        viewModel.addNoteForDate(content, date)
                        showAddNoteDialog = false
                    }
                )
            }
        }
        composable(
            route = "note/edit/{date}?content={content}&styles={styles}&noteId={noteId}",
            arguments = listOf(
                navArgument("date") { type = NavType.StringType },
                navArgument("content") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("styles") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
            val date = LocalDate.parse(dateString)
            val encodedContent = backStackEntry.arguments?.getString("content")
            val encodedStyles = backStackEntry.arguments?.getString("styles")
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            
            val notes by viewModel.notesForSelectedDate.collectAsState()
            val content = try {
                encodedContent?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            } catch (e: Exception) {
                ""
            }
            val styles = try {
                encodedStyles?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            } catch (e: Exception) {
                ""
            }
            
            NoteEditScreen(
                date = date,
                initialContent = content,
                initialStyles = styles,
                initialTags = try {
                    val note = notes.find { it.id == noteId }
                    note?.tags ?: ""
                } catch (e: Exception) { "" },
                initialMood = try {
                    val note = notes.find { it.id == noteId }
                    note?.mood ?: ""
                } catch (e: Exception) { "" },
                initialPriority = try {
                    val note = notes.find { it.id == noteId }
                    note?.priority ?: 0
                } catch (e: Exception) { 0 },
                initialWeather = try {
                    val note = notes.find { it.id == noteId }
                    note?.weather ?: ""
                } catch (e: Exception) { "" },
                initialLocation = try {
                    val note = notes.find { it.id == noteId }
                    note?.location ?: ""
                } catch (e: Exception) { "" },
                onNavigateBack = { navController.popBackStack() },
                onSave = { newContent, newStyles, newTags, newMood, newPriority, newWeather, newLocation ->
                    if (noteId != -1) {
                        // Обновляем существующую заметку
                        viewModel.updateNote(Note(
                            id = noteId,
                            date = date,
                            content = newContent,
                            contentStyle = newStyles,
                            tags = newTags,
                            mood = newMood,
                            priority = newPriority,
                            weather = newWeather,
                            location = newLocation
                        ))
                    } else {
                        // Создаем новую заметку
                        viewModel.addNoteForDate(
                            content = newContent,
                            date = date,
                            styles = newStyles,
                            tags = newTags,
                            mood = newMood,
                            priority = newPriority,
                            weather = newWeather,
                            location = newLocation
                        )
                    }
                },
                modifier = Modifier
            )
        }
    }
} 