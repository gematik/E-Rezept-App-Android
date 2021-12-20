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

import android.content.Context
import android.nfc.NfcAdapter
import android.os.Build
import de.gematik.ti.erp.app.app
import de.gematik.ti.erp.app.cardwall.ui.model.CardWallData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.repository.CardWallRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
open class CardWallUseCaseProduction @Inject constructor(
    private val idpRepository: IdpRepository,
    private val cardWallRepository: CardWallRepository,
    private val profilesUseCase: ProfilesUseCase,
) : CardWallUseCase {

    override var cardWallIntroIsAccepted
        set(v) {
            cardWallRepository.introAccepted = v
        }
        get() = cardWallRepository.introAccepted

    override suspend fun cardAccessNumberWasSaved(): Flow<Boolean> {
        return profilesUseCase.activeProfileName().flatMapLatest {
            idpRepository.cardAccessNumber(it)
        }.map {
            it?.isNotBlank() == true
        }
    }

    override suspend fun setCardAccessNumber(can: String?) {
        val activeProfileName = profilesUseCase.activeProfileName().first()
        idpRepository.setCardAccessNumber(activeProfileName, can)
    }

    override fun cardAccessNumber(): Flow<String?> {
        return profilesUseCase.activeProfileName().flatMapLatest {
            idpRepository.cardAccessNumber(it)
        }
    }

    override var deviceHasNFCAndAndroidMOrHigher: Boolean
        get() = app().deviceHasNFCAndAndroidMOrHigher() || cardWallRepository.hasFakeNFCEnabled
        set(value) {
            cardWallRepository.hasFakeNFCEnabled = value
        }

    override val deviceHasNFCEnabled
        get() = app().nfcEnabled()

    override suspend fun getAuthenticationMethod(profileName: String): CardWallData.AuthenticationMethod =
        when (idpRepository.getSingleSignOnTokenScope(profileName).first()) {
            SingleSignOnToken.Scope.Default -> CardWallData.AuthenticationMethod.HealthCard
            SingleSignOnToken.Scope.AlternateAuthentication -> CardWallData.AuthenticationMethod.Alternative
            null -> CardWallData.AuthenticationMethod.None
        }
}

private fun Context.deviceHasNFCAndAndroidMOrHigher(): Boolean {
    val hasNfc = this.packageManager.hasSystemFeature("android.hardware.nfc")
    val isAndroidMOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    return hasNfc && isAndroidMOrHigher
}

private fun Context.nfcEnabled(): Boolean = if (this.deviceHasNFCAndAndroidMOrHigher()) {
    NfcAdapter.getDefaultAdapter(this).isEnabled
} else {
    false
}
