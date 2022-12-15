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

package de.gematik.ti.erp.app.debug.ui

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.App
import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.VisibleDebugTree
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import de.gematik.ti.erp.app.debug.data.DebugSettingsData
import de.gematik.ti.erp.app.debug.data.Environment
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.featuretoggle.Features
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyDirectRedeemUseCase
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.vau.repository.VauRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.io.pem.PemReader
import org.jose4j.base64url.Base64Url
import org.jose4j.jws.EcdsaUsingShaAlgorithm
import java.math.BigInteger
import java.security.KeyFactory
import java.security.Signature
import java.time.Instant
import java.time.temporal.ChronoUnit

private val HealthCardCert = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE
private val HealthCardCertPrivateKey = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY

@Suppress("LongParameterList")
class DebugSettingsViewModel(
    visibleDebugTree: VisibleDebugTree,
    private val endpointHelper: EndpointHelper,
    private val cardWallUseCase: CardWallUseCase,
    private val prescriptionUseCase: PrescriptionUseCase,
    private val vauRepository: VauRepository,
    private val idpRepository: IdpRepository,
    private val idpUseCase: IdpUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val featureToggleManager: FeatureToggleManager,
    private val pharmacyDirectRedeemUseCase: PharmacyDirectRedeemUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel() {

    var debugSettingsData by mutableStateOf(createDebugSettingsData())

    val rotatingLog = visibleDebugTree.rotatingLog

    private fun createDebugSettingsData() = DebugSettingsData(
        eRezeptServiceURL = endpointHelper.eRezeptServiceUri,
        eRezeptActive = endpointHelper.isUriOverridden(EndpointHelper.EndpointUri.BASE_SERVICE_URI),
        idpUrl = endpointHelper.idpServiceUri,
        idpActive = endpointHelper.isUriOverridden(EndpointHelper.EndpointUri.IDP_SERVICE_URI),
        pharmacyServiceUrl = endpointHelper.pharmacySearchBaseUri,
        pharmacyServiceActive = endpointHelper.isUriOverridden(EndpointHelper.EndpointUri.PHARMACY_SERVICE_URI),
        bearerToken = "",
        bearerTokenIsSet = true,
        fakeNFCCapabilities = cardWallUseCase.deviceHasNFCAndAndroidMOrHigher,
        cardAccessNumberIsSet = false,
        multiProfile = false,
        activeProfileId = "",
        virtualHealthCardCert = HealthCardCert,
        virtualHealthCardPrivateKey = HealthCardCertPrivateKey
    )

    suspend fun state() {
        val it = profilesUseCase.activeProfileId().first()
        updateState(
            debugSettingsData.copy(
                cardAccessNumberIsSet = (
                    cardWallUseCase.authenticationData(it)
                        .first().singleSignOnTokenScope as? IdpData.TokenWithHealthCardScope
                    )?.cardAccessNumber?.isNotEmpty()
                    ?: false,
                activeProfileId = it,
                bearerToken = idpRepository.decryptedAccessToken(it).first() ?: ""
            )
        )
    }

    fun updateState(debugSettingsData: DebugSettingsData) {
        this.debugSettingsData = debugSettingsData
    }

    fun selectEnvironment(environment: Environment) {
        updateState(getDebugSettingsdataForEnvironment(environment))
    }

    private fun getDebugSettingsdataForEnvironment(environment: Environment): DebugSettingsData {
        return when (environment) {
            Environment.PU -> debugSettingsData.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_PU,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_PU,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_PU,
                pharmacyServiceActive = true
            )
            Environment.TU -> debugSettingsData.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_TU,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_TU,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_RU,
                pharmacyServiceActive = true
            )
            Environment.RU -> debugSettingsData.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_RU,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_RU,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_RU,
                pharmacyServiceActive = true
            )
            Environment.TR -> debugSettingsData.copy(
                eRezeptServiceURL = BuildKonfig.BASE_SERVICE_URI_TR,
                eRezeptActive = true,
                idpUrl = BuildKonfig.IDP_SERVICE_URI_TR,
                idpActive = true,
                pharmacyServiceUrl = BuildKonfig.PHARMACY_SERVICE_URI_RU,
                pharmacyServiceActive = true
            )
        }
    }

    fun changeBearerToken(activeProfileId: ProfileIdentifier) {
        idpRepository.saveDecryptedAccessToken(activeProfileId, debugSettingsData.bearerToken)
        updateState(debugSettingsData.copy(bearerTokenIsSet = true))
    }

    suspend fun breakSSOToken() {
        withContext(dispatchers.IO) {
            val activeProfileId = profilesUseCase.activeProfileId().first()
            idpRepository.authenticationData(activeProfileId).first().singleSignOnTokenScope?.let {
                val newToken = when (it) {
                    is IdpData.AlternateAuthenticationToken ->
                        IdpData.AlternateAuthenticationToken(
                            token = it.token?.breakToken(),
                            cardAccessNumber = it.cardAccessNumber,
                            aliasOfSecureElementEntry = it.aliasOfSecureElementEntry,
                            healthCardCertificate = it.healthCardCertificate.encoded
                        )
                    is IdpData.DefaultToken ->
                        IdpData.DefaultToken(
                            token = it.token?.breakToken(),
                            cardAccessNumber = it.cardAccessNumber,
                            healthCardCertificate = it.healthCardCertificate.encoded
                        )
                    is IdpData.ExternalAuthenticationToken ->
                        IdpData.ExternalAuthenticationToken(
                            token = it.token?.breakToken(),
                            authenticatorName = it.authenticatorName,
                            authenticatorId = it.authenticatorId
                        )
                    else -> it
                }
                idpRepository.saveSingleSignOnToken(
                    activeProfileId,
                    newToken
                )
                Napier.d("SSO token is now: $newToken", tag = "Debug Settings")
            }
        }
    }

    private fun IdpData.SingleSignOnToken.breakToken(): IdpData.SingleSignOnToken {
        val (_, rest) = this.token.split('.', limit = 2)
        val someHoursBeforeNow = Instant.now().minus(48, ChronoUnit.HOURS).epochSecond
        val headerWithExpiresOn = Base64Url.encodeUtf8ByteRepresentation("""{"exp":$someHoursBeforeNow}""")
        return IdpData.SingleSignOnToken("$headerWithExpiresOn.$rest")
    }

    suspend fun saveAndRestartApp() {
        endpointHelper.setUriOverride(
            EndpointHelper.EndpointUri.BASE_SERVICE_URI,
            debugSettingsData.eRezeptServiceURL,
            debugSettingsData.eRezeptActive
        )
        endpointHelper.setUriOverride(
            EndpointHelper.EndpointUri.IDP_SERVICE_URI,
            debugSettingsData.idpUrl,
            debugSettingsData.idpActive
        )
        endpointHelper.setUriOverride(
            EndpointHelper.EndpointUri.PHARMACY_SERVICE_URI,
            debugSettingsData.pharmacyServiceUrl,
            debugSettingsData.pharmacyServiceActive
        )
        profilesUseCase.profiles.flowOn(Dispatchers.IO).first().forEach {
            idpRepository.invalidate(it.id)
        }
        vauRepository.invalidate()
        restart()
    }

    fun getCurrentEnvironment() = endpointHelper.getCurrentEnvironment()

    fun allowNfc(value: Boolean) {
        cardWallUseCase.deviceHasNFCAndAndroidMOrHigher = value
        updateState(debugSettingsData.copy(fakeNFCCapabilities = value))
    }

    fun refreshPrescriptions() {
        viewModelScope.launch {
            prescriptionUseCase.downloadTasks(profilesUseCase.activeProfileId().first())
        }
    }

    fun features() = featureToggleManager.features

    fun featuresState() =
        featureToggleManager.featuresState()

    fun toggleFeature(feature: Features) {
        viewModelScope.launch {
            val key = booleanPreferencesKey(feature.featureName)
            featureToggleManager.toggleFeature(key)
        }
    }

    private fun restart() {
        val context = App.appContext
        val packageManager: PackageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    fun onResetVirtualHealthCard() {
        updateState(
            debugSettingsData.copy(
                virtualHealthCardCert = HealthCardCert,
                virtualHealthCardPrivateKey = HealthCardCertPrivateKey
            )
        )
    }

    fun onSetVirtualHealthCardCertificate(cert: String) {
        updateState(debugSettingsData.copy(virtualHealthCardCert = cert))
    }

    fun onSetVirtualHealthCardPrivateKey(privateKey: String) {
        updateState(debugSettingsData.copy(virtualHealthCardPrivateKey = privateKey))
    }

    fun getVirtualHealthCardCertificateSubjectInfo(): String =
        try {
            X509CertificateHolder(Base64.decode(debugSettingsData.virtualHealthCardCert)).subject.toString()
        } catch (e: Exception) {
            e.message ?: "Error"
        }

    suspend fun onTriggerVirtualHealthCard(
        certificateBase64: String,
        privateKeyBase64: String
    ) = withContext(dispatchers.IO) {
        idpUseCase.authenticationFlowWithHealthCard(
            profileId = profilesUseCase.activeProfileId().first(),
            cardAccessNumber = "123123",
            healthCardCertificate = { Base64.decode(certificateBase64) },
            sign = {
                val curveSpec = ECNamedCurveTable.getParameterSpec("brainpoolP256r1")
                val keySpec =
                    org.bouncycastle.jce.spec.ECPrivateKeySpec(BigInteger(Base64.decode(privateKeyBase64)), curveSpec)
                val privateKey = KeyFactory.getInstance("EC", BCProvider).generatePrivate(keySpec)
                val signed = Signature.getInstance("NoneWithECDSA").apply {
                    initSign(privateKey)
                    update(it)
                }.sign()
                EcdsaUsingShaAlgorithm.convertDerToConcatenated(signed, 64)
            }
        )
    }

    suspend fun redeemDirect(
        url: String,
        message: String,
        certificatesPEM: String
    ) {
        val pemReader = PemReader(certificatesPEM.reader())

        val certificates = mutableListOf<X509CertificateHolder>()
        do {
            val obj = pemReader.readPemObject()
            if (obj != null) {
                certificates += X509CertificateHolder(obj.content)
            }
        } while (obj != null)

        pharmacyDirectRedeemUseCase.redeemPrescription(
            url = url,
            message = message,
            telematikId = "",
            recipientCertificates = certificates
        ).getOrThrow()
    }
}
