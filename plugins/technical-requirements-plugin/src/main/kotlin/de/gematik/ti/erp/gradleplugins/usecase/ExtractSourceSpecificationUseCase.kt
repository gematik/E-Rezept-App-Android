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

package de.gematik.ti.erp.gradleplugins.usecase

import de.gematik.ti.erp.gradleplugins.model.Cell
import de.gematik.ti.erp.gradleplugins.model.ElementHolder
import de.gematik.ti.erp.gradleplugins.model.Row
import de.gematik.ti.erp.gradleplugins.model.Table
import de.gematik.ti.erp.gradleplugins.model.Text
import de.gematik.ti.erp.gradleplugins.model.UnorderedList
import org.jsoup.nodes.Element

class ExtractSourceSpecificationUseCase {

    operator fun invoke(
        sourceElement: Element
    ): List<ElementHolder> {
        val elements = mutableListOf<ElementHolder>()

        // extract paragraphs
        sourceElement.addParagraphs { paragraph ->
            elements.add(paragraph)
        }

        // extract unordered lists
        sourceElement.addLists { unorderedList ->
            elements.add(unorderedList)
        }

        // extract tables
        sourceElement.addTables { table ->
            elements.add(table)
        }

        return elements
    }
}

private fun Element.addTables(
    action: (Table) -> Unit
) {
    val tableElements = this.select("table")

    tableElements.forEach { tableElement ->
        val rows = mutableListOf<Row>()
        tableElement.select("tr").map { rowElement ->
            val cells = mutableListOf<Cell>()
            rowElement.children().forEach { cellElement ->
                val isHeader = cellElement.tagName() == "th"
                val rowspan = cellElement.attr("rowspan").ifEmpty { "1" }.toInt()
                val colspan = cellElement.attr("colspan").ifEmpty { "1" }.toInt()
                cells.add(Cell(cellElement.text(), isHeader, rowspan, colspan))
            }
            rows.add(Row(cells))
        }
        val table = Table(rows)
        action(table)
    }
}

private fun Element.addLists(
    action: (UnorderedList) -> Unit
) {
    val listElements = this.select("ul")

    listElements.forEach { listElement ->
        val items = mutableListOf<String>()
        listElement.select("li").map { itemElement ->
            items.add(itemElement.text())
        }
        val unorderedList = UnorderedList(items)
        action(unorderedList)
    }
}

private fun Element.addParagraphs(
    action: (Text) -> Unit
) {
    val paragraphElements = this.select("p")

    paragraphElements.forEach { paragraphElement ->
        action(Text(paragraphElement.text()))
    }
}
