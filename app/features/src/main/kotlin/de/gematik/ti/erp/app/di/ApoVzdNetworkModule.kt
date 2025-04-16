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

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.interceptor.PharmacySearchApiKeyInterceptor
import de.gematik.ti.erp.app.pharmacy.api.ApoVzdPharmacySearchService
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import retrofit2.Retrofit

@Requirement(
    "O.Ntwk_3#5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We use OkHttp with different interceptors to make it more secure."
)
val apoVzdNetworkModule = DI.Module("ApoVzdNetworkModule") {

    // Apo-vzd Pharmacy Search Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>(REQUIRED_HTTP_CLIENT).newBuilder()
        val endpointHelper = instance<EndpointHelper>()

        clientBuilder.addInterceptor(PharmacySearchApiKeyInterceptor(instance()))

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.pharmacyApoVzdBaseUri)
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(ApoVzdPharmacySearchService::class.java)
    }
}
