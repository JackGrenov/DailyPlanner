package com.example.daily_planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { 
    ignoreUnknownKeys = true 
    isLenient = true
    encodeDefaults = true
    prettyPrint = false
}

@Serializable
data class Tag(
    val name: String,
    val color: String
)

@Composable
fun NoteMeta(
    tags: String,
    mood: String,
    priority: Int,
    weather: String,
    location: String,
    onTagClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Теги
        if (tags.isNotEmpty()) {
            val tagList = try {
                json.decodeFromString<List<Tag>>(tags)
            } catch (e: Exception) {
                emptyList()
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(tagList) { tag ->
                    val tagColor = try {
                        Color(android.graphics.Color.parseColor(tag.color))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = tagColor.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { onTagClick(tag.name) }
                    ) {
                        Text(
                            text = tag.name,
                            color = tagColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
        
        // Нижний ряд с метаданными
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Настроение
            if (mood.isNotEmpty()) {
                Icon(
                    imageVector = when (mood) {
                        "happy" -> Icons.Default.SentimentVerySatisfied
                        "neutral" -> Icons.Default.SentimentNeutral
                        "sad" -> Icons.Default.SentimentVeryDissatisfied
                        else -> Icons.Default.SentimentNeutral
                    },
                    contentDescription = "Настроение",
                    tint = when (mood) {
                        "happy" -> Color(0xFF4CAF50)
                        "neutral" -> Color(0xFFFFC107)
                        "sad" -> Color(0xFFF44336)
                        else -> LocalContentColor.current
                    }
                )
            }
            
            // Приоритет
            if (priority > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(priority) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Приоритет",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // Погода
            if (weather.isNotEmpty()) {
                Icon(
                    imageVector = when (weather) {
                        "sunny" -> Icons.Default.WbSunny
                        "cloudy" -> Icons.Default.Cloud
                        "rainy" -> Icons.Default.Grain
                        else -> Icons.Default.WbSunny
                    },
                    contentDescription = "Погода",
                    tint = when (weather) {
                        "sunny" -> Color(0xFFFFB300)
                        "cloudy" -> Color(0xFF90A4AE)
                        "rainy" -> Color(0xFF42A5F5)
                        else -> LocalContentColor.current
                    }
                )
            }
            
            // Местоположение
            if (location.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Местоположение",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 