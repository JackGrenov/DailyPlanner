package com.example.daily_planner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: LocalDate,
    val content: String,
    val contentStyle: String = "", // JSON строка со стилями текста
    val tags: String = "", // JSON строка с тегами
    val mood: String = "", // Настроение дня
    val priority: Int = 0, // Приоритет заметки (0 - обычный, 1 - важный, 2 - срочный)
    val weather: String = "", // Погода в момент создания заметки
    val location: String = "", // Местоположение
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 