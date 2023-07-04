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

//
// DO NOT MODIFY - GENERATED ON 2021-09-20T18:49:22.365036
//
@file:Suppress("RedundantVisibilityModifier")

package de.gematik.ti.erp.app.common.pinning

import okhttp3.CertificatePinner

public fun buildCertificatePinner(): CertificatePinner = CertificatePinner.Builder()
    // expires on 2030-09-23T00:00
    .add("erp-ref.app.ti-dienste.de", "sha256/RRM1dGqnDFsCJXBTHky16vi1obOlCgFFn/yOhI/y+ho=")
    // expires on 2030-09-23T00:00
    .add("erp-ref.app.ti-dienste.de", "sha256/e0IRz5Tio3GA1Xs4fUVWmH1xHDiH2dMbVtCBSkOIdqM=")
    // expires on 2030-09-23T00:00
    .add("erp-test.app.ti-dienste.de", "sha256/RRM1dGqnDFsCJXBTHky16vi1obOlCgFFn/yOhI/y+ho=")
    // expires on 2030-09-23T00:00
    .add("erp-test.app.ti-dienste.de", "sha256/e0IRz5Tio3GA1Xs4fUVWmH1xHDiH2dMbVtCBSkOIdqM=")
    // expires on 2030-09-23T00:00
    .add("erp.app.ti-dienste.de", "sha256/RRM1dGqnDFsCJXBTHky16vi1obOlCgFFn/yOhI/y+ho=")
    // expires on 2030-09-23T00:00
    .add("erp.app.ti-dienste.de", "sha256/e0IRz5Tio3GA1Xs4fUVWmH1xHDiH2dMbVtCBSkOIdqM=")
    // expires on 2026-09-21T00:00
    .add("idp-ref.app.ti-dienste.de", "sha256/86fLIetopQLDNxFZ0uMI66Xpl1pFgLlHHn9v6kT0i4I=")
    // expires on 2026-09-21T00:00
    .add("idp-test.app.ti-dienste.de", "sha256/86fLIetopQLDNxFZ0uMI66Xpl1pFgLlHHn9v6kT0i4I=")
    // expires on 2026-09-21T00:00
    .add("idp.app.ti-dienste.de", "sha256/86fLIetopQLDNxFZ0uMI66Xpl1pFgLlHHn9v6kT0i4I=")
    // expires on 2030-09-23T00:00
    .add("apovzd.app.ti-dienste.de", "sha256/e0IRz5Tio3GA1Xs4fUVWmH1xHDiH2dMbVtCBSkOIdqM=")
    // expires on 2031-04-13
    .add("erp-ref.app.ti-dienste.de", "sha256/qBRjZmOmkSNJL0p70zek7odSIzqs/muR4Jk9xYyCP+E=")
    // expires on 2024-01-30"
    .add("erp-test.app.ti-dienste.de", "sha256/qBRjZmOmkSNJL0p70zek7odSIzqs/muR4Jk9xYyCP+E=")
    // expires on 2024-01-30
    .add("erp.app.ti-dienste.de", "sha256/qBRjZmOmkSNJL0p70zek7odSIzqs/muR4Jk9xYyCP+E=")
    // expires on 2024-07-07
    .add("apovzd.app.ti-dienste.de", "sha256/qBRjZmOmkSNJL0p70zek7odSIzqs/muR4Jk9xYyCP+E=")
    // expires on TODO
    .add("idp-ref.app.ti-dienste.de", "sha256/OD/WDbD3VsfMwwNzzy9MWd9JXppKB77Vb3ST2wn9meg=")
    // expires on TODO
    .add("idp-test.app.ti-dienste.de", "sha256/OD/WDbD3VsfMwwNzzy9MWd9JXppKB77Vb3ST2wn9meg=")
    // expires on TODO
    .add("idp.app.ti-dienste.de", "sha256/OD/WDbD3VsfMwwNzzy9MWd9JXppKB77Vb3ST2wn9meg=")
    .build()
