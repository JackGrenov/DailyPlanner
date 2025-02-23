package com.example.daily_planner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE date = :date")
    fun getNotesByDate(date: LocalDate): Flow<List<Note>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Query("SELECT * FROM notes WHERE date BETWEEN :startDate AND :endDate")
    fun getNotesBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<Note>>
} 