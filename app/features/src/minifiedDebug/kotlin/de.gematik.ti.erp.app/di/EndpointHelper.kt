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

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.debugsettings.data.Environment
import okhttp3.Interceptor

/**
 * Documentation: documentation-internal/variants/build_variants.adoc
 */
class EndpointHelper(
    private val networkPrefs: SharedPreferences
) {

    enum class EndpointUri(val original: String, val preferenceKey: String) {
        BASE_SERVICE_URI(
            BuildKonfig.BASE_SERVICE_URI,
            "BASE_SERVICE_URI_OVERRIDE"
        ),
        IDP_SERVICE_URI(
            BuildKonfig.IDP_SERVICE_URI,
            "IDP_SERVICE_URI_OVERRIDE"
        ),

        // apo-vzd-uri
        PHARMACY_SERVICE_URI(
            BuildKonfig.PHARMACY_SERVICE_URI,
            "PHARMACY_BASE_URI_OVERRIDE"
        ),

        // fhir-vzd-uri
        PHARMACY_FHIRVZD_SERVICE_URI(
            BuildKonfig.FHIRVZD_PHARMACY_SERVICE_URI,
            "PHARMACY_FHIRVZD_SERVICE_URI_OVERRIDE"
        ),
        PHARMACY_FHIRVZD_SEARCH_ACCESS_TOKEN_URI(
            BuildKonfig.FHIRVZD_SEARCH_ACCESS_TOKEN_URI,
            "PHARMACY_FHIRVZD_SEARCH_ACCESS_TOKEN_URI_OVERRIDE"
        )
    }

    val eRezeptServiceUri
        get() = getUriForEndpoint(EndpointUri.BASE_SERVICE_URI)

    val idpServiceUri
        get() = getUriForEndpoint(EndpointUri.IDP_SERVICE_URI)

    val pharmacyApoVzdBaseUri
        get() = getUriForEndpoint(EndpointUri.PHARMACY_SERVICE_URI)

    val pharmacyFhirVzdBaseUri
        get() = getUriForEndpoint(EndpointUri.PHARMACY_FHIRVZD_SERVICE_URI)

    val pharmacyFhirVzdSearchAccessTokenUri
        get() = getUriForEndpoint(EndpointUri.PHARMACY_FHIRVZD_SEARCH_ACCESS_TOKEN_URI)

    private val emptyInterceptor = Interceptor { chain ->
        chain.proceed(chain.request()) // no modification
    }

    private fun getUriForEndpoint(uri: EndpointUri): String {
        var url = uri.original
        if (isUriOverridden(uri)) {
            url = networkPrefs.getString(
                uri.preferenceKey,
                uri.original
            ) ?: ""
        }
        return when {
            url.isEmpty() -> "https://github.com/gematik/E-Rezept-App-Android/"
            url.last() != '/' -> {
                url += '/'
                url
            }

            else -> return url
        }
    }

    private fun overrideSwitchKey(uri: EndpointUri): String {
        return uri.preferenceKey + "_ACTIVE"
    }

    fun isUriOverridden(uri: EndpointUri): Boolean {
        return networkPrefs.getBoolean(overrideSwitchKey(uri), false)
    }

    fun setUriOverride(uri: EndpointUri, debugUri: String, active: Boolean) {
        networkPrefs.edit(commit = true) {
            putBoolean(overrideSwitchKey(uri), active)
            putString(uri.preferenceKey, debugUri)
        }
    }

    @Suppress("CyclomaticComplexMethod")
    fun getCurrentEnvironment(): Environment {
        return when {
            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_PU &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_PU &&
                pharmacyApoVzdBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_PU &&
                pharmacyFhirVzdBaseUri == BuildKonfig.FHIRVZD_PHARMACY_SERVICE_URI_PU -> {
                Environment.PU
            }

            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_RU &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_RU &&
                pharmacyApoVzdBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_RU &&
                pharmacyFhirVzdBaseUri == BuildKonfig.FHIRVZD_PHARMACY_SERVICE_URI_RU -> {
                Environment.RU
            }

            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_RU_DEV &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_RU_DEV &&
                pharmacyApoVzdBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_RU &&
                pharmacyFhirVzdBaseUri == BuildKonfig.FHIRVZD_PHARMACY_SERVICE_URI_RU -> {
                Environment.RUDEV
            }

            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_TU &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_TU &&
                pharmacyApoVzdBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_RU &&
                pharmacyFhirVzdBaseUri == BuildKonfig.FHIRVZD_PHARMACY_SERVICE_URI_RU -> {
                Environment.TU
            }

            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_TR &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_TR &&
                pharmacyApoVzdBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_RU &&
                pharmacyFhirVzdBaseUri == BuildKonfig.FHIRVZD_PHARMACY_SERVICE_URI_RU -> {
                Environment.TR
            }

            else -> {
                return Environment.PU
            }
        }
    }

    fun getOrganDonationRegisterIntentHost() =
        if (getCurrentEnvironment() == Environment.PU) {
            BuildKonfig.ORGAN_DONATION_REGISTER_PU
        } else {
            BuildKonfig.ORGAN_DONATION_REGISTER_RU
        }

    fun getOrganDonationRegisterInfoHost() = BuildKonfig.ORGAN_DONATION_INFO

    fun getErpApiKey(): String {
        return if (BuildKonfig.INTERNAL) {
            when (getCurrentEnvironment()) {
                Environment.PU -> BuildKonfig.ERP_API_KEY_GOOGLE_PU
                Environment.TU -> BuildKonfig.ERP_API_KEY_GOOGLE_TU
                Environment.RUDEV, Environment.RU -> BuildKonfig.ERP_API_KEY_GOOGLE_RU
                Environment.TR -> BuildKonfig.ERP_API_KEY_GOOGLE_TR
            }
        } else {
            BuildKonfig.ERP_API_KEY
        }
    }

    fun getIdpScope(): String {
        return if (BuildKonfig.INTERNAL) {
            when (getCurrentEnvironment()) {
                Environment.RUDEV -> BuildKonfig.IDP_SCOPE_DEVRU
                else -> BuildKonfig.IDP_DEFAULT_SCOPE
            }
        } else {
            BuildKonfig.IDP_DEFAULT_SCOPE
        }
    }

    fun getPharmacyApiKey(): String {
        return if (BuildKonfig.INTERNAL) {
            when (getCurrentEnvironment()) {
                Environment.PU -> BuildKonfig.PHARMACY_API_KEY_PU
                else -> BuildKonfig.PHARMACY_API_KEY_RU
            }
        } else {
            BuildKonfig.PHARMACY_API_KEY
        }
    }

    fun getSearchAccessTokenApiKey(): String {
        return if (BuildKonfig.INTERNAL) {
            when (getCurrentEnvironment()) {
                Environment.PU -> BuildKonfig.FHIR_VZD_API_KEY_PU
                else -> BuildKonfig.FHIR_VZD_API_KEY_RU
            }
        } else {
            BuildKonfig.FHIRVZD_API_KEY
        }
    }

    fun getTrustAnchor(): String {
        return if (BuildKonfig.INTERNAL) {
            when (getCurrentEnvironment()) {
                Environment.PU -> BuildKonfig.APP_TRUST_ANCHOR_BASE64_PU
                else -> BuildKonfig.APP_TRUST_ANCHOR_BASE64_TU
            }
        } else {
            BuildKonfig.APP_TRUST_ANCHOR_BASE64
        }
    }

    fun getClientId() = when (getCurrentEnvironment()) {
        Environment.PU -> BuildKonfig.CLIENT_ID_PU
        Environment.TU -> BuildKonfig.CLIENT_ID_TU
        Environment.RU -> BuildKonfig.CLIENT_ID_RU
        Environment.RUDEV -> BuildKonfig.CLIENT_ID_RU
        Environment.TR -> BuildKonfig.CLIENT_ID_RU
    }

    fun getHttpLoggingInterceptor(context: Context): Interceptor {
        return emptyInterceptor
    }
}
