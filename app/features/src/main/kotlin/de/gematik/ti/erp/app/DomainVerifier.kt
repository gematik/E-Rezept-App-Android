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

package de.gematik.ti.erp.app

import android.content.Context
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.os.Build
import androidx.annotation.RequiresApi

interface DomainVerifier {
    val areDomainsVerified: Boolean
}

/**
 * Provides information about the domains associated with the app.
 *
 * * [verifiedDomainsSize] gives the number of domains that are verified by the app.
 *
 * * [requiredDomainsSize] gives the number of domains that are not-verified by the app but are required.
 *
 * * [areDomainsVerified] checks if the verified domains will open with the app
 *
 */
@RequiresApi(Build.VERSION_CODES.S)
data class Sdk31DomainVerifier(
    private val context: Context
) : DomainVerifier {
    private val manager = context.getSystemService(DomainVerificationManager::class.java)
    private val userState = manager.getDomainVerificationUserState(context.packageName)

    @Suppress("UnusedPrivateMember")
    private val verifiedDomainsSize = userState?.hostToStateMap
        ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_VERIFIED }
        ?.size ?: 0

    @Suppress("UnusedPrivateMember")
    // Domains that haven't passed Android App Links verification but that the user has associated with an app.
    private val requiredDomainsSize = userState?.hostToStateMap
        ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_SELECTED }?.size ?: 0

    override val areDomainsVerified: Boolean = true
}

/**
 * Provides information about the domains associated with the app.
 *
 * * [areDomainsVerified] always provides true as the app is running on an older SDK.
 *
 */
data class OlderSdkDomainVerifier(
    override val areDomainsVerified: Boolean = true
) : DomainVerifier
