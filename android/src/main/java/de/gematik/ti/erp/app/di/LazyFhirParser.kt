/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.di

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.parser.IParser
import ca.uhn.fhir.parser.IParserErrorHandler
import ca.uhn.fhir.rest.api.EncodingEnum
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.instance.model.api.IIdType
import java.io.InputStream
import java.io.Reader
import java.io.Writer

class LazyFhirParser : IParser {
    private val fhirParser: IParser by lazy { FhirContext.forR4().newJsonParser() }

    override fun encodeResourceToString(theResource: IBaseResource?): String {
        return fhirParser.encodeResourceToString(theResource)
    }

    override fun encodeResourceToWriter(theResource: IBaseResource?, theWriter: Writer?) {
        return fhirParser.encodeResourceToWriter(theResource, theWriter)
    }

    override fun getEncodeForceResourceId(): IIdType {
        return fhirParser.getEncodeForceResourceId()
    }

    override fun getEncoding(): EncodingEnum {
        return fhirParser.getEncoding()
    }

    override fun getPreferTypes(): MutableList<Class<out IBaseResource>> {
        return fhirParser.getPreferTypes()
    }

    override fun isOmitResourceId(): Boolean {
        return fhirParser.isOmitResourceId()
    }

    override fun getStripVersionsFromReferences(): Boolean {
        return fhirParser.getStripVersionsFromReferences()
    }

    override fun isSummaryMode(): Boolean {
        return fhirParser.isSummaryMode()
    }

    override fun <T : IBaseResource?> parseResource(theResourceType: Class<T>?, theReader: Reader?): T {
        return fhirParser.parseResource(theResourceType, theReader)
    }

    override fun <T : IBaseResource?> parseResource(theResourceType: Class<T>?, theInputStream: InputStream?): T {
        return fhirParser.parseResource(theResourceType, theInputStream)
    }

    override fun <T : IBaseResource?> parseResource(theResourceType: Class<T>?, theString: String?): T {
        return fhirParser.parseResource(theResourceType, theString)
    }

    override fun parseResource(theReader: Reader?): IBaseResource {
        return fhirParser.parseResource(theReader)
    }

    override fun parseResource(theInputStream: InputStream?): IBaseResource {
        return fhirParser.parseResource(theInputStream)
    }

    override fun parseResource(theMessageString: String?): IBaseResource {
        return fhirParser.parseResource(theMessageString)
    }

    override fun setDontEncodeElements(theDontEncodeElements: MutableCollection<String>?): IParser {
        return fhirParser.setDontEncodeElements(theDontEncodeElements)
    }

    override fun setEncodeElements(theEncodeElements: MutableSet<String>?): IParser {
        return fhirParser.setEncodeElements(theEncodeElements)
    }

    override fun setEncodeElementsAppliesToChildResourcesOnly(theEncodeElementsAppliesToChildResourcesOnly: Boolean) {
        return fhirParser.setEncodeElementsAppliesToChildResourcesOnly(theEncodeElementsAppliesToChildResourcesOnly)
    }

    override fun isEncodeElementsAppliesToChildResourcesOnly(): Boolean {
        return fhirParser.isEncodeElementsAppliesToChildResourcesOnly()
    }

    override fun setEncodeForceResourceId(theForceResourceId: IIdType?): IParser {
        return fhirParser.setEncodeForceResourceId(theForceResourceId)
    }

    override fun setOmitResourceId(theOmitResourceId: Boolean): IParser {
        return fhirParser.setOmitResourceId(theOmitResourceId)
    }

    override fun setParserErrorHandler(theErrorHandler: IParserErrorHandler?): IParser {
        return fhirParser.setParserErrorHandler(theErrorHandler)
    }

    override fun setPreferTypes(thePreferTypes: MutableList<Class<out IBaseResource>>?) {
        return fhirParser.setPreferTypes(thePreferTypes)
    }

    override fun setPrettyPrint(thePrettyPrint: Boolean): IParser {
        return fhirParser.setPrettyPrint(thePrettyPrint)
    }

    override fun setServerBaseUrl(theUrl: String?): IParser {
        return fhirParser.setServerBaseUrl(theUrl)
    }

    override fun setStripVersionsFromReferences(theStripVersionsFromReferences: Boolean?): IParser {
        return fhirParser.setStripVersionsFromReferences(theStripVersionsFromReferences)
    }

    override fun setOverrideResourceIdWithBundleEntryFullUrl(theOverrideResourceIdWithBundleEntryFullUrl: Boolean?): IParser {
        return fhirParser.setOverrideResourceIdWithBundleEntryFullUrl(theOverrideResourceIdWithBundleEntryFullUrl)
    }

    override fun setSummaryMode(theSummaryMode: Boolean): IParser {
        return fhirParser.setSummaryMode(theSummaryMode)
    }

    override fun setSuppressNarratives(theSuppressNarratives: Boolean): IParser {
        return fhirParser.setSuppressNarratives(theSuppressNarratives)
    }

    override fun setDontStripVersionsFromReferencesAtPaths(vararg thePaths: String?): IParser {
        return fhirParser.setDontStripVersionsFromReferencesAtPaths(*thePaths)
    }

    override fun setDontStripVersionsFromReferencesAtPaths(thePaths: MutableCollection<String>?): IParser {
        return fhirParser.setDontStripVersionsFromReferencesAtPaths(thePaths)
    }

    override fun getDontStripVersionsFromReferencesAtPaths(): MutableSet<String> {
        return fhirParser.getDontStripVersionsFromReferencesAtPaths()
    }
}
