package org.golfcat.team.project.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp

@Composable
fun GCButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun GCIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

@Composable
fun GCTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    TextButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun GCOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun GCElevatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.elevatedShape,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    elevation: CardElevation = CardDefaults.elevatedCardElevation(),
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    ElevatedCard(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GCFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
) {
    val haptic = LocalHapticFeedback.current
    FilterChip(
        selected = selected,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        label = label,
        modifier = modifier,
        enabled = enabled,
        colors = colors
    )
}

@Composable
fun GCRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
) {
    val haptic = LocalHapticFeedback.current
    RadioButton(
        selected = selected,
        onClick = if (onClick != null) {
            {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        } else null,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}

@Composable
fun ScoreBadge(score: Int, par: Int) {
    if (score == 0) {
        Text("-", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        return
    }

    val isBirdie = score == par - 1
    val isEagleOrBetter = score <= par - 2
    val isBogey = score == par + 1
    val isDoubleBogeyOrWorse = score >= par + 2

    Surface(
        modifier = Modifier.size(28.dp),
        shape = if (isBirdie || isEagleOrBetter) androidx.compose.foundation.shape.CircleShape else RectangleShape,
        color = androidx.compose.ui.graphics.Color.White,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shadowElevation = 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                isEagleOrBetter -> {
                    // Double circle
                    Box(Modifier.size(26.dp).border(1.dp, MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape))
                    Box(Modifier.size(22.dp).border(1.dp, MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape))
                }
                isBirdie -> {
                    // Single circle
                    Box(Modifier.size(24.dp).border(1.dp, MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape))
                }
                isDoubleBogeyOrWorse -> {
                    // Double square
                    Box(Modifier.size(26.dp).border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape))
                    Box(Modifier.size(21.dp).border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape))
                }
                isBogey -> {
                    // Single square
                    Box(Modifier.size(24.dp).border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape))
                }
            }
            
            Text(
                text = "$score",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize * 1.1f,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                color = when {
                    score < par -> MaterialTheme.colorScheme.secondary // 活力草地綠
                    score > par -> MaterialTheme.colorScheme.error // 警示紅
                    else -> MaterialTheme.colorScheme.onSurface // 炭墨黑
                }
            )
        }
    }
}
