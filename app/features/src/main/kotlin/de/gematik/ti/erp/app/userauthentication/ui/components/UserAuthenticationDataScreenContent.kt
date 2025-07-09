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

package de.gematik.ti.erp.app.userauthentication.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationActions
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData

@Composable
internal fun UserAuthenticationDataScreenContent(
    contentPadding: PaddingValues,
    authenticationState: AuthenticationStateData.AuthenticationState,
    timeout: Long,
    enteredPassword: String,
    enteredPasswordError: Boolean,
    showPasswordLogin: Boolean,
    userAuthenticationActions: UserAuthenticationActions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
    ) {
        @Requirement(
            "O.Pass_4#4",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "If the user has failed to authenticate," +
                " the number of failed authentication attempts is displayed."
        )
        AuthenticationHintCard(
            state = authenticationState
        )
        Spacer(modifier = Modifier.height(PaddingDefaults.Large))
        UserAuthenticationLoginSection(
            authenticationState = authenticationState,
            timeout = timeout,
            enteredPassword = enteredPassword,
            enteredPasswordError = enteredPasswordError,
            showPasswordLogin = showPasswordLogin,
            userAuthenticationActions = userAuthenticationActions
        )
        Spacer(modifier = Modifier.weight(1f))
        if (authenticationState.authentication.showFailedAuthenticationAttemptsError) {
            UserAuthenticationBottomErrorSection(authenticationState)
        } else {
            UserAuthenticationBottomSection()
        }
    }
}
