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

package de.gematik.ti.erp.gradleplugins.repository

import de.gematik.ti.erp.gradleplugins.BSI_REQUIREMENTS_PATH
import de.gematik.ti.erp.gradleplugins.NO_LINK
import de.gematik.ti.erp.gradleplugins.model.SpecificationSource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File

private const val TARGET_ELEMENT = "target-element"

class SourceSpecificationRepository {
    // download the source specification from gemspec or gitlab
    @Suppress("NestedBlockDepth")
    fun download(
        source: SpecificationSource,
        isFromGemSpec: Boolean,
        targetRequirement: String
    ): Result<Pair<String, Element>> = try {
        val document: Document?
        val updatedHtml: String
        if (isFromGemSpec) {
            updatedHtml = source.url
            document = Jsoup.connect(source.url).get()
            // download the latest version of the source
            /* if (document.getElementById("versionDropDownButton") != null && !source.useLatestOnly) {
                val versionDropDownButton = document.getElementById("versionDropDownButton")
                val version = versionDropDownButton?.text()
                val html = source.url.replace("latest/index.html", "${source.spec}_V$version.html")
                updatedHtml = html
                document = Jsoup.connect(html).get()
            } */
            val elements = document.getElementsByClass(TARGET_ELEMENT)
            val targetElement = elements.find { it.id() == targetRequirement }
            targetElement?.let { Result.success(updatedHtml to it) }
                ?: run { Result.failure(Exception("Element not found")) }
        } else {
            val htmlFile = File(BSI_REQUIREMENTS_PATH)
            Jsoup.parse(htmlFile).let { bsiDocument ->
                val elements = bsiDocument.getElementsByClass(TARGET_ELEMENT)
                // O.Source_5 as OSource_5 in the html file
                val updatedTargetRequirement = targetRequirement.replace(".", "")
                val targetElement = elements.find { it.id() == updatedTargetRequirement }
                targetElement?.let { Result.success(NO_LINK to it) }
                    ?: run { Result.failure(Exception("$TARGET_ELEMENT not found in local bsi-spec html file")) }
            }
        }
    } catch (e: Exception) {
        println("Error for $source for requirement:$targetRequirement ")
        println("Error message: ${e.message}")
        Result.failure(e)
    }
}
