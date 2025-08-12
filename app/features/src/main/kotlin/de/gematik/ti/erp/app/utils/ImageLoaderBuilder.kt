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

package de.gematik.ti.erp.app.utils

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension

private const val MEMORY_CACHE_PERCENT = 0.125
private const val DISK_CACHE_PERCENT = 0.01

internal fun Context.buildImageLoader(
    memoryCacheSizeInPercent: Double? = MEMORY_CACHE_PERCENT,
    diskCacheSizeInPercent: Double? = DISK_CACHE_PERCENT
): ImageLoader = ImageLoader(this)
    .newBuilder()
    .apply {
        memoryCacheSizeInPercent?.let { memoryCacheSize ->
            memoryCachePolicy(CachePolicy.ENABLED)
            memoryCache(
                MemoryCache.Builder(this@buildImageLoader)
                    .maxSizePercent(memoryCacheSize)
                    .strongReferencesEnabled(true)
                    .weakReferencesEnabled(true)
                    .build()
            )
        }
        diskCacheSizeInPercent?.let { diskCacheSize ->
            diskCachePolicy(CachePolicy.ENABLED)
            diskCache(
                DiskCache.Builder()
                    .maxSizePercent(diskCacheSize)
                    .directory(cacheDir)
                    .build()
            )
        }
        if (BuildConfigExtension.isInternalDebug) {
            logger(DebugLogger())
        }
        components {
            when {
                Build.VERSION.SDK_INT >= 28 -> add(ImageDecoderDecoder.Factory())
                else -> add(GifDecoder.Factory())
            }
        }
    }.build()
