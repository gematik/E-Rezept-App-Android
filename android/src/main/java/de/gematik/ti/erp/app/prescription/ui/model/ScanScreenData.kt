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

package de.gematik.ti.erp.app.prescription.ui.model

import androidx.compose.runtime.Immutable

object ScanScreenData {
    enum class ScanState {
        Hold,
        Save,
        Error,
        Final
    }

    enum class VibrationPattern {
        None,
        Focused,
        Saved,
        Error
    }

    sealed class Info {
        object Focus : Info()
        object ErrorNotValid : Info()
        object ErrorDuplicated : Info()

        @Immutable
        data class Scanned(val nr: Int) : Info()
    }

    @Immutable
    data class ActionBar(val totalNrOfPrescriptions: Int, val totalNrOfCodes: Int) {
        fun shouldShow() =
            totalNrOfPrescriptions > 0 && totalNrOfCodes > 0
    }

    @Immutable
    data class OverlayState(
        val area: FloatArray?,
        val state: ScanState,
        val info: Info
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OverlayState

            if (area != null) {
                if (other.area == null) return false
                if (!area.contentEquals(other.area)) return false
            } else if (other.area != null) return false
            if (state != other.state) return false

            return true
        }

        override fun hashCode(): Int {
            var result = area?.contentHashCode() ?: 0
            result = 31 * result + state.hashCode()
            return result
        }
    }

    val defaultOverlayState = OverlayState(null, ScanState.Hold, Info.Focus)

    @Immutable
    data class State(
        val snackBar: ActionBar
    ) {
        fun hasCodesToSave() =
            snackBar.shouldShow()
    }

    val defaultScreenState = State(
        snackBar = ActionBar(0, 0)
    )
}
