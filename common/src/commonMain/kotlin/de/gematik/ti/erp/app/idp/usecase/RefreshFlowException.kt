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

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.idp.model.IdpData
import java.io.IOException

/**
 * Exception thrown by [IdpUseCase.loadAccessToken].
 */
class RefreshFlowException : IOException {
    /**
     * Is true if the sso token is not valid anymore and the user is required to authenticate again.
     */
    val isUserAction: Boolean
    val ssoToken: IdpData.SingleSignOnTokenScope?

    constructor(
        userActionRequired: Boolean,
        ssoToken: IdpData.SingleSignOnTokenScope?,
        cause: Throwable
    ) : super(cause) {
        this.isUserAction = userActionRequired
        this.ssoToken = ssoToken
    }

    constructor(
        userActionRequired: Boolean,
        ssoToken: IdpData.SingleSignOnTokenScope?,
        message: String
    ) : super(message) {
        this.isUserAction = userActionRequired
        this.ssoToken = ssoToken
    }
}
