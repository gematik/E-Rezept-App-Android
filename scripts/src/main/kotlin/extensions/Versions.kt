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

package extensions

internal object Versions {
    internal object SdkVersions {
        const val MIN_SDK_VERSION = 26 // Android 8.0
        const val COMPILE_SDK_VERSION = 35
        const val TARGET_SDK_VERSION = 35
    }

    internal const val BUILD_TOOLS_VERSION = "35.0.0"

    internal object JavaVersion {
        const val KOTLIN_OPTIONS_JVM_TARGET = "17"
        val PROJECT_JAVA_VERSION = org.gradle.api.JavaVersion.VERSION_17
    }

    internal object CVSS {
        const val allowed = 6.9f
    }
}
