/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

@file:Suppress("TooManyFunctions", "MagicNumber")

package de.gematik.ti.erp.app.utils.compose

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.net.Uri
import android.text.format.DateFormat
import android.util.Patterns
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.ErpTimeFormatter.Style
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.openUriWhenValid
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

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

fun String.toAnnotatedString() =
    buildAnnotatedString { append(this@toAnnotatedString) }

@Composable
fun annotatedLinkString(uri: String, text: String, tag: String = "URL"): AnnotatedString =
    buildAnnotatedString {
        pushStringAnnotation(tag, uri)
        pushStyle(AppTheme.typography.subtitle2.toSpanStyle())
        pushStyle(SpanStyle(color = AppTheme.colors.primary700))
        append(text)
        pop()
        pop()
        pop()
    }

@Composable
fun annotatedLinkStringLight(uri: String, text: String, tag: String = "URL"): AnnotatedString =
    buildAnnotatedString {
        pushStringAnnotation(tag, uri)
        pushStyle(SpanStyle(color = AppTheme.colors.primary700))
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
fun LabeledSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    header: String,
    description: String? = null
) {
    LabeledSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled
    ) {
        val iconColorTint = if (enabled) AppTheme.colors.primary700 else AppTheme.colors.primary300
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
            .padding(SizeDefaults.double)
            .semantics(true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SizeDefaults.one)
    ) {
        label()

        // for better visibility in dark mode
        CompositionLocalProvider(LocalAbsoluteElevation provides SizeDefaults.one) {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled
            )
        }
    }
}

@Composable
fun LabelButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    contentDescription: String = text,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(PaddingDefaults.Medium)
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
    ) {
        Icon(icon, null, tint = AppTheme.colors.primary700)
        SpacerMedium()
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = AppTheme.typography.body1
        )
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
    }
}

@Composable
fun LabelButton(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Image(painter = icon, contentDescription = null)
        SpacerMedium()
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = AppTheme.typography.body1
        )
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
    }
}

@Suppress("SpreadOperator")
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
            if (argIt.hasNext()) {
                append(argIt.next())
            }
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

@Deprecated(
    "Please do not use this function anymore. Use ErezeptAlertDialog instead.",
    replaceWith = ReplaceWith("de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog"),
    level = DeprecationLevel.WARNING
)
@Composable
fun CommonAlertDialog(
    icon: ImageVector? = null,
    header: String?,
    info: String,
    cancelText: String,
    cancelTextColor: Color? = null,
    actionText: String,
    actionTextColor: Color? = null,
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
                cancelTextColor?.let {
                    Text(cancelText, color = it)
                } ?: Text(cancelText)
            }
            TextButton(
                modifier = Modifier.testTag(TestTag.AlertDialog.ConfirmButton),
                onClick = onClickAction,
                enabled = enabled
            ) {
                actionTextColor?.let {
                    Text(actionText, color = it)
                } ?: Text(actionText)
            }
        }
    )

