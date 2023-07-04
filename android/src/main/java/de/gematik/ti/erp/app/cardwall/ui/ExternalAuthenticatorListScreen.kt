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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.idp.api.models.AuthenticationId
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.rememberProfileHandler
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import org.kodein.di.compose.rememberViewModel

@Composable
fun ExternalAuthenticatorListScreen(
    profileId: ProfileIdentifier,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel by rememberViewModel<ExternalAuthenticatorListViewModel>()
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        navigationMode = NavigationBarMode.Back,
        topBarTitle = stringResource(R.string.cdw_fasttrack_title),
        onBack = onBack,
        listState = listState,
        actions = @Composable {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        AuthenticatorList(
            profileId = profileId,
            viewModel = viewModel,
            onNext = onNext,
            listState = listState
        )
    }
}

private val WhitespaceRegex = "\\s+".toRegex()

@Composable
private fun rememberFilteredAuthenticatorsList(
    source: List<AuthenticationId>,
    keywords: String
): State<List<AuthenticationId>> {
    val result = remember(source) { mutableStateOf(source) }
    LaunchedEffect(source, keywords) {
        result.value = if (keywords.isNotBlank()) {
            val kw = keywords.split(WhitespaceRegex)
            source.filter { src ->
                kw.all { src.name.contains(it, ignoreCase = true) }
            }
        } else {
            source
        }
    }
    return result
}

@Stable
private sealed interface RefreshState {
    @Stable
    object Loading : RefreshState

    @Stable
    class WithResults(val result: List<AuthenticationId>) : RefreshState

    @Stable
    class Error(val throwable: Throwable) : RefreshState
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AuthenticatorList(
    profileId: ProfileIdentifier,
    viewModel: ExternalAuthenticatorListViewModel,
    onNext: () -> Unit,
    listState: LazyListState
) {
    val refreshFlow = remember { MutableSharedFlow<Unit>() }
    var state by remember { mutableStateOf<RefreshState>(RefreshState.Loading) }
    LaunchedEffect(Unit) {
        refreshFlow
            .onStart { emit(Unit) } // emit once to start the flow directly
            .collectLatest {
                state = RefreshState.Loading
                state = try {
                    RefreshState.WithResults(viewModel.externalAuthenticatorIDList())
                } catch (expected: Throwable) {
                    RefreshState.Error(expected)
                }
            }
    }

    val coroutineScope = rememberCoroutineScope()
    val profileHandler = rememberProfileHandler()

    var search by remember { mutableStateOf("") }
    val externalAuthenticatorListFiltered by rememberFilteredAuthenticatorsList(
        source = (state as? RefreshState.WithResults)?.result ?: emptyList(),
        keywords = search
    )

    val intentHandler = LocalIntentHandler.current

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(PaddingDefaults.Medium)) {
            Text(stringResource(R.string.cdw_fasttrack_choose_insurance), style = MaterialTheme.typography.h6)
            SpacerSmall()
            Text(
                stringResource(R.string.cdw_fasttrack_help_info),
                style = AppTheme.typography.body2l
            )
        }
        when (state) {
            is RefreshState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(PaddingDefaults.Medium)
                ) {
                    CircularProgressIndicator(
                        Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            is RefreshState.Error -> {
                ErrorScreen(
                    onClickRetry = {
                        coroutineScope.launch {
                            refreshFlow.emit(Unit)
                        }
                    }
                )
            }
            is RefreshState.WithResults -> {
                SpacerLarge()
                SearchField(
                    value = search,
                    onValueChange = {
                        search = it
                    }
                )
                SpacerMedium()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = listState,
                    contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
                ) {
                    items(externalAuthenticatorListFiltered) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                coroutineScope.launch {
                                    val redirectUri =
                                        viewModel.startAuthorizationWithExternal(
                                            profileId = profileId,
                                            auth = it
                                        )
                                    intentHandler.startFastTrackApp(redirectUri)
                                    if (it.id.endsWith("pkv")) {
                                        profileHandler.switchProfileToPKV(profileId)
                                    }
                                    onNext()
                                }
                            }
                        ) {
                            Text(text = it.name, modifier = Modifier.padding(PaddingDefaults.Medium))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit
) =
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        placeholder = {
            Text(
                stringResource(R.string.cdw_fasttrack_search_placeholder),
                style = AppTheme.typography.body1l
            )
        },
        shape = RoundedCornerShape(16.dp),
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = AppTheme.colors.neutral100,
            placeholderColor = AppTheme.colors.neutral600,
            leadingIconColor = AppTheme.colors.neutral600,
            focusedBorderColor = Color.Unspecified,
            unfocusedBorderColor = Color.Unspecified,
            disabledBorderColor = Color.Unspecified,
            errorBorderColor = Color.Unspecified
        )
    )

@Composable
private fun ErrorScreen(
    onClickRetry: () -> Unit
) =
    Box(
        Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
    ) {
        Column(
            modifier = Modifier.align(BiasAlignment(horizontalBias = 0f, verticalBias = -0.33f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            Text(
                stringResource(R.string.cdw_fasttrack_error_title),
                style = AppTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.cdw_fasttrack_error_info),
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
            TextButton(
                onClick = onClickRetry
            ) {
                Icon(Icons.Rounded.Refresh, null)
                SpacerSmall()
                Text(stringResource(R.string.cdw_fasttrack_try_again))
            }
        }
    }
