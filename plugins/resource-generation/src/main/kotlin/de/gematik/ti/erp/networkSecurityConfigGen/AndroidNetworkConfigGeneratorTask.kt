/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.networkSecurityConfigGen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.buildCodeBlock
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.SerialName
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
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

private val warningDuration = Duration.ofDays(30)

@Serializable
@XmlSerialName("digest", "", "")
internal enum class Digest {
    @SerialName("SHA-256")
    SHA256,

    @SerialName("SHA-1")
    SHA1
}

@Serializable
@XmlSerialName("pin", "", "")
internal data class Pin(
    @XmlElement(false)
    val digest: Digest,
    @XmlValue(true)
    val hash: String
)

@Serializable
@XmlSerialName("pin-set", "", "")
internal data class PinSet(
    val expiration: String,
    val pins: List<Pin>
)

@Serializable
@XmlSerialName("domain", "", "")
internal data class Domain(
    val includeSubdomains: Boolean,
    @XmlValue(true)
    val domain: String
)

@Serializable
@XmlSerialName("domain-config", "", "")
internal data class DomainConfig(
    val domain: Domain,
    val pinSet: PinSet
)

@Serializable
@XmlSerialName("network-security-config", "", "")
internal data class NetworkConfig(
    @XmlElement(true)
    val domainConfigurations: List<DomainConfig>
)

open class AndroidNetworkConfigGeneratorTask : DefaultTask() {
    @InputFile
    lateinit var resourceFile: File

    @OutputDirectory
    lateinit var outputPath: File

    @Input
    lateinit var packagePath: String

    @OptIn(ExperimentalXmlUtilApi::class, ExperimentalStdlibApi::class)
    @TaskAction
    fun generateNetworkConfig() {
        val xml = XML {
            policy = DefaultXmlSerializationPolicy(
                pedantic = false,
                autoPolymorphic = true,
                unknownChildHandler = { _, _, _, _, _ -> emptyList() }
            )
        }

        val serializer = serializer<NetworkConfig>()
        val config = xml.decodeFromString(serializer, resourceFile.readBytes().decodeToString())

        FileSpec.builder(packagePath, "Pinning")
            .addFileComment("\nDO NOT MODIFY - GENERATED ON ${LocalDateTime.now()}\n")
            .addImport("okhttp3", "CertificatePinner")
            .addAnnotation(
                AnnotationSpec.builder(ClassName("", "Suppress"))
                    .addMember("%S", "RedundantVisibilityModifier")
                    .build()
            )
            .addFunction(
                FunSpec.builder("buildCertificatePinner")
                    .returns(TypeVariableName("CertificatePinner"))
                    .addCode(
                        buildCodeBlock {
                            addStatement(
                                "return %L",
                                buildCodeBlock {
                                    indent()
                                    addStatement("CertificatePinner.Builder()")
                                    indent()
                                    config.domainConfigurations.forEach {
                                        val domainPath = it.domain.domain
                                        val subDomainsPrefix = if (it.domain.includeSubdomains) "**." else ""

                                        val expiresOn = LocalDate.parse(it.pinSet.expiration).atStartOfDay()
                                        val now = LocalDate.now().atStartOfDay()
                                        if (now > expiresOn) {
                                            error("Pin-Set for `$domainPath` expired! Remove them from the config.")
                                        }
                                        if (now + warningDuration >= expiresOn) {
                                            println(
                                                "WARNING: Only ${
                                                Duration.between(now, expiresOn).toDays()
                                                } days left until pin-set $domainPath expires"
                                            )
                                        }
                                        it.pinSet.pins.forEach {
                                            val prefix = when (it.digest) {
                                                Digest.SHA256 -> "sha256"
                                                Digest.SHA1 -> "sha1"
                                            }
                                            addStatement("// expires on %L", expiresOn.toString())
                                            addStatement(
                                                ".add(%S, %S)",
                                                "$subDomainsPrefix$domainPath",
                                                "$prefix/${it.hash}"
                                            )
                                        }
                                    }
                                    unindent()
                                    addStatement(".build()")
                                    unindent()
                                }
                            )
                        }
                    )
                    .build()
            )
            .build().writeTo(outputPath)
    }
}
