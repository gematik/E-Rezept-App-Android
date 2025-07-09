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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.utils.compose

import android.content.Context
import com.nulabinc.zxcvbn.StandardDictionaries
import com.nulabinc.zxcvbn.Zxcvbn
import com.nulabinc.zxcvbn.ZxcvbnBuilder
import com.nulabinc.zxcvbn.io.Resource
import com.nulabinc.zxcvbn.matchers.DictionaryLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PasswordEvaluatorHolder {
    @Volatile
    private var INSTANCE: Zxcvbn? = null

    suspend fun getInstance(context: Context): Zxcvbn {
        return INSTANCE ?: withContext(Dispatchers.IO) {
            synchronized(this@PasswordEvaluatorHolder) {
                INSTANCE ?: createPasswordEvaluator(context).also { INSTANCE = it }
            }
        }
    }

    private fun createPasswordEvaluator(context: Context): Zxcvbn {
        val assetManager = context.assets
        val germanDictionaryFile = assetManager.open("german_dictionary.txt")
        val germanDictionaryResource = Resource { germanDictionaryFile }

        return ZxcvbnBuilder().dictionaries(
            StandardDictionaries.loadAllDictionaries()
        )
            .dictionary(DictionaryLoader("german_dictionary", germanDictionaryResource).load())
            .build()
    }
}
