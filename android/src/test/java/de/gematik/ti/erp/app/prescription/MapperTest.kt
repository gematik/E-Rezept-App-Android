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

// TODO: work in progress - replace fhir parser

//
// package de.gematik.ti.erp.app.prescription
//
// import ca.uhn.fhir.context.FhirContext
// import de.gematik.ti.erp.app.db.entities.v1.task.OrganizationEntityV1
// import de.gematik.ti.erp.app.prescription.repository.FhirOrganization
// import de.gematik.ti.erp.app.prescription.repository.extractResources
// import de.gematik.ti.erp.app.prescription.repository.toOrganizationEntityV1
// import org.hl7.fhir.r4.model.Bundle
// import org.hl7.fhir.r4.model.Organization
// import org.hl7.fhir.r4.model.Property
// import java.util.zip.ZipEntry
// import java.util.zip.ZipFile
// import java.util.zip.ZipInputStream
// import kotlin.test.BeforeTest
// import kotlin.test.Test
// import kotlin.test.assertEquals
//
// private typealias FlatBundle = Map<String, String>
//
// class MapperTest {
//
//    @BeforeTest
//    fun setUp() {
//    }
//
//    @Test
//    fun `medication request`() {
//        val parser = FhirContext.forR4().newXmlParser()
//
//        ZipFile("src/test/res/KBV_1.0.2_1000_Auswahl.zip").use { zip ->
//            zip.entries().asSequence().forEach { entry ->
//                println(entry.name)
//                zip.getInputStream(entry).use { input ->
//                    val bundle = try {
//                         parser.parseResource(input) as Bundle
//                    } catch (e : Exception) {
//                        println(e)
//                        null
//                    }
//
//                    if (bundle != null) {
//                        val flatBundle = walkBundle(bundle)
//
//                        flatBundle.print()
//
//                        testOrganizationEntityV1(
//                            flatBundle,
//                            bundle.extractResources<FhirOrganization>().firstOrNull()!!.toOrganizationEntityV1()
//                        )
//
//                        flatBundle.print()
//                    }
//                }
//                return@use
//            }
//        }
//    }
//
//    private fun testOrganizationEntityV1(flatBundle: FlatBundle, organization: OrganizationEntityV1) {
//        val (organizationIndex) = flatBundle.indicesFor("resource","Organization")
//
//        assertEquals(flatBundle["entry{$organizationIndex}.resource{0}.name"], organization.name)
//
//        val (phoneIndex) = flatBundle.indicesFor("system", "phone", "entry{$organizationIndex}.resource{0}.telecom")
//
//        val telecomPrefix = "entry{$organizationIndex}.resource{0}"
//        assertEquals(flatBundle["$telecomPrefix.telecom{$phoneIndex}.system"], "phone")
//        assertEquals(flatBundle["$telecomPrefix.telecom{$phoneIndex}.value"], organization.phone)
//        // assertEquals(flatBundle["$telecomPrefix.telecom{$phoneIndex}.system"], "fax")
//        // assertEquals(flatBundle["$telecomPrefix.telecom{$phoneIndex}.value"], organization.fax)
//        assertEquals(flatBundle["$telecomPrefix.telecom{$phoneIndex}.system"], "email")
//        assertEquals(flatBundle["$telecomPrefix.telecom{$phoneIndex}.value"], organization.mail)
// //                        entry{5}.resource{0}.telecom{0}.value: 0301234567
// //                        entry{5}.resource{0}.telecom{1}.system: fax
// //                        entry{5}.resource{0}.telecom{1}.value: 030123456789
// //                        entry{5}.resource{0}.telecom{2}.system: email
// //                        entry{5}.resource{0}.telecom{2}.value: mvz@e-mail.de
// //                        entry{5}.resource{0}.address{0}.type: both
// //                        entry{5}.resource{0}.address{0}.line: Herbert-Lewin-Platz 2
//
//    }
//
//
//
// //    fun List<Pair<String, String>>.valueOf(path: String) = find { (p, _) -> p == path }?.second
// //
// //    fun List<Pair<String, String>>.pathPrefixForResource(type: String, prefix: String = ""): String? =
// //        this.find { (name, value) ->
// //            name.startsWith(prefix) && name.endsWith("resource") && value == type
// //        }?.let { (name) ->
// //            name
// //        }
//
//
// //
// //    fun List<Pair<String, String>>.pathPrefixFor(path: String, value: String, prefix: String = ""): String? {
// //        val pathRegex = path.replace("{?}", "\\{\\d}").toRegex()
// //
// //        return find { (name, v) ->
// //            if (name.startsWith(prefix)) {
// //                name.removePrefix(prefix).matches(pathRegex) && v == value
// //            } else {
// //                false
// //            }
// //        }?.let { (name) ->
// //            name
// //        }
// //    }
// //
// //    fun String.popPath() =
// //        this@popPath.substringBeforeLast('.')
//
//
// }
//
// private fun FlatBundle.print() {
//    forEach { (k, v) ->
//        println("$k: $v")
//    }
// }
//
// private val rg = """\{(\d)}""".toRegex()
//
// private fun FlatBundle.indicesFor(type: String, value: String, prefix: String = ""): List<Int> {
//    // find matching entry
//    val entry = entries.find { (name, v) ->
//        name.startsWith(prefix) && name.endsWith(type) && v == value
//    }
//
//    // return all indices contained in `{?}`
//    return entry?.let { (name) ->
//        rg.findAll(name.removePrefix(prefix)).map { match ->
//            match.groupValues[1].toInt()
//        }.toList()
//    } ?: emptyList()
// }
//
// private fun walkBundle(bundle: Bundle): FlatBundle {
//    val out = mutableMapOf<String, String>()
//    bundle.children().forEach {
//        walk(property = it, name = null, out = out)
//    }
//    return out
// }
//
// private fun walk(property: Property, name: String?, out: MutableMap<String, String>) {
//    val prefix = name?.let { "$name.${property.name}" } ?: property.name
//
//    property.values.forEachIndexed { index, base ->
//        if (base.isPrimitive) {
//            out[prefix] = base.primitiveValue()
//        }
//        if (base.isResource) {
//            out[prefix] = base.fhirType()
//        }
//        base.children().forEach {
//            walk(it, "$prefix{$index}", out)
//        }
//    }
// }
