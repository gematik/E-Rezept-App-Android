/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.pharmacy.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.GetShowTelematikIdStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.ChangePharmacyFavoriteStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.IsPharmacyFavoriteUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class PharmacyDetailsController(
    private val isPharmacyFavoriteUseCase: IsPharmacyFavoriteUseCase,
    private val changePharmacyFavoriteStateUseCase: ChangePharmacyFavoriteStateUseCase,
    private val getShowTelematikIdStateUseCase: GetShowTelematikIdStateUseCase
) : Controller() {

    private val _isPharmacyFavorite = MutableStateFlow(false)
    val isPharmacyFavorite = _isPharmacyFavorite.asStateFlow()

    val showTelematikId = getShowTelematikIdStateUseCase.invoke()

    fun isPharmacyFavorite(pharmacy: PharmacyUseCaseData.Pharmacy) {
        controllerScope.launch {
            isPharmacyFavoriteUseCase(pharmacy).collectLatest {
                _isPharmacyFavorite.value = it
            }
        }
    }

    fun changePharmacyAsFavorite(pharmacy: PharmacyUseCaseData.Pharmacy, state: Boolean) {
        controllerScope.launch {
            changePharmacyFavoriteStateUseCase(pharmacy, state)
            _isPharmacyFavorite.value = state
        }
    }
}

@Composable
internal fun rememberPharmacyDetailsController(): PharmacyDetailsController {
    val isPharmacyFavoriteUseCase by rememberInstance<IsPharmacyFavoriteUseCase>()
    val changePharmacyFavoriteStateUseCase by rememberInstance<ChangePharmacyFavoriteStateUseCase>()
    val getShowTelematikIdStateUseCase by rememberInstance<GetShowTelematikIdStateUseCase>()

    return remember {
        PharmacyDetailsController(
            isPharmacyFavoriteUseCase = isPharmacyFavoriteUseCase,
            changePharmacyFavoriteStateUseCase = changePharmacyFavoriteStateUseCase,
            getShowTelematikIdStateUseCase = getShowTelematikIdStateUseCase
        )
    }
}
