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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.gradleplugins.reports.html

import de.gematik.ti.erp.gradleplugins.model.AnnotationBody
import de.gematik.ti.erp.gradleplugins.model.SpecificationSource
import kotlinx.html.BODY
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FlowOrHeadingContent
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.br
import kotlinx.html.button
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.h5
import kotlinx.html.head
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.onClick
import kotlinx.html.p
import kotlinx.html.pre
import kotlinx.html.span
import kotlinx.html.sub
import kotlinx.html.title
import kotlinx.html.ul

/**
 * HTML components for the technical requirements report
 * NOTE: The classes and ids used in the components
 * all are related to requirements/audit_style.css and
 * requirements/audit_script.js. Please make sure of those things
 * before changing the classes and ids.
 */

fun HTML.pageHeader() {
    head {
        meta { charset = "utf-8" }
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1"
        }
        title { +"Requirements" }
        // stylesheets
        link(href = "requirements/audit_style.css", rel = "stylesheet")
        link(
            href = "https://cdnjs.cloudflare.com/ajax/libs/prism/1.24.1/themes/prism-tomorrow.min.css",
            rel = "stylesheet"
        )
    }
}

fun BODY.pageTitle() {
    h3("centered-text") { +"TECHNICAL REQUIREMENTS" }
}

fun BODY.floatingActionScrollToTopButton() {
    button(classes = "fab", type = ButtonType.button) {
        id = "scrollToTopBtn"
        onClick = "scrollToTop()"
        +"↑"
    }
}

fun BODY.specificationsTableOfContents(specifications: Set<String>) {
    div("toc-box") {
        p { b { +"Specifications" } }
        ul {
            specifications.forEach { specification ->
                li {
                    a("#$specification") { +specification }
                }
            }
        }
    }
}

fun BODY.requirementsTableOfContents(requirements: List<String>) {
    p { b { +"Requirements" } }
    div("toc-grid") {
        requirements.forEach { requirement ->
            div("toc-item") {
                a("#$requirement") { +requirement }
            }
        }
    }
}

fun BODY.specification(value: String) {
    h2 {
        id = value
        b { +value }
    }
}

fun BODY.specificationSourceLink(value: SpecificationSource) {
    p { a(href = value.url) { +value.url } }
}

fun BODY.requirementHeader(value: String) {
    h4 {
        id = value
        +value
    }
}

fun BODY.notesTitle() {
    p { b { +"Notes and Code" } }
}

fun BODY.notesBox(value: String) {
    p { +value }
}

fun FlowOrPhrasingContent.lineBreak() {
    br { }
}

fun BODY.horizontalLine() {
    hr { }
}

fun FlowOrPhrasingContent.fileLink(value: String) {
    sub {
        a(
            href = value,
            classes = "file-link"
        ) { +value }
    }
}

fun FlowContent.codeBlock(body: AnnotationBody) {
    val codeBlockId = "codeBlock-${body.hashCode()}"

    div(classes = "toggle-row") {
        attributes["data-target"] = codeBlockId
        span(classes = "code-block-label") { fileLink(body) }
        span(classes = "arrow") { +"▼" }
    }

    pre(classes = "code-box") {
        id = codeBlockId
        code(classes = "language-kt") {
            body.codeBlock.code.forEach {
                val codeLine = it.trim()
                if (codeLine.isNotEmpty() && codeLine.isNotBlank()) {
                    p { +it }
                }
            }
        }
    }
}

fun FlowOrHeadingContent.subTitle(body: AnnotationBody) {
    h5 {
        span(classes = "box") {
            +"${body.requirement} - ${body.extractSuffixNumber()}"
        }
    }
}

fun FlowContent.description(value: String) {
    val (intro, listLines, outro) = splitRationaleLines(value.lines())

    if (intro.isNotBlank()) {
        p { renderInlineCode(intro) }
    }

    if (listLines.isNotEmpty()) {
        ul {
            listLines.forEach {
                li {
                    renderInlineCode(
                        it.trimStart()
                            .removePrefix("•")
                            .removePrefix("✅")
                            .removePrefix("-")
                            .trim()
                    )
                }
            }
        }
    }

    if (outro.isNotBlank()) {
        p { renderInlineCode(outro) }
    }
}

fun FlowContent.renderInlineCode(text: String) {
    val regex = Regex("`([^`]+)`")
    var lastIndex = 0

    regex.findAll(text).forEach { match ->
        val before = text.substring(lastIndex, match.range.first)
        if (before.isNotEmpty()) +before
        code { +match.groupValues[1] }
        lastIndex = match.range.last + 1
    }

    val after = text.substring(lastIndex)
    if (after.isNotEmpty()) +after
}

private fun splitRationaleLines(lines: List<String>): Triple<String, List<String>, String> {
    val nonEmptyLines = lines.filter { it.isNotBlank() }

    val bulletStartIndex = nonEmptyLines.indexOfFirst { it.trimStart().startsWithAny("•", "✅", "-") }
    val bulletEndIndex = nonEmptyLines.indexOfLast { it.trimStart().startsWithAny("•", "✅", "-") }

    // If there are no bullet points at all
    if (bulletStartIndex == -1) {
        val introOnly = nonEmptyLines.joinToString(" ").trim()
        return Triple(introOnly, emptyList(), "")
    }

    val intro = if (bulletStartIndex > 0) {
        nonEmptyLines.subList(0, bulletStartIndex).joinToString(" ").trim()
    } else {
        ""
    }

    val bullets = nonEmptyLines.subList(bulletStartIndex, bulletEndIndex + 1)

    val outro = if (bulletEndIndex + 1 < nonEmptyLines.size) {
        nonEmptyLines.subList(bulletEndIndex + 1, nonEmptyLines.size).joinToString(" ").trim()
    } else {
        ""
    }

    return Triple(intro, bullets, outro)
}

private fun String.startsWithAny(vararg prefixes: String): Boolean =
    prefixes.any { this.trimStart().startsWith(it) }

private fun FlowOrPhrasingContent.fileLink(body: AnnotationBody) {
    sub {
        a(
            href = body.fileLink(),
            classes = "file-link"
        ) { +body.fileName }
    }
}
