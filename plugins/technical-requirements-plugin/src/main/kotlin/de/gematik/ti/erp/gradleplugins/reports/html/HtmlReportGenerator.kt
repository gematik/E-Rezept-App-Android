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

package de.gematik.ti.erp.gradleplugins.reports.html

import de.gematik.ti.erp.gradleplugins.NO_LINK
import de.gematik.ti.erp.gradleplugins.model.ElementHolder
import de.gematik.ti.erp.gradleplugins.model.RequirementData
import de.gematik.ti.erp.gradleplugins.model.SpecificationSource
import de.gematik.ti.erp.gradleplugins.model.Table
import de.gematik.ti.erp.gradleplugins.model.Text
import de.gematik.ti.erp.gradleplugins.model.UnorderedList
import de.gematik.ti.erp.gradleplugins.model.filter
import de.gematik.ti.erp.gradleplugins.provider.SpecificationSourceProvider
import de.gematik.ti.erp.gradleplugins.reports.ReportGenerator
import kotlinx.html.b
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.html
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import kotlinx.html.ul
import org.gradle.api.Project
import java.util.Properties

class HtmlReportGenerator(project: Project?) : ReportGenerator(project) {

    private var requirementProperties = Properties()

    @Suppress("NestedBlockDepth", "CyclomaticComplexMethod")
    override fun generate(
        project: Project,
        requirements: List<RequirementData>,
        properties: Properties?,
        specificationSourceProvider: SpecificationSourceProvider
    ): String {
        if (properties != null) {
            requirementProperties = properties
        } else {
            throw IllegalArgumentException("Properties file must not be null")
        }

        val specifications = requirements.map { requirement -> requirement.header.specification }.distinct().toSet()

        val html = createHTMLDocument().html {
            pageHeader()
            body {
                script(src = "requirements/audit_script.js") {}
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.24.1/prism.min.js") {}
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.24.1/components/prism-kotlin.min.js") {}
                pageTitle()
                lineBreak()
                specificationsTableOfContents(specifications)

                floatingActionScrollToTopButton()

                for (specification in specifications) {
                    val specificationSource = SpecificationSource.fromSpec(specification)
                    specification(value = specification)
                    // Do not place the link if it is a BSI specification
                    if (specification != SpecificationSource.BSI_ERP_EPA.spec) {
                        specificationSource?.let(::specificationSourceLink)
                    }
                    val codeRequirements = requirements.filter(specification.trim()).toSet()
                    val codeRequirementHeaders = codeRequirements.map { it.header.requirement.trim() }.toSet()

                    // load the items based on the specification block we are in
                    val requirementSubTitles = requirementProperties.stringPropertyNames()
                        .filter { it.startsWith(specification.trim()) }
                        .map { it.split("$specification.")[1] }

                    // combine the requirements from the code and the properties file and group them by prefix
                    // and sort them by number
                    val combinedRequirementHeaders = codeRequirementHeaders
                        .union(requirementSubTitles)
                        .groupBy { extractPrefix(it) }
                        .toSortedMap()
                        .flatMap { entry -> entry.value.sortedWith(compareBy { extractNumber(it) }) }

                    requirementsTableOfContents(combinedRequirementHeaders)

                    combinedRequirementHeaders.forEach { filteredRequirementHeader ->

                        // get info from the properties file
                        val notes = requirementProperties.getProperty("$specification.$filteredRequirementHeader")
                        requirementHeader(filteredRequirementHeader)

                        // download the specification source
                        specificationSource?.let {
                            specificationSourceProvider.getSourceElements(
                                source = specificationSource,
                                targetRequirement = filteredRequirementHeader,
                                isFromGemSpec = specificationSource.isFromGemSpec,
                                onSuccess = { (sourceHtml: String, elements: List<ElementHolder>) ->
                                    // BSIs do not have a source link
                                    if (sourceHtml != NO_LINK) {
                                        fileLink(sourceHtml)
                                    }
                                    // lineBreak()
                                    div("box") {
                                        elements.forEachIndexed { index, element ->
                                            if (element is Text && index == 0) {
                                                p { b { +element.item } }
                                            } else {
                                                if (element is Text) {
                                                    p { +element.item }
                                                }
                                                if (element is UnorderedList) {
                                                    ul {
                                                        element.items.forEach {
                                                            li {
                                                                p { +it }
                                                            }
                                                        }
                                                    }
                                                }
                                                if (element is Table) {
                                                    table("centered-text") {
                                                        element.rows.forEach { row ->
                                                            tr {
                                                                row.cells.forEach { cell ->
                                                                    if (cell.isHeader) {
                                                                        th {
                                                                            +cell.content
                                                                            rowSpan = cell.rowspan.toString()
                                                                            colSpan = cell.colspan.toString()
                                                                        }
                                                                    } else {
                                                                        td {
                                                                            +cell.content
                                                                            rowSpan = cell.rowspan.toString()
                                                                            colSpan = cell.colspan.toString()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        lineBreak()
                                    }
                                },
                                onFailure = {
                                    println("Probably wrong in document: $filteredRequirementHeader")
                                }
                            )
                        }
                        notesTitle()
                        if (notes != null) {
                            notesBox(notes)
                        }

                        if (codeRequirements.isEmpty()) {
                            horizontalLine()
                        }
                        // print the code from the project
                        codeRequirements
                            .filter { it.header.requirement == filteredRequirementHeader }
                            .map { filteredRequirement ->
                                div(classes = "visible") {
                                    filteredRequirement.body
                                        .sortedBy { it.requirement }
                                        .sortedBy { it.extractSuffixNumber() }
                                        .forEach { requirementBody ->
                                            requirementBody.apply {
                                                subTitle(this)
                                                description(this.rationale())
                                                if (codeBlock.code.isNotEmpty()) {
                                                    // fileLink(this)
                                                    codeBlock(this)
                                                    lineBreak()
                                                }
                                            }
                                        }
                                }
                            }
                        if (codeRequirements.isNotEmpty()) {
                            horizontalLine()
                        }
                    }
                }
            }
        }

        val result = html.serialize(prettyPrint = true)
        return result
    }

    private fun extractPrefix(value: String): String {
        // Extract prefix (e.g., "O.Auth", "O.Arch")
        return value.split("_").firstOrNull() ?: value
    }

    private fun extractNumber(value: String): Int {
        // Find the first occurrence of a numeric segment in the string
        val regex = "\\d+".toRegex()
        val match = regex.find(value)
        return match?.value?.toInt() ?: Int.MAX_VALUE
    }
}
