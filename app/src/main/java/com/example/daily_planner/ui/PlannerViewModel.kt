package com.example.daily_planner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daily_planner.data.Note
import com.example.daily_planner.data.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class PlannerViewModel(private val repository: NoteRepository) : ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    val allNotes = repository.getAllNotes().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val notesForSelectedDate = selectedDate.flatMapLatest { date ->
        val startOfWeek = date.minusDays(date.dayOfWeek.value.toLong() - 1)
        val endOfWeek = startOfWeek.plusDays(6)
        repository.getNotesBetweenDates(startOfWeek, endOfWeek)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addNote(content: String) {
        addNoteForDate(content, selectedDate.value)
    }

    fun addNoteForDate(
        content: String,
        date: LocalDate,
        styles: String = "",
        tags: String = "",
        mood: String = "",
        priority: Int = 0,
        weather: String = "",
        location: String = ""
    ) {
        viewModelScope.launch {
            val note = Note(
                date = date,
                content = content,
                contentStyle = styles,
                tags = tags,
                mood = mood,
                priority = priority,
                weather = weather,
                location = location
            )
            repository.insertNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
}

class PlannerViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlannerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 