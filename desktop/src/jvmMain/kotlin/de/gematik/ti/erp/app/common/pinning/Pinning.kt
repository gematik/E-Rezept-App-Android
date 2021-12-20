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
    .build()
