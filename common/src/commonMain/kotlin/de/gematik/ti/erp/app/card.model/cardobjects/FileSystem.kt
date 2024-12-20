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

package de.gematik.ti.erp.app.card.model.cardobjects

/**
 * eGK 2.1 file system objects
 * @see gemSpec_eGK_ObjSys_G2_1_V4_0_0 'Spezifikation der eGK Objektsystem G2.1'
 */

object Ef {
    object CardAccess {
        const val FID = 0x011C
        const val SFID = 0x1C
    }

    object Version2 {
        const val FID = 0x2F11
        const val SFID = 0x11
    }
}

object Df {
    object Esign {
        const val AID = "A000000167455349474E"
    }
}

object Mf {
    object MrPinHome {
        const val PWID = 0x02
    }
    object Df {
        object Esign {
            object Ef {
                object CchAutE256 {
                    const val FID = 0xC504
                    const val SFID = 0x04
                }
            }
            object PrK {
                object ChAutE256 {
                    const val KID = 0x04
                }
            }
        }
    }
}
