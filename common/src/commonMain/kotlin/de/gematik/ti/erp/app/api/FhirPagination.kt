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

package de.gematik.ti.erp.app.api

import io.github.aakira.napier.Napier

class FhirPagination {

    /**
     * Generic pagination runner with per-page handling:
     *  - `onFirstPage` fetches the initial page
     *  - `onNextPage` fetches subsequent pages by URL
     *  - `nextOf` extracts the next‐page link from each payload
     *  - `onPage` is invoked for each payload T as soon as it’s fetched
     *
     * Any exception thrown by onFirstPage, nextOf, or onNextPage will abort further paging.
     */
    internal suspend fun <T> paginate(
        onFirstPage: suspend () -> Result<T>,
        onNextPage: suspend (String) -> Result<T>,
        nextOf: (T) -> String?,
        onPage: suspend (T) -> Unit
    ) {
        var next: String? = null
        var pageIndex = 0

        do {
            pageIndex++

            if (next == null) {
                Napier.i { "paginate ▶ fetching initial page (page #$pageIndex)" }
            } else {
                Napier.i { "paginate ▶ fetching next page (page #$pageIndex) URL=$next" }
            }

            // fetch & unwrap or throw
            val page = (if (next == null) onFirstPage() else onNextPage(next)).getOrThrow()

            Napier.i { "paginate ▶ fetched page #$pageIndex, nextLink=${nextOf(page) ?: "none"}" }

            // handle this page immediately
            onPage(page)

            // advance
            next = nextOf(page)
        } while (next != null)

        Napier.i { "paginate ✅ complete, total pages handled = $pageIndex" }
    }
}
