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

package de.gematik.ti.erp.app.pkv.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.cardwall.mini.ui.Authenticator
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.model.PkvHtmlTemplate
import de.gematik.ti.erp.app.invoice.usecase.InvoiceUseCase
import de.gematik.ti.erp.app.pkv.FileProviderAuthority
import de.gematik.ti.erp.app.pkv.usecase.createSharableFileInCache
import de.gematik.ti.erp.app.pkv.usecase.sharePDFFile
import de.gematik.ti.erp.app.pkv.usecase.writePDFAttachments
import de.gematik.ti.erp.app.pkv.usecase.writePdfFromHtml
import de.gematik.ti.erp.app.prescription.ui.GeneralErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.ui.RefreshedState
import de.gematik.ti.erp.app.prescription.ui.catchAndTransformRemoteExceptions
import de.gematik.ti.erp.app.prescription.ui.retryWithAuthenticator
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.utils.asFhirTemporal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.kodein.di.compose.rememberInstance
import java.net.HttpURLConnection

class InvoicesController(
    profileId: ProfileIdentifier,
    private val invoiceUseCase: InvoiceUseCase,
    private val authenticator: Authenticator,
    private val fileProviderAuthority: FileProviderAuthority

) {

    sealed interface State : PrescriptionServiceState {

        object InvoiceDeleted : State

        sealed interface Error : State, PrescriptionServiceErrorState {
            object InvoiceAlreadyDeleted : Error
        }
        fun PrescriptionServiceState.isInvoiceDeleted() =
            this == InvoiceDeleted || this == Error.InvoiceAlreadyDeleted
    }

    private val stateFlow: Flow<Map<Int, List<InvoiceData.PKVInvoice>>> =
        invoiceUseCase.invoices(profileId)

    val state
        @Composable
        get() = stateFlow.collectAsStateWithLifecycle(null)

    fun detailState(taskId: String): Flow<InvoiceData.PKVInvoice?> =
        invoiceUseCase.invoiceById(taskId)

    val isRefreshing
        @Composable
        get() = invoiceUseCase.refreshInProgress.collectAsStateWithLifecycle()

    fun downloadInvoices(
        profileId: ProfileIdentifier
    ): Flow<PrescriptionServiceState> =
        invoiceUseCase.downloadInvoices(profileId)
            .map {
                RefreshedState(it)
            }
            .retryWithAuthenticator(
                isUserAction = true,
                authenticate = authenticator.authenticateForPrescriptions(profileId)
            )
            .catchAndTransformRemoteExceptions()
            .flowOn(Dispatchers.IO)

    suspend fun shareInvoicePDF(
        context: Context,
        invoice: InvoiceData.PKVInvoice,
        fileProviderAuthority: FileProviderAuthority
    ) {
        val html = PkvHtmlTemplate.createHTML(invoice)

        val file = createSharableFileInCache(context, "invoices", "invoice")
        writePdfFromHtml(context, "Invoice_${invoice.taskId}", html, file)
        invoiceUseCase.loadAttachments(invoice.taskId)?.let {
            writePDFAttachments(file, it)
        }
        val subject = invoice.medicationRequest.medication?.name() + "_" +
            invoice.timestamp.asFhirTemporal().formattedString()
        sharePDFFile(context, file, subject, fileProviderAuthority)
    }

    suspend fun deleteInvoice(
        profileId: ProfileIdentifier,
        taskId: String
    ): PrescriptionServiceState =
        deleteInvoiceFlow(profileId = profileId, taskId = taskId).cancellable().first()

    private fun deleteInvoiceFlow(profileId: ProfileIdentifier, taskId: String) =
        flow {
            emit(invoiceUseCase.deleteInvoice(profileId = profileId, taskId = taskId))
        }.map { result ->
            result.fold(
                onSuccess = {
                    State.InvoiceDeleted
                },
                onFailure = {
                    if (it is ApiCallException) {
                        when (it.response.code()) {
                            HttpURLConnection.HTTP_NOT_FOUND,
                            HttpURLConnection.HTTP_GONE -> State.Error.InvoiceAlreadyDeleted
                            else -> throw it
                        }
                    } else {
                        throw it
                    }
                }
            )
        }.retryWithAuthenticator(
            isUserAction = true,
            authenticate = authenticator.authenticateForPrescriptions(profileId)
        )
            .catchAndTransformRemoteExceptions()
            .flowOn(Dispatchers.IO)
}

@Composable
fun rememberInvoicesController(profileId: ProfileIdentifier): InvoicesController {
    val invoiceUseCase by rememberInstance<InvoiceUseCase>()
    val authenticator = LocalAuthenticator.current
    val fileProviderAuthority by rememberInstance<FileProviderAuthority>()

    return remember {
        InvoicesController(
            profileId = profileId,
            invoiceUseCase = invoiceUseCase,
            authenticator = authenticator,
            fileProviderAuthority = fileProviderAuthority
        )
    }
}

fun refreshInvoicesErrorMessage(context: Context, errorState: PrescriptionServiceErrorState): String? =
    when (errorState) {
        GeneralErrorState.NetworkNotAvailable ->
            context.getString(R.string.error_message_network_not_available)

        is GeneralErrorState.ServerCommunicationFailedWhileRefreshing ->
            context.getString(R.string.error_message_server_communication_failed).format(errorState.code)

        GeneralErrorState.FatalTruststoreState ->
            context.getString(R.string.error_message_vau_error)

        else -> null
    }
