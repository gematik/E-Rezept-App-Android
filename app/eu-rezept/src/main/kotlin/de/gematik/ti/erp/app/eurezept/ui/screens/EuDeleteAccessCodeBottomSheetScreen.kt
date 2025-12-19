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
package de.gematik.ti.erp.app.eurezept.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.presentation.EuAccessCodeDeleteState
import de.gematik.ti.erp.app.eurezept.presentation.rememberDeleteAccessCodeBottomSheetScreenController
import de.gematik.ti.erp.app.eurezept.ui.component.AnimatedCheckLoadingIcon
import de.gematik.ti.erp.app.eurezept.ui.component.AnimatedCheckLoadingIconState.Error
import de.gematik.ti.erp.app.eurezept.ui.component.AnimatedCheckLoadingIconState.Loading
import de.gematik.ti.erp.app.eurezept.ui.component.AnimatedCheckLoadingIconState.Success
import de.gematik.ti.erp.app.eurezept.ui.component.EuLogoRounded
import de.gematik.ti.erp.app.eurezept.ui.preview.EuDeleteAccessCodeBottomSheetContentPreviewProvider
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import kotlinx.coroutines.delay

class EuDeleteAccessCodeBottomSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : BottomSheetScreen(withCloseButton = true) {
    @Composable
    override fun Content() {
        val controller = rememberDeleteAccessCodeBottomSheetScreenController()
        val state by controller.deleteState.collectAsStateWithLifecycle()

        LaunchedEffect(state) {
            if (state == EuAccessCodeDeleteState.Success) {
                delay(500)
                navController.popBackStack()
            }
        }

        EuDeleteAccessCodeBottomSheetContent(state = state) {
            controller.deleteAccessCode()
        }
    }
}

@Composable
private fun EuDeleteAccessCodeBottomSheetContent(
    state: EuAccessCodeDeleteState,
    onClick: () -> Unit
) {
    // accessibility resources
    val focusRequester = remember { FocusRequester() }
    val a11yEuAccessCodeLoadingIcon = stringResource(R.string.a11y_eu_access_code_deleting)
    val a11yEuAccessCodeSuccessIcon = stringResource(R.string.a11y_eu_access_code_deleted_success)
    val a11yEuAccessCodeErrorIcon = stringResource(R.string.a11y_eu_access_code_deleted_error)
    val a11yPrimaryButtonLoading = stringResource(R.string.a11y_eu_access_code_delete_button_disabled)
    val a11yPrimaryButtonNotLoading = stringResource(R.string.a11y_eu_access_code_delete_button)

    Column(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .animateContentSize()
            .padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        when (state) {
            EuAccessCodeDeleteState.Initial -> EuLogoRounded()

            EuAccessCodeDeleteState.Loading -> AnimatedCheckLoadingIcon(
                state = Loading,
                modifier = Modifier.semantics {
                    contentDescription = a11yEuAccessCodeLoadingIcon
                    liveRegion = LiveRegionMode.Polite
                }
            )

            EuAccessCodeDeleteState.Success -> AnimatedCheckLoadingIcon(
                state = Success,
                modifier = Modifier.semantics {
                    contentDescription = a11yEuAccessCodeSuccessIcon
                    liveRegion = LiveRegionMode.Assertive
                }
            )

            EuAccessCodeDeleteState.Error -> AnimatedCheckLoadingIcon(
                state = Error,
                modifier = Modifier.semantics {
                    contentDescription = a11yEuAccessCodeErrorIcon
                    liveRegion = LiveRegionMode.Assertive
                }
            )
        }

        Text(
            modifier = Modifier
                .padding(top = PaddingDefaults.Medium)
                .semantics { heading() },
            text = if (state == EuAccessCodeDeleteState.Error) {
                stringResource(R.string.eu_access_code_delete_failure_header)
            } else {
                stringResource(R.string.eu_access_code_delete_header)
            },
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.padding(bottom = PaddingDefaults.Medium),
            text = if (state == EuAccessCodeDeleteState.Error) {
                stringResource(R.string.eu_access_code_delete_failure_body)
            } else {
                stringResource(R.string.eu_access_code_delete_body)
            },
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )

        PrimaryButton(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = when (state) {
                        EuAccessCodeDeleteState.Loading -> a11yPrimaryButtonLoading
                        else -> a11yPrimaryButtonNotLoading
                    }
                },
            enabled = state != EuAccessCodeDeleteState.Loading,
            onClick = onClick,
            shape = RoundedCornerShape(SizeDefaults.triple),
            contentPadding = PaddingValues(
                vertical = PaddingDefaults.MediumSmall,
                horizontal = PaddingDefaults.XXLargePlus
            )
        ) {
            Text(
                text = stringResource(R.string.eu_access_code_delete_button_text)
            )
        }
    }
}

@LightDarkPreview
@Composable
fun EuDeleteAccessCodeBottomSheetContentPreview(
    @PreviewParameter(EuDeleteAccessCodeBottomSheetContentPreviewProvider::class)
    previewData: EuAccessCodeDeleteState
) {
    PreviewTheme {
        EuDeleteAccessCodeBottomSheetContent(state = previewData) { }
    }
}
