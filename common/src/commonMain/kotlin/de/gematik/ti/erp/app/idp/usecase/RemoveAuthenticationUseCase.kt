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

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Requirement(
    "A_20186",
    "A_21326",
    "A_21327",
    "A_20499-01",
    "A_21603",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Invalidate/delete session data upon logout. " +
        "since we have automatic memory management, we can't delete the token. " +
        "Due to the use of frameworks we have sensitive data as immutable objects and hence " +
        "cannot override it"
)
@Requirement(
    "O.Tokn_6#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "invalidate config and token "
)
class RemoveAuthenticationUseCase(
    private val repository: IdpRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(profileId: ProfileIdentifier) {
        withContext(dispatcher) {
            repository.invalidate(profileId)
        }
    }
}
