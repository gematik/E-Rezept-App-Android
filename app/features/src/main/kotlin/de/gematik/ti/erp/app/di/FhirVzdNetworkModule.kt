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

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.digas.data.repository.DigaInformationRemoteDataSource
import de.gematik.ti.erp.app.interceptor.ERezeptBackendTokenApiKeyInterceptor
import de.gematik.ti.erp.app.interceptor.PharmacySearchApiKeyInterceptor
import de.gematik.ti.erp.app.pharmacy.api.ERezeptBackendService
import de.gematik.ti.erp.app.pharmacy.api.FhirVzdService
import de.gematik.ti.erp.app.pharmacy.api.model.SearchAccessTokenInterceptor
import de.gematik.ti.erp.app.pharmacy.repository.DefaultPharmacySearchAccessTokenRepository
import de.gematik.ti.erp.app.pharmacy.repository.PharmacySearchAccessTokenRepository
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.PharmacySearchAccessTokenProvider
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.PharmacySearchAccessTokenRemoteDataSource
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import retrofit2.Retrofit

@Requirement(
    "O.Ntwk_3#6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We use OkHttp with different interceptors to make it more secure."
)
val fhirVzdNetworkModule = DI.Module("FhirVzdNetworkModule") {

    // data sources
    bindProvider { PharmacySearchAccessTokenLocalDataSource(instance()) }
    bindProvider { DigaInformationRemoteDataSource(instance()) }
    bindProvider { PharmacySearchAccessTokenRemoteDataSource(instance<ERezeptBackendService>()) }

    // repository
    bindProvider<PharmacySearchAccessTokenRepository> { DefaultPharmacySearchAccessTokenRepository(instance(), instance()) }

    // Fhir-vzd Pharmacy Search access token Provider
    bindProvider { PharmacySearchAccessTokenProvider(instance()) }

    // Fhir-vzd Pharmacy Search access token Interceptor that uses the access token from the provider
    bindProvider { SearchAccessTokenInterceptor(instance()) }

    // Fhir-vzd Pharmacy Search Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>(REQUIRED_HTTP_CLIENT).newBuilder()
        val endpointHelper = instance<EndpointHelper>()
        val searchAccessTokenInterceptor = instance<SearchAccessTokenInterceptor>()

        clientBuilder
            // adding the API-Key to handle versioning
            .addInterceptor(PharmacySearchApiKeyInterceptor(instance()))
            // adding the access token to the request
            .addInterceptor(searchAccessTokenInterceptor)

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.pharmacyFhirVzdBaseUri)
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(FhirVzdService::class.java)
    }

    // Fhir-vzd Pharmacy Search access token Service
    bindSingleton {
        val clientBuilder = instance<OkHttpClient>(REQUIRED_HTTP_CLIENT).newBuilder()
        val endpointHelper = instance<EndpointHelper>()

        // adding the API-Key to handle versioning
        clientBuilder.addInterceptor(ERezeptBackendTokenApiKeyInterceptor(instance()))

        Retrofit.Builder()
            .client(clientBuilder.build())
            .baseUrl(endpointHelper.pharmacyFhirVzdSearchAccessTokenUri)
            .addConverterFactory(instance(JsonConverterFactoryTag))
            .build()
            .create(ERezeptBackendService::class.java)
    }
}
