package de.gematik.ti.erp.app

object Constants {
    object IDP {
        val serviceUri = BuildKonfig.IDP_SERVICE_URI
    }

    object ERP {
        val serviceUri = BuildKonfig.BASE_SERVICE_URI
        val apiKey = BuildKonfig.ERP_API_KEY
    }

    val userAgent = BuildKonfig.USER_AGENT
    val trustAnchor = BuildKonfig.APP_TRUST_ANCHOR_BASE64
}
