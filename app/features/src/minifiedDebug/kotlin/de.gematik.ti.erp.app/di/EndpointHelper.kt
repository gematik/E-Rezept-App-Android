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

package de.gematik.ti.erp.app.di

import android.content.SharedPreferences
import androidx.core.content.edit
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.data.Environment

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
        PHARMACY_SERVICE_URI(
            BuildKonfig.PHARMACY_SERVICE_URI,
            "PHARMACY_BASE_URI_OVERRIDE"
        )
    }

    val eRezeptServiceUri
        get() = getUriForEndpoint(EndpointUri.BASE_SERVICE_URI)

    val idpServiceUri
        get() = getUriForEndpoint(EndpointUri.IDP_SERVICE_URI)

    val pharmacySearchBaseUri
        get() = getUriForEndpoint(EndpointUri.PHARMACY_SERVICE_URI)

    private fun getUriForEndpoint(uri: EndpointUri): String {
        var url = uri.original
        if (isUriOverridden(uri)) {
            url = networkPrefs.getString(
                uri.preferenceKey,
                uri.original
            )!!
        }
        if (url.last() != '/') {
            url += '/'
        }
        return url
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

    fun getCurrentEnvironment(): Environment {
        return when {
            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_PU &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_PU &&
                pharmacySearchBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_PU -> {
                Environment.PU
            }
            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_RU &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_RU &&
                pharmacySearchBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_RU -> {
                Environment.RU
            }
            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_RU_DEV &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_RU_DEV &&
                pharmacySearchBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_RU -> {
                Environment.RUDEV
            }
            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_TU &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_TU &&
                pharmacySearchBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_RU -> {
                Environment.TU
            }
            eRezeptServiceUri == BuildKonfig.BASE_SERVICE_URI_TR &&
                idpServiceUri == BuildKonfig.IDP_SERVICE_URI_TR &&
                pharmacySearchBaseUri == BuildKonfig.PHARMACY_SERVICE_URI_RU -> {
                Environment.TR
            }
            else -> {
                return Environment.PU
            }
        }
    }

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
}
