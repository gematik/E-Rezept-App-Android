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

package de.gematik.ti.erp.gradleplugins

import de.gematik.ti.erp.gradleplugins.Regex.ANNOTATION_REGEX
import de.gematik.ti.erp.gradleplugins.Regex.CODE_LINES_REGEX
import de.gematik.ti.erp.gradleplugins.Regex.QUOTES_REGEX
import de.gematik.ti.erp.gradleplugins.Regex.REQUIREMENT_REGEX
import de.gematik.ti.erp.gradleplugins.Regex.SPEC_REGEX
import de.gematik.ti.erp.gradleplugins.model.AnnotationBody
import de.gematik.ti.erp.gradleplugins.model.AnnotationHeader
import de.gematik.ti.erp.gradleplugins.model.CodeBlock
import de.gematik.ti.erp.gradleplugins.model.RequirementData
import de.gematik.ti.erp.gradleplugins.model.SpecificationSource
import de.gematik.ti.erp.gradleplugins.provider.SpecificationSourceProvider
import de.gematik.ti.erp.gradleplugins.reports.ReportGenerator
import de.gematik.ti.erp.gradleplugins.reports.html.HtmlReportGenerator
import de.gematik.ti.erp.gradleplugins.repository.SourceSpecificationRepository
import de.gematik.ti.erp.gradleplugins.usecase.ExtractSourceSpecificationUseCase
import de.gematik.ti.erp.gradleplugins.usecase.GetSourceSpecificationUseCase
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.Properties

class TechnicalRequirementsPlugin : Plugin<Project> {
    private var project: Project? = null
    private var reportGenerator: ReportGenerator? = null
    private val specificationSourceProvider = loadSpecificationSourceProvider()

    // Load the notes for all the requirements from the properties file
    private val requirementProperties = Properties()

