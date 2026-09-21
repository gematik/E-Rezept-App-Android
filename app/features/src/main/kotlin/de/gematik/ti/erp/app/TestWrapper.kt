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

package de.gematik.ti.erp.app

import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.prescription.remote.PrescriptionRemoteDataSource
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
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
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val remoteDataSource: PrescriptionRemoteDataSource,
    private val localDataSource: TaskLocalDataSource,
    private val idpUseCase: IdpUseCase
) {
    init {
        require(BuildKonfig.INTERNAL)
        require(BuildConfig.DEBUG)
    }

    fun deleteTask(taskId: String) = runBlocking(Dispatchers.IO) {
        remoteDataSource.deleteTask(getActiveProfileUseCase().first().id, taskId)
    }

    fun deleteAllTasksSafe() = runBlocking(Dispatchers.IO) {
        val profileId = getActiveProfileUseCase().first().id
        localDataSource.loadTaskListByProfileId(profileId).first().forEach { taskErpModel ->
            remoteDataSource.deleteTask(profileId, taskErpModel.taskId)
                .onSuccess {
                    Napier.d { "Deleted ${taskErpModel.taskId}" }
                }
                .onFailure {
                    Napier.e { "Could not delete ${taskErpModel.taskId}" }
                }
            localDataSource.deleteTaskByTaskId(taskErpModel.taskId)
        }
    }

    fun loginWithVirtualHealthCard(
        certificateBase64: String = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE,
        privateKeyBase64: String = BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_PRIVATE_KEY
    ) {
        runBlocking(Dispatchers.IO) {
            idpUseCase.authenticationFlowWithHealthCard(
                profileId = getActiveProfileUseCase().first().id,
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
