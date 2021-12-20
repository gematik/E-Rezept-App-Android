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

package de.gematik.ti.erp.app.core

import ca.uhn.fhir.parser.IParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Resource
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class FhirConverterFactory(private val parser: IParser) : Converter.Factory() {

    companion object {
        fun create(parser: IParser) = FhirConverterFactory(parser)
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return FhirBundleConverter(parser)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<Resource, RequestBody> {
        return FhirResourceConverter(parser)
    }

    class FhirBundleConverter(private val parser: IParser) : Converter<ResponseBody, Any> {

        override fun convert(value: ResponseBody): Any? {
            return parser.parseResource(value.byteStream())
        }
    }

    class FhirResourceConverter(private val parser: IParser) : Converter<Resource, RequestBody> {

        override fun convert(value: Resource): RequestBody {
            val result = parser.setPrettyPrint(false).encodeResourceToString(value)
            // TODO Remove this replace as soon as the spec is updated and the backend handles the accessCode itself
            return result
                .replace("\$accept", "/\$accept")
                .toRequestBody("application/fhir+json".toMediaType())
        }
    }
}
