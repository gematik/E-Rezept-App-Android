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

package de.gematik.ti.erp.app.demomode.datasource.data

class FunnyAppNameProvider {

    private val allNames = listOf(
        "PainAway.exe",
        "Sniffle Solutions™️",
        "Demo DiGa App",
        "TotallyNotSpyware",
        "MediLOL",
        "Prescription Impossible",
        "Appy McAppface",
        "Pill It Up!",
        "Tap That Tablet",
        "Dr. Feelgood's Assistant",
        "Placebo Pro",
        "CureOS",
        "Heal Yeah!",
        "Med-Zilla",
        "The Daily Dose",
        "404 Symptoms Found",
        "Take This App And Call Me",
        "SickNote Simulator",
        "DigiDripp",
        "QuackTrack"
    ).toMutableList()

    private val usedNames = mutableSetOf<String>()

    fun next(): String {
        if (allNames.isEmpty()) {
            // Reset the pool when all names are used
            allNames += usedNames.shuffled()
            usedNames.clear()
        }

        val name = allNames.removeAt(0)
        usedNames += name
        return name
    }
}
