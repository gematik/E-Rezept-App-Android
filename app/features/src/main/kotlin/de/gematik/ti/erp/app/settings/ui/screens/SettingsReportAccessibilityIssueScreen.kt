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

package de.gematik.ti.erp.app.settings.ui.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.topbar.AnimatedTitleContent
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.buildAccessibilityReportBodyWithDeviceInfo
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.openMailClient
import org.kodein.di.compose.rememberInstance
import kotlin.getValue

class SettingsReportAccessibilityIssueScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val listState = rememberLazyListState()

        val buildConfig by rememberInstance<BuildConfigInformation>()
        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        val talkbackEnabled = try {
            val accessibilityManager: AccessibilityManager =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                    ?: throw IllegalStateException("AccessibilityManager not found")
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
            enabledServices.isNotEmpty()
        } catch (e: Exception) {
            false
        }
        val yes = stringResource(R.string.yes)
        val no = stringResource(R.string.no)

        val mailAddress = stringResource(R.string.settings_report_accessibility_issue_mail_address)
        val subject = stringResource(R.string.settings_report_accessibility_issue_mail_subject)
        val body = buildAccessibilityReportBodyWithDeviceInfo(
            darkMode = buildConfig.inDarkTheme(),
            versionName = buildConfig.versionName(),
            language = buildConfig.language(),
            phoneModel = buildConfig.model(),
            talkback = if (talkbackEnabled) yes else no,
            fontScale = configuration.fontScale
        )

        val onBack by rememberUpdatedState { navController.popBackStack() }
        BackHandler { onBack() }
        SettingsReportAccessibilityIssueScreenScaffold(
            onBack = { onBack() },
            listState = listState,
            onClickReport = {
                openMailClient(
                    context = context,
                    address = mailAddress,
                    subject = subject,
                    body = body
                )
            }
        )
    }
}

@Composable
private fun SettingsReportAccessibilityIssueScreenScaffold(
    listState: LazyListState,
    onBack: () -> Unit,
    onClickReport: () -> Unit
) {
    AnimatedElevationScaffold(
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        listState = listState,
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        topBarTitle = {
            AnimatedTitleContent(
                listState = listState,
                title = stringResource(R.string.settings_report_accessibility_issue_title)
            )
        },
        bottomBar = {
            SettingsReportAccessibilityIssueBottomBar(
                onClickReport = onClickReport
            )
        }
    ) { innerPadding ->
        SettingsReportAccessibilityIssueScreenContent(
            listState = listState,
            innerPadding = innerPadding
        )
    }
}

@Composable
fun SettingsReportAccessibilityIssueScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues
) {
    LazyColumn(
        state = listState,
        contentPadding = innerPadding
    ) {
        settingsReportAccessibilityIssueTitleSection()
        settingsReportAccessibilityIssueWhatToReportSection()
        settingsReportAccessibilityIssueWhatNotToReportSection()
        settingsReportAccessibilityIssueHowToReportSection()
        settingsReportAccessibilityIssueWhatIsNextSection()
    }
}

private fun LazyListScope.settingsReportAccessibilityIssueTitleSection() {
    item {
        SpacerMedium()
        Text(
            modifier = Modifier.semanticsHeading().padding(horizontal = PaddingDefaults.Medium),
            text = stringResource(R.string.settings_report_accessibility_issue_title),
            style = MaterialTheme.typography.h4
        )
    }
    item {
        Text(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            text = stringResource(R.string.settings_report_accessibility_issue_description),
            style = MaterialTheme.typography.body1,
            color = AppTheme.colors.neutral700
        )
        SpacerXLarge()
    }
}

private fun LazyListScope.settingsReportAccessibilityIssueWhatToReportSection() {
    item {
        Column() {
            Text(
                modifier = Modifier.semanticsHeading().padding(horizontal = PaddingDefaults.Medium),
                text = stringResource(R.string.settings_report_accessibility_issue_what_to_report_title),
                style = MaterialTheme.typography.h6
            )
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_what_to_report_item1),
                        style = MaterialTheme.typography.body1
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.green600
                    )
                }
            )
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_what_to_report_item2),
                        style = MaterialTheme.typography.body1
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.green600
                    )
                }
            )
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_what_to_report_item3),
                        style = MaterialTheme.typography.body1
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.green600
                    )
                }
            )
            SpacerXLarge()
        }
    }
}

private fun LazyListScope.settingsReportAccessibilityIssueWhatNotToReportSection() {
    item {
        Column() {
            Text(
                modifier = Modifier.semanticsHeading().padding(horizontal = PaddingDefaults.Medium),
                text = stringResource(R.string.settings_report_accessibility_issue_what_not_to_report_title),
                style = MaterialTheme.typography.h6
            )
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_what_not_to_report_item1),
                        style = MaterialTheme.typography.body1
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = AppTheme.colors.red600
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_what_not_to_report_item1_description),
                        style = MaterialTheme.typography.caption
                    )
                }
            )
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_what_not_to_report_item2),
                        style = MaterialTheme.typography.body1
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = AppTheme.colors.red600
                    )
                }
            )
            SpacerXLarge()
        }
    }
}

private fun LazyListScope.settingsReportAccessibilityIssueHowToReportSection() {
    item {
        Column() {
            Text(
                modifier = Modifier.semanticsHeading().padding(horizontal = PaddingDefaults.Medium),
                text = stringResource(R.string.settings_report_accessibility_issue_how_to_report_title),
                style = MaterialTheme.typography.h6
            )
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_how_to_report_item1),
                        style = MaterialTheme.typography.body1
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.green600
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_how_to_report_item1_description),
                        style = MaterialTheme.typography.caption
                    )
                }
            )
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_how_to_report_item2),
                        style = MaterialTheme.typography.body1
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.green600
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_how_to_report_item2_description),
                        style = MaterialTheme.typography.caption
                    )
                }
            )
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_how_to_report_item3),
                        style = MaterialTheme.typography.body1
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.green600
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_how_to_report_item3_description),
                        style = MaterialTheme.typography.caption
                    )
                }
            )
            ListItem(
                colors = GemListItemDefaults.gemListItemColors(),
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_report_accessibility_issue_how_to_report_item4),
                        style = MaterialTheme.typography.body1
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.green600
                    )
                }
            )
            SpacerXLarge()
        }
    }
}

private fun LazyListScope.settingsReportAccessibilityIssueWhatIsNextSection() {
    item {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium)
                .semantics(mergeDescendants = true) {},
            colors = CardDefaults.cardColors().copy(containerColor = AppTheme.colors.neutral000),
            shape = RoundedCornerShape(SizeDefaults.double),
            border = BorderStroke(width = 1.dp, color = AppTheme.colors.primary500)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaddingDefaults.Medium)
            ) {
                Text(
                    text = stringResource(R.string.settings_report_accessibility_issue_what_is_next_title),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = stringResource(R.string.settings_report_accessibility_issue_what_is_next_description),
                    style = MaterialTheme.typography.caption
                )
            }
        }
        SpacerXLarge()
    }
}

@Composable
private fun SettingsReportAccessibilityIssueBottomBar(
    onClickReport: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.navigationBarsPadding(),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryButtonSmall(
                onClick = onClickReport
            ) {
                Text(text = stringResource(R.string.settings_report_accessibility_issue_button))
            }
        }
    }
}

@Preview
@Composable
fun SettingsReportAccessibilityIssueScreenPreview() {
    AppTheme {
        SettingsReportAccessibilityIssueScreenScaffold(
            listState = rememberLazyListState(),
            onClickReport = {},
            onBack = {}
        )
    }
}
