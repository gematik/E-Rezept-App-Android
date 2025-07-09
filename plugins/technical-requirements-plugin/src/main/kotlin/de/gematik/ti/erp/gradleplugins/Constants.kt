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

@file:Suppress("TopLevelPropertyNaming")

package de.gematik.ti.erp.gradleplugins

import de.gematik.ti.erp.gradleplugins.Regex.ANNOTATION_REGEX
import de.gematik.ti.erp.gradleplugins.model.RequirementData

internal const val REQUIREMENTS_FILE = "requirements/requirements.properties"

internal const val generateTechnicalRequirements = "generateTechnicalRequirements"
internal const val downloadBsiSpecs = "downloadBsiSpecs"
internal const val extractRequirements = "extractRequirements"
internal const val gutachterFolder = "gutachter"
internal const val ANDROID_APP_PATH = "/app/android/src/main/java/de/gematik/ti/erp/app"
internal const val APP_FEATURES_PATH = "/app/features"
internal const val SHARED_MODULE_PATH = "/common/src/commonMain/kotlin/de/gematik/ti/erp/app"
internal const val SHARED_TEST_MODULE_PATH = "/common/src/commonTest/kotlin/de/gematik/ti/erp/app"
internal const val SHARED_ANDROID_MODULE_PATH = "/common/src/androidMain/kotlin/de/gematik/ti/erp/app"
internal const val FHIR_PARSER_MODULE_PATH = "/fhir-parser/src/main/java/de/gematik/ti/erp/app"
internal const val CODE_LINE = "codeLines ="
internal const val REQUIREMENTS_PATH = "requirements"
internal const val BSI_REQUIREMENTS_FILE_NAME = "bsi-requirements.html"
internal const val BSI_REQUIREMENTS_PATH = "requirements/bsi-requirements.html"
internal const val NO_LINK = "No link"

// Add any other specification values that should be excluded here
val excludedSpecifications = setOf(
    "gemSpec_eRp_FdV",
    "BSI-eRp-ePA",
    "gemF_Tokenverschlüsselung",
    "gemSpec_IDP_Frontend",
    "gemSpec_Krypt",
    "unused",
    "gemF_Biometrie",
    "E-Rezept-App-Authentifizierungskonzept.pdf"
)

object Regex {
    val ANNOTATION_REGEX = Regex("""@Requirement\([^)]*\)""", RegexOption.MULTILINE)
    val REQUIREMENT_REGEX = Regex("""\s*"([a-zA-Z_][a-zA-Z0-9_#-.]*)"\s*,?\s*""")
    val SPEC_REGEX = Regex("""sourceSpecification\s*=\s*"(.*?)"""")
    val CODE_LINES_REGEX = Regex("""codeLines\s*=\s*[^)]*""", RegexOption.MULTILINE)
    val QUOTES_REGEX = Regex(""""(.*?)"""")
}

/**
 * The requirement string is sanitized by removing the indexing part of it
 */
fun String.sanitizeRequirement(): String {
    val regex = "(.*?)(?=#)|(.+)".toRegex()
    val matchResult = regex.find(this)
    return matchResult?.value ?: this
}

fun String.removeAllRequirementAnnotations(): String = ANNOTATION_REGEX.replace(this, "")

fun List<RequirementData>.print() {
    this.forEach {
        println("-- Requirement --")
        println("Header: ${it.header.requirement}")
        it.body.forEach { body ->
            println(body.requirement)
        }
    }
}
