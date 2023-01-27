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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.addCallback
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import io.github.aakira.napier.Napier
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

@Composable
fun SpacerMaxWidth() =
    Spacer(modifier = Modifier.fillMaxWidth())

@Composable
fun Spacer40() =
    Spacer(modifier = Modifier.size(40.dp))

@Composable
fun Spacer32() =
    Spacer(modifier = Modifier.size(32.dp))

@Composable
fun Spacer24() =
    Spacer(modifier = Modifier.size(24.dp))

@Composable
fun Spacer16() =
    Spacer(modifier = Modifier.size(16.dp))

@Composable
fun Spacer8() =
    Spacer(modifier = Modifier.size(8.dp))

@Composable
fun Spacer4() =
    Spacer(modifier = Modifier.size(4.dp))

@Composable
fun Spacer48() =
    Spacer(modifier = Modifier.size(48.dp))

@Composable
fun SpacerLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Large))

@Composable
fun SpacerXLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XLarge))

@Composable
fun SpacerXXLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XXLarge))

@Composable
fun SpacerMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Medium))

@Composable
fun SpacerShortMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.ShortMedium))

@Composable
fun SpacerXXLargeMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XXLargeMedium))

@Composable
fun SpacerSmall() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Small))

@Composable
fun SpacerTiny() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Tiny))

@Composable
fun LargeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: Shape = RoundedCornerShape(8.dp),
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = PaddingDefaults.Medium,
        vertical = PaddingDefaults.Large / 2
    ),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = border,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Internal testing only. This extension changes the [contentDescription] to [id].
 * Important: add this to the end of the [Modifier] chain.
 */
@Deprecated("replace with testTag()")
fun Modifier.testId(id: String) =
    if (BuildKonfig.INTERNAL && BuildKonfig.DEBUG_TEST_IDS_ENABLED) {
        this.semantics {
            contentDescription = id
        }
    } else {
        this
    }

@Composable
fun BackInterceptor(
    onBack: () -> Unit
) {
    val activity = LocalActivity.current

    DisposableEffect(activity) {
        val callback = activity.onBackPressedDispatcher.addCallback {
            onBack()
        }

        onDispose {
            callback.remove()
        }
    }
}

@Composable
fun NavigationClose(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val acc = stringResource(R.string.cancel)

    IconButton(
        onClick = onClick,
        modifier = modifier
            .semantics { contentDescription = acc }
            .testTag(TestTag.TopNavigation.CloseButton)
    ) {
        Icon(
            Icons.Rounded.Close,
            null,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun annotatedLinkString(uri: String, text: String, tag: String = "URL"): AnnotatedString =
    buildAnnotatedString {
        pushStringAnnotation(tag, uri)
        pushStyle(AppTheme.typography.subtitle2.toSpanStyle())
        pushStyle(SpanStyle(color = AppTheme.colors.primary600))
        append(text)
        pop()
        pop()
        pop()
    }

@Composable
fun annotatedLinkStringLight(uri: String, text: String, tag: String = "URL"): AnnotatedString =
    buildAnnotatedString {
        pushStringAnnotation(tag, uri)
        pushStyle(SpanStyle(color = AppTheme.colors.primary500))
        append(text)
        pop()
        pop()
    }

@Composable
fun ClickableTaggedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onClick: (AnnotatedString.Range<String>) -> Unit
) {
    val textColor =
        style.color.takeOrElse {
            LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
        }

    ClickableText(
        text = text,
        modifier = modifier,
        style = style.copy(color = textColor),
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onClick = { offset ->
            text.getStringAnnotations(offset, offset).firstOrNull()?.let {
                onClick(it)
            }
        }
    )
}

@Composable
fun NavigationBack(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val acc = stringResource(R.string.back)

    IconButton(
        onClick = onClick,
        modifier = modifier
            .semantics { contentDescription = acc }
            .testTag(TestTag.TopNavigation.BackButton)
    ) {
        Icon(
            Icons.Rounded.ArrowBack,
            null,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

enum class NavigationBarMode {
    Back,
    Close
}

@Composable
fun NavigationTopAppBar(
    navigationMode: NavigationBarMode?,
    title: String,
    backgroundColor: Color = MaterialTheme.colors.surface,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    actions: @Composable RowScope.() -> Unit = {},
    onBack: () -> Unit
) = TopAppBar(
    title = {
        Text(title, overflow = TextOverflow.Ellipsis)
    },
    backgroundColor = backgroundColor,
    navigationIcon = {
        when (navigationMode) {
            NavigationBarMode.Back -> NavigationBack { onBack() }
            NavigationBarMode.Close -> NavigationClose { onBack() }
            else -> {}
        }
    },
    elevation = elevation,
    actions = actions
)

@Composable
fun LabeledSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    header: String,
    description: String?
) {
    LabeledSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled
    ) {
        val iconColorTint = if (enabled) AppTheme.colors.primary600 else AppTheme.colors.primary300
        val textColor = if (enabled) AppTheme.colors.neutral900 else AppTheme.colors.neutral600
        val descriptionColor = if (enabled) AppTheme.colors.neutral600 else AppTheme.colors.neutral400

        Row(
            modifier = Modifier.weight(1.0f)
        ) {
            Icon(icon, null, tint = iconColorTint)
            SpacerSmall()
            Column(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(horizontal = PaddingDefaults.Small)
            ) {
                Text(
                    text = header,
                    style = AppTheme.typography.body1,
                    color = textColor
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = AppTheme.typography.body2l,
                        color = descriptionColor
                    )
                }
            }
        }
    }
}

@Composable
fun LabeledSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .fillMaxWidth()
            .padding(16.dp)
            .semantics(true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        label()

        // for better visibility in dark mode
        CompositionLocalProvider(LocalAbsoluteElevation provides 8.dp) {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled
            )
        }
    }
}

