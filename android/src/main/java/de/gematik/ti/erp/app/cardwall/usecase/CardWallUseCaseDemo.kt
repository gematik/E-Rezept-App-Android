/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.usecase

import de.gematik.ti.erp.app.cardwall.ui.model.CardWallData
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CardWallUseCaseDemo @Inject constructor() : CardWallUseCase {

    override var cardWallIntroIsAccepted: Boolean = false

    override var deviceHasNFCAndAndroidMOrHigher = true

    override val deviceHasNFCEnabled = true

    override suspend fun cardAccessNumberWasSaved(): Flow<Boolean> = flow { emit(false) }

    override suspend fun getAuthenticationMethod(profileName: String): CardWallData.AuthenticationMethod =
        CardWallData.AuthenticationMethod.None

    override suspend fun setCardAccessNumber(can: String?) {}

    override fun cardAccessNumber() = flow {
        emit(null)
    }
}
