package de.gematik.ti.erp.app.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatchersProvider {
    fun main(): CoroutineDispatcher = Dispatchers.Main
    fun default(): CoroutineDispatcher = Dispatchers.Default
    fun io(): CoroutineDispatcher = Dispatchers.IO
    fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}