@Composable
fun annotatedStringResource(@StringRes id: Int, vararg args: Any): AnnotatedString =
    annotatedStringResource(id, *(args.map { AnnotatedString(it.toString()) }.toTypedArray()))

@Composable
fun annotatedStringResource(@StringRes id: Int, vararg args: AnnotatedString): AnnotatedString =
    buildAnnotatedString {
        val res = stringResource(id)
        appendSubStrings(args, res)
    }

@Composable
private fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}

@Composable
fun annotatedPluralsResource(@PluralsRes id: Int, quantity: Int): AnnotatedString =
    buildAnnotatedString {
        append(resources().getQuantityString(id, quantity))
    }

@Composable
fun annotatedPluralsResource(
    @PluralsRes id: Int,
    quantity: Int,
    vararg args: AnnotatedString
): AnnotatedString =
    buildAnnotatedString {
        val res = resources().getQuantityString(id, quantity)

        appendSubStrings(args, res)
    }

private fun AnnotatedString.Builder.appendSubStrings(
    args: Array<out AnnotatedString>,
    res: String
) {
    val argIt = args.iterator()
    var i = 0
    while (i <= res.length) {
        val j = res.indexOf("%s", i)

        if (j != -1) {
            append(res.substring(i, j))
            append(argIt.next())

            i = j + 2
        } else {
            append(res.substring(i, res.length))

            break
        }
    }
}

@Composable
fun annotatedStringBold(text: String) =
    buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text)
        }
    }

@Composable
fun CommonAlertDialog(
    icon: ImageVector? = null,
    header: String?,
    info: String,
    cancelText: String,
    actionText: String,
    enabled: Boolean = true,
    onCancel: () -> Unit,
    onClickAction: () -> Unit
) =
    AlertDialog(
        modifier = Modifier.testTag(TestTag.AlertDialog.Modal),
        title = header?.let { { Text(header) } },
        onDismissRequest = onCancel,
        text = { Text(info) },
        icon = icon,
        buttons = {
            TextButton(
                modifier = Modifier.testTag(TestTag.AlertDialog.CancelButton),
                onClick = onCancel,
                enabled = enabled
            ) {
                Text(cancelText)
            }
            TextButton(
                modifier = Modifier.testTag(TestTag.AlertDialog.ConfirmButton),
                onClick = onClickAction,
                enabled = enabled
            ) {
                Text(actionText)
            }
        }
    )

@Composable
fun CommonAlertDialog(
    icon: ImageVector? = null,
    header: AnnotatedString?,
    info: AnnotatedString,
    cancelText: String,
    actionText: String,
    onCancel: () -> Unit,
    onClickAction: () -> Unit
) =
    AlertDialog(
        icon = icon,
        title = header?.let { { Text(header) } },
        onDismissRequest = onCancel,
        text = { Text(info) },
        buttons = {
            TextButton(onClick = onCancel) {
                Text(cancelText)
            }
            TextButton(onClick = onClickAction) {
                Text(actionText)
            }
        }
    )

