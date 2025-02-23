package com.example.daily_planner.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface NoteRepository {
    fun getNotesByDate(date: LocalDate): Flow<List<Note>>
    fun getNotesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Note>>
    fun getAllNotes(): Flow<List<Note>>
    suspend fun insertNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun updateNote(note: Note)
}

class DefaultNoteRepository(private val noteDao: NoteDao) : NoteRepository {
    override fun getNotesByDate(date: LocalDate): Flow<List<Note>> {
        return noteDao.getNotesByDate(date)
    }

    override fun getNotesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Note>> {
        return noteDao.getNotesBetweenDates(startDate, endDate)
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
    }

    override suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }
} 