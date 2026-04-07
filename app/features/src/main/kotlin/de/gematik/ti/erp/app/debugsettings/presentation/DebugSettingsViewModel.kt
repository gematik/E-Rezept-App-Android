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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.debugsettings.presentation

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.ErezeptApp
import de.gematik.ti.erp.app.appupdate.usecase.ChangeAppUpdateManagerFlagUseCase
import de.gematik.ti.erp.app.appupdate.usecase.GetAppUpdateManagerFlagUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import de.gematik.ti.erp.app.consent.usecase.RevokeConsentUseCase
import de.gematik.ti.erp.app.database.datastore.featuretoggle.FeatureEntity
import de.gematik.ti.erp.app.database.datastore.virtualhealthcard.VirtualHealthCardLocalDataSource
import de.gematik.ti.erp.app.database.settings.CommunicationDigaVersion
import de.gematik.ti.erp.app.database.settings.CommunicationDigaVersionDataStore
import de.gematik.ti.erp.app.database.settings.CommunicationVersion
import de.gematik.ti.erp.app.database.settings.CommunicationVersionDataStore
import de.gematik.ti.erp.app.database.settings.ConsentVersion
import de.gematik.ti.erp.app.database.settings.ConsentVersionDataStore
import de.gematik.ti.erp.app.database.settings.EuVersion
import de.gematik.ti.erp.app.database.settings.EuVersionDataStore
import de.gematik.ti.erp.app.datastore.featuretoggle.FeatureToggleRepository
import de.gematik.ti.erp.app.debugsettings.data.DebugSettingsData
import de.gematik.ti.erp.app.debugsettings.data.Environment
import de.gematik.ti.erp.app.di.DebugSettings.getDebugSettingsDataForEnvironment
import de.gematik.ti.erp.app.di.EndpointHelper
import de.gematik.ti.erp.app.digas.domain.usecase.GetIknrUseCase
import de.gematik.ti.erp.app.digas.domain.usecase.UpdateIknrUseCase
import de.gematik.ti.erp.app.fhir.consent.model.ConsentCategory
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.AccessToken
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.invoice.usecase.SaveInvoiceUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.MarkAllUnreadMessagesAsReadUseCase
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetTaskIdsUseCase
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.usecase.GetAndroid8DeprecationOverrideUseCase
import de.gematik.ti.erp.app.settings.usecase.ResetOnboardingUseCase
import de.gematik.ti.erp.app.settings.usecase.SetAndroid8DeprecationOverrideUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import de.gematik.ti.erp.app.vau.repository.VauRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
import org.jose4j.base64url.Base64Url
import org.jose4j.jws.EcdsaUsingShaAlgorithm
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.minutes

@Suppress("LongParameterList")
class DebugSettingsViewModel(
    private val endpointHelper: EndpointHelper,
    private val cardWallUseCase: CardWallUseCase,
    private val prescriptionUseCase: PrescriptionUseCase,
    private val vauRepository: VauRepository,
    private val idpRepository: IdpRepository,
    private val saveInvoiceUseCase: SaveInvoiceUseCase,
    private val idpUseCase: IdpUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val featureToggleRepository: FeatureToggleRepository,
    private val getAppUpdateManagerFlagUseCase: GetAppUpdateManagerFlagUseCase,
    private val changeAppUpdateManagerFlagUseCase: ChangeAppUpdateManagerFlagUseCase,
    private val markAllUnreadMessagesAsReadUseCase: MarkAllUnreadMessagesAsReadUseCase,
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase,
    private val getTaskIdsUseCase: GetTaskIdsUseCase,
    private val getIknrUseCase: GetIknrUseCase,
    private val updateIknrUseCase: UpdateIknrUseCase,
    private val revokeEuConsentUseCase: RevokeConsentUseCase,
    private val consentVersionDataStore: ConsentVersionDataStore,
    private val communicationVersionDataStore: CommunicationVersionDataStore,
    private val communicationDigaVersionDataStore: CommunicationDigaVersionDataStore,
    private val euVersionDataStore: EuVersionDataStore,
    private val getAndroid8DeprecationOverrideUseCase: GetAndroid8DeprecationOverrideUseCase,
    private val setAndroid8DeprecationOverrideUseCase: SetAndroid8DeprecationOverrideUseCase,
    private val resetOnboardingUseCase: ResetOnboardingUseCase,
    private val virtualHealthCardPrivateKeyDataStore: VirtualHealthCardLocalDataSource,
    private val dispatchers: DispatchProvider
) : ViewModel() {
    private val _appUpdateManager = MutableStateFlow(true)
    val appUpdateManager: StateFlow<Boolean> = _appUpdateManager.asStateFlow()

    private val _messageMarkingLoading = MutableStateFlow(false)
    val messageMarkingLoading: StateFlow<Boolean> = _messageMarkingLoading.asStateFlow()

    private val _prescriptionDeletionLoading = MutableStateFlow(false)
    val prescriptionDeletionLoading: StateFlow<Boolean> = _prescriptionDeletionLoading.asStateFlow()

    private val _virtualHealthCardLoading = MutableStateFlow(false)
    val virtualHealthCardLoading: StateFlow<Boolean> = _virtualHealthCardLoading.asStateFlow()

    private val _virtualHealthCardError = MutableStateFlow<String?>(null)
    val virtualHealthCardError: StateFlow<String?> = _virtualHealthCardError.asStateFlow()

    private val _pairingLoading = MutableStateFlow(false)
    val pairingLoading: StateFlow<Boolean> = _pairingLoading.asStateFlow()

    private val _pairingError = MutableStateFlow<String?>(null)
    val pairingError: StateFlow<String?> = _pairingError.asStateFlow()

    val onVirtualHealthCardLoginSuccessEvent = ComposableEvent<Unit>()
    val onPairingLoginSuccessEvent = ComposableEvent<Unit>()

    private val _featureToggles: MutableStateFlow<Set<FeatureEntity>> = MutableStateFlow(emptySet())
    val featureToggles: StateFlow<Set<FeatureEntity>> = _featureToggles

    private val aokBwIknr = "108018007"
    private val _iknr = MutableStateFlow(aokBwIknr)
    val iknr = _iknr.asStateFlow()
    val onIknrChangedEvent = ComposableEvent<Unit>()

    init {
        viewModelScope.launch {
            val value = getAppUpdateManagerFlagUseCase()
            _appUpdateManager.value = value
            featureToggleRepository.getFeatures().collect {
                _featureToggles.value = it
            }
            getIknrUseCase().collectLatest {
                _iknr.value = it
            }
        }
    }

    var debugSettingsData by mutableStateOf(createDebugSettingsData())

    private val _android8DeprecationOverride = MutableStateFlow(getAndroid8DeprecationOverrideUseCase.invoke())
    val android8DeprecationOverride: StateFlow<Boolean> = _android8DeprecationOverride.asStateFlow()

    init {
        viewModelScope.launch {
            // populate app update manager flag and other async state
            val value = getAppUpdateManagerFlagUseCase()
            _appUpdateManager.value = value
            featureToggleRepository.getFeatures().collect {
                _featureToggles.value = it
            }
            getIknrUseCase().collectLatest {
                _iknr.value = it
            }
        }
    }

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
        virtualHealthCardCert = "",
        virtualHealthCardPrivateKey = "",
        clientId = endpointHelper.getClientId()
    )

    suspend fun state() {
        val profileId = profilesUseCase.activeProfileId().first()
        val ssoTokenScope = cardWallUseCase.authenticationData(profileId)
            .first().singleSignOnTokenScope as? IdpData.TokenWithHealthCardScope
        val savedCert = virtualHealthCardPrivateKeyDataStore.getCert()
            .ifEmpty {
                ssoTokenScope?.healthCardCertificate?.let {
                    java.util.Base64.getEncoder().encodeToString(it.encoded)
                } ?: ""
            }
        val savedPrivateKey = virtualHealthCardPrivateKeyDataStore.getPrivateKey()
        updateState(
            debugSettingsData.copy(
                cardAccessNumberIsSet = ssoTokenScope?.cardAccessNumber?.isNotEmpty() ?: false,
                activeProfileId = profileId,
                bearerToken = idpRepository.decryptedAccessToken(profileId).first()?.accessToken ?: "",
                virtualHealthCardCert = debugSettingsData.virtualHealthCardCert.ifEmpty { savedCert },
                virtualHealthCardPrivateKey = debugSettingsData.virtualHealthCardPrivateKey.ifEmpty { savedPrivateKey }
            )
        )
    }

    fun updateState(debugSettingsData: DebugSettingsData) {
        this.debugSettingsData = debugSettingsData
    }

    fun setAndroid8DeprecationOverride(enabled: Boolean) {
        setAndroid8DeprecationOverrideUseCase.invoke(enabled)
        _android8DeprecationOverride.value = enabled
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            resetOnboardingUseCase()
        }
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

    fun toggleFeature(feature: FeatureEntity) {
        viewModelScope.launch {
            featureToggleRepository.toggleFeature(feature)
        }
    }

    fun onRevokeEuConsent() {
        viewModelScope.launch {
            profilesUseCase.activeProfileId().first().let { profile ->
                revokeEuConsentUseCase(profile, category = ConsentCategory.EUCONSENT)
            }
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

    fun onSetVirtualHealthCardCertificate(cert: String) {
        updateState(debugSettingsData.copy(virtualHealthCardCert = cert))
    }

    fun onSetVirtualHealthCardPrivateKey(privateKey: String) {
        updateState(debugSettingsData.copy(virtualHealthCardPrivateKey = privateKey))
    }

    fun clearVirtualHealthCardError() {
        _virtualHealthCardError.value = null
    }

    fun clearPairingError() {
        _pairingError.value = null
    }

    fun onPairingAuthFailed() {
        _pairingError.value = "Secure element authentication failed or was cancelled"
        _pairingLoading.value = false
    }

    fun getVirtualHealthCardCertificateSubjectInfo(): String =
        try {
            X509CertificateHolder(Base64.decode(debugSettingsData.virtualHealthCardCert)).subject.toString()
        } catch (e: Exception) {
            e.message ?: "Error"
        }

    private fun decodeBase64(value: String): ByteArray = java.util.Base64.getDecoder().decode(value)

    private fun signWithVirtualHealthCard(privateKeyBase64: String): suspend (ByteArray) -> ByteArray = {
        val curveSpec = ECNamedCurveTable.getParameterSpec("brainpoolP256r1")
        val keySpec = ECPrivateKeySpec(
            BigInteger(1, decodeBase64(privateKeyBase64)),
            curveSpec
        )
        val privateKey = KeyFactory.getInstance("EC", BCProvider).generatePrivate(keySpec)
        val signed = Signature.getInstance("NoneWithECDSA").apply {
            initSign(privateKey)
            update(it)
        }.sign()
        EcdsaUsingShaAlgorithm.convertDerToConcatenated(signed, 64)
    }

    fun loginWithVirtualHealthCard(
        cardAccessNumber: String,
        certificateBase64: String,
        privateKeyBase64: String
    ) {
        viewModelScope.launch {
            _virtualHealthCardLoading.value = true
            val result = withContext(dispatchers.io) {
                runCatching {
                    idpUseCase.authenticationFlowWithHealthCard(
                        profileId = profilesUseCase.activeProfileId().first(),
                        cardAccessNumber = cardAccessNumber,
                        healthCardCertificate = { decodeBase64(certificateBase64) },
                        sign = signWithVirtualHealthCard(privateKeyBase64)
                    )
                }
            }
            _virtualHealthCardLoading.value = false
            result
                .onSuccess {
                    virtualHealthCardPrivateKeyDataStore.save(certificateBase64, privateKeyBase64)
                    onVirtualHealthCardLoginSuccessEvent.trigger()
                }
                .onFailure {
                    _virtualHealthCardError.value = it.message
                }
        }
    }

    fun loginWithVirtualHealthCardAndSecureElement(
        cardAccessNumber: String,
        certificateBase64: String,
        privateKeyBase64: String,
        aliasOfSecureElementEntry: ByteArray,
        publicKeyOfSecureElementEntry: PublicKey
    ) {
        viewModelScope.launch {
            _pairingLoading.value = true
            val result = withContext(dispatchers.io) {
                runCatching {
                    val profileId = profilesUseCase.activeProfileId().first()
                    idpUseCase.pairSecureElementWithHealthCard(
                        profileId = profileId,
                        cardAccessNumber = cardAccessNumber,
                        publicKeyOfSecureElementEntry = publicKeyOfSecureElementEntry,
                        aliasOfSecureElementEntry = aliasOfSecureElementEntry,
                        healthCardCertificate = { decodeBase64(certificateBase64) },
                        signWithHealthCard = signWithVirtualHealthCard(privateKeyBase64)
                    )
                    idpUseCase.authenticateWithSecureElement(
                        profileId = profileId,
                        scope = IdpScope.Default
                    )
                }
            }
            _pairingLoading.value = false
            result
                .onSuccess {
                    virtualHealthCardPrivateKeyDataStore.save(certificateBase64, privateKeyBase64)
                    onPairingLoginSuccessEvent.trigger()
                }
                .onFailure { _pairingError.value = it.message }
        }
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
            _appUpdateManager.value = useOriginal
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

    fun updateIknr(text: String) {
        viewModelScope.launch {
            _iknr.value = text
        }
    }

    fun saveIknr() {
        viewModelScope.launch {
            val iknrToBeSaved = if (_iknr.value.isNotNullOrEmpty()) _iknr.value else aokBwIknr
            updateIknrUseCase.invoke(iknrToBeSaved)
            onIknrChangedEvent.trigger()
        }
    }

    // Consent Version (DEBUG ONLY)
    val consentVersion: StateFlow<ConsentVersion> =
        consentVersionDataStore.consentVersion

    fun setConsentVersion(version: ConsentVersion) {
        consentVersionDataStore.saveConsentVersion(version)
    }

    // Communication Version (DEBUG ONLY)
    val communicationVersion: StateFlow<CommunicationVersion> =
        communicationVersionDataStore.communicationVersion

    fun setCommunicationVersion(version: CommunicationVersion) {
        communicationVersionDataStore.saveCommunicationVersion(version)
    }

    // Communication DiGA Version (DEBUG ONLY)
    val communicationDigaVersion: StateFlow<CommunicationDigaVersion> =
        communicationDigaVersionDataStore.communicationDigaVersion

    fun setCommunicationDigaVersion(version: CommunicationDigaVersion) {
        communicationDigaVersionDataStore.saveCommunicationDigaVersion(version)
    }

    // Eu Version (DEBUG ONLY)
    val euVersion: StateFlow<EuVersion> =
        euVersionDataStore.euVersion

    fun setEuVersion(version: EuVersion) {
        euVersionDataStore.saveEuVersion(version)
    }
}
