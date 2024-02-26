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

package de.gematik.ti.erp.app.plugins.buildapp

import de.gematik.ti.erp.app.ErpPlugin
import de.gematik.ti.erp.app.utils.GRADLE_ERROR
import de.gematik.ti.erp.app.utils.GRADLE_USER_AGENT
import de.gematik.ti.erp.app.utils.GRADLE_VERSION_CODE
import de.gematik.ti.erp.app.utils.GRADLE_VERSION_NAME
import de.gematik.ti.erp.app.utils.TaskNames
import de.gematik.ti.erp.app.utils.VERSION_EXCEPTION
import de.gematik.ti.erp.app.utils.extractVersion
import de.gematik.ti.erp.app.utils.versionCode
import de.gematik.ti.erp.app.utils.versionName
import org.gradle.api.GradleScriptException
import org.gradle.api.Project
import org.gradle.api.Task
import java.util.Properties

@Suppress("unused")
class UpdatePropertiesPlugin : ErpPlugin {
    override fun apply(project: Project) {
        project.tasks.register(TaskNames.updateGradleProperties) {
            runDependencyTasks()
            val properties = Properties()
            doLast {
                val versionCode = project.versionCode()
                val versionName = project.versionName()?.extractVersion()

                val gradlePropertiesFile = project.file("gradle.properties")
                properties.load(gradlePropertiesFile.reader())

                if (versionName == null || versionCode == null) {
                    throw GradleScriptException(GRADLE_ERROR, Exception(VERSION_EXCEPTION))
                }

                properties.setProperty(GRADLE_USER_AGENT, "eRp-App-Android/$versionName GMTIK/eRezeptApp")
                properties.setProperty(GRADLE_VERSION_CODE, versionCode.toString())
                properties.setProperty(GRADLE_VERSION_NAME, versionName)

                println("Gradle properties updated.")

                properties.store(gradlePropertiesFile.writer(), null)
            }
        }
    }

    private fun Task.runDependencyTasks() {
        dependsOn(TaskNames.versionApp)
    }
}
