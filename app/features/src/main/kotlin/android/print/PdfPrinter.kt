/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import java.io.File
import kotlin.coroutines.suspendCoroutine

/**
 * Placed in "android.print" package similar to the one in internal android since
 * PrintDocumentAdapter.LayoutResultCallback and PrintDocumentAdapter.WriteResultCallback
 * can only be accessed from inside the android.print package
 */
class PdfPrint(private val printAttributes: PrintAttributes) {
    suspend fun print(printAdapter: PrintDocumentAdapter, outputFd: File) = suspendCoroutine { continuation ->
        printAdapter.onLayout(
            null,
            printAttributes,
            null,
            object : PrintDocumentAdapter.LayoutResultCallback() {
                override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
                    printAdapter.onWrite(
                        arrayOf(PageRange.ALL_PAGES),
                        ParcelFileDescriptor.open(outputFd, ParcelFileDescriptor.MODE_READ_WRITE),
                        CancellationSignal(),
                        object : PrintDocumentAdapter.WriteResultCallback() {
                            override fun onWriteFinished(pages: Array<PageRange>) {
                                super.onWriteFinished(pages)
                                continuation.resumeWith(Result.success(Unit))
                            }
                        }
                    )
                }
            },
            null
        )
    }
}
