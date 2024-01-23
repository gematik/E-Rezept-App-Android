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

package de.gematik.ti.erp.gradleplugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import java.io.File

class TechnicalRequirementsPlugin : Plugin<Project> {
    var project: Project? = null
    override fun apply(project: Project) {
        this.project = project
        project.tasks.register("generateTechnicalRequirementsMarkdown") {
            doLast {
                val sourceDirs = setOf(
                    File(project.rootDir.path + "/android/src/main/java/de/gematik/ti/erp/app"),
                    File(project.rootDir.path + "/common/src/commonMain/kotlin/de/gematik/ti/erp/app")
                )
                // println(sourceDirs)
                processSourceDirectory(sourceDirs)
            }
        }
    }

    private fun processSourceDirectory(sourceDirs: Set<File>) {
        val allAnnotations = mutableListOf<AnnotationData>()

        for (sourceDir in sourceDirs) {
            val files = sourceDir.walk().filter { it.isFile }

            files.forEach { file ->
                val annotationData = findAnnotationsInFile(file)
                annotationData.forEach {
                    // println(it)
                }
                allAnnotations.addAll(annotationData)
            }
        }

        generateHtmlReport(allAnnotations)
    }

    private fun findAnnotationsInFile(file: File): MutableList<AnnotationData> {
        val foundAnnotations = mutableListOf<AnnotationData>()

        val content = file.readText()

        val projectRootDir = project?.rootDir?.canonicalPath ?: file.parentFile.canonicalPath
        val relativePath = file.canonicalPath.removePrefix(projectRootDir)
        val fileName = relativePath.removePrefix(File.separator)
        val matches = ANNOTATION_REGEX.findAll(content)
        for (match in matches) {
            val annotationText = match.value
            // println(annotationText)
            val startLine = content.substring(0, match.range.first).count { it == '\n' } + 1

            val requirements = parseRequirements(annotationText)
            val specificationValue = parseSpecification(annotationText)
            val rationaleValue = parseRationale(annotationText)

            for (requirement in requirements) {
                foundAnnotations.add(
                    AnnotationData(
                        fileName,
                        startLine,
                        requirement,
                        specificationValue,
                        rationaleValue
                    )
                )
            }
        }

        return foundAnnotations
    }

    private fun parseRequirements(annotationText: String): List<String> {
        val matches = REQUIREMENT_REGEX.findAll(annotationText)
        val requirements = mutableListOf<String>()

        for (match in matches) {
            val requirement = match.groupValues[1]
            if (!isSpecification(requirement)) {
                requirements.add(requirement)
            }
        }
        return requirements
    }

    private fun isSpecification(text: String): Boolean {
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
        return text in excludedSpecifications
    }

    private fun parseSpecification(annotationText: String): String {
        val match = SPEC_REGEX.find(annotationText)
        return extractTextInsideQuotes(match?.value ?: "")
    }

    private fun parseRationale(annotationText: String): String {
        val match = RATIONALE_REGEX.find(annotationText)
        println(annotationText)
        println(match?.value ?: "_________")

        return match?.value ?: ""
    }

    private fun extractTextInsideQuotes(input: String): String {
        return QUOTES_REGEX.find(input)?.value?.replace("\"", "") ?: ""
    }

    companion object {
        private val ANNOTATION_REGEX = Regex("""@Requirement\([^)]*\)""", RegexOption.MULTILINE)
        val REQUIREMENT_REGEX = Regex("""\s*"([a-zA-Z_][a-zA-Z0-9_#-.]*)"\s*,?\s*""")
        private val SPEC_REGEX = Regex("""sourceSpecification\s*=\s*"(.*?)"""")
        private val RATIONALE_REGEX = Regex("""rationale\s*=\s*[^)]*""", RegexOption.MULTILINE)
        private val QUOTES_REGEX = Regex(""""(.*?)"""")
    }

    private fun generateHtmlReport(annotationList: List<AnnotationData>) {
        val specifications = annotationList.map { it.specification }.distinct()

        val htmlBuilder = StringBuilder()

        htmlBuilder.append("<!DOCTYPE html>")
        htmlBuilder.append("<meta charset=\"UTF-8\">")
        htmlBuilder.append("<html>")
        htmlBuilder.append("<head>")
        htmlBuilder.append("<title>Technical Requirements Report</title>")

        htmlBuilder.append("</head>")
        htmlBuilder.append("<body>")

        htmlBuilder.append("<h1>Technical Requirements Report</h1>")

        htmlBuilder.append("<ul>")
        for (specification in specifications) {
            htmlBuilder.append("<li>")
            htmlBuilder.append("<h2 class=\"toggle-line\" onclick=\"toggleNextLine(this)\">$specification</h2>")
            htmlBuilder.append("<ul class=\"hidden\">")
            val annotationDataList = annotationList.filter { it.specification == specification }
                .sortedWith(compareBy<AnnotationData> { it.requirement }.thenBy { it.extractSuffixNumber() ?: 0 })
            for (data in annotationDataList) {
                htmlBuilder.append("<li>")
                htmlBuilder.append(
                    "<h3 class=\"toggle-line\" onclick=\"toggleNextLine(this)\">" +
                        "${data.requirement}</h3>"
                )
                htmlBuilder.append("<div class=\"hidden styled-div\">${data.toHTML()}</div>")
                htmlBuilder.append("</li>")
            }
            htmlBuilder.append("</ul>")
            htmlBuilder.append("</li>")
        }
        htmlBuilder.append("</ul>")

        htmlBuilder.append("</body>")
        htmlBuilder.append("</html>")

        htmlBuilder.append("<style>")
        htmlBuilder.append("h2 { cursor: pointer;}")
        htmlBuilder.append("h3 { cursor: pointer;}")
        htmlBuilder.append(".hidden { display: none; }")
        htmlBuilder.append(".styled-div { padding-left: 2.5em; }")
        htmlBuilder.append("</style>")

        htmlBuilder.append("<script>")
        htmlBuilder.append("function toggleNextLine(clickedElement) {")
        htmlBuilder.append("const nextLine = clickedElement.nextElementSibling;")
        htmlBuilder.append("nextLine.classList.toggle('hidden');")
        htmlBuilder.append("}")
        htmlBuilder.append("</script>")

        val outputFile = File(project?.rootDir?.path + "/technical_requirements_report.html")
        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }
        outputFile.writeText(htmlBuilder.toString())
    }
}

data class AnnotationData(
    val fileName: String,
    val line: Int,
    val requirement: String,
    val specification: String,
    val rationale: String,
    val suffixNumber: Int = 0 // Default value for requirements without a suffix

) {

    @Suppress("MagicNumber")
    fun extractSuffixNumber(): Int {
        val suffixIndex = requirement.indexOf('#')
        return if (suffixIndex != -1 && suffixIndex < requirement.length - 1) {
            requirement.substring(suffixIndex + 1).toIntOrNull() ?: 0
        } else {
            0
        }
    }
    fun toHTML(): String {
        return """
        <h4>Filename</h4>
        <p>$fileName</p>
        <h4>Line</h4>
        <p>$line</p>
        <h4>Description</h4>
        <p>${formatDescription(rationale)}</p>
        """.trimIndent()
    }

    private fun formatDescription(description: String): String {
        return description
            .replace("rationale =", "")
            .replace("\"", "")
            .replace("+", " ")
            .replace("\n", "<br>")
            .removeSuffix(")")
    }
}
