package de.gematik.ti.erp.app.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults

@Composable
fun Dialog(
    title: String,
    subtitle: String? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    onDismissRequest: (() -> Unit)? = null
) {
//    var rootBounds by remember { mutableStateOf(Offset.Zero) }
//
//    Box(
//        Modifier.onGloballyPositioned { childCoordinates ->
//            rootBounds = childCoordinates.positionInRoot()
//        }
//    )

//    Box(Modifier.layout { measurable, _ ->
//        val placeable = measurable.measure(Constraints.fixed())
//        layout() {
//
//        }
//    }) {
    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                return IntOffset.Zero
            }
        }
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.33f))) {
            Surface(
                elevation = 24.dp,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box {
                    Column(
                        Modifier.padding(PaddingDefaults.Medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (onDismissRequest != null) {
                            SpacerMedium()
                        }
                        Text(
                            title,
                            style = MaterialTheme.typography.h6,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = PaddingDefaults.Large)
                        )
                        subtitle?.let {
                            SpacerSmall()
                            Text(subtitle, style = AppTheme.typography.body1l, textAlign = TextAlign.Center)
                        }
                        SpacerMedium()
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            dismissButton?.invoke()
                            confirmButton()
                        }
                    }
                    onDismissRequest?.let {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Filled.Cancel, null, tint = AppTheme.colors.neutral400)
                        }
                    }
                }
            }
        }
    }
}
