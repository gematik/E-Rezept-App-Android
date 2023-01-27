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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import kotlinx.coroutines.delay
import java.util.Locale

@Immutable
data class HintCardProperties(
    val shape: Shape,
    val backgroundColor: Color,
    val contentColor: Color?,
    val border: BorderStroke?,
    val elevation: Dp
)

object HintCardDefaults {
    @Composable
    fun properties(
        shape: Shape = RoundedCornerShape(8.dp),
        backgroundColor: Color = AppTheme.colors.neutral050,
        contentColor: Color? = null,
        border: BorderStroke = BorderStroke(1.dp, AppTheme.colors.neutral300),
        elevation: Dp = 0.dp
    ) = HintCardProperties(
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        border = border,
        elevation = elevation
    )

    @Composable
    fun flatProperties(
        shape: Shape = RoundedCornerShape(8.dp),
        backgroundColor: Color = MaterialTheme.colors.surface,
        contentColor: Color? = null
    ) = HintCardProperties(
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        border = null,
        elevation = 0.dp
    )

    @Composable
    fun flatPropertiesAlert() = flatProperties(
        backgroundColor = AppTheme.colors.red100
    )
}

@Composable
fun HintCard(
    modifier: Modifier = Modifier,
    properties: HintCardProperties = HintCardDefaults.properties(),
    image: @Composable RowScope.(innerPadding: PaddingValues) -> Unit,
    title: (@Composable () -> Unit)?,
    body: @Composable () -> Unit,
    action: (@Composable ColumnScope.() -> Unit)? = null,
    close: (@Composable (innerPadding: PaddingValues) -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = properties.shape,
        backgroundColor = properties.backgroundColor,
        contentColor = properties.contentColor ?: contentColorFor(properties.backgroundColor),
        border = properties.border,
        elevation = properties.elevation
    ) {
        if (properties.contentColor != null) {
            MaterialTheme(
                colors = MaterialTheme.colors.copy(
                    primary = properties.contentColor
                ),
                content = { HintCardInnerLayout(image, title, body, action, close) }
            )
        } else {
            HintCardInnerLayout(image, title, body, action, close)
        }
    }
}

@Composable
private fun HintCardInnerLayout(
    image: @Composable RowScope.(innerPadding: PaddingValues) -> Unit,
    title: (@Composable () -> Unit)?,
    body: @Composable () -> Unit,
    action: (@Composable ColumnScope.() -> Unit)? = null,
    close: (@Composable (innerPadding: PaddingValues) -> Unit)? = null
) {
    val padding = PaddingDefaults.Medium
    val innerPaddingLeft = PaddingValues(start = padding, top = padding, bottom = padding)
    val innerPaddingRight = PaddingValues(end = padding, top = padding, bottom = padding)

    Row(
        modifier = Modifier
            .graphicsLayer {
                clip = false
            }
    ) {
        image(innerPaddingLeft)

        Column(
            modifier = Modifier
                .padding(start = padding, bottom = padding)
                .weight(1.0f)
                .align(Alignment.CenterVertically)
                .graphicsLayer {
                    clip = false
                }
        ) {
            val noTitleModifier = if (title == null) {
                Modifier.padding(top = padding)
            } else {
                Modifier
            }
            if (title != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(padding)) {
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .padding(top = padding, end = padding)
                    ) {
                        CompositionLocalProvider(
                            LocalTextStyle provides AppTheme.typography.subtitle1
                        ) {
                            title()
                        }
                    }
                    if (close != null) {
                        close(innerPaddingRight)
                    }
                }
                SpacerTiny()
            }
            Column(
                modifier = Modifier
                    .padding(end = padding)
                    .then(noTitleModifier)
                    .graphicsLayer {
                        clip = false
                    }
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides AppTheme.typography.body2
                ) {
                    body()
                }
                if (action != null) {
                    SpacerTiny()
                    action()
                }
            }
        }
        if (close != null && title == null) {
            close(innerPaddingRight)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedHintCard(
    modifier: Modifier = Modifier,
    onTransitionEnd: suspend (Boolean) -> Unit,
    initiallyVisible: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    visibleState: MutableTransitionState<Boolean> = remember { MutableTransitionState(initiallyVisible.value) },
    enterDelay: Int = 300,
    enter: EnterTransition = fadeIn() + expandVertically(),
    exit: ExitTransition = shrinkVertically() + fadeOut(),
    properties: HintCardProperties = HintCardDefaults.properties(),
    image: @Composable RowScope.(innerPadding: PaddingValues) -> Unit,
    title: (@Composable () -> Unit)?,
    body: @Composable () -> Unit,
    action: (@Composable ColumnScope.() -> Unit)? = null,
    close: (@Composable (innerPadding: PaddingValues) -> Unit)? = {
        HintCloseButton(innerPadding = it) {
            visibleState.targetState = false
        }
    }
) {
    LaunchedEffect(visibleState.currentState) {
        if (visibleState.currentState != initiallyVisible.value) {
            onTransitionEnd(visibleState.currentState)
            initiallyVisible.value = true
        }
    }

    LaunchedEffect(Unit) {
        delay(enterDelay.toLong())
        visibleState.targetState = true
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = enter,
        exit = exit
    ) {
        HintCard(
            modifier = modifier,
            properties = properties,
            image = image,
            title = title,
            body = body,
            action = action,
            close = close
        )
    }
}

@Composable
fun RowScope.HintLargeImage(
    painter: Painter,
    contentDescription: String? = null,
    innerPadding: PaddingValues
) {
    val lDs = LocalLayoutDirection.current
    val padding = PaddingValues(
        top = innerPadding.calculateTopPadding(),
        start = innerPadding.calculateStartPadding(lDs)
    )

    Image(
        painter,
        contentDescription,
        modifier = Modifier
            .padding(padding)
            .requiredWidth(80.dp)
            .align(Alignment.Bottom)
    )
}

@Composable
fun RowScope.HintSmallImage(
    painter: Painter,
    contentDescription: String? = null,
    innerPadding: PaddingValues
) {
    Image(
        painter,
        contentDescription,
        modifier = Modifier
            .padding(innerPadding)
            .size(80.dp)
            .align(Alignment.Top)
    )
}

@Composable
fun HintActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier.padding(top = PaddingDefaults.Tiny),
        onClick = onClick,
        elevation = ButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 16.dp,
            disabledElevation = 2.dp
        ),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text.uppercase(Locale.getDefault()))
    }
}

