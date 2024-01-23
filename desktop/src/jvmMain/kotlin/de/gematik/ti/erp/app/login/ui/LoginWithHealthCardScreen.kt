/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.login.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.mouse.mouseScrollFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.gematik.ti.erp.app.cardwall.AuthenticationState
import de.gematik.ti.erp.app.common.App
import de.gematik.ti.erp.app.common.ClosablePopupScaffold
import de.gematik.ti.erp.app.common.SpacerLarge
import de.gematik.ti.erp.app.common.SpacerMedium
import de.gematik.ti.erp.app.common.SpacerSmall
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun LoginWithHealthCard(
    viewModel: LoginWithHealthCardViewModel,
    onFinished: () -> Unit,
    onClose: () -> Unit
) {
    var privacyAndTermsToggled by remember { mutableStateOf(false) }
    var cardReaderPresent by remember { mutableStateOf(false) }
    var personalIdentificationNumber by remember { mutableStateOf("") }
    var cardAccessNumber by remember { mutableStateOf("") }

    ClosablePopupScaffold(onClose = onClose) {
        val state = rememberLazyListState()
        Box {
            var authState by remember { mutableStateOf(AuthenticationState.None) }

            var triggerAuth by remember { mutableStateOf(true) }
            LaunchedEffect(state.firstVisibleItemIndex == 4, triggerAuth) {
                if (triggerAuth && state.firstVisibleItemIndex == 4 &&
                    cardAccessNumber.isNotBlank() && personalIdentificationNumber.isNotBlank()
                ) {
                    viewModel.authenticate(can = cardAccessNumber, pin = personalIdentificationNumber)
                        .collect {
                            if (it.isFinal()) {
                                onFinished()
                            } else {
                                authState = it
                            }

                            if (it.isFailure()) {
                                triggerAuth = false
                            }
                        }
                }
            }

            val isCardAccessNumberValid = cardAccessNumber.matches("""^\d{6}$""".toRegex())
            val isPersonalIdentificationNumberValid = personalIdentificationNumber.matches("""^\d{6,8}$""".toRegex())

            val maxPages =
                if (privacyAndTermsToggled) {
                    if (cardReaderPresent) {
                        if (isCardAccessNumberValid) {
                            if (isPersonalIdentificationNumberValid) 5
                            else 4
                        } else 3
                    } else 2
                } else 1

            val scope = rememberCoroutineScope()

            val onNextPage = {
                scope.launch { state.animateScrollToItem(state.firstVisibleItemIndex + 1) }
            }

            Column(Modifier.fillMaxSize()) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    state = state
                ) {
                    items(count = maxPages) { page ->
                        when (page) {
                            0 -> PageContainer(App.strings.desktopLoginPageDataTerms()) {
                                PrivacyAndTerms(
                                    toggled = privacyAndTermsToggled,
                                    onPrivacyAndTermsToggled = {
                                        privacyAndTermsToggled = it
                                    }
                                )
                            }

                            1 -> PageContainer(App.strings.desktopLoginPageConnectReader()) {
                                AwaitCardReader(
                                    viewModel,
                                    onCardReaderPresent = {
                                        cardReaderPresent = true
                                    }
                                )
                            }

                            2 -> PageContainer(App.strings.desktopLoginPageEnterCan()) {
                                EnterCardAccessNumber(
                                    can = cardAccessNumber,
                                    onCanChange = { cardAccessNumber = it },
                                    onEnter = {
                                        if (isCardAccessNumberValid) {
                                            onNextPage()
                                        }
                                    }
                                )
                            }

                            3 -> PageContainer(App.strings.desktopLoginPageEnterPin()) {
                                EnterPersonalIdentificationNumber(
                                    pin = personalIdentificationNumber,
                                    onPinChange = { personalIdentificationNumber = it },
                                    onEnter = {
                                        if (isPersonalIdentificationNumberValid) {
                                            onNextPage()
                                        }
                                    }
                                )
                            }

                            4 -> PageContainer(App.strings.desktopLoginPageConnectHealthcard()) {
                                ReadHealthCardAndDownloadData(
                                    authState,
                                    onReenterCan = {
                                        scope.launch {
                                            state.animateScrollToItem(2)
                                            triggerAuth = true
                                        }
                                    },
                                    onReenterPin = {
                                        scope.launch {
                                            state.animateScrollToItem(3)
                                            triggerAuth = true
                                        }
                                    },
                                    onRetry = { triggerAuth = true },
                                    onSuccess = {
                                        println("CAN $cardAccessNumber")
                                        Arrays.fill(cardAccessNumber.toCharArray(), '\u0000')
                                        println("CAN nulled $cardAccessNumber")
                                    }
                                )
                            }
                        }
                    }
                }

                PageIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), listState = state, 5)
            }

            if (state.firstVisibleItemIndex > 0) {
                NavigationBack(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(start = PaddingDefaults.Large, bottom = 44.dp),
                    onClick = {
                        scope.launch { state.animateScrollToItem(state.firstVisibleItemIndex - 1) }
                    }
                ) {
                    Icon(Icons.Rounded.ArrowBack, null)
                    Spacer(Modifier.size(8.dp))
                    Text(App.strings.back())
                }
            }

            NavigationForward(
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = PaddingDefaults.Large, bottom = 44.dp),
                enabled = state.firstVisibleItemIndex < maxPages - 1,
                onClick = {
                    onNextPage()
                }
            ) {
                Icon(Icons.Rounded.ArrowForward, null)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LazyItemScope.PageContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        SpacerLarge()
        Row(
            Modifier
                .mouseScrollFilter { event, bounds -> true }
                .fillParentMaxSize()
                .padding(horizontal = 124.dp)
        ) {
            val scrollState = rememberScrollState()
            val scrollbarAdapter = rememberScrollbarAdapter(scrollState)
            Box(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState).weight(1f),
                propagateMinConstraints = true
            ) {
                content()
            }
            VerticalScrollbar(scrollbarAdapter)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NavigationForward(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val backgroundColor =
        if (enabled) {
            MaterialTheme.colors.secondary
        } else {
            AppTheme.colors.neutral300
        }

    val contentColor = if (enabled) {
        contentColorFor(backgroundColor)
    } else {
        AppTheme.colors.neutral500
    }

    val interactionSource = remember { MutableInteractionSource() }
    rememberRipple()
    Surface(
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor,
        elevation = if (enabled) FloatingActionButtonDefaults.elevation().elevation(interactionSource).value else 0.dp
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
            ProvideTextStyle(MaterialTheme.typography.button) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 32.dp, minHeight = 32.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NavigationBack(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        modifier = modifier,
        shape = CircleShape,
        color = Color.Unspecified,
        contentColor = AppTheme.colors.neutral600
    ) {
        ProvideTextStyle(MaterialTheme.typography.button) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 32.dp, minHeight = 32.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PageIndicator(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    maxPages: Int
) {
    Box(
        modifier = modifier
            .padding(bottom = PaddingDefaults.Large)
            .clip(CircleShape)
            .background(AppTheme.colors.neutral100.copy(alpha = 0.5f))
            .padding(PaddingDefaults.Small)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            repeat(maxPages) {
                Dot(color = AppTheme.colors.neutral300)
            }
        }
        val pageSize =
            listState.layoutInfo.visibleItemsInfo.find { it.index == listState.firstVisibleItemIndex }?.size ?: 1
        val fraction =
            listState.firstVisibleItemIndex.toFloat() + (listState.firstVisibleItemScrollOffset / pageSize.toFloat())
        val offsetX = with(LocalDensity.current) {
            val gap = PaddingDefaults.Small.roundToPx()
            val size = 8.dp.roundToPx()
            (fraction * (size + gap)).toDp()
        }
        Dot(modifier = Modifier.offset(x = offsetX), color = AppTheme.colors.primary500)
    }
}

@Composable
private fun Dot(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .size(8.dp)
    )
}

@Composable
private fun PrivacyAndTerms(
    toggled: Boolean,
    onPrivacyAndTermsToggled: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            text = App.strings.onBoardingPage4Info(),
            style = MaterialTheme.typography.body1
        )

        var privacyChecked by remember { mutableStateOf(toggled) }
        var termsChecked by remember { mutableStateOf(toggled) }

        LaunchedEffect(privacyChecked, termsChecked) {
            onPrivacyAndTermsToggled(privacyChecked && termsChecked)
        }

        SpacerLarge()

        val uriHandler = LocalUriHandler.current
        val dataUri = App.strings.desktopDataLink()
        val termsUri = App.strings.desktopTermsLink()
        Toggle(
            text = App.strings.onBoardingPage4InfoAcceptInfo(
                count = 1,
                buildAnnotatedString {
                    pushStyle(SpanStyle(color = AppTheme.colors.primary500))
                    append(App.strings.onBoardingPage4InfoDataprotection())
                    pop()
                }
            ),
            checked = privacyChecked,
            onCheckedChange = {
                privacyChecked = it
            },
            onClickInfo = {
                uriHandler.openUri(dataUri)
            }
        )
        SpacerMedium()
        Toggle(
            text = App.strings.onBoardingPage4InfoAcceptInfo(
                count = 1,
                buildAnnotatedString {
                    pushStyle(SpanStyle(color = AppTheme.colors.primary500))
                    append(App.strings.onBoardingPage4InfoTos())
                    pop()
                }
            ),
            checked = termsChecked,
            onCheckedChange = {
                termsChecked = it
            },
            onClickInfo = {
                uriHandler.openUri(termsUri)
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Toggle(
    text: AnnotatedString,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClickInfo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClickInfo
                )
        )

        SpacerMedium()

        AnimatedContent(
            targetState = checked,
            transitionSpec = {
                fadeIn() with fadeOut()
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.Checkbox,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(
                        bounded = false,
                        radius = PaddingDefaults.Large
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            when (it) {
                false -> Icon(
                    Icons.Rounded.RadioButtonUnchecked,
                    null,
                    tint = AppTheme.colors.neutral400
                )

                true -> Icon(
                    Icons.Rounded.CheckCircle,
                    null,
                    tint = AppTheme.colors.primary600
                )
            }
        }
    }
}

private enum class ReaderState {
    Searching, Found, Error
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AwaitCardReader(
    viewModel: LoginWithHealthCardViewModel,
    onCardReaderPresent: () -> Unit
) {
    var readerState by remember { mutableStateOf(ReaderState.Searching) }

    LaunchedEffect(Unit) {
        delay(1000)
        viewModel.waitForAnyReader()
        delay(1000)
        onCardReaderPresent()
        readerState = ReaderState.Found
    }

    Box(Modifier.fillMaxSize().padding(PaddingDefaults.Medium), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedContent(targetState = readerState, modifier = Modifier.size(72.dp), transitionSpec = {
                when (readerState) {
                    ReaderState.Searching -> fadeIn() with fadeOut()
                    ReaderState.Found -> slideInVertically { height -> height } + fadeIn() with fadeOut()
                    ReaderState.Error -> fadeIn() with fadeOut()
                }
            }) {
                when (it) {
                    ReaderState.Searching -> CircularProgressIndicator(color = AppTheme.colors.primary400)
                    ReaderState.Found -> Icon(Icons.Filled.Power, null, tint = AppTheme.colors.green400)
                    ReaderState.Error -> Icon(Icons.Filled.Error, null, tint = AppTheme.colors.red600)
                }
            }
            SpacerMedium()
            Text(
                when (readerState) {
                    ReaderState.Searching -> App.strings.desktopLoginPageReaderSearchHealthcard()
                    ReaderState.Found -> App.strings.desktopLoginPageReaderFoundHealthcard()
                    ReaderState.Error -> App.strings.desktopLoginPageReaderError()
                },
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EnterPersonalIdentificationNumber(
    pin: String,
    onPinChange: (String) -> Unit,
    onEnter: () -> Unit
) {
    Box(Modifier.fillMaxSize().padding(PaddingDefaults.Medium)) {
        Column(Modifier.align(Alignment.Center).fillMaxWidth()) {
            var isFocussed by remember { mutableStateOf(false) }

            CredentialsTextField(
                value = pin,
                valuePattern = """^\d{0,8}$""".toRegex(),
                onValueChange = onPinChange,
                onEnter = onEnter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = PaddingDefaults.Medium,
                        bottom = PaddingDefaults.Small,
                        end = PaddingDefaults.Medium
                    )
                    .onFocusChanged {
                        isFocussed = it.isFocused
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val shape = RoundedCornerShape(8.dp)
                    val borderModifier = Modifier.border(
                        BorderStroke(1.dp, color = AppTheme.colors.primary700),
                        shape
                    )

                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .shadow(1.dp, shape)
                            .then(if (isFocussed) borderModifier else Modifier)
                            .background(
                                color = AppTheme.colors.neutral200,
                                shape = shape
                            )
                    ) {
                        var pinVisible by remember { mutableStateOf(false) }
                        val transformedPin = if (pinVisible) {
                            pin
                        } else {
                            pin.asIterable().joinToString(separator = "") { "\u2B24" }
                        }

                        val spacer = with(LocalDensity.current) {
                            48.dp.roundToPx()
                        }
                        var textWidth by remember { mutableStateOf(1) }
                        Text(
                            text = transformedPin,
                            style = MaterialTheme.typography.h6,
                            letterSpacing = 0.7.em,
                            textAlign = TextAlign.Center,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            onTextLayout = { res ->
                                textWidth = res.multiParagraph.getLineWidth(0).roundToInt()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 48.dp)
                                .wrapContentWidth()
                                .layout { measurable, constraints ->
                                    val p = measurable.measure(constraints)
                                    val width = constraints.constrainWidth(p.width)
                                    val height = constraints.constrainHeight(p.height)
                                    layout(width, height) {
                                        val space = max(min(width - textWidth, 0), -spacer)
                                        p.place(x = space, y = 0)
                                    }
                                }
                                .align(Alignment.CenterVertically)
                                .clearAndSetSemantics { }
                                .drawWithContent {
                                    val sW = min(1f, (spacer + size.width) / textWidth)
                                    scale(sW, sW, pivot = Offset(x = 0f, y = center.y)) {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                        )

                        IconToggleButton(
                            checked = pinVisible,
                            onCheckedChange = { pinVisible = it },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            when (pinVisible) {
                                true -> Icon(
                                    Icons.Rounded.Visibility,
                                    null,
                                    tint = AppTheme.colors.neutral600
                                )

                                false -> Icon(
                                    Icons.Rounded.VisibilityOff,
                                    null,
                                    tint = AppTheme.colors.neutral600
                                )
                            }
                        }
                    }
                    Spacer(Modifier.size(16.dp))
                    Text(
                        App.strings.cdwPinCaption(),
                        textAlign = TextAlign.Center,
                        style = AppTheme.typography.body2l,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun EnterCardAccessNumber(
    can: String,
    onCanChange: (String) -> Unit,
    onEnter: () -> Unit
) {
    Box(Modifier.fillMaxSize().padding(PaddingDefaults.Medium)) {
        Column(Modifier.fillMaxSize().align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource("images/card_wall_card_can.webp"),
                null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.width(278.dp)
            )
            Spacer(Modifier.size(16.dp))

            var isFocussed by remember { mutableStateOf(false) }

            CredentialsTextField(
                value = can,
                valuePattern = """^\d{0,6}$""".toRegex(),
                onValueChange = onCanChange,
                onEnter = onEnter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = PaddingDefaults.Large, bottom = 8.dp, end = PaddingDefaults.Large)
                    .onFocusChanged {
                        isFocussed = it.isFocused
                    }
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        val shape = RoundedCornerShape(8.dp)
                        val backgroundColor = AppTheme.colors.neutral200
                        val borderModifier = Modifier.border(
                            BorderStroke(1.dp, color = AppTheme.colors.primary700),
                            shape
                        )

                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp, 48.dp)
                                    .shadow(1.dp, shape)
                                    .then(
                                        if ((can.length == it || it == 5 && can.length == 6) && isFocussed) {
                                            borderModifier
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .background(
                                        color = backgroundColor,
                                        shape
                                    )
                                    .graphicsLayer {
                                        clip = false
                                    }
                            ) {
                                Text(
                                    text = can.getOrNull(it)?.toString() ?: " ",
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .clearAndSetSemantics { }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(PaddingDefaults.Medium))
            Text(
                App.strings.cdwCanCaption(),
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ReadHealthCardAndDownloadData(
    authState: AuthenticationState,
    onReenterPin: () -> Unit,
    onReenterCan: () -> Unit,
    onRetry: () -> Unit,
    onSuccess: () -> Unit
) {
    Box(Modifier.fillMaxSize().padding(PaddingDefaults.Medium), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val icSize = Modifier.size(64.dp)
            if (!authState.isFailure()) {
                CircularProgressIndicator(color = AppTheme.colors.primary400, modifier = icSize)
            } else {
                Icon(Icons.Rounded.Error, null, modifier = icSize, tint = AppTheme.colors.red600)
            }
            SpacerMedium()

            when {
                authState.isInProgress() && authState == AuthenticationState.AuthenticationFlowInitialized ->
                    Text(
                        "Bitte jetzt die Gesundheitskarte auflegen",
                        style = AppTheme.typography.body2l,
                        textAlign = TextAlign.Center
                    )

                authState.isInProgress() && authState != AuthenticationState.AuthenticationFlowInitialized ->
                    Text(
                        "Gesundheitskarte wird gelesen und Rezepte werden geladen",
                        style = AppTheme.typography.body2l,
                        textAlign = TextAlign.Center
                    )
            }

            SpacerMedium()

            if (authState.isFailure()) {
                val title = when (authState) {
                    AuthenticationState.HealthCardCardAccessNumberWrong ->
                        App.strings.cdwNfcIntroStep2HeaderOnCanError()

                    AuthenticationState.HealthCardPin2RetriesLeft ->
                        App.strings.cdwNfcIntroStep2HeaderOnPinError(count = 2, 2)

                    AuthenticationState.HealthCardPin1RetryLeft ->
                        App.strings.cdwNfcIntroStep2HeaderOnPinError(count = 1, 1)

                    AuthenticationState.HealthCardBlocked ->
                        App.strings.cdwNfcIntroStep2HeaderOnCardBlocked()

                    AuthenticationState.IDPCommunicationFailed ->
                        App.strings.cdwNfcIntroStep1HeaderOnError()

                    AuthenticationState.HealthCardCommunicationInterrupted ->
                        App.strings.desktopLoginPageConnectHealthcardErrorIoTitle()

                    else ->
                        App.strings.cdwNfcIntroStep1HeaderOnError()
                }
                val subtitle = when (authState) {
                    AuthenticationState.HealthCardCardAccessNumberWrong ->
                        App.strings.cdwNfcIntroStep2InfoOnCanError()

                    AuthenticationState.HealthCardPin2RetriesLeft ->
                        App.strings.cdwNfcIntroStep2InfoOnPinError(count = 2, 2)

                    AuthenticationState.HealthCardPin1RetryLeft ->
                        App.strings.cdwNfcIntroStep2InfoOnPinError(count = 1, 1)

                    AuthenticationState.HealthCardBlocked ->
                        App.strings.cdwNfcIntroStep2InfoOnCardBlocked()

                    AuthenticationState.IDPCommunicationFailed ->
                        App.strings.cdwNfcIntroStep1InfoOnError()

                    AuthenticationState.HealthCardCommunicationInterrupted ->
                        App.strings.desktopLoginPageConnectHealthcardErrorIoSubtitle()

                    else ->
                        App.strings.cdwNfcIntroStep1InfoOnError()
                }
                val button = when (authState) {
                    AuthenticationState.HealthCardCardAccessNumberWrong,
                    AuthenticationState.HealthCardPin2RetriesLeft,
                    AuthenticationState.HealthCardPin1RetryLeft ->
                        App.strings.cdwAuthRetryPinCan()

                    else ->
                        App.strings.cdwAuthRetry()
                }
                Text(title, style = MaterialTheme.typography.subtitle1, textAlign = TextAlign.Center)
                SpacerSmall()
                Text(subtitle, style = AppTheme.typography.body2l, textAlign = TextAlign.Center)
                SpacerMedium()
                Button(onClick = {
                    when (authState) {
                        AuthenticationState.HealthCardCardAccessNumberWrong -> onReenterCan()
                        AuthenticationState.HealthCardPin2RetriesLeft,
                        AuthenticationState.HealthCardPin1RetryLeft -> onReenterPin()

                        else -> onRetry()
                    }
                }) {
                    Text(button)
                }
            } else {
                onSuccess()
            }
        }
    }
}
