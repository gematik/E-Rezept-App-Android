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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.debugsettings.presentation

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.ErezeptApp
import de.gematik.ti.erp.app.VisibleDebugTree
import de.gematik.ti.erp.app.appupdate.usecase.ChangeAppUpdateManagerFlagUseCase
import de.gematik.ti.erp.app.appupdate.usecase.GetAppUpdateManagerFlagUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import de.gematik.ti.erp.app.debugsettings.data.DebugSettingsData
import de.gematik.ti.erp.app.debugsettings.data.Environment
import de.gematik.ti.erp.app.di.DebugSettings.getDebugSettingsDataForEnvironment
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.featuretoggle.Features
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.AccessToken
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.invoice.usecase.SaveInvoiceUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.MarkAllUnreadMessagesAsReadUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyDirectRedeemUseCase
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetTaskIdsUseCase
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.vau.repository.VauRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.io.pem.PemReader
import org.jose4j.base64url.Base64Url
import org.jose4j.jws.EcdsaUsingShaAlgorithm
import java.math.BigInteger
import java.security.KeyFactory
import java.security.Signature
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

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
    private val saveInvoiceUseCase: SaveInvoiceUseCase,
    private val idpUseCase: IdpUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val featureToggleManager: FeatureToggleManager,
    private val pharmacyDirectRedeemUseCase: PharmacyDirectRedeemUseCase,
    private val getAppUpdateManagerFlagUseCase: GetAppUpdateManagerFlagUseCase,
    private val changeAppUpdateManagerFlagUseCase: ChangeAppUpdateManagerFlagUseCase,
    private val markAllUnreadMessagesAsReadUseCase: MarkAllUnreadMessagesAsReadUseCase,
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase,
    private val getTaskIdsUseCase: GetTaskIdsUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel() {

    private val appUpdateManager = MutableStateFlow(true)
    val appUpdateManagerState
        @Composable
        get() = appUpdateManager.collectAsStateWithLifecycle()

    private val _messageMarkingLoading = MutableStateFlow(false)
    val messageMarkingLoadingState
        @Composable
        get() = _messageMarkingLoading.collectAsStateWithLifecycle()

    private val _prescriptionDeletionLoading = MutableStateFlow(false)
    val prescriptionDeletionLoadingState
        @Composable
        get() = _prescriptionDeletionLoading.collectAsStateWithLifecycle()

    init {
        viewModelScope.launch {
            val value = getAppUpdateManagerFlagUseCase()
            appUpdateManager.value = value
        }
    }

    var debugSettingsData by mutableStateOf(createDebugSettingsData())

    val rotatingLog = visibleDebugTree.rotatingLog

    private fun createDebugSettingsData() = DebugSettingsData(
        eRezeptServiceURL = endpointHelper.eRezeptServiceUri,
        eRezeptActive = endpointHelper.isUriOverridden(EndpointHelper.EndpointUri.BASE_SERVICE_URI),
        idpUrl = endpointHelper.idpServiceUri,
        idpActive = endpointHelper.isUriOverridden(EndpointHelper.EndpointUri.IDP_SERVICE_URI),
        apoVzdPharmacyServiceUrl = endpointHelper.pharmacyApoVzdBaseUri,
        fhirVzdPharmacyServiceUrl = endpointHelper.pharmacyFhirVzdBaseUri,
        fhirVzdPharmacySearchAccessTokenUrl = endpointHelper.pharmacyFhirVzdSearchAccessTokenUri,
        pharmacyServiceActive = endpointHelper.isUriOverridden(EndpointHelper.EndpointUri.PHARMACY_SERVICE_URI),
        bearerToken = "",
        bearerTokenIsSet = true,
        fakeNFCCapabilities = false,
        cardAccessNumberIsSet = false,
        multiProfile = false,
        activeProfileId = "",
        virtualHealthCardCert = HealthCardCert,
        virtualHealthCardPrivateKey = HealthCardCertPrivateKey,
        clientId = endpointHelper.getClientId()
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
                bearerToken = idpRepository.decryptedAccessToken(it).first()?.accessToken ?: ""
            )
        )
    }

    fun updateState(debugSettingsData: DebugSettingsData) {
        this.debugSettingsData = debugSettingsData
    }

    fun selectEnvironment(environment: Environment) {
        updateState(
            debugSettingsData.getDebugSettingsDataForEnvironment(environment)
        )
    }

    fun changeBearerToken(activeProfileId: ProfileIdentifier) {
        idpRepository.saveDecryptedAccessToken(
            activeProfileId,
            AccessToken(
                debugSettingsData.bearerToken,
                Clock.System.now().plus(
                    5.minutes
                )
            )
        )
        updateState(debugSettingsData.copy(bearerTokenIsSet = true))
    }

    suspend fun breakSSOToken() {
        withContext(dispatchers.io) {
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
                    profileId = activeProfileId,
                    token = newToken
                )
                idpRepository.invalidateDecryptedAccessToken(activeProfileId)
            }
        }
    }

    private fun IdpData.SingleSignOnToken.breakToken(): IdpData.SingleSignOnToken {
        val (_, rest) = this.token.split('.', limit = 2)
        val someHoursBeforeNow = Instant.now().minus(48, ChronoUnit.HOURS).epochSecond
        val headerWithExpiresOn =
            Base64Url.encodeUtf8ByteRepresentation("""{"exp":$someHoursBeforeNow}""")
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
            debugSettingsData.apoVzdPharmacyServiceUrl,
            debugSettingsData.pharmacyServiceActive
        )
        endpointHelper.setUriOverride(
            EndpointHelper.EndpointUri.PHARMACY_FHIRVZD_SERVICE_URI,
            debugSettingsData.fhirVzdPharmacyServiceUrl,
            debugSettingsData.pharmacyServiceActive
        )
        endpointHelper.setUriOverride(
            EndpointHelper.EndpointUri.PHARMACY_FHIRVZD_SEARCH_ACCESS_TOKEN_URI,
            debugSettingsData.fhirVzdPharmacySearchAccessTokenUrl,
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
        cardWallUseCase.updateDeviceNFCCapability(value)
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
        val context = ErezeptApp.applicationModule.androidContext()
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
    ) = withContext(dispatchers.io) {
        idpUseCase.authenticationFlowWithHealthCard(
            profileId = profilesUseCase.activeProfileId().first(),
            cardAccessNumber = "123123",
            healthCardCertificate = { java.util.Base64.getDecoder().decode(certificateBase64) },
            sign = {
                val curveSpec = ECNamedCurveTable.getParameterSpec("brainpoolP256r1")
                val keySpec =
                    ECPrivateKeySpec(
                        BigInteger(
                            1,
                            java.util.Base64.getDecoder().decode(privateKeyBase64)
                        ),
                        curveSpec
                    )
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

        pharmacyDirectRedeemUseCase.redeemPrescriptionDirectly(
            url = url,
            message = message,
            telematikId = "",
            recipientCertificates = certificates,
            transactionId = UUID.randomUUID().toString()
        ).getOrThrow()
    }

    fun saveInvoice(invoiceBundle: String) {
        viewModelScope.launch {
            val profileId = profilesUseCase.activeProfileId().first()
            val bundle = Json.parseToJsonElement(invoiceBundle)
            saveInvoiceUseCase.invoke(profileId, bundle)
        }
    }

    fun changeAppUpdateManager(useOriginal: Boolean) {
        viewModelScope.launch {
            appUpdateManager.value = useOriginal
            changeAppUpdateManagerFlagUseCase(useOriginal)
        }
    }

    fun markAllUnreadMessagesAsRead(
        onComplete: suspend (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = try {
                _messageMarkingLoading.value = true
                withContext(dispatchers.io) {
                    markAllUnreadMessagesAsReadUseCase()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                _messageMarkingLoading.value = false
            }
            onComplete(result)
        }
    }

    fun deleteAllPrescriptions(
        profileId: ProfileIdentifier,
        deleteLocallyOnly: Boolean = false,
        onComplete: suspend (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = try {
                _prescriptionDeletionLoading.value = true

                val taskIds = getTaskIdsUseCase(profileId)
                if (taskIds.isEmpty()) {
                    throw IllegalStateException("No prescriptions to delete")
                }

                withContext(dispatchers.io) {
                    coroutineScope {
                        taskIds.forEach { taskId ->
                            launch {
                                deletePrescriptionUseCase(profileId, taskId, deleteLocallyOnly).first()
                            }
                        }
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                _prescriptionDeletionLoading.value = false
            }
            onComplete(result)
        }
    }
}
