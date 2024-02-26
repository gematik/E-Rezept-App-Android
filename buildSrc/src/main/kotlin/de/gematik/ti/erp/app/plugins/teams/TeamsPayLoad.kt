package de.gematik.ti.erp.app.plugins.teams

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class TeamsPayLoad(
    val summary: String,
    val versionName: VersionName,
    val versionCode: VersionCode,
    val currentTime: LocalDateTime = LocalDateTime.now(),
    val branchName: BranchName,
    val releaseStatus: ReleaseStatus,
    val lastCommitMessage: LastCommitMessage,
    val appCenterInformation: AppCenterInformation,
    val buildNumber: BuildNumber,
    val jobName: JobName?,
) {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val payLoad = """
        {
            "type": "MessageCard",
            "text": "📣 $summary",
            "themeColor": "7DD700",
            "sections": [
                {
                    "activityTitle": "📬 Version $versionName ($versionCode)",
                    "activitySubtitle": "🕐 Built on ${currentTime.format(formatter)}",
                    "activityImage": "https://www.apotheke-johannstadt.de/assets/images/5/Euclid%20gematik%20E-Rezept%20Logo%20vert%20rgb_ohneTitel-2ea7aa74.png",
                    "facts": [
                        {
                            "name": "Status:",
                            "value": "${branchName.value} is ${releaseStatus.value}"
                        },
                        {
                            "name": "🔨 GitlabLink",
                            "value": "${branchName.gitlabLink()}"  
                        },
                        {
                            "name": "🚀🚀 Jenkins Link",
                            "value": "https://jenkins.prod.ccs.gematik.solutions/job/${jobName?.correctedName()}/${buildNumber.value}/"  
                        },
                        {
                            "name": "🕹️ AppCenter Link:",
                            "value": "Please access this build using the link ${appCenterInformation.appCenterUrl()}"
                        }
                    ],
                    "images": [
                        {
                            "image": "${appCenterInformation.qrCodeUrl()}",
                            "title": "QR Code",
                            "tap": {
                                "type": "openUrl",
                                "value": "${appCenterInformation.appCenterUrl()}"
                            }
                        }
                    ],
                    "markdown": true
                },
                {
                    "activitySubtitle": "🔽 More information on the build",
                    "activityImage": "https://freepngimg.com/thumb/animation/4-2-animation-png-pic.png",
                    "facts": [
                        {
                            "name": "🔖 Release Notes (Last commit)",
                            "value": "${lastCommitMessage.value}"
                        },
                        {
                            "name": "Build:",
                            "value": "The current build number is ${buildNumber.value}"
                        },
                        {
                            "name": "Job:",
                            "value": "The name of the current job is ${jobName?.value}"
                        }
                    ],
                    "text": "Happy testing! 😀",
                    "markdown": true
                }
            ]
        }
    """.trimIndent()
}

private fun AppCenterInformation.appCenterUrl(
    // TODO: Find out how to get the latest id from app-center
) = "https://install.appcenter.ms/orgs/$owner/apps/$appName/releases"

private fun AppCenterInformation.qrCodeUrl() =
    "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${appCenterUrl()}"