@Composable
fun HintTextActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    align: Alignment.Horizontal = Alignment.Start,
    onClick: () -> Unit
) {
    val offset = if (align == Alignment.Start) {
        ButtonDefaults.TextButtonContentPadding.calculateStartPadding(LocalLayoutDirection.current) * -1
    } else {
        ButtonDefaults.TextButtonContentPadding.calculateEndPadding(LocalLayoutDirection.current)
    }

    TextButton(
        modifier = modifier.absoluteOffset(x = offset),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, style = AppTheme.typography.body2)
    }
}

@Composable
fun HintTextLearnMoreButton(
    modifier: Modifier = Modifier,
    uri: String = "https://www.das-e-rezept-fuer-deutschland.de/fragen-antworten",
    align: Alignment.Horizontal = Alignment.Start
) {
    val uriHandler = LocalUriHandler.current

    HintTextActionButton(
        modifier = modifier,
        onClick = { uriHandler.openUri(uri) },
        enabled = true,
        align = align,
        text = stringResource(R.string.cdw_health_insurance_caption_recognize_healthcard)
    )
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun HintCloseButton(
    innerPadding: PaddingValues,
    onClick: () -> Unit
) {
    IconButton(
        modifier = Modifier
            .padding(top = 2.dp, end = 2.dp),
        onClick = onClick
    ) {
        Icon(Icons.Rounded.Close, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colors.primary)
    }
}

//
// previews
//

@Preview
@Composable
fun HintCardWithActionButtonAndLargeImage() {
    AppTheme {
        HintCard(
            image = {
                HintLargeImage(painterResource(R.drawable.pharmacist), null, it)
            },
            title = { Text("Ihnen wurden 3 Medikamente verschrieben") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintActionButton("Jetzt einlösen") {} }
        )
    }
}

@Preview
@Composable
fun HintCardWithTextActionButtonAndLargeImage() {
    AppTheme {
        HintCard(
            image = {
                HintLargeImage(painterResource(R.drawable.pharmacist), null, it)
            },
            title = { Text("Ihnen wurden 3 Medikamente verschrieben") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintTextActionButton("Jetzt einlösen") {} }
        )
    }
}

@Preview
@Composable
fun HintCardWithActionButton() {
    AppTheme {
        HintCard(
            image = {
                HintSmallImage(painterResource(R.drawable.pharmacist_hint), null, it)
            },
            title = { Text("Ihnen wurden 3 Medikamente verschrieben") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintActionButton("Jetzt einlösen") {} }
        )
    }
}

@Preview
@Composable
fun HintCardWithTextActionButton() {
    AppTheme {
        HintCard(
            image = {
                HintSmallImage(painterResource(R.drawable.pharmacist_hint), null, it)
            },
            title = { Text("Ihnen wurden 3 Medikamente verschrieben") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintTextActionButton("Jetzt einlösen") {} }
        )
    }
}

@Preview
@Composable
fun ClosableHintCardWithActionButtonAndLargeImage() {
    AppTheme {
        HintCard(
            image = {
                HintLargeImage(painterResource(R.drawable.pharmacist), null, it)
            },
            title = { Text("Ihnen wurden 3 Medikamente verschrieben") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintActionButton("Jetzt einlösen") {} },
            close = { HintCloseButton(it) { } }
        )
    }
}

@Preview
@Composable
fun ClosableHintCardWithTextActionButtonAndLargeImage() {
    AppTheme {
        HintCard(
            image = {
                HintLargeImage(painterResource(R.drawable.pharmacist), null, it)
            },
            title = { Text("Ihnen wurden 3 Medikamente verschrieben") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintTextActionButton("Jetzt einlösen") {} },
            close = { HintCloseButton(it) { } }
        )
    }
}

@Preview
@Composable
fun ClosableHintCardWithActionButton() {
    AppTheme {
        HintCard(
            image = {
                HintSmallImage(painterResource(R.drawable.pharmacist_hint), null, it)
            },
            title = { Text("Ihnen wurden 3 Medikamente verschrieben") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintActionButton("Jetzt einlösen") {} },
            close = { HintCloseButton(it) { } }
        )
    }
}

@Preview
@Composable
fun ClosableHintCardWithTextActionButton() {
    AppTheme {
        HintCard(
            image = {
                HintSmallImage(painterResource(R.drawable.pharmacist_hint), null, it)
            },
            title = { Text("Ihnen wurden 3 Medikamente verschrieben") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintTextActionButton("Jetzt einlösen") {} },
            close = { HintCloseButton(it) { } }
        )
    }
}

@Preview
@Composable
fun ClosableHintCardWithActionButtonAndColors() {
    AppTheme {
        HintCard(
            properties = HintCardDefaults.properties(
                backgroundColor = AppTheme.colors.red100,
                contentColor = AppTheme.colors.red900,
                border = BorderStroke(0.5.dp, AppTheme.colors.red300)
            ),
            image = {
                Icon(
                    Icons.Rounded.WarningAmber,
                    null,
                    modifier = Modifier
                        .padding(it)
                        .requiredSize(40.dp)
                )
            },
            title = { Text("Ihnen wurden 3 Medikamente verschrieben") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintActionButton("Jetzt einlösen") {} },
            close = { HintCloseButton(it) { } }
        )
    }
}

@Preview
@Composable
fun ClosableHintCardWithActionButtonAndColorsAndShortTitle() {
    AppTheme {
        HintCard(
            properties = HintCardDefaults.properties(
                backgroundColor = AppTheme.colors.red100,
                contentColor = AppTheme.colors.red900,
                border = BorderStroke(0.5.dp, AppTheme.colors.red300)
            ),
            image = {
                Icon(
                    Icons.Rounded.WarningAmber,
                    null,
                    modifier = Modifier
                        .padding(it)
                        .requiredSize(40.dp)
                )
            },
            title = { Text("Short title") },
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") },
            action = { HintActionButton("Jetzt einlösen") {} },
            close = { HintCloseButton(it) { } }
        )
    }
}

@Preview
@Composable
fun HintCardWithNoTitle() {
    AppTheme {
        HintCard(
            properties = HintCardDefaults.properties(
                backgroundColor = AppTheme.colors.primary100,
                contentColor = AppTheme.colors.primary900
            ),
            image = {
                Icon(
                    Icons.Rounded.WarningAmber,
                    null,
                    modifier = Modifier
                        .padding(it)
                        .requiredSize(40.dp)
                )
            },
            title = null,
            body = { Text("Hier tippen, um sie in einer Apotheke einzulösen") }
        )
    }
}

@Preview
@Composable
fun ClosableHintCardWithNoTitle() {
    AppTheme {
        HintCard(
            properties = HintCardDefaults.properties(
                backgroundColor = AppTheme.colors.primary100,
                contentColor = AppTheme.colors.primary900
            ),
            image = {
                Icon(
                    Icons.Rounded.WarningAmber,
                    null,
                    modifier = Modifier
                        .padding(it)
                        .requiredSize(40.dp)
                )
            },
            title = null,
            body = {
                Text(
                    "Hier tippen, um sie in einer Apotheke einzulösen, Hier tippen, um sie in einer " +
                        "Apotheke einzulösen, Hier tippen, um sie in einer Apotheke einzulösen"
                )
            },
            close = { HintCloseButton(it) { } }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun AnimatedHintCardPreview() {
    AppTheme {
        Column {
            var text by remember { mutableStateOf("state = ?") }
            Text(text)

            AnimatedHintCard(
                onTransitionEnd = { text = "state = $it" },
                enter = fadeIn(animationSpec = tween(2000)),
                exit = fadeOut(animationSpec = tween(2000)),
                properties = HintCardDefaults.properties(
                    backgroundColor = AppTheme.colors.primary100,
                    contentColor = AppTheme.colors.primary900
                ),
                image = {
                    Icon(
                        Icons.Rounded.WarningAmber,
                        null,
                        modifier = Modifier
                            .padding(it)
                            .requiredSize(40.dp)
                    )
                },
                title = null,
                body = {
                    Text(
                        "Hier tippen, um sie in einer Apotheke einzulösen, Hier tippen, um sie in einer " +
                            "Apotheke einzulösen, Hier tippen, um sie in einer Apotheke einzulösen"
                    )
                }
            )
        }
    }
}
