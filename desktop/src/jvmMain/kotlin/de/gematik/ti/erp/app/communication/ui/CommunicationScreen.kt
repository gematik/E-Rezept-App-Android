/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.communication.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.common.App
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults
import de.gematik.ti.erp.app.communication.usecase.model.CommunicationUseCaseData.Communication.SupplyOption
import de.gematik.ti.erp.app.communication.usecase.model.CommunicationUseCaseData.Communication.SupplyOption.Delivery
import de.gematik.ti.erp.app.communication.usecase.model.CommunicationUseCaseData.Communication.SupplyOption.OnPremise
import de.gematik.ti.erp.app.communication.usecase.model.CommunicationUseCaseData.Communication.SupplyOption.Shipment
import de.gematik.ti.erp.app.rememberScope
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.flow.collect
import org.kodein.di.bind
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton

@Composable
fun CommunicationScreen() {
    val scope = rememberScope()

    subDI(diBuilder = {
        bind { scoped(scope).singleton { CommunicationViewModel(instance(), instance()) } }
    }) {
        val communicationViewModel by rememberInstance<CommunicationViewModel>()
        val state by produceState(communicationViewModel.defaultState) {
            communicationViewModel.screenState().collect {
                value = it
            }
        }

        val lazyListState = rememberLazyListState()
        val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)

        val dtFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT) }

        if (state.pharmacyCommunications.isNotEmpty()) {
            SelectionContainer {
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.widthIn(max = 560.dp).align(Alignment.Center),
                        state = lazyListState
                    ) {
                        itemsIndexed(state.pharmacyCommunications) { index, it ->
                            CommunicationEntry(
                                modifier = Modifier.fillMaxWidth().padding(PaddingDefaults.Medium),
                                medication = it.name,
                                type = it.supplyOption,
                                code = it.pickUpCode,
                                url = it.url,
                                infoText = it.infoText,
                                sender = it.sender,
                                recipient = it.recipient,
                                sent = it.sent?.format(dtFormatter)
                            )
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
}

@Composable
private fun CommunicationEntry(
    modifier: Modifier,
    medication: String,
    type: SupplyOption?,
    code: String?,
    url: String?,
    infoText: String?,
    sender: String,
    recipient: String,
    sent: String?
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)) {
        Text(medication, style = MaterialTheme.typography.h6)
        Row(horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)) {
            type?.let {
                Chip(
                    when (type) {
                        OnPremise -> App.strings.desktopCommunicationOnPremise()
                        Shipment -> App.strings.desktopCommunicationShipment()
                        Delivery -> App.strings.desktopCommunicationDelivery()
                    }
                )
            }
            code?.let { Chip(it) }
            url?.let { Chip(it) }
        }
        infoText?.let {
            Text(it, style = MaterialTheme.typography.body1)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(sender, style = AppTheme.typography.body2l)
            Icon(Icons.Rounded.ArrowForward, null, tint = AppTheme.colors.neutral400, modifier = Modifier.size(12.dp))
            Text(recipient, style = AppTheme.typography.body2l)
        }
        sent?.let { Text(it, style = AppTheme.typography.body2l) }
    }
}

@Composable
private fun Chip(
    text: String
) {
    Box(
        Modifier
            .background(AppTheme.colors.neutral100, shape = CircleShape)
            .padding(horizontal = PaddingDefaults.Small, vertical = PaddingDefaults.Tiny / 2)
    ) {
        Text(text, style = AppTheme.typography.captionl)
    }
}
