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

package de.gematik.ti.erp.app.pkv.usecase

import android.content.Context
import android.print.PdfPrint
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentNameDictionary
import com.tom_roush.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode
import com.tom_roush.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification
import com.tom_roush.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.pkv.FileProviderAuthority
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.GregorianCalendar
import java.util.UUID
import kotlin.coroutines.suspendCoroutine

private const val FileProviderAuthority = "${BuildConfig.LIBRARY_PACKAGE_NAME}.fileprovider"
private const val PDFDensity = 600
private const val PDFMargin = 24

fun createSharableFileInCache(context: Context, path: String, filePrefix: String): File {
    val uuid = UUID.randomUUID().toString()
    val pdfPath = File(context.cacheDir, path).apply {
        mkdirs()
        // clean up old codes
        listFiles()?.forEach {
            if (!it.isDirectory) {
                Napier.d("Delete cache file ${it.name}")
                it.delete()
            }
        }
    }
    val newFile = File(pdfPath, "$filePrefix-$uuid.pdf")
    Napier.d("Created cache file ${newFile.name}")

    newFile.createNewFile()

    return newFile
}

fun sharePDFFile(
    context: Context,
    file: File,
    subject: String,
    fileProviderAuthority: FileProviderAuthority
) {
    val path = fileProviderAuthority.getFilePath()
    val uri = FileProvider.getUriForFile(context, path, file)

    ShareCompat.IntentBuilder(context)
        .setType("application/pdf")
        .setSubject(subject)
        .addStream(uri)
        .setChooserTitle(subject)
        .startChooser()
}

suspend fun writePdfFromHtml(context: Context, title: String, html: String, out: File) =
    withContext(Dispatchers.Main) {
        val webView: WebView
        suspendCoroutine { continuation ->
            webView = WebView(context).apply {
                settings.javaScriptEnabled = false
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        continuation.resumeWith(Result.success(Unit))
                    }
                }
            }

            webView.loadData(
                Base64.encodeToString(html.encodeToByteArray(), Base64.NO_PADDING),
                "text/html",
                "base64"
            )
        }
        val adapter = webView.createPrintDocumentAdapter(title)
        writePDF(adapter, out)
        webView.destroy()
    }

suspend fun writePDF(adapter: PrintDocumentAdapter, out: File) {
    val attributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
        .setResolution(PrintAttributes.Resolution("pdf", "pdf", PDFDensity, PDFDensity))
        .setMinMargins(PrintAttributes.Margins(PDFMargin, PDFMargin, PDFMargin, PDFMargin)).build()
    val pdfPrint = PdfPrint(attributes)

    pdfPrint.print(adapter, out)
}

fun writePDFAttachments(out: File, attachments: List<Triple<String, String, ByteArray>>) {
    val doc = PDDocument.load(out)

    val efTree = PDEmbeddedFilesNameTreeNode()

    efTree.names = attachments.associate { (name, type, data) ->
        val ef = PDEmbeddedFile(doc, data.inputStream())
        ef.subtype = type
        ef.size = data.size
        ef.creationDate = GregorianCalendar()

        val fs = PDComplexFileSpecification()
        fs.file = name
        fs.embeddedFile = ef

        name to fs
    }

    val names = PDDocumentNameDictionary(doc.documentCatalog)
    names.embeddedFiles = efTree
    doc.documentCatalog.names = names

    doc.save(out)
}
