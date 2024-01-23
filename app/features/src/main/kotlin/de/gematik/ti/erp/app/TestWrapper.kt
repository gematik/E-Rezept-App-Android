/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app

import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.prescription.repository.PrescriptionLocalDataSource
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRemoteDataSource
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.util.encoders.Base64
import org.jose4j.jws.EcdsaUsingShaAlgorithm
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.math.BigInteger
import java.security.KeyFactory
import java.security.Signature

private const val SignatureOutputSize = 64

class TestWrapper(
    private val profilesUseCase: ProfilesUseCase,
    private val remoteDataSource: PrescriptionRemoteDataSource,
    private val localDataSource: PrescriptionLocalDataSource,
    private val idpUseCase: IdpUseCase
) {
    init {
        require(BuildKonfig.INTERNAL)
        require(BuildConfig.DEBUG)
    }

    fun deleteTask(taskId: String) = runBlocking(Dispatchers.IO) {
        remoteDataSource.deleteTask(profilesUseCase.activeProfile.first().id, taskId)
    }

    fun deleteAllTasksSafe() = runBlocking(Dispatchers.IO) {
        val profileId = profilesUseCase.activeProfile.first().id
        localDataSource.loadTaskIds().first().forEach { taskId ->
            remoteDataSource.deleteTask(profileId, taskId)
                .onSuccess {
                    Napier.d { "Deleted $taskId" }
                }
                .onFailure {
                    Napier.e { "Could not delete $taskId" }
                }
            localDataSource.deleteTask(taskId)
        }
    }

    fun loginWithVirtualHealthCard(
        certificateBase64: String = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE,
        privateKeyBase64: String = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY
    ) {
        runBlocking(Dispatchers.IO) {
            idpUseCase.authenticationFlowWithHealthCard(
                profileId = profilesUseCase.activeProfileId().first(),
                cardAccessNumber = "123123",
                healthCardCertificate = { Base64.decode(certificateBase64) },
                sign = {
                    val curveSpec = ECNamedCurveTable.getParameterSpec("brainpoolP256r1")
                    val keySpec =
                        ECPrivateKeySpec(BigInteger(Base64.decode(privateKeyBase64)), curveSpec)
                    val privateKey = KeyFactory.getInstance("EC", BCProvider).generatePrivate(keySpec)
                    val signed = Signature.getInstance("NoneWithECDSA").apply {
                        initSign(privateKey)
                        update(it)
                    }.sign()
                    EcdsaUsingShaAlgorithm.convertDerToConcatenated(signed, SignatureOutputSize)
                }
            )
        }
    }
}

fun DI.MainBuilder.debugOverrides() {
    bindSingleton { TestWrapper(instance(), instance(), instance(), instance()) }
}
