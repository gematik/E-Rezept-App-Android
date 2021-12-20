package de.gematik.ti.erp.app.core

import io.github.aakira.napier.Napier
import okhttp3.logging.HttpLoggingInterceptor

class NapierLogger() : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Napier.d(message)
    }
}

class PrefixedLogger(val prefix: String) : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Napier.d("[$prefix] $message")
    }
}
