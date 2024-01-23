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
        printAdapter.onLayout(null, printAttributes, null, object : PrintDocumentAdapter.LayoutResultCallback() {
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
        }, null)
    }
}