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
import de.gematik.ti.erp.app.tasks.registerBuildAppGalleryBundle
import de.gematik.ti.erp.app.tasks.registerBuildDebugApp
import de.gematik.ti.erp.app.tasks.registerBuildGoogleTuApp
import de.gematik.ti.erp.app.tasks.registerBuildKonnyApp
import de.gematik.ti.erp.app.tasks.registerBuildMinifiedApp
import de.gematik.ti.erp.app.tasks.registerBuildMinifiedKonnyApp
import de.gematik.ti.erp.app.tasks.registerBuildMockApp
import de.gematik.ti.erp.app.tasks.registerBuildPlayStoreApp
import de.gematik.ti.erp.app.tasks.registerBuildPlayStoreBundle
import de.gematik.ti.erp.app.tasks.registerCopyAppGalleryBundle
import de.gematik.ti.erp.app.tasks.registerCopyDebugApp
import de.gematik.ti.erp.app.tasks.registerCopyGoogleTuApp
import de.gematik.ti.erp.app.tasks.registerCopyKonnyApp
import de.gematik.ti.erp.app.tasks.registerCopyMinifiedApp
import de.gematik.ti.erp.app.tasks.registerCopyMockApp
import de.gematik.ti.erp.app.tasks.registerCopyPlayStoreBundle
import org.gradle.api.Project

@Suppress("unused")
class BuildAppFlavoursPlugin : ErpPlugin {
    override fun apply(project: Project) {

        project.tasks.apply {

            // build tasks
            registerBuildPlayStoreBundle()
            registerBuildPlayStoreApp()
            registerBuildAppGalleryBundle()
            registerBuildGoogleTuApp()
            registerBuildKonnyApp()
            registerBuildDebugApp()
            registerBuildMockApp()
            registerBuildMinifiedApp()
            registerBuildMinifiedKonnyApp()

            // copy files tasks
            registerCopyPlayStoreBundle()
            registerCopyAppGalleryBundle()
            registerCopyKonnyApp()
            registerCopyGoogleTuApp()
            registerCopyDebugApp()
            registerCopyMockApp()
            registerCopyMinifiedApp()
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
