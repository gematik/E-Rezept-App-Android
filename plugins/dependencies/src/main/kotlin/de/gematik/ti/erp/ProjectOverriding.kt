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

package de.gematik.ti.erp

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getPlugin
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

// For version code and version name
fun Project.overriding() =
    PropertyDelegateProvider { _: Any?, _ ->
        ReadOnlyProperty<Any?, String> { _, property ->
            project.plugins.getPlugin(AppDependenciesPlugin::class)
                .overrideProperties
                .getProperty(property.name)
                ?: (project.properties[property.name] as? String)
                ?: ""
        }
    }
