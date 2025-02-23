package com.example.daily_planner.ui

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TextStyleData(
    val start: Int,
    val end: Int,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val color: String? = null
)

fun parseStyles(stylesJson: String): List<TextStyleData> {
    return try {
        if (stylesJson.isBlank()) {
            emptyList()
        } else {
            Json.decodeFromString(stylesJson)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun serializeStyles(styles: List<TextStyleData>): String {
    return try {
        Json.encodeToString(styles)
    } catch (e: Exception) {
        ""
    }
} 