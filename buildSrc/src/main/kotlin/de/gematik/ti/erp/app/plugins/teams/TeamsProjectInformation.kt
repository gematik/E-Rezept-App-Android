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

package de.gematik.ti.erp.app.plugins.teams

data class TeamsProjectInformation(
    val versionName: String,
    val versionCode: String,
    val appCenterAppName: String,
    val appCenterOwner: String,
    val branchName: String,
    val buildStatus: String,
    val buildNumber: String,
    val buildJob: String?
) {
    companion object {
        fun TeamsProjectInformation.toPayLoad(
            summary: String,
            lastCommitMessage: String
        ) =
            TeamsPayLoad(
                versionName = VersionName(versionName),
                versionCode = VersionCode(versionCode),
                branchName = BranchName(branchName),
                releaseStatus = ReleaseStatus(buildStatus),
                buildNumber = BuildNumber(buildNumber),
                jobName = buildJob?.let { JobName(it) },
                lastCommitMessage = LastCommitMessage(lastCommitMessage),
                appCenterInformation = AppCenterInformation(owner = appCenterOwner, appName = appCenterAppName),
                summary = summary
            ).payLoad
    }
}
