/*
 * Copyright (c) 2021 gematik GmbH
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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.LocationDisabled
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyNavigationScreens
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.AnimatedHintCard
import de.gematik.ti.erp.app.utils.compose.Chip
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.compose.FlowRow
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXLarge
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.OffsetDateTime

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class,
    FlowPreview::class
)
@Composable
fun PharmacySearchScreen(
    mainNavController: NavController,
    navController: NavController,
    selectedPharmacy: MutableState<PharmacyUseCaseData.Pharmacy?>,
    viewModel: PharmacySearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var showLocationHint by remember { mutableStateOf(false) }
    val state by produceState<PharmacyUseCaseData.State?>(null) {
        viewModel.screenState().collect {
            value = it

            showLocationHint = it.showLocationHint
        }
    }

    val locationEnabled by rememberSaveable(state?.search) {
        mutableStateOf(
            locationPermissionGranted(context) &&
                state?.search?.let { it.locationMode is PharmacyUseCaseData.LocationMode.Enabled } ?: false
        )
    }
    var searchText by rememberSaveable(state?.search) {
        mutableStateOf(state?.search?.name ?: "")
    }
    val searchFilter by rememberSaveable(state?.search) {
        mutableStateOf(state?.search?.filter ?: PharmacyUseCaseData.Filter())
    }

    val searchListState = rememberLazyListState()

    val searchPagingItems = viewModel.pharmacySearchFlow.collectAsLazyPagingItems()

    val scaffoldState = rememberScaffoldState()

    val errorTitle = stringResource(R.string.search_pharmacy_error_title)
    val errorSubtitle = stringResource(R.string.search_pharmacy_error_subtitle)
    val errorAction = stringResource(R.string.search_pharmacy_error_action)
    LaunchedEffect(searchPagingItems.loadState) {
        searchPagingItems.loadState.let {
            val anyErr = it.append is LoadState.Error || it.prepend is LoadState.Error || it.refresh is LoadState.Error
            if (anyErr && searchPagingItems.itemCount > 1) {
                val result =
                    scaffoldState.snackbarHostState.showSnackbar(
                        errorTitle,
                        errorAction,
                        duration = SnackbarDuration.Short
                    )
                if (result == SnackbarResult.ActionPerformed) {
                    searchPagingItems.retry()
                }
            }
        }
    }

    lateinit var _search: (searchTxt: String, locEnabled: Boolean, filter: PharmacyUseCaseData.Filter, triggeredByUser: Boolean) -> Unit

    fun search(
        searchTxt: String = searchText,
        locEnabled: Boolean = locationEnabled,
        filter: PharmacyUseCaseData.Filter = searchFilter,
        triggeredByUser: Boolean = true
    ) = _search(searchTxt, locEnabled, filter, triggeredByUser)

    val keyboardController = LocalSoftwareKeyboardController.current
    var keyboardHideToggle by remember { mutableStateOf(false) }
    DisposableEffect(keyboardHideToggle) {
        keyboardController?.hide()
        onDispose {}
    }

    var showEnableLocationDialog by remember { mutableStateOf(false) }

    if (showEnableLocationDialog) {
        EnableLocationDialog {
            showEnableLocationDialog = false
        }
    }

    val scope = rememberCoroutineScope()

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            search(locEnabled = it)
        }

    var isPreLoading by remember { mutableStateOf(false) }
    _search = { searchTxt: String,
        locEnabled: Boolean,
        filter: PharmacyUseCaseData.Filter,
        triggeredByUser: Boolean ->
        if (locEnabled && !locationPermissionGranted(context)) {
            locationPermissionLauncher.launch(locationPermission)
        } else {
            scope.launch {
                try {
                    isPreLoading = true

                    // workaround for certain huawei devices
                    keyboardHideToggle = !keyboardHideToggle

                    searchListState.scrollToItem(0)
                    showEnableLocationDialog = viewModel.searchPharmacies(
                        searchTxt, filter,
                        locEnabled
                    )
                } finally {
                    isPreLoading = false
                }
            }
        }
    }

    val loadState = searchPagingItems.loadState

    val modal = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    ModalBottomSheetLayout(
        sheetContent = {
            FilterBottomSheet(
                filter = state?.search?.filter ?: PharmacyUseCaseData.Filter(),
                onClickChip = { search(filter = it) },
                onClickClose = { scope.launch { modal.hide() } }
            )
        },
        sheetState = modal
    ) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                NavigationTopAppBar(
                    NavigationBarMode.Close,
                    headline = stringResource(R.string.search_pharmacy_title),
                    onClick = { mainNavController.popBackStack() }
                )
            }
        ) {
            Box {
                Column(modifier = Modifier.fillMaxSize()) {

                    val itemPaddingModifier = Modifier
                        .fillMaxWidth()
                        .padding(PaddingDefaults.Medium)

                    val showNothingFound =
                        listOf(loadState.prepend, loadState.append)
                            .all {
                                when (it) {
                                    is LoadState.NotLoading ->
                                        it.endOfPaginationReached && searchPagingItems.itemCount == 1
                                    else -> false
                                }
                            } && loadState.refresh is LoadState.NotLoading

                    val showError = searchPagingItems.itemCount <= 1 && loadState.refresh is LoadState.Error

                    Box {
                        var heightLazyColumn by remember { mutableStateOf(1) }
                        var heightHeader by remember { mutableStateOf(1) }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .onSizeChanged { heightLazyColumn = it.height },
                            state = searchListState
                        ) {
                            item {
                                Column(modifier = Modifier.onSizeChanged { heightHeader = it.height }) {
                                    SearchField(
                                        searchValue = searchText,
                                        onSearchChange = { searchText = it },
                                        locationEnabled = locationEnabled,
                                        onSearch = { searchTxt, locEnabled ->
                                            search(searchTxt, locEnabled)
                                        }
                                    )

                                    SpacerMedium()

                                    FilterSection(
                                        filter = searchFilter,
                                        onClickChip = { search(filter = it) },
                                        onClickFilter = { scope.launch { modal.show() } }
                                    )

                                    SpacerMedium()
                                }
                            }
                            if (showNothingFound) {
                                item {
                                    PharmacySearchErrorHint(
                                        title = stringResource(R.string.search_pharmacy_nothing_found_header),
                                        subtitle = stringResource(R.string.search_pharmacy_nothing_found_info),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillParentMaxHeight(
                                                1f - heightHeader / heightLazyColumn.toFloat()
                                            )
                                    )
                                }
                            }
                            if (showError) {
                                item {
                                    PharmacySearchErrorHint(
                                        title = errorTitle,
                                        subtitle = errorSubtitle,
                                        action = errorAction,
                                        onClickAction = { searchPagingItems.retry() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillParentMaxHeight(
                                                1f - heightHeader / heightLazyColumn.toFloat()
                                            )
                                    )
                                }
                            }
                            if (loadState.prepend is LoadState.Error) {
                                item {
                                    PharmacySearchErrorHint(
                                        title = errorTitle,
                                        subtitle = errorSubtitle,
                                        action = errorAction,
                                        onClickAction = { searchPagingItems.retry() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(PaddingDefaults.Medium)
                                    )
                                }
                            }
                            itemsIndexed(searchPagingItems) { index, item ->
                                when (item) {
                                    PharmacySearchUi.LocationHint -> {
                                        // enable location information
                                        if (showLocationHint) {
                                            AnimatedHintCard(
                                                modifier = Modifier
                                                    .padding(
                                                        start = PaddingDefaults.Medium,
                                                        end = PaddingDefaults.Medium
                                                    ),
                                                image = {
                                                    HintSmallImage(
                                                        painterResource(R.drawable.pharmacist_hint),
                                                        innerPadding = it
                                                    )
                                                },
                                                title = { Text(stringResource(R.string.search_enable_location_hint_header)) },
                                                body = { Text(stringResource(R.string.search_enable_location_hint_info)) },
                                                action = {
                                                    HintTextActionButton(stringResource(R.string.search_enable_location_hint_enable)) {
                                                        search(searchText, true)
                                                    }
                                                },
                                                onTransitionEnd = {
                                                    if (!it) {
                                                        viewModel.cancelLocationHint()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    is PharmacySearchUi.Pharmacy -> {
                                        Column {
                                            PharmacyResultCard(
                                                modifier = itemPaddingModifier,
                                                pharmacy = item.pharmacy
                                            ) {
                                                selectedPharmacy.value = item.pharmacy
                                                navController.navigate(PharmacyNavigationScreens.PharmacyDetails.path())
                                            }
                                            if (index < searchPagingItems.itemCount - 1) {
                                                Divider(startIndent = PaddingDefaults.Medium)
                                            }
                                        }
                                    }
                                    null -> {
                                        if (loadState.prepend !is LoadState.Error && loadState.append !is LoadState.Error) {
                                            Column {
                                                PharmacyResultPlaceholder(itemPaddingModifier)
                                                if (index < searchPagingItems.itemCount - 1) {
                                                    Divider(startIndent = PaddingDefaults.Medium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (loadState.append is LoadState.Error) {
                                item {
                                    PharmacySearchErrorHint(
                                        title = errorTitle,
                                        subtitle = errorSubtitle,
                                        action = errorAction,
                                        onClickAction = { searchPagingItems.retry() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(PaddingDefaults.Medium)
                                    )
                                }
                            }
                        }

                        // TODO needs to be fixed
//                        val showInitialLoadingAnimation =
//                            state == null && listOf(
//                                loadState.prepend,
//                                loadState.append,
//                                loadState.refresh
//                            )
//                                .any {
//                                    when (it) {
//                                        is LoadState.NotLoading -> state?.search == null
//                                        is LoadState.Loading -> true
//                                        else -> false
//                                    }
//                                }
//
//                        val alpha by animateFloatAsState(if (showInitialLoadingAnimation) 1f else 0f)
//
//                        // initial loading animation
//                        if (alpha > 0f) {
//                            RepeatingColumn(
//                                modifier = Modifier
//                                    .alpha(alpha),
//                                stepSize = 5
//                            ) {
//                                PharmacyResultPlaceholder(itemPaddingModifier)
//                                Divider(startIndent = PaddingDefaults.Medium)
//                            }
//                        }
                    }
                }

                val isLoading = isPreLoading || listOf(loadState.prepend, loadState.append, loadState.refresh)
                    .any {
                        when (it) {
                            is LoadState.NotLoading -> state?.search == null // initial ui only loading indicator
                            is LoadState.Loading -> true
                            else -> false
                        }
                    }

                val loadingAlpha by animateFloatAsState(
                    if (isLoading) 1f else 0f,
                    animationSpec = tween()
                )

                LinearProgressIndicator(
                    modifier = Modifier
                        .alpha(loadingAlpha)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PharmacySearchErrorHint(
    title: String,
    subtitle: String,
    action: String? = null,
    onClickAction: (() -> Unit)? = null,
    modifier: Modifier
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
            if (action != null && onClickAction != null) {
                TextButton(onClick = onClickAction) {
                    Text(action)
                }
            }
        }
    }
}

@Composable
private fun EnableLocationDialog(
    onClose: () -> Unit
) {
    AcceptDialog(
        header = stringResource(R.string.search_pharmacies_location_na_header),
        info = stringResource(R.string.search_pharmacies_location_na_info),
        acceptText = stringResource(R.string.ok),
        onClickAccept = onClose,
    )
}

@Composable
fun RepeatingColumn(
    modifier: Modifier = Modifier,
    stepSize: Int = 1,
    content: @Composable ColumnScope.(Int) -> Unit
) {
    require(stepSize >= 1)
    var count by remember { mutableStateOf(stepSize) }

    Column(
        modifier = modifier,
        content = {
            repeat(count) {
                content(it)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .onSizeChanged { if (it.height > 0) count += stepSize }
            ) {}
        }
    )
}

@Composable
private fun PharmacyResultPlaceholder(
    modifier: Modifier = Modifier
) {
    val bgModifier = Modifier
        .background(AppTheme.colors.neutral200)
        .testTag("pharmacy_search_screen")

    val alphaTransition = rememberInfiniteTransition()
    val alpha by alphaTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(330, easing = LinearEasing, delayMillis = (0..100).random()),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = modifier.alpha(alpha)
    ) {
        val fontSize = with(LocalDensity.current) {
            MaterialTheme.typography.subtitle1.fontSize.toDp()
        }
        val heightModifier = bgModifier.height(fontSize)
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = heightModifier
                    .fillMaxWidth(0.8f)
            )
            SpacerSmall()
            Box(
                modifier = heightModifier
                    .fillMaxWidth(0.4f)
            )
            Box(
                modifier = heightModifier
                    .fillMaxWidth(0.25f)
            )
            SpacerSmall()
            Box(
                modifier = heightModifier
                    .fillMaxWidth(0.6f)
            )
        }
        SpacerMedium()
        Box(
            modifier = heightModifier
                .width(fontSize * 2)
                .align(Alignment.CenterVertically)
        )
    }
}

@Preview
@Composable
fun PharmacyResultPlaceholderPreview() {
    AppTheme {
        PharmacyResultPlaceholder()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SearchField(
    searchValue: String,
    onSearchChange: (String) -> Unit,
    locationEnabled: Boolean,
    onSearch: (String, Boolean) -> Unit,
) {
    val searchTextInputColor = if (searchValue.isEmpty()) {
        AppTheme.colors.neutral400
    } else {
        AppTheme.colors.neutral900
    }

    val textStyle =
        LocalTextStyle.current.copy(color = contentColorFor(MaterialTheme.colors.surface))

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
        modifier = Modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .height(48.dp)
    ) {
        BasicTextField(
            value = searchValue,
            onValueChange = onSearchChange,
            textStyle = textStyle,
            cursorBrush = SolidColor(textStyle.color),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(searchValue, locationEnabled)
                }
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .semantics(true) {}
        ) { textField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                Icon(
                    Icons.Rounded.Search,
                    null,
                    tint = AppTheme.colors.neutral600,
                    modifier = Modifier
                        .align(
                            Alignment.CenterVertically
                        )
                        .padding(start = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .align(
                            Alignment.CenterVertically
                        )
                        .padding(start = 16.dp, end = 16.dp)
                ) {
                    if (searchValue.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_example_input),
                            color = searchTextInputColor,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    textField()
                }

                IconToggleButton(
                    checked = locationEnabled,
                    onCheckedChange = {
                        onSearch(searchValue, it)
                    },
                ) {
                    when (locationEnabled) {
                        true -> Icon(
                            Icons.Rounded.LocationSearching,
                            null,
                            tint = AppTheme.colors.primary600
                        )

                        false -> Icon(
                            Icons.Rounded.LocationDisabled,
                            null,
                            tint = AppTheme.colors.neutral600
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchFieldPreview() {
    AppTheme {
        SearchField(
            "",
            {},
            false,
            { _, _ -> }
        )
    }
}

@Composable
fun FilterSection(
    filter: PharmacyUseCaseData.Filter,
    onClickChip: (PharmacyUseCaseData.Filter) -> Unit,
    onClickFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
    ) {
        TextButton(onClickFilter, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Rounded.FilterList, null)
            Text(stringResource(R.string.search_pharmacies_filter))
        }
        if (filter.isAnySet()) {
            SpacerSmall()
            FlowRow(
                modifier = Modifier
                    .padding(top = ButtonDefaults.TextButtonContentPadding.calculateTopPadding()),
                spaceBetweenItems = PaddingDefaults.Small,
                spaceBetweenRows = PaddingDefaults.Small
            ) {
                if (filter.openNow) {
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_open_now),
                        closable = true,
                        checked = false
                    ) {
                        onClickChip(filter.copy(openNow = false))
                    }
                }
                if (filter.ready) {
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_ready),
                        closable = true,
                        checked = false
                    ) {
                        onClickChip(filter.copy(ready = false))
                    }
                }
                if (filter.deliveryService) {
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_delivery_service),
                        closable = true,
                        checked = false
                    ) {
                        onClickChip(filter.copy(deliveryService = false))
                    }
                }
                if (filter.onlineService) {
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_online_service),
                        closable = true,
                        checked = false
                    ) {
                        onClickChip(filter.copy(onlineService = false))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
private fun FilterBottomSheet(
    filter: PharmacyUseCaseData.Filter,
    onClickChip: (PharmacyUseCaseData.Filter) -> Unit,
    onClickClose: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        ) {
            IconButton(onClick = onClickClose) {
                Icon(Icons.Rounded.Close, null, tint = AppTheme.colors.primary700)
            }
            Spacer(modifier = Modifier.size(20.dp))
            Text(
                stringResource(R.string.search_pharmacies_filter_header),
                style = MaterialTheme.typography.h6
            )
        }
        SpacerLarge()
        Column(modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)) {
            Text(
                stringResource(R.string.search_pharmacies_filter_section_favorites),
                style = MaterialTheme.typography.h6
            )
            SpacerMedium()
            Column(modifier = Modifier.verticalScroll(rememberScrollState(), true)) {
                FlowRow(
                    spaceBetweenItems = PaddingDefaults.Small,
                    spaceBetweenRows = PaddingDefaults.Small
                ) {
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_open_now),
                        closable = false,
                        checked = filter.openNow
                    ) {
                        onClickChip(
                            filter.copy(
                                openNow = it,
                                onlineService = false
                            )
                        )
                    }
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_ready),
                        closable = false,
                        checked = filter.ready
                    ) {
                        onClickChip(
                            filter.copy(
                                ready = it,
                                deliveryService = if (!it) false else filter.deliveryService,
                                onlineService = if (!it) false else filter.onlineService
                            )
                        )
                    }
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_delivery_service),
                        closable = false,
                        checked = filter.deliveryService
                    ) {
                        onClickChip(
                            filter.copy(
                                deliveryService = it,
                                ready = true,
                                onlineService = false
                            )
                        )
                    }
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_online_service),
                        closable = false,
                        checked = filter.onlineService
                    ) {
                        onClickChip(
                            filter.copy(
                                onlineService = it,
                                ready = true,
                                deliveryService = false,
                                openNow = false
                            )
                        )
                    }
                }
            }
        }
        SpacerXLarge()
    }
}

@Composable
private fun PharmacyResultCard(
    modifier: Modifier,
    pharmacy: PharmacyUseCaseData.Pharmacy,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier)
    ) {
        val distanceTxt = pharmacy.distance?.let { distance ->
            formattedDistance(distance)
        }

        Column(modifier = Modifier.weight(1f)) {
            PharmacyName(pharmacy.name, pharmacy.ready)

            Text(
                pharmacy.address ?: "",
                style = AppTheme.typography.body2l,
                modifier = Modifier

            )

            val pharmacyLocalServices = pharmacy.provides.first()
            val now = OffsetDateTime.now()

            if (pharmacyLocalServices.isOpenAt(now)) {
                val text = if (pharmacyLocalServices.isAllDayOpen(now.dayOfWeek)) {
                    stringResource(R.string.search_pharmacy_continuous_open)
                } else {
                    stringResource(
                        R.string.search_pharmacy_open_until,
                        requireNotNull(pharmacyLocalServices.openUntil(now)).toString()
                    )
                }
                Text(
                    text,
                    style = AppTheme.typography.subtitle2l,
                    color = AppTheme.colors.green600
                )
            } else {
                val text =
                    pharmacyLocalServices.opensAt(now)?.let {
                        stringResource(
                            R.string.search_pharmacy_opens_at,
                            it.toString()
                        )
                    }
                if (text != null) {
                    Text(
                        text,
                        style = AppTheme.typography.subtitle2l,
                        color = AppTheme.colors.yellow600
                    )
                }
            }
        }

        SpacerMedium()

        if (distanceTxt != null) {
            Text(
                distanceTxt,
                style = AppTheme.typography.body2l,
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                textAlign = TextAlign.End
            )
        }
        Icon(
            Icons.Rounded.KeyboardArrowRight, null,
            tint = AppTheme.colors.neutral400,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
        )
    }
}

private fun formattedDistance(distanceInMeters: Double): String {
    val f = DecimalFormat()
    return if (distanceInMeters < 1000) {
        f.maximumFractionDigits = 0
        f.format(distanceInMeters).toString() + " m"
    } else {
        f.maximumFractionDigits = 1
        f.format(distanceInMeters / 1000).toString() + " km"
    }
}

@Composable
private fun PharmacyName(name: String, showReadyFlag: Boolean) {
    val txt = buildAnnotatedString {
        append(name)
        if (showReadyFlag) {
            append(" ")
            appendInlineContent("ready", "ready")
        }
    }
    val c = mapOf(
        "ready" to InlineTextContent(
            Placeholder(
                width = 0.em,
                height = 0.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            ReadyFlag()
        }
    )
    DynamicText(
        txt,
        style = MaterialTheme.typography.subtitle1,
        inlineContent = c
    )
}

@Preview
@Composable
private fun PharmacyNamePreview() {
    AppTheme {
        PharmacyName("Some Pharmacy Name", true)
    }
}

@Composable
fun ReadyFlag(modifier: Modifier = Modifier) {
    with(LocalDensity.current) {
        val style = MaterialTheme.typography.caption
        val fontSize = style.fontSize.toDp()
        val space = fontSize / 3

        Row(
            modifier = modifier
                .background(color = AppTheme.colors.primary100, shape = RoundedCornerShape(8.dp))
                .wrapContentSize()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(space))
            Icon(
                painterResource(R.drawable.ic_logo_outlined),
                contentDescription = null,
                tint = AppTheme.colors.primary500,
                modifier = Modifier.size(fontSize * 1.5f)
            )
            Spacer(modifier = Modifier.width(space))
            Text(
                stringResource(R.string.search_pharmacy_ready_flag),
                style = style,
                color = AppTheme.colors.primary900
            )
            Spacer(modifier = Modifier.width(space))
        }
    }
}
