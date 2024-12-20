/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.plugins.buildapp

import de.gematik.ti.erp.app.ErpPlugin
import de.gematik.ti.erp.app.tasks.buildAppGalleryBundle
import de.gematik.ti.erp.app.tasks.buildDebugApp
import de.gematik.ti.erp.app.tasks.buildGoogleTuApp
import de.gematik.ti.erp.app.tasks.buildKonnyApp
import de.gematik.ti.erp.app.tasks.buildMinifiedApp
import de.gematik.ti.erp.app.tasks.buildMinifiedKonnyApp
import de.gematik.ti.erp.app.tasks.buildMockApp
import de.gematik.ti.erp.app.tasks.buildPlayStoreApp
import de.gematik.ti.erp.app.tasks.buildPlayStoreBundle
import de.gematik.ti.erp.app.tasks.copyAppGalleryBundle
import de.gematik.ti.erp.app.tasks.copyDebugApp
import de.gematik.ti.erp.app.tasks.copyGoogleTuApp
import de.gematik.ti.erp.app.tasks.copyKonnyApp
import de.gematik.ti.erp.app.tasks.copyMinifiedApp
import de.gematik.ti.erp.app.tasks.copyMockApp
import de.gematik.ti.erp.app.tasks.copyPlayStoreBundle
import org.gradle.api.Project

@Suppress("unused")
class BuildAppFlavoursPlugin : ErpPlugin {
    override fun apply(project: Project) {
        project.tasks.apply {
            // build tasks
            buildPlayStoreBundle()
            buildPlayStoreApp()
            buildAppGalleryBundle()
            buildGoogleTuApp()
            buildKonnyApp()
            buildDebugApp()
            buildMockApp()
            buildMinifiedApp()
            buildMinifiedKonnyApp()

            // copy files tasks
            copyPlayStoreBundle()
            copyAppGalleryBundle()
            copyKonnyApp()
            copyGoogleTuApp()
            copyDebugApp()
            copyMockApp()
            copyMinifiedApp()
        }
    }

    enum class BuildCondition(
        val assembleTask: String,
        val buildFlavour: String
    ) {
        PlayStoreBundle("bundleGooglePuExternalRelease", "googlePuExternal"),
        PlayStoreApk("assembleGooglePuExternalRelease", "googlePuExternal"),
        AppGalleryBundle("bundleHuaweiPuExternalRelease", "huaweiPuExternal"),
        GoogleTuApk("assembleGoogleTuExternalRelease", "googleTuExternal"),
        KonnyApk("assembleKonnektathonRuInternalDebug", "konnektathonRuInternal"),
        MinifiedKonnyApk("assembleKonnektathonRuInternalMinifiedDebug", "konnektathonRuInternal"),
        DebugApk("assembleGoogleTuInternalDebug", "googleTuInternal"),
        MinifiedDebugApk("assembleGoogleTuInternalMinifiedDebug", "googleTuInternal"),
        MockApk("assembleDebug", "googleTuInternal")
    }

    enum class MappingFileName(
        private val mappingFileName: String
    ) {
        PlayStore("android-googlePuExternal-release-mapping"),
        AppGallery("android-huaweiPuExternal-release-mapping"),
        TuExternal("android-googleTuExternal-release-mapping"),
        MinifiedApp("android-googleTuInternal-minifiedDebug");

        fun fileName() = this.mappingFileName
    }
}
