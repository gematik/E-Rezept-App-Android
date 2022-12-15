/*
 * Copyright (c) 2022 gematik GmbH
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

@file:Suppress("ktlint:max-line-length", "MaxLineLength")

package de.gematik.ti.erp.stringResGen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.buildCodeBlock
import java.io.File
import java.time.LocalDateTime
import java.util.Locale
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@Serializable
internal sealed class ResTranslatable {
    abstract val name: String
}

@Serializable
@XmlSerialName("string", "", "")
internal data class ResSingular(
    override val name: String,
    @XmlValue(true)
    val string: String
) : ResTranslatable()

@Serializable
@XmlSerialName("plurals", "", "")
internal data class ResPlural(
    override val name: String,
    @XmlElement(true)
    @XmlSerialName("item", "", "")
    val items: List<ResPluralItem>
) : ResTranslatable()

@Serializable
internal data class ResPluralItem(
    val quantity: String,
    @XmlValue(true)
    val string: String
)

@Serializable
@XmlSerialName("resources", "", "")
internal data class Resources(
    @XmlElement(true)
    val translatable: List<ResTranslatable>
)

open class AndroidStringResourceGeneratorTask : DefaultTask() {
    @Input
    lateinit var resourceFiles: List<Pair<String, Locale>>

    @OutputDirectory
    lateinit var outputPath: File

    @Input
    lateinit var packagePath: String

    @OptIn(ExperimentalXmlUtilApi::class, ExperimentalStdlibApi::class)
    @TaskAction
    fun generateStringResources() {
        val xml = XML {
            policy = DefaultXmlSerializationPolicy(
                pedantic = false,
                autoPolymorphic = true,
                unknownChildHandler = { _, _, _, _, _ -> emptyList() }
            )
        }

        val serializer = serializer<Resources>()
        val resources = resourceFiles.map { (path, locale) ->
            Pair(locale, xml.decodeFromString(serializer, File(path).readBytes().decodeToString()))
        }.groupBy {
            it.first
        }.mapValues { (_, value) ->
            value.flatMap { it.second.translatable }
        }

        val primaryUniqueNames = resources.values.first().map { it.name }
        val primaryUniqueNamesWithId = primaryUniqueNames.mapIndexed { index: Int, s: String -> Pair(s, index) }.toMap()

        val trTypeName = TypeVariableName("Translatable")
        val trListTypeName = TypeVariableName("Map<Int, Translatable>")

        val stableAnnotation = ClassName("androidx.compose.runtime", "Stable")

        val stringsClassSpec = TypeSpec
            .classBuilder("Strings")
            .addAnnotation(stableAnnotation)
            .let { classBuilder ->
                val constBuilder = FunSpec.constructorBuilder().addParameter("src", trListTypeName)

                primaryUniqueNamesWithId.forEach { (s, index) ->
                    val name = s.toCamelCase()
                    classBuilder.addProperty(
                        PropertySpec.builder(name, trTypeName)
                            .addAnnotation(stableAnnotation)
                            .initializer("src.getValue(%L)", index)
                            .build()
                    )
                }

                classBuilder.primaryConstructor(constBuilder.build())
            }

        val valueSpecs = resources.map { (locale, translatable) ->
            val primaryLocale = resources.keys.first()
            val thisTrs = translatable.map { it.name }.toSet()
            val primaryTrs = primaryUniqueNames.toSet()
            val missingTrs = primaryTrs - thisTrs
            val additionalTrs = thisTrs - primaryTrs

            if (additionalTrs.isNotEmpty()) {
                println(
                    "WARNING: Additional `${locale.language}` keys not found in primary language `${primaryLocale.language}`"
                )
                println("WARNING: $additionalTrs")
                println("WARNING: These keys will be ignored!")
                println()
            }

            PropertySpec.builder(
                stringsVariableName(locale),
                TypeVariableName("Strings")
            )
                .let { valueSpec ->
                    valueSpec.initializer(
                        buildCodeBlock {
                            addStatement("Strings(")
                            indent()
                            addStatement("mapOf(")
                            indent()
                            translatable.filter { it.name in primaryTrs }.forEach { tr ->
                                when (tr) {
                                    is ResPlural ->
                                        addStatement(
                                            "%L to Plurals(${tr.items.toTemplateString()}),",
                                            primaryUniqueNamesWithId.getValue(tr.name),
                                            *tr.items.toArgArray()
                                        )
                                    is ResSingular ->
                                        addStatement(
                                            "%L to Singular(%L),",
                                            primaryUniqueNamesWithId.getValue(tr.name),
                                            tr.string.escapeLiteralString()
                                        )
                                }
                            }
                            missingTrs.forEach {
                                val name = it.toCamelCase()
                                val id = primaryUniqueNamesWithId.getValue(it)
                                addStatement("%L to %L.%L,", id, stringsVariableName(primaryLocale), name)
                            }
                            unindent()
                            addStatement(")")
                            unindent()
                            addStatement(")")
                        }
                    )
                }
        }

        FileSpec.builder(packagePath, "StringResource")
            .addFileComment("\nDO NOT MODIFY - GENERATED ON ${LocalDateTime.now()}\n")
            .addAnnotation(
                AnnotationSpec.builder(ClassName("", "Suppress"))
                    .addMember("%S", "RedundantVisibilityModifier")
                    .build()
            )
            .addType(stringsClassSpec.build())
            .apply {
                valueSpecs.forEach {
                    addProperty(it.build())
                }
            }
            .addFunction(
                FunSpec.builder("getStrings")
                    .addParameter("locale", Locale::class)
                    .returns(TypeVariableName("Strings"))
                    .addCode(
                        buildCodeBlock {
                            val format = resources.keys.joinToString(", ") { "%S to %L" }
                            val values =
                                resources.keys.flatMap { listOf(it.language, stringsVariableName(it)) }.toTypedArray()

                            addStatement("val strings = mapOf($format)", *values)
                            addStatement(
                                "return strings.getOrDefault(locale.language, %L)",
                                stringsVariableName(resources.keys.first())
                            )
                        }
                    )
                    .build()
            )
            .build().writeTo(outputPath)

        println("Wrote StringResource.kt file to: ${outputPath.name}")
    }

    private fun String.escapeLiteralString(): String =
        "\"${replace(" ", "·").replace("$", "\\$")}\""

    @OptIn(ExperimentalStdlibApi::class)
    private fun stringsVariableName(locale: Locale) =
        "strings${locale.language.replaceFirstChar { it.uppercaseChar() }}"

    @OptIn(ExperimentalStdlibApi::class)
    private fun String.toCamelCase(): String =
        lowercase(Locale.getDefault())
            .split("_")
            .joinToString("") { s -> s.replaceFirstChar { it.uppercaseChar() } }
            .replaceFirstChar { it.lowercaseChar() }

    private fun List<ResPluralItem>.toTemplateString(): String =
        "mapOf(" + joinToString(", ") {
            "%L to %L"
        } + ")"

    private fun List<ResPluralItem>.toArgArray(): Array<String> =
        flatMap {
            listOf(
                when (it.quantity) {
                    "zero" -> "Plurals.Type.Zero"
                    "one" -> "Plurals.Type.One"
                    "two" -> "Plurals.Type.Two"
                    "few" -> "Plurals.Type.Few"
                    "many" -> "Plurals.Type.Many"
                    "other" -> "Plurals.Type.Other"
                    else -> error("unknown quantity")
                },
                it.string.escapeLiteralString()
            )
        }.toTypedArray()
}
