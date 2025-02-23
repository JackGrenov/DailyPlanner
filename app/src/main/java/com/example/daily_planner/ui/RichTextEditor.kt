package com.example.daily_planner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.daily_planner.ui.TextStyleData
import com.example.daily_planner.ui.parseStyles
import com.example.daily_planner.ui.serializeStyles

@Composable
fun RichTextEditor(
    value: String,
    onValueChange: (String, String) -> Unit,
    initialStyles: String = "",
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(value) }
    var styles by remember { mutableStateOf(parseStyles(initialStyles)) }
    var selection by remember { mutableStateOf(TextRange(0, 0)) }
    
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }
    var isUnderline by remember { mutableStateOf(false) }
    var currentColor by remember { mutableStateOf<String?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }

    // Обновляем состояние форматирования при изменении selection
    LaunchedEffect(selection) {
        if (selection.start != selection.end) {
            val selectedStyles = styles.filter { style ->
                style.start <= selection.start && style.end >= selection.end
            }
            isBold = selectedStyles.any { it.isBold }
            isItalic = selectedStyles.any { it.isItalic }
            isUnderline = selectedStyles.any { it.isUnderline }
            currentColor = selectedStyles.firstOrNull()?.color
        }
    }

    // Применяем начальные стили при первом запуске
    LaunchedEffect(Unit) {
        styles = parseStyles(initialStyles)
    }

    Column(modifier = modifier) {
        // Панель инструментов форматирования
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconToggleButton(
                checked = isBold,
                onCheckedChange = { checked ->
                    isBold = checked
                    if (selection.start != selection.end) {
                        styles = updateStyle(styles, selection, "bold", checked)
                        val serializedStyles = serializeStyles(styles)
                        onValueChange(text, serializedStyles)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FormatBold,
                    contentDescription = "Жирный",
                    tint = if (isBold) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }

            IconToggleButton(
                checked = isItalic,
                onCheckedChange = { checked ->
                    isItalic = checked
                    if (selection.start != selection.end) {
                        styles = updateStyle(styles, selection, "italic", checked)
                        val serializedStyles = serializeStyles(styles)
                        onValueChange(text, serializedStyles)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FormatItalic,
                    contentDescription = "Курсив",
                    tint = if (isItalic) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }

            IconToggleButton(
                checked = isUnderline,
                onCheckedChange = { checked ->
                    isUnderline = checked
                    if (selection.start != selection.end) {
                        styles = updateStyle(styles, selection, "underline", checked)
                        val serializedStyles = serializeStyles(styles)
                        onValueChange(text, serializedStyles)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FormatUnderlined,
                    contentDescription = "Подчеркнутый",
                    tint = if (isUnderline) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }

            IconButton(onClick = { showColorPicker = true }) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Цвет текста"
                )
            }
        }

        // Поле для редактирования текста
        val annotatedString = buildAnnotatedString {
            append(text)
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
                        // Проверяем, что строка цвета начинается с #
                        val normalizedColorString = if (colorString.startsWith("#")) {
                            colorString
                        } else {
                            "#$colorString"
                        }
                        addStyle(
                            SpanStyle(color = Color(android.graphics.Color.parseColor(normalizedColorString))),
                            style.start,
                            style.end
                        )
                    } catch (e: Exception) {
                        // Игнорируем некорректные цвета
                    }
                }
            }
        }

        BasicTextField(
            value = TextFieldValue(
                text = text,
                selection = selection
            ),
            onValueChange = { textFieldValue ->
                text = textFieldValue.text
                selection = textFieldValue.selection
                onValueChange(text, serializeStyles(styles))
            },
            visualTransformation = { originalText ->
                TransformedText(
                    annotatedString,
                    OffsetMapping.Identity
                )
            },
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        if (showColorPicker) {
            AlertDialog(
                onDismissRequest = { showColorPicker = false },
                title = { Text("Выберите цвет") },
                text = {
                    Column {
                        listOf(
                            Color.Black to "Черный",
                            Color.Red to "Красный",
                            Color.Blue to "Синий",
                            Color.Green to "Зеленый"
                        ).forEach { (color, name) ->
                            TextButton(
                                onClick = {
                                    if (selection.start != selection.end) {
                                        styles = updateStyle(
                                            styles,
                                            selection,
                                            "color",
                                            "#%02X%02X%02X".format(
                                                (color.red * 255).toInt(),
                                                (color.green * 255).toInt(),
                                                (color.blue * 255).toInt()
                                            )
                                        )
                                        val serializedStyles = serializeStyles(styles)
                                        onValueChange(text, serializedStyles)
                                    }
                                    showColorPicker = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(name, color = color)
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

private fun updateStyle(
    styles: List<TextStyleData>,
    selection: TextRange,
    type: String,
    value: Any
): List<TextStyleData> {
    // Сохраняем стили, которые не пересекаются с текущим выделением
    val nonOverlappingStyles = styles.filter { 
        it.start >= selection.end || it.end <= selection.start 
    }
    
    // Находим существующие стили для выделенного текста
    val existingStyle = styles.find { style ->
        style.start == selection.start && style.end == selection.end
    }
    
    val newStyle = TextStyleData(
        start = selection.start,
        end = selection.end,
        isBold = if (type == "bold") value as Boolean else existingStyle?.isBold ?: false,
        isItalic = if (type == "italic") value as Boolean else existingStyle?.isItalic ?: false,
        isUnderline = if (type == "underline") value as Boolean else existingStyle?.isUnderline ?: false,
        color = if (type == "color") value as String else existingStyle?.color
    )
    
    return nonOverlappingStyles + newStyle
} 