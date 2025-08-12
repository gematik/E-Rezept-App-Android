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

package de.gematik.ti.erp.app.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.settings.model.HealthCardClickActions
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.LabelButton
import de.gematik.ti.erp.app.utils.extensions.sectionPadding

@Composable
fun HealthCardSection(
    healthCardClickActions: HealthCardClickActions
) {
    Column {
        Text(
            text = stringResource(R.string.health_card_section_header),
            style = AppTheme.typography.h6,
            modifier = Modifier
                .sectionPadding()
                .semanticsHeading()
        )
        LabelButton(
            modifier = Modifier.testTag(TestTag.Settings.OrderNewCardButton),
            icon = painterResource(R.drawable.ic_order_egk),
            text = stringResource(R.string.health_card_section_order_card)
        ) {
            healthCardClickActions.onClickOrderHealthCard()
        }
        LabelButton(
            Icons.AutoMirrored.Outlined.HelpOutline,
            stringResource(R.string.health_card_section_unlock_card_forgot_pin)
        ) {
            healthCardClickActions.onClickUnlockEgk(UnlockMethod.ResetRetryCounterWithNewSecret)
        }
        LabelButton(
            painterResource(R.drawable.ic_reset_pin),
            stringResource(R.string.health_card_section_unlock_card_reset_pin)
        ) {
            healthCardClickActions.onClickUnlockEgk(UnlockMethod.ChangeReferenceData)
        }
        LabelButton(
            Icons.Outlined.LockOpen,
            stringResource(R.string.health_card_section_unlock_card_no_reset)
        ) {
            healthCardClickActions.onClickUnlockEgk(UnlockMethod.ResetRetryCounter)
        }
    }
}
