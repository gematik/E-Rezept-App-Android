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

package de.gematik.ti.erp.app.api

import de.gematik.ti.erp.app.Requirement

@Requirement(
    "A_19937#1",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Server error codes are defined in the HTTP standard."
)
// Informational responses (100-199)
const val HTTP_CONTINUE = 100
const val HTTP_SWITCHING_PROTOCOLS = 101
const val HTTP_PROCESSING = 102
const val HTTP_EARLY_HINTS = 103

// Successful responses (200-299)
const val HTTP_OK = 200
const val HTTP_CREATED = 201
const val HTTP_ACCEPTED = 202
const val HTTP_NON_AUTHORITATIVE = 203
const val HTTP_NO_CONTENT = 204
const val HTTP_RESET = 205
const val HTTP_PARTIAL = 206
const val HTTP_MULTI_STATUS = 207
const val HTTP_ALREADY_REPORTED = 208
const val HTTP_IM_USED = 226

// Redirection messages (300-399)
const val HTTP_MULTIPLE_CHOICES = 300
const val HTTP_MOVED_PERM = 301
const val HTTP_MOVED_TEMP = 302
const val HTTP_SEE_OTHER = 303
const val HTTP_NOT_MODIFIED = 304
const val HTTP_USE_PROXY = 305
const val HTTP_RESERVED = 306
const val HTTP_TEMPORARY_REDIRECT = 307
const val HTTP_PERMANENT_REDIRECT = 308

// Client error responses (400-499)
const val HTTP_BAD_REQUEST = 400
const val HTTP_UNAUTHORIZED = 401
const val HTTP_PAYMENT_REQUIRED = 402
const val HTTP_FORBIDDEN = 403
const val HTTP_NOT_FOUND = 404
const val HTTP_METHOD_NOT_ALLOWED = 405
const val HTTP_NOT_ACCEPTABLE = 406
const val HTTP_PROXY_AUTH = 407
const val HTTP_REQUEST_TIMEOUT = 408
const val HTTP_CONFLICT = 409
const val HTTP_GONE = 410
const val HTTP_LENGTH_REQUIRED = 411
const val HTTP_PRECONDITION_FAILED = 412
const val HTTP_ENTITY_TOO_LARGE = 413
const val HTTP_URI_TOO_LONG = 414
const val HTTP_UNSUPPORTED_MEDIA_TYPE = 415
const val HTTP_RANGE_NOT_SATISFIABLE = 416
const val HTTP_EXPECTATION_FAILED = 417
const val HTTP_I_AM_A_TEAPOT = 418
const val HTTP_MISDIRECTED_REQUEST = 421
const val HTTP_UNPROCESSABLE_ENTITY = 422
const val HTTP_LOCKED = 423
const val HTTP_FAILED_DEPENDENCY = 424
const val HTTP_UPGRADE_REQUIRED = 426
const val HTTP_PRECONDITION_REQUIRED = 428
const val HTTP_TOO_MANY_REQUESTS = 429
const val HTTP_REQUEST_HEADER_FIELDS_TOO_LARGE = 431
const val HTTP_UNAVAILABLE_FOR_LEGAL_REASONS = 451

// Server error responses (500-599)
const val HTTP_INTERNAL_ERROR = 500
const val HTTP_NOT_IMPLEMENTED = 501
const val HTTP_BAD_GATEWAY = 502
const val HTTP_SERVICE_UNAVAILABLE = 503
const val HTTP_GATEWAY_TIMEOUT = 504
const val HTTP_VERSION_NOT_SUPPORTED = 505
const val HTTP_VARIANT_ALSO_NEGOTIATES = 506
const val HTTP_INSUFFICIENT_STORAGE = 507
const val HTTP_LOOP_DETECTED = 508
const val HTTP_NOT_EXTENDED = 510
const val HTTP_NETWORK_AUTHENTICATION_REQUIRED = 511
