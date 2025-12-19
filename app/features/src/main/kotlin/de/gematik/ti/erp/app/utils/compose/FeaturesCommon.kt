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
import android.net.Uri
import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.ErpTimeFormatter.Style
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.material3.components.switchs.GemSwitch
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
        val textColor = if (enabled) AppTheme.colors.neutral900 else AppTheme.colors.neutral700
        val descriptionColor = if (enabled) AppTheme.colors.neutral700 else AppTheme.colors.neutral400

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
            GemSwitch(
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

fun provideWebIntent(address: String) = Intent(Intent.ACTION_VIEW, Uri.parse(address))

fun provideWebIntentAsNewTask(url: String): Intent {
    return Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

fun providePhoneIntent(phoneNumber: String) =
    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))

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
                    LocalContentColor provides AppTheme.colors.red700
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
