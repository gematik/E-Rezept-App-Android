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

package de.gematik.ti.erp.app.prescription.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.theme.SizeDefaults

object PrescriptionScreenData {
    @Immutable
    data class State(
        val prescriptions: List<PrescriptionUseCaseData.Prescription>,
        val redeemedPrescriptions: List<PrescriptionUseCaseData.Prescription>
    )

    sealed class AvatarDimensions {
        data class Small(
            val dimension: AvatarDimension = AvatarDimension(
                SizeDefaults.fivefold,
                SizeDefaults.double,
                SizeDefaults.doubleHalf,
                DpOffset(SizeDefaults.one, SizeDefaults.threeQuarter),
                SizeDefaults.quarter,
                SizeDefaults.oneHalf
            )
        ) : AvatarDimensions()

        data class Default(
            val dimension: AvatarDimension = AvatarDimension(
                SizeDefaults.twelvefold,
                SizeDefaults.triple,
                SizeDefaults.fivefold,
                DpOffset(SizeDefaults.oneHalf, SizeDefaults.oneHalf),
                SizeDefaults.half,
                SizeDefaults.double
            )
        ) : AvatarDimensions()
    }

    data class AvatarDimension(
        val avatarSize: Dp,
        val chooseSize: Dp,
        val statusSize: Dp,
        val statusOffset: DpOffset,
        val statusBorder: Dp,
        val iconSize: Dp
    )
}
