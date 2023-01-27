/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.utils.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import androidx.compose.foundation.layout.systemBarsPadding
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import kotlinx.coroutines.flow.filter
import java.util.UUID

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    buttons: @Composable () -> Unit,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(PaddingDefaults.Large),
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties()
) {
    val dismissModifier = if (properties.dismissOnClickOutside) {
        Modifier.clickable(
            onClick = onDismissRequest,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )
    } else {
        Modifier.pointerInput(Unit) { }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            Modifier
                .semantics(false) { }
                .imePadding()
                .fillMaxSize()
                .then(dismissModifier)
                .background(SolidColor(Color.Black), alpha = 0.5f)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(vertical = PaddingDefaults.Medium),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = modifier
                    .wrapContentHeight()
                    .fillMaxWidth(0.78f),
                color = backgroundColor,
                border = BorderStroke(1.dp, AppTheme.colors.neutral300),
                contentColor = contentColor,
                shape = shape,
                elevation = 8.dp
            ) {
                Column(Modifier.padding(PaddingDefaults.Large)) {
                    icon?.let {
                        Icon(icon, null, modifier = Modifier.align(Alignment.CenterHorizontally))
                        SpacerMedium()
                    }
                    CompositionLocalProvider(
                        LocalTextStyle provides AppTheme.typography.h6
                    ) {
                        title?.let {
                            title()
                            SpacerMedium()
                        }
                    }
                    CompositionLocalProvider(
                        LocalTextStyle provides AppTheme.typography.body2
                    ) {
                        text?.let {
                            text()
                            SpacerMedium()
                        }
                    }
                    FlowRow(
                        modifier = Modifier
                            .wrapContentWidth()
                            .align(Alignment.End),
                        mainAxisAlignment = MainAxisAlignment.End,
                        content = buttons
                    )
                }
            }
        }
    }
}

@Composable
fun Dialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    BackHandler {
        if (properties.dismissOnBackPress) {
            onDismissRequest()
        }
    }

    val key = remember { UUID.randomUUID() }
    var stack by LocalDialogHostState.current.stack

    DisposableEffect(Unit) {
        stack = stack + (key to content)
        onDispose {
            stack = stack - key
        }
    }
}

@Composable
fun DialogHost(
    content: @Composable () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        val dialogHostState = remember { DialogHostState() }

        CompositionLocalProvider(
            LocalDialogHostState provides dialogHostState
        ) {
            content()

            val stack by LocalDialogHostState.current.stack

            var previous by remember { mutableStateOf<Map.Entry<Any, @Composable () -> Unit>?>(null) }

            previous?.let { p ->
                AnimatedVisibility(
                    visible = stack.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    key(p.key) { p.value() }
                }
            }

            LaunchedEffect(Unit) {
                snapshotFlow {
                    stack.entries
                }
                    .filter {
                        it.isNotEmpty()
                    }
                    .collect {
                        previous = it.last()
                    }
            }
        }
    }
}

class DialogHostState {
    val stack = mutableStateOf<Map<Any, @Composable () -> Unit>>(emptyMap())
}

val LocalDialogHostState = compositionLocalOf<DialogHostState> { error("no dialog host state provided") }
