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

import generated.androidxConcurrentFuturesKtxLibrary
import generated.androidxConcurrentFuturesLibrary
import generated.guavaLibrary
import generated.jacksonCoreLibrary
import generated.kotlinReflectLibrary
import generated.kotlinStdlibJdk8Library
import generated.kotlinStdlibLibrary
import generated.kotlinxCoroutinesAndroidLibrary
import generated.kotlinxCoroutinesCoreLibrary
import generated.kotlinxCoroutinesPlayServicesLibrary
import generated.kotlinxCoroutinesTestLibrary
import generated.nettyCodecHttp2Library
import generated.nettyCodecHttpLibrary
import generated.nettyCodecLibrary
import generated.nettyHandlerLibrary
import generated.nettyHandlerProxyLibrary
import generated.protobufJavaLibrary
import generated.protobufJavaUtilLibrary
import generated.qualityCheckstyleLibrary
import generated.testYamlLibrary
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog

fun Project.applyForcedDependencies(versionCatalog: VersionCatalog) {
    project.rootProject.allprojects {
        configurations.all {
            resolutionStrategy {
                force(versionCatalog.protobufJavaLibrary)
                force(versionCatalog.protobufJavaUtilLibrary)
                force(versionCatalog.nettyCodecHttpLibrary)
                force(versionCatalog.nettyCodecHttp2Library)
                force(versionCatalog.nettyHandlerLibrary)
                // Fixes CVE-2026-42583, CVE-2026-42587 (netty-codec 4.1.x via grpc-netty)
                force(versionCatalog.nettyCodecLibrary)
                // Fixes CVE-2026-42578 (netty-handler-proxy 4.1.x via grpc-netty)
                force(versionCatalog.nettyHandlerProxyLibrary)
                // Fixes jackson-core DoS (via primsys-rest-client)
                force(versionCatalog.jacksonCoreLibrary)
                force(versionCatalog.guavaLibrary)
                force(versionCatalog.androidxConcurrentFuturesLibrary)
                force(versionCatalog.androidxConcurrentFuturesKtxLibrary)
                force(versionCatalog.kotlinxCoroutinesCoreLibrary)
                force(versionCatalog.kotlinxCoroutinesAndroidLibrary)
                force(versionCatalog.kotlinxCoroutinesPlayServicesLibrary)
                force(versionCatalog.kotlinxCoroutinesTestLibrary)
                force(versionCatalog.qualityCheckstyleLibrary)
                // Fixes CVE-2022-1471
                force(versionCatalog.testYamlLibrary)
                // external dependencies bring kotlin to 1.9.* transitively
                force(versionCatalog.kotlinStdlibLibrary)
                force(versionCatalog.kotlinStdlibJdk8Library)
                // mockk 1.14.11+ is compiled with Kotlin 2.2 and pulls kotlin-reflect:2.2.x
                // which is incompatible with this project's Kotlin 2.0.21 compiler
                force(versionCatalog.kotlinReflectLibrary)
            }
        }
    }
}