@Deprecated(
    "Use material3, will soon be changed to DeprecationLevel.ERROR",
    replaceWith = ReplaceWith("de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog"),
    level = DeprecationLevel.WARNING
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

@Deprecated(
    "Use material3, will soon be changed to DeprecationLevel.ERROR",
    replaceWith = ReplaceWith("de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog"),
    level = DeprecationLevel.WARNING
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

@Deprecated(
    "Use material3, will soon be changed to DeprecationLevel.ERROR",
    replaceWith = ReplaceWith("de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog"),
    level = DeprecationLevel.WARNING
)
@Composable
fun AcceptDialog(
    modifier: Modifier = Modifier.testTag(TestTag.AlertDialog.Modal),
    header: String,
    info: String,
    acceptText: String,
    onClickAccept: () -> Unit
) =
    AlertDialog(
        modifier = modifier,
        title = { Text(header) },
        onDismissRequest = {},
        text = { Text(info) },
        buttons = {
            TextButton(
                modifier = Modifier.testTag(TestTag.AlertDialog.ConfirmButton),
                onClick = onClickAccept
            ) {
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

fun provideWebIntentAsNewTask(url: String): Intent {
    return Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

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
    colors: TextFieldColors = erezeptTextFieldColors(),
    isError: Boolean = false,
    errorText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    keyBoardType: KeyboardType? = null
) {
    val initialValue = rememberSaveable { value }
    val undoDescription = stringResource(R.string.onb_undo_description)
    Column {
        ErezeptOutlineText(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            modifier = modifier
                .heightIn(min = 56.dp),
            singleLine = singleLine,
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
                        Icon(Icons.AutoMirrored.Rounded.Undo, null)
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

/**
 * Combines the two args to something like "created at Jan 12, 1952"
 */
@Composable
fun dateWithIntroductionString(@StringRes id: Int, instant: Instant): String {
    val formatter = rememberErpTimeFormatter()
    val date = remember { formatter.date(instant) }
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

@Composable
fun HealthPortalLink(
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.pres_detail_health_portal_description),
            style = AppTheme.typography.body2l
        )

        val linkInfo = stringResource(R.string.pres_detail_health_portal_description_url_info)
        val link = stringResource(R.string.pres_detail_health_portal_description_url)
        val uriHandler = LocalUriHandler.current
        val annotatedLink = annotatedLinkStringLight(link, linkInfo)

        SpacerSmall()
        ClickableText(
            text = annotatedLink,
            onClick = {
                annotatedLink
                    .getStringAnnotations("URL", it, it)
                    .firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUriWhenValid(stringAnnotation.item)
                    }
            },
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun rememberContentPadding(innerPadding: PaddingValues) = remember(innerPadding) {
    derivedStateOf {
        PaddingValues(
            top = PaddingDefaults.Medium,
            bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
            start = PaddingDefaults.Medium,
            end = PaddingDefaults.Medium
        )
    }
}

@Composable
fun createPhoneNumberAnnotations(
    text: String,
    textColor: Color = LocalContentColor.current,
    phoneNumberColor: Color = AppTheme.colors.primary600,
    tag: String = TestTag.Orders.Messages.PhoneNumber
): AnnotatedString = remember(text, textColor, phoneNumberColor) {
    buildAnnotatedString {
        val matcher = Patterns.PHONE.matcher(text)
        var lastIndex = 0

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                withStyle(SpanStyle(color = textColor)) {
                    append(text.substring(lastIndex, matcher.start()))
                }
            }

            val phoneNumber = matcher.group()
            pushStringAnnotation(tag, phoneNumber)
            withStyle(
                style = SpanStyle(
                    color = phoneNumberColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(phoneNumber)
            }
            pop()

            lastIndex = matcher.end()
        }

        if (lastIndex < text.length) {
            withStyle(SpanStyle(color = textColor)) {
                append(text.substring(lastIndex))
            }
        }
    }
}

@Composable
fun ClickableAnnotatedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onClick: (AnnotatedString.Range<String>) -> Unit,
    onLongPress: (() -> Unit)? = null
) {
    val textColor = style.color.takeOrElse {
        LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    textLayoutResult?.let { layout ->
                        val position = layout.getOffsetForPosition(offset)
                        text.getStringAnnotations(position, position)
                            .firstOrNull()?.let(onClick)
                    }
                },
                onLongPress = { onLongPress?.invoke() }
            )
        }
    ) {
        Text(
            onTextLayout = { textLayoutResult = it },
            text = text,
            style = style.copy(color = textColor),
            softWrap = softWrap,
            overflow = overflow,
            maxLines = maxLines
        )
    }
}

/**
 * Just a overview for formatting dates and times.
 * todo Con be deleted when ticket is closed
 * erp locales: de, ar, bg, cs, da, en, fr, iw, it, nl, pl, ro, ru, tr, uk, es, ga,
 */
@Preview(locale = "de", device = Devices.NEXUS_7)
@Composable
fun DateTimePreview() {
    PreviewAppTheme {
        val formatter = rememberErpTimeFormatter()
        val now = Clock.System.now()
        val timeString = formatter.time(now)
        val dateString = formatter.date(now)

        val nine = LocalTime.parse("09:00")
        val five = LocalTime.parse("17:00")

        Column(modifier = Modifier.padding(PaddingDefaults.Large)) {
            Line("Locale", formatter.locale)
            Line("timezone", formatter.timezone)
            Line("default SHORT")
            Line("time", formatter.time(now, style = Style.SHORT))
            Line("date", formatter.date(now, style = Style.SHORT))
            Line("ts", formatter.timestamp(now, style = Style.SHORT))
            Line("MEDIUM")
            Line("time", formatter.time(now, style = Style.MEDIUM))
            Line("date", formatter.date(now, style = Style.MEDIUM))
            Line("ts", formatter.timestamp(now, style = Style.MEDIUM))
            Line("LONG")
            Line("time", formatter.time(now, style = Style.LONG))
            Line("date", formatter.date(now, style = Style.LONG))
            Line("ts", formatter.timestamp(now, style = Style.LONG))
            Line("FULL")
            Line("time", formatter.time(now, style = Style.FULL))
            Line("date", formatter.date(now, style = Style.FULL))
            Line("ts", formatter.timestamp(now, style = Style.FULL))
            Line("ERP")
            Line("orders_timestamp", stringResource(R.string.orders_timestamp, dateString, timeString))
            Line("received_on_minute", stringResource(R.string.received_on_minute, timeString))
            Line("provided_at_hour", stringResource(R.string.provided_at_hour, timeString))

            Line("nine to five", right = "${formatter.time(nine)} - ${formatter.time(five)}")
        }
    }
}

@Composable
private fun Line(left: String, right: Any? = null) {
    if (right == null) {
        Text(
            text = left,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(color = Color.Gray)
                .fillMaxWidth()
                .padding(8.dp)
        )
    } else {
        Row {
            Text(
                left,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Thin
            )
            Text(right.toString())
        }
    }
}
