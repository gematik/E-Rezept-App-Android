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
@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.utils

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

internal const val APP_PROJECT_NAME = "android"
internal const val APP_MOCK_PROJECT_NAME = "android-mock"
private const val VERSION_PARTS = 4
private const val VERSION_NAME_PART = 3
private const val VERSION_CODE_PART = 1
internal const val VERSION_CODE_STRING = "versionCode"
internal const val VERSION_NAME_STRING = "versionName"
internal const val LAST_MESSAGE_STRING = "lastCommitMessage"
internal const val SHELL_RES_FOLDER = "ci"
internal const val GRADLE_USER_AGENT = "USER_AGENT"
internal const val GRADLE_VERSION_CODE = "VERSION_CODE"
internal const val GRADLE_VERSION_NAME = "VERSION_NAME"
internal const val VERSION_EXCEPTION = "Could not get the version name and code correctly"
internal const val VERSION_PATTERN_EXCEPTION =
    "The created version code does not follow the allowed pattern of 1.19.1-RC1. " +
            "The initial R is removed in the shell script"
internal const val VERSION_NAME_EXCEPTION = "Missing version name"
internal const val BUILD_EXCEPTION = "Could not get the version name and code correctly to build the app"
internal const val VERSION_ERROR = "Versioning error"
internal const val GRADLE_ERROR = "Versioning/UserAgent error"
internal const val OUTPUT_DIRECTORY = "artifacts"
internal const val PLAY_STORE_BUNDLE_PATH = "outputs/bundle/googlePuExternalRelease"
internal const val HUAWEI_STORE_BUNDLE_PATH = "outputs/bundle/huaweiPuExternalRelease"
internal const val KONNY_APP_APK_PATH = "outputs/apk/konnektathonRuInternal/debug"
internal const val TU_INTERNAL_APP_APK_PATH = "outputs/apk/googleTuInternal/debug"
internal const val TU_EXTERNAL_APP_APK_PATH = "outputs/apk/googleTuExternal/release"
internal const val MOCK_APP_APK_PATH = "outputs/apk/debug"
internal const val MINIFIED_APP_APK_PATH = "outputs/apk/googleTuInternal/minifiedDebug"
internal const val PLAY_STORE_BUNDLE_FILE = "android-googlePuExternal-release.aab"
internal const val HUAWEI_STORE_BUNDLE_FILE = "android-huaweiPuExternal-release.aab"
internal const val KONNY_APP_APK_FILE = "android-konnektathonRuInternal-debug.apk"
internal const val TU_INTERNAL_APP_APK_FILE = "android-googleTuInternal-debug.apk"
internal const val TU_EXTERNAL_APP_APK_FILE = "android-googleTuExternal-release.apk"
internal const val MOCK_APP_APK_FILE = "android-mock-debug.apk"
internal const val MINIFIED_APP_APK_FILE = "android-googleTuInternal-minifiedDebug.apk"
internal const val PLAY_STORE_MAPPING_PATH = "outputs/mapping/googlePuExternalRelease"
internal const val HUAWEI_STORE_MAPPING_PATH = "outputs/mapping/huaweiPuExternalRelease"
internal const val GOOGLE_TU_EXTERNAL_MAPPING_PATH = "outputs/mapping/googleTuExternalRelease"
internal const val MINIFIED_APP_MAPPING_PATH = "outputs/mapping/googleTuInternalMinifiedDebug"
internal const val MAPPING_FILE = "mapping.txt"
internal const val PAYLOAD = "payload"
internal const val OS_NAME = "os.name"
internal const val WINDOWS = "win"

internal fun String.splitForVersionParts() = this.split(":")
internal fun Project.versionCode() = extra[VERSION_CODE_STRING] as? Int
internal fun Project.versionName() = extra[VERSION_NAME_STRING] as? String
internal fun List<String>.versionCode() = this[VERSION_CODE_PART].trim().toInt()
internal fun List<String>.versionName() = this[VERSION_NAME_PART].trim()
internal fun List<String>.isVersionNameEmpty() = this.versionName().isEmpty()
internal fun List<String>.hasVersionCodeName() = this.size == VERSION_PARTS
internal fun List<String>.doesNotHaveVersionCodeName() = !hasVersionCodeName()

internal fun Project.lastCommit() = extra[LAST_MESSAGE_STRING] as? String

// The script expects the string split in the end to execute the gradle task
internal fun String.execute() = split(" ")
internal fun (MutableList<String>?).execute() = this?.joinToString(" ")

internal fun String.isValidVersionCode(): Boolean {
    // Checks if the version follows 1.10.2-RC1, the R is removed in the shell script
    val pattern = Regex("\\d+\\.\\d+\\.\\d+-RC\\d+(-[a-zA-Z0-9]+)?$")
    return pattern.matches(this)
}

internal fun String.isInvalidVersioningPattern() = !isValidVersionCode()
internal fun String.extractVersion(): String? {
    // Make R1.19.1-RC2 to 1.19.1
    val regex = Regex("\\d+\\.\\d+\\.\\d+")
    val matchResult = regex.find(this)
    return matchResult?.value
}

internal fun String.extractRCVersion(): String {
    // Make R1.19.1-RC2 to 1.19.1-RC2
    val regex = Regex("^R(.+)$")
    return regex.replace(this, "$1")
}

internal fun Project.detectPropertyOrNull(name: String) = findProperty(name) as? String
internal fun Project.detectPropertyOrThrow(name: String): String {
    val property = findProperty(name) as? String
    return property ?: throw GradleException("Missing argument $name")
}

/**
 * List of gradle tasks used for the build process
 */
object TaskNames {
    const val versionApp = "versionApp"
    const val printVersionCode = "printVersionCode"
    const val printVersionName = "printVersionName"
    const val updateGradleProperties = "updateGradleProperties"
    const val buildPlayStoreBundle = "buildPlayStoreBundle"
    const val buildPlayStoreApp = "buildPlayStoreApp"
    const val buildAppGalleryBundle = "buildAppGalleryBundle"
    const val buildGoogleTuApp = "buildGoogleTuApp"
    const val buildKonnyApp = "buildKonnyApp"
    const val buildDebugApp = "buildDebugApp"
    const val buildMockApp = "buildMockApp"
    const val buildMinifiedApp = "buildMinifiedApp"
    const val buildMinifiedKonnyApp = "buildMinifiedKonnyApp"
    const val copyPlayStoreBundle = "copyPlayStoreBundle"
    const val copyAppGalleryBundle = "copyAppGalleryBundle"
    const val copyGoogleTuApp = "copyGoogleTuApp"
    const val copyKonnyApp = "copyKonnyApp"
    const val copyDebugApp = "copyDebugApp"
    const val copyMockApp = "copyMockApp"
    const val copyMinifiedApp = "copyMinifiedApp"
    const val sendTeamsNotification = "sendTeamsNotification"
    const val lastMessage = "lastMessage"
}