    override fun apply(project: Project) {
        // run ./gradlew :downloadBsiSpecs -Ptoken=YOUR_PRIVATE_TOKEN
        project.tasks.register<Exec>(downloadBsiSpecs) {
            val downloadUrl = SpecificationSource.BSI_ERP_EPA.url
            project.getToken()?.let { token ->
                val outputFile = File(
                    "${project.rootProject.projectDir}/$REQUIREMENTS_PATH",
                    BSI_REQUIREMENTS_FILE_NAME
                )
                commandLine("curl", "--header", "PRIVATE-TOKEN: $token", "-o", outputFile.absolutePath, downloadUrl)
                doLast {
                    println("Downloaded HTML page saved to ${outputFile.absolutePath}")
                }
            }
        }

        // run ./gradlew :generateTechnicalRequirements to generate the technical requirements file
        project.tasks.register(generateTechnicalRequirements) {
            init(project)
            // load all the files that need to be analyzed
            doLast {
                val sourceDirs = setOf(
                    File(project.rootDir.path, ANDROID_APP_PATH),
                    File(project.rootDir, APP_FEATURES_PATH),
                    File(project.rootDir.path, SHARED_MODULE_PATH),
                    File(project.rootDir.path, SHARED_TEST_MODULE_PATH),
                    File(project.rootDir.path, SHARED_ANDROID_MODULE_PATH),
                    File(project.rootDir.path, FHIR_PARSER_MODULE_PATH)
                )
                try {
                    val requirements = generateRequirements(sourceDirs)
                    reportGenerator?.generate(
                        project = project,
                        requirements = requirements,
                        properties = requirementProperties,
                        specificationSourceProvider = specificationSourceProvider
                    )?.let { result ->
                        val reportFile = File(
                            "${project.rootProject.projectDir}",
                            "requirements-report.html"
                        )
                        reportFile.writeText(result)
                    } ?: run {
                        println("Error: Report generation failed")
                    }
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
            finalizedBy(extractRequirements)
        }

        // run ./gradlew :extractRequirements to prepare a folder for the gutachter
        project.tasks.register<Copy>(extractRequirements) {
            val sourceRoot = project.rootDir
            val targetDir = File(sourceRoot, gutachterFolder)
            from(File(sourceRoot, "requirements")) {
                include("audit_script.js")
                include("audit_style.css")
                into("requirements")
            }
            from(File(sourceRoot, "requirements-report.html"))
            into(targetDir)

            doLast {
                println("Files copied to ${targetDir.absolutePath}")
            }
        }
    }

    private fun init(project: Project) {
        this.project = project
        val requirementsFile = project.file(REQUIREMENTS_FILE)
        requirementProperties.load(requirementsFile.reader())
        reportGenerator = HtmlReportGenerator(project)
    }

    private fun loadSpecificationSourceProvider(
        repository: SourceSpecificationRepository = SourceSpecificationRepository(),
        extractSourceSpecificationUseCase: ExtractSourceSpecificationUseCase = ExtractSourceSpecificationUseCase(),
        getSourceSpecificationUseCase: GetSourceSpecificationUseCase = GetSourceSpecificationUseCase(repository)
    ): SpecificationSourceProvider = SpecificationSourceProvider(
        getSourceSpecificationUseCase = getSourceSpecificationUseCase,
        extractSourceSpecificationUseCase = extractSourceSpecificationUseCase
    )

    /**
     * Generate the requirements from the source directories
     * Create a list of [RequirementData] objects which hold the requirement header and the body
     */
    private fun generateRequirements(sourceDirs: Set<File>): List<RequirementData> {
        val annotations = mutableListOf<RequirementData>()
        val annotationBodies = mutableListOf<AnnotationBody>()
        val annotationHeaders = mutableSetOf<String>()

        for (sourceDir in sourceDirs) {
            val files = sourceDir.walk().filter { it.isFile }

            files.forEach { file ->
                val annotationBody = extractRequirementBody(file)
                annotationBody.map { it.requirement }.forEach { requirement ->
                    annotationHeaders.add(requirement.sanitizeRequirement())
                }
                annotationBodies.addAll(annotationBody)
            }
        }

        // TODO: Check for empty header
        annotationHeaders.forEach { requirement ->
            val bodies = annotationBodies.filter { it.requirement.sanitizeRequirement() == requirement }
            val specification = bodies.firstOrNull()?.specification ?: ""
            val specificationSource = SpecificationSource.fromSpec(specification)
            annotations.add(
                RequirementData(
                    AnnotationHeader(
                        specification = specification,
                        requirement = requirement,
                        specificationSource = specificationSource
                    ),
                    bodies
                )
            )
        }

        annotationHeaders.clear()
        annotationBodies.clear()

        return annotations.toList()
    }

    @Suppress("MagicNumber")
    private fun extractRequirementBody(file: File): MutableList<AnnotationBody> {
        val annotationBodies = mutableListOf<AnnotationBody>()
        val content = file.readText()

        val projectRootDir = project?.rootDir?.canonicalPath ?: file.parentFile.canonicalPath
        val relativePath = file.canonicalPath.removePrefix(projectRootDir)
        val fileName = relativePath.removePrefix(File.separator)
        val requirementMatches = ANNOTATION_REGEX.findAll(content)
        for (requirementMatch in requirementMatches) {
            val annotationText = requirementMatch.value
            val startLine = content.substring(0, requirementMatch.range.first).count { it == '\n' } + 1
            val code = content.split(annotationText)[1].removeAllRequirementAnnotations()

            val requirements = parseRequirements(annotationText)
            val specificationValue = parseSpecification(annotationText)
            val rationaleValue = parseRationale(annotationText)

            requirements.forEach { requirement ->
                annotationBodies.add(
                    AnnotationBody(
                        fileName = fileName,
                        line = startLine,
                        requirement = requirement,
                        specification = specificationValue,
                        rationale = rationaleValue,
                        codeBlock = CodeBlock(
                            code = when {
                                code.isNotEmpty() && code.isNotBlank() -> {
                                    val requiredCodeLines = parseCodeLines(annotationText, 15)
                                    val codeLines = code.split("\n").take(requiredCodeLines)
                                    codeLines
                                }

                                else -> emptyList()
                            }
                        )
                    )
                )
            }
        }

        return annotationBodies
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

    private fun isSpecification(text: String): Boolean = text in excludedSpecifications

    private fun parseSpecification(annotationText: String): String {
        val match = SPEC_REGEX.find(annotationText)
        return extractTextInsideQuotes(match?.value ?: "")
    }

    private fun parseRationale(annotationText: String): String {
        val tripleQuoteRegex = Regex(
            """rationale\s*=\s*\"\"\"(.*?)\"\"\"""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
        )
        val multiLineConcatRegex = Regex(
            """rationale\s*=\s*((?:"[^"]*"\s*\+\s*)*"[^"]*")""",
            RegexOption.MULTILINE
        )

        // 1. Check for triple-quoted string block
        val tripleMatch = tripleQuoteRegex.find(annotationText)?.groupValues?.get(1)
        if (tripleMatch != null) return tripleMatch

        // 2. Fallback: extract and join concatenated strings
        val match = multiLineConcatRegex.find(annotationText)?.groupValues?.get(1) ?: return ""
        val stringParts = Regex(""""([^"]*)"""").findAll(match).map { it.groupValues[1] }

        return stringParts.joinToString("")
    }

    @Suppress("MagicNumber")
    private fun parseCodeLines(annotationText: String, defaultLines: Int = 15): Int {
        val match = CODE_LINES_REGEX.find(annotationText)
        return match?.value?.let { value ->
            val integerValue = value.split(CODE_LINE).last().trim()
            try {
                Integer.parseInt(integerValue)
            } catch (e: NumberFormatException) {
                println("Error: ${e.stackTraceToString()}")
                defaultLines
            }
        } ?: defaultLines
    }

    private fun extractTextInsideQuotes(input: String): String {
        return QUOTES_REGEX.find(input)?.value?.replace("\"", "") ?: ""
    }

    private fun Project.getToken(): String? = findProperty("token") as? String
}