@Composable
fun AcceptDialog(
    header: AnnotatedString,
    info: AnnotatedString,
    acceptText: String,
    onClickAccept: () -> Unit
) =
    AlertDialog(
        title = { Text(header) },
        onDismissRequest = {},
        text = { Text(info) },
        buttons = {
            TextButton(onClick = onClickAccept) {
                Text(acceptText)
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )

@Composable
fun AcceptDialog(
    header: String,
    info: String,
    acceptText: String,
    onClickAccept: () -> Unit
) =
    AlertDialog(
        title = { Text(header) },
        onDismissRequest = {},
        text = { Text(info) },
        buttons = {
            TextButton(onClick = onClickAccept) {
                Text(acceptText)
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )

fun provideEmailIntent(address: String, subject: String? = null, body: String? = null) =
    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
        subject?.let {
            putExtra(Intent.EXTRA_SUBJECT, it)
        }
        body?.let { text ->
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

fun provideWebIntent(address: String) = Intent(Intent.ACTION_VIEW, Uri.parse(address))

fun providePhoneIntent(phoneNumber: String) =
    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))

fun canHandleIntent(intent: Intent, packageManager: PackageManager): Boolean {
    val activities: List<ResolveInfo> = packageManager.queryIntentActivities(
        intent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    return activities.isNotEmpty()
}

fun Context.handleIntent(
    intent: Intent,
    onCouldNotHandleIntent: (() -> Unit)? = null
) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Napier.e("Couldn't start intent", e)
        onCouldNotHandleIntent?.let { it() }
    }
}

/**
 * Measures all inlined composables upfront and applies the size to the actual placeable of the [Text].
 */
@Composable
fun DynamicText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    inlineContent: Map<String, InlineTextContent>,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    CompositionLocalProvider(LocalTextStyle provides AppTheme.typography.subtitle1) {
        SubcomposeLayout(modifier = Modifier.wrapContentSize()) { constraints ->
            val contentPlaceables = inlineContent.mapValues { (key, content) ->
                val maxSize = subcompose(key, content = { content.children(key) }).map {
                    it.measure(constraints)
                }.fold(IntSize.Zero) { acc, p ->
                    IntSize(
                        width = maxOf(acc.width, p.width),
                        height = maxOf(acc.height, p.height)
                    )
                }

                Pair(content, maxSize)
            }

            val main = subcompose(1) {
                Text(
                    text = text,
                    modifier = modifier,
                    color = color,
                    fontSize = fontSize,
                    fontStyle = fontStyle,
                    fontWeight = fontWeight,
                    fontFamily = fontFamily,
                    letterSpacing = letterSpacing,
                    textDecoration = textDecoration,
                    textAlign = textAlign,
                    lineHeight = lineHeight,
                    overflow = overflow,
                    softWrap = softWrap,
                    maxLines = maxLines,
                    inlineContent = with(LocalDensity.current) {
                        contentPlaceables.mapValues { (_, value) ->
                            val (content, maxSize) = value

                            InlineTextContent(
                                placeholder = content.placeholder.copy(
                                    width = maxSize.width.toSp(),
                                    height = maxSize.height.toSp()
                                ),
                                children = content.children
                            )
                        }
                    },
                    onTextLayout = onTextLayout,
                    style = style
                )
            }.first().measure(constraints)

            layout(main.measuredWidth, main.measuredHeight) {
                main.placeRelative(0, 0)
            }
        }
    }
}

@Composable
fun SimpleCheck(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.CheckCircle, null, tint = AppTheme.colors.green500)
        SpacerMedium()
        Text(text, style = AppTheme.typography.body1, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (value: String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
    isError: Boolean = false,
    errorText: @Composable (() -> Unit)? = null,
    keyBoardType: KeyboardType? = null
) {
    val initialValue = rememberSaveable { value }
    val undoDescription = stringResource(R.string.onb_undo_description)
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            modifier = modifier
                .heightIn(min = 56.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                keyboardType = keyBoardType ?: KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                if (value.isNotEmpty()) {
                    onSubmit(value)
                }
            },
            label = label,
            shape = RoundedCornerShape(8.dp),
            colors = colors,
            isError = isError,
            trailingIcon = if (initialValue != value) {
                {
                    IconButton(
                        modifier = Modifier
                            .semantics { contentDescription = undoDescription },
                        onClick = { onValueChange(initialValue) }
                    ) {
                        Icon(Icons.Rounded.Undo, null)
                    }
                }
            } else {
                null
            }
        )
        if (isError) {
            errorText?.let {
                CompositionLocalProvider(
                    LocalTextStyle provides AppTheme.typography.caption1,
                    LocalContentColor provides AppTheme.colors.red600
                ) {
                    Box(Modifier.padding(start = PaddingDefaults.Medium, top = PaddingDefaults.Small)) {
                        errorText()
                    }
                }
            }
        }
    }
}

@Composable
fun phrasedDateString(date: LocalDateTime): String {
    val locales = LocalConfiguration.current.locales
    val context = LocalContext.current

    val timeFormatter = remember { DateFormat.getTimeFormat(context) }
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locales[0]) }

    val timeOfDate = Date.from(date.atZone(ZoneId.systemDefault()).toInstant())

    val at = stringResource(R.string.at)
    // TODO take more care of the characteristics in different languages
    // val clock = stringResource(R.string.descriptive_date_appendix)

    return "${date.format(dateFormatter)} $at ${timeFormatter.format(timeOfDate)}"
}

fun dateString(date: LocalDateTime): String {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    return date.format(dateFormatter)
}

fun timeString(date: LocalDateTime): String {
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    return date.format(timeFormatter)
}

/**
 * Combines the two args to something like "created at Jan 12, 1952"
 */
@Composable
fun dateWithIntroductionString(@StringRes id: Int, instant: Instant): String {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val date = remember {
        instant.atZone(ZoneId.systemDefault())
            .toLocalDate().format(dateFormatter)
    }
    val combinedString = annotatedStringResource(id, date).toString()
    return remember { combinedString }
}

/**
 * Shows the given content if != null labeled with a description as described in Figma for ProfileScreen.
 */
@Composable
fun LabeledText(description: String, content: String?) {
    if (content != null) {
        Text(content, style = AppTheme.typography.body1)
        Text(description, style = AppTheme.typography.body2l)
        SpacerMedium()
    }
}

/**
 * Same as [LabeledText] but uses the given resource for the description tag.
 *
 */
@Composable
fun LabeledText(descriptionResource: Int, content: String?) {
    LabeledText(stringResource(descriptionResource), content)
}
