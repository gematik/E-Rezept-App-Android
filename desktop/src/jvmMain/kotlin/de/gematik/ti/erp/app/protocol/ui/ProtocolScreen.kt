/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.protocol.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.rememberScope
import org.kodein.di.bind
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import de.gematik.ti.erp.app.common.SpacerTiny
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults
import de.gematik.ti.erp.app.protocol.usecase.ProtocolUseCaseData
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ProtocolScreen() {
    val scope = rememberScope()

    subDI(diBuilder = {
        bind { scoped(scope).singleton { ProtocolViewModel(instance()) } }
    }) {
        val protocolViewModel by rememberInstance<ProtocolViewModel>()

        val searchPagingItems = protocolViewModel.protocolSearchFlow.collectAsLazyPagingItems()

        val lazyListState = rememberLazyListState()
        val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Box {
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
                    contentPadding = PaddingValues(vertical = PaddingDefaults.Medium),
                    modifier = Modifier.widthIn(max = 560.dp).align(Alignment.Center)
                ) {
                    items(searchPagingItems) { item ->
                        item?.run { ProtocolEntry(item) }
                    }
                }
                VerticalScrollbar(
                    scrollbarAdapter,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(horizontal = 1.dp).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun ProtocolEntry(
    protocolEntry: ProtocolUseCaseData.ProtocolEntry
) {
    Card(
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        elevation = 0.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Text(protocolEntry.text, style = MaterialTheme.typography.body2)
            SpacerTiny()
            Text(
                phrasedDateString(date = protocolEntry.timestamp),
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
private fun phrasedDateString(date: LocalDateTime): String {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG) }

    return date.atZone(ZoneId.systemDefault()).format(dateFormatter)
}
