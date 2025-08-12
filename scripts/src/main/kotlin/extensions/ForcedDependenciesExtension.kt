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

import generated.guavaLibrary
import generated.kotlinStdlibJdk8Library
import generated.kotlinStdlibLibrary
import generated.nettyCodecHttp2Library
import generated.nettyHandlerLibrary
import generated.protobufJavaLibrary
import generated.protobufJavaUtilLibrary
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog

fun Project.applyForcedDependencies(versionCatalog: VersionCatalog) {
    project.rootProject.allprojects {
        configurations.all {
            resolutionStrategy {
                force(versionCatalog.protobufJavaLibrary)
                force(versionCatalog.protobufJavaUtilLibrary)
                force(versionCatalog.nettyCodecHttp2Library)
                force(versionCatalog.nettyHandlerLibrary)
                force(versionCatalog.guavaLibrary)
                // external dependencies bring kotlin to 1.9.* transitively
                force(versionCatalog.kotlinStdlibLibrary)
                force(versionCatalog.kotlinStdlibJdk8Library)
            }
        }
    }
}
