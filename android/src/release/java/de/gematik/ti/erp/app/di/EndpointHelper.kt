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

    fun getErpApiKey(): String =
        BuildKonfig.ERP_API_KEY

    fun getIdpScope(): String =
        BuildKonfig.IDP_DEFAULT_SCOPE

    fun getPharmacyApiKey(): String =
        BuildKonfig.PHARMACY_API_KEY

    fun getTrustAnchor(): String =
        BuildKonfig.APP_TRUST_ANCHOR_BASE64
}
