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

package de.gematik.ti.erp.app.plugins.teams

@JvmInline
value class VersionName(val value: String)

@JvmInline
value class VersionCode(val value: String)

@JvmInline
value class BranchName(val value: String) {
    fun gitlabLink(): String {
        // From MR-1234 get only 1234
        val pullRequestLink = value.replace(Regex("[^0-9]"), "")
        return "https://gitlab.prod.ccs.gematik.solutions/git/erezept/app/erp-app-android/-/merge_requests/$pullRequestLink"
    }
}

@JvmInline
value class ReleaseStatus(val value: String)

@JvmInline
value class LastCommitMessage(val value: String)

@JvmInline
value class BuildNumber(val value: String)

@JvmInline
value class JobName(val value: String) {
    fun correctedName(): String {
        // eRp-Android-Multibranch/MR-1360 converted to eRp-Android-Multibranch/job/MR-1360
        val parts = value.split("/")
        return if (parts.size == 2) "${parts[0]}/job/${parts[1]}" else value
    }
}

// In jenkins file EarlyExit is given to inform that there is no link
data class AppCenterInformation(
    val owner: String,
    val appName: String
)
