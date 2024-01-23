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

package de.gematik.ti.erp.app.vau.api

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import retrofit2.Response
import retrofit2.http.GET

@Requirement(
    "O.Purp_8#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Interface of vau service"
)
interface VauService {
    @GET("CertList")
    suspend fun getCertList(): Response<UntrustedCertList>

    @GET("OCSPList")
    suspend fun getOcspResponses(): Response<UntrustedOCSPList>
}
