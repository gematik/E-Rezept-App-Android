/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.common.usecase

import android.content.SharedPreferences
import androidx.core.content.edit
import de.gematik.ti.erp.app.common.usecase.model.CancellableHint
import de.gematik.ti.erp.app.common.usecase.model.Hint
import de.gematik.ti.erp.app.di.ApplicationPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

private val knownCancellableHints = CancellableHint::class.sealedSubclasses.mapNotNull { it.objectInstance }.toSet()
private const val preferencePrefix = "CancellableHint_"

class HintUseCase @Inject constructor(
    @ApplicationPreferences
    private val preferences: SharedPreferences
) {
    private val _cancelledHints = MutableStateFlow(setOf<Hint>())
    val cancelledHints: Flow<Set<Hint>>
        get() = _cancelledHints

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key.startsWith(preferencePrefix)) {
            val k = key.removePrefix(preferencePrefix)
            val hint = requireNotNull(
                knownCancellableHints.find {
                    it.id == k
                }
            )

            if (isHintCanceled(hint)) {
                _cancelledHints.value += hint
            } else {
                _cancelledHints.value -= hint
            }
        }
    }

    init {
        _cancelledHints.value = knownCancellableHints
            .filter {
                isHintCanceled(it)
            }
            .toSet()

        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun isHintCanceled(hint: CancellableHint): Boolean {
        require(knownCancellableHints.contains(hint))
        return preferences.getBoolean(hint.prefKey(), false)
    }

    fun cancelHint(hint: CancellableHint) {
        require(knownCancellableHints.contains(hint))
        preferences.edit {
            putBoolean(hint.prefKey(), true)
        }
    }

    fun resetAllHints() {
        preferences.edit {
            knownCancellableHints.forEach {
                putBoolean(it.prefKey(), false)
            }
        }
    }

    fun resetHint(hint: CancellableHint) {
        require(knownCancellableHints.contains(hint))
        preferences.edit {
            putBoolean(hint.prefKey(), false)
        }
    }

    private fun CancellableHint.prefKey() = preferencePrefix + this.id
}
