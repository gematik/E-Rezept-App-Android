/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.model.PharmacyOverviewViewModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Stable
private sealed interface RefreshState {
    @Stable
    object Loading : RefreshState

    @Stable
    class Success(val pharmacy: PharmacyUseCaseData.Pharmacy) : RefreshState

    @Stable
    object NotFound : RefreshState

    @Stable
    object Error : RefreshState
}

@Composable
fun FavoritePharmacyCard(
    overviewPharmacy: OverviewPharmacyData.OverviewPharmacy,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit,
    pharmacyViewModel: PharmacyOverviewViewModel
) {
    var showFailedPharmacyCallDialog by remember { mutableStateOf(false) }
    var showNoInternetConnectionDialog by remember { mutableStateOf(false) }

    var state by remember { mutableStateOf<RefreshState>(RefreshState.Loading) }
    LaunchedEffect(overviewPharmacy) {
        refresh(
            pharmacyViewModel = pharmacyViewModel,
            pharmacyTelematikId = overviewPharmacy.telematikId,
            onStateChange = {
                state = it
            }
        )
    }

    val scope = rememberCoroutineScope()

    if (showNoInternetConnectionDialog) {
        CommonAlertDialog(
            header = stringResource(R.string.pharmacy_search_apovz_call_no_internet_header),
            info = stringResource(R.string.pharmacy_search_apovz_call_no_internet_info),
            cancelText = stringResource(R.string.pharmacy_search_apovz_call_no_internet_cancel),
            actionText = stringResource(R.string.pharmacy_search_apovz_call_no_internet_retry),
            onCancel = { showNoInternetConnectionDialog = false },
            onClickAction = {
                scope.launch {
                    refresh(
                        pharmacyViewModel = pharmacyViewModel,
                        pharmacyTelematikId = overviewPharmacy.telematikId,
                        onStateChange = {
                            state = it
                        }
                    )
                }
            }
        )
    }
    if (showFailedPharmacyCallDialog) {
        AcceptDialog(
            header = stringResource(R.string.pharmacy_search_apovz_call_failed_header),
            info = stringResource(R.string.pharmacy_search_apovz_call_failed_body),
            onClickAccept = {
                scope.launch { pharmacyViewModel.deleteOverviewPharmacy(overviewPharmacy) }
                showFailedPharmacyCallDialog = false
            },
            acceptText = stringResource(R.string.pharmacy_search_apovz_call_failed_accept)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button) {
                when (state) {
                    is RefreshState.Success -> onSelectPharmacy((state as RefreshState.Success).pharmacy)
                    is RefreshState.Error -> showNoInternetConnectionDialog = true
                    is RefreshState.NotFound -> showFailedPharmacyCallDialog = true
                    else -> {}
                }
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        elevation = 0.dp
    ) {
        CardContent(overviewPharmacy)
    }
}

private suspend fun refresh(
    pharmacyViewModel: PharmacyOverviewViewModel,
    pharmacyTelematikId: String,
    onStateChange: (RefreshState) -> Unit
) {
    onStateChange(RefreshState.Loading)
    val result = pharmacyViewModel.findPharmacyByTelematikIdState(pharmacyTelematikId).first().fold(
        onFailure = {
            Napier.e("Could not find pharmacy by telematikId", it)
            RefreshState.Error
        },
        onSuccess = {
            if (it == null) {
                RefreshState.NotFound
            } else {
                RefreshState.Success(it)
            }
        }
    )
    onStateChange(result)
}

@Composable
private fun CardContent(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        PharmacyImagePlaceholder(Modifier.padding(PaddingDefaults.Medium))

        Column(
            modifier = Modifier
                .padding(
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium,
                    bottom = PaddingDefaults.Medium
                )
                .weight(1f)
        ) {
            Text(
                overviewPharmacy.pharmacyName,
                style = AppTheme.typography.subtitle1
            )
            Text(
                overviewPharmacy.address,
                style = AppTheme.typography.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (overviewPharmacy.isFavorite) {
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = PaddingDefaults.Medium)
                    .size(24.dp),
                tint = AppTheme.colors.yellow500
            )
        }
    }
}
