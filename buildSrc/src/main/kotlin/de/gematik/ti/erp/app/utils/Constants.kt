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

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.utils

internal const val APP_PROJECT_NAME = "android"
internal const val APP_MOCK_PROJECT_NAME = "android-mock"
internal const val VERSION_PARTS = 4
internal const val VERSION_NAME_PART = 3
internal const val VERSION_CODE_PART = 1

//region csv data
internal const val ITEMS_IN_ROW = 5
internal const val VERSION_ITEM = 4
internal const val API_KEY_POSITION = 1
//endregion

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
internal const val TU_EXTERNAL_APP_APK_FILE_UNSIGNED = "android-googleTuExternal-release-unsigned.apk"
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
internal const val GRADLE_PROPERTIES_FILE = "gradle.properties"
internal const val API_TOKEN = "token"
