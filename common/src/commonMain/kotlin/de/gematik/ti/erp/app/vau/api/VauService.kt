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

package de.gematik.ti.erp.app.vau.api

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

@Requirement(
    "O.Purp_8#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Interface of vau service"
)
interface VauService {

    @GET("PKICertificates")
    suspend fun getPkiCertList(
        @Query("currentRoot") currentRoot: String
    ): Response<UntrustedCertList>

    @Requirement(
        "A_21216",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Client must fetch VAU certificate from /VAUCertificate for TSL-based validation."
    )
    @Streaming
    @GET("VAUCertificate")
    suspend fun getVauCertList(): Response<ResponseBody>

    @Requirement(
        "A_21216",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Client must fetch OCSP response for VAU certificate from /OCSPResponse and use it for validation (TUC_PKI_018)."
    )
    @Streaming
    @GET("OCSPResponse")
    suspend fun getOcspResponseRaw(
        @Query("issuer-cn") issuerCn: String,
        @Query("serial-nr") serialNr: String
    ): Response<ResponseBody>
}
