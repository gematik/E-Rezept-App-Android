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

package de.gematik.ti.erp.app.invoice.usecase

import de.gematik.ti.erp.app.api.HttpErrorState.Unknown
import de.gematik.ti.erp.app.fhir.model.extractTaskIdsFromChargeItemBundle
import de.gematik.ti.erp.app.invoice.mapper.mapJsonToInvoiceError
import de.gematik.ti.erp.app.invoice.model.InvoiceResult
import de.gematik.ti.erp.app.invoice.model.InvoiceResult.InvoiceError
import de.gematik.ti.erp.app.invoice.model.InvoiceResult.InvoiceSuccess
import de.gematik.ti.erp.app.invoice.model.InvoiceResult.InvoiceSuccess.SuccessOnChargeItemBundleDownload
import de.gematik.ti.erp.app.invoice.model.InvoiceResult.InvoiceSuccess.SuccessOnChargeItemDownload
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

class DownloadInvoicesUseCase(
    private val profileRepository: ProfileRepository,
    private val invoiceRepository: InvoiceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        profileId: String,
        onDownloadStarted: () -> Unit
    ): Flow<Result<InvoiceResult>> =
        withContext(dispatcher) {
            val isSsoTokenValid = profileRepository.isSsoTokenValid(profileId).first()
            if (!isSsoTokenValid) {
                return@withContext flowOf(Result.failure(InvoiceResult.UserNotLoggedInError(profileId)))
            }
            onDownloadStarted()
            Napier.i(
                tag = DownloadInvoicesUseCase::class.simpleName,
                message = "starting invoices download"
            )
            invoiceRepository.getLatestTimeStamp(profileId)
                .map { latestTimeStamp ->
                    invoiceRepository.downloadChargeItemBundle(
                        profileId = profileId,
                        lastUpdated = latestTimeStamp
                    ).mapJsonToInvoiceError { fhirJsonBundle ->
                        val (total, taskIds) = extractTaskIdsFromChargeItemBundle(fhirJsonBundle) // TODO: Needs a mapper class or extension method
                        supervisorScope {
                            val invoiceResults = taskIds.map { taskId ->
                                invoiceRepository.downloadChargeItemByTaskId(profileId, taskId)
                                    .mapJsonToInvoiceError { fhirJsonTask ->
                                        invoiceRepository.saveInvoice(profileId, fhirJsonTask) // TODO: Mapper needs to be extracted before save
                                        SuccessOnChargeItemBundleDownload
                                    }
                            }
                            val totalDownloads = invoiceResults.filter { it.isSuccess }.size
                            val totalDownloadsFailures = invoiceResults.filter { it.isFailure }.size
                            Napier.i(
                                tag = DownloadInvoicesUseCase::class.simpleName,
                                message = "total downloads $totalDownloads"
                            )
                            when {
                                taskIds.size == totalDownloads && (totalDownloads > 0 || totalDownloadsFailures <= 0) -> SuccessOnChargeItemDownload(
                                    total = total,
                                    downloaded = totalDownloads
                                )
                                // TODO: Needs to be used in the UI to inform the user that they need to download again
                                invoiceResults.any { it.isSuccess } -> InvoiceSuccess.SuccessOnChargeItemDownloadWithErrors(
                                    total = total,
                                    downloaded = totalDownloads,
                                    errors = invoiceResults.filter { it.isFailure }.map { it.exceptionOrNull() as? InvoiceError ?: InvoiceError(Unknown) }
                                )

                                else -> {
                                    val httpErrors = if (invoiceResults.isNotEmpty()) {
                                        invoiceResults.map { (it.getOrNull() as? InvoiceError)?.errorState ?: Unknown }
                                    } else {
                                        listOf(Unknown)
                                    }
                                    InvoiceResult.InvoiceCombinedError(errorStates = httpErrors)
                                }
                            }
                        }
                    }
                }
        }
}
