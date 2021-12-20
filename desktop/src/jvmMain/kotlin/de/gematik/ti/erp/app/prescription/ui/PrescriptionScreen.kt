
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.common.App
import de.gematik.ti.erp.app.common.Dialog
import de.gematik.ti.erp.app.common.HorizontalDivider
import de.gematik.ti.erp.app.common.HorizontalSplittable
import de.gematik.ti.erp.app.common.SpacerSmall
import de.gematik.ti.erp.app.common.SpacerTiny
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults
import de.gematik.ti.erp.app.main.ui.MainNavigation
import de.gematik.ti.erp.app.navigation.ui.Navigation
import de.gematik.ti.erp.app.prescription.ui.PrescriptionDetailsScreen
import de.gematik.ti.erp.app.prescription.ui.PrescriptionViewModel
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.rememberScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.bind
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PrescriptionScreen(
    navigation: Navigation
) {
    val scope = rememberScope()

    subDI(diBuilder = {
        bind { scoped(scope).singleton { PrescriptionViewModel(instance(), instance()) } }
    }) {
        val prescriptionViewModel by rememberInstance<PrescriptionViewModel>()
        val state by produceState(prescriptionViewModel.defaultState) {
            prescriptionViewModel.screenState().collect {
                value = it
            }
        }

        DeleteAlertDialog(prescriptionViewModel)

        val coScope = rememberCoroutineScope()
        val selectedPrescription = state.selectedPrescription

        LaunchedEffect(navigation.currentBackStackEntry) {
            when (navigation.currentBackStackEntry) {
                is MainNavigation.PrescriptionsUnredeemed ->
                    coScope.launch { prescriptionViewModel.onSelectNotDispensed() }
                is MainNavigation.PrescriptionsRedeemed ->
                    coScope.launch { prescriptionViewModel.onSelectDispensed() }
            }
        }

        if (state.prescriptions.isNotEmpty() && selectedPrescription != null) {
            HorizontalSplittable(
                split = 0.3f,
                contentLeft = {
                    PrescriptionList(
                        state.prescriptions,
                        selectedPrescription = selectedPrescription.prescription,
                        onClickPrescription = {
                            coScope.launch {
                                prescriptionViewModel.onSelectPrescription(
                                    it
                                )
                            }
                        }
                    )
                },
                contentRight = {
                    PrescriptionDetailsScreen(
                        navigation = navigation,
                        prescription = selectedPrescription,
                        audits = state.selectedPrescriptionAudits,
                        onClickDelete = {
                            prescriptionViewModel.deletePrescription(selectedPrescription.prescription)
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun DeleteAlertDialog(
    prescriptionViewModel: PrescriptionViewModel
) {
    var deleteState by remember { mutableStateOf<PrescriptionScreenData.DeleteState?>(null) }

    LaunchedEffect(Unit) {
        prescriptionViewModel.deleteState().collect {
            deleteState = it
        }
    }
    deleteState?.let {
        Dialog(
            title = it.error ?: "",
            confirmButton = {
                TextButton(onClick = {
                    deleteState = null
                }) {
                    Text(App.strings.cancel().uppercase(Locale.getDefault()))
                }
            },
            onDismissRequest = { deleteState = null }
        )
    }
}

@Composable
private fun PrescriptionList(
    prescriptions: List<PrescriptionUseCaseData.Prescription>,
    selectedPrescription: PrescriptionUseCaseData.Prescription,
    onClickPrescription: (PrescriptionUseCaseData.Prescription) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    val lazyListState = rememberLazyListState()
    val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState
        ) {
            itemsIndexed(prescriptions, key = { _, it -> it }) { index, it ->
                Prescription(
                    modifier = Modifier.fillMaxWidth(),
                    name = it.name,
                    selected = it.taskId == selectedPrescription.taskId,
                    expiresOnText = expiresOrAcceptedUntil(it),
                    prescribedOnText = App.strings.desktopPrescriptionPrescribedOn(
                        count = 1,
                        it.authoredOn.format(formatter)
                    ),
                    onClick = { onClickPrescription(it) }
                )
                HorizontalDivider()
            }
        }
        VerticalScrollbar(
            scrollbarAdapter,
            modifier = Modifier.align(Alignment.CenterEnd).padding(horizontal = 1.dp).fillMaxHeight()
        )
    }
}

@Composable
fun expiresOrAcceptedUntil(
    prescription: PrescriptionUseCaseData.Prescription
): String {
    val now = remember { LocalDate.now().toEpochDay() }
    val expiryDaysLeft = prescription.expiresOn.toEpochDay() - now
    val acceptDaysLeft = prescription.acceptUntil.toEpochDay() - now

    return when {
        prescription.redeemedOn != null -> {
            val dtFormatter =
                remember(prescription) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
            prescription.redeemedOn.format(dtFormatter)
        }
        acceptDaysLeft >= 0 -> App.strings.desktopPrescriptionAcceptUntil(
            count = acceptDaysLeft.toInt(),
            acceptDaysLeft
        )
        expiryDaysLeft >= 0 -> App.strings.desktopPrescriptionExpiresOn(count = expiryDaysLeft.toInt(), expiryDaysLeft)
        else -> App.strings.desktopPrescriptionExpired()
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun Prescription(
    modifier: Modifier,
    name: String,
    selected: Boolean,
    expiresOnText: String,
    prescribedOnText: String,
    onClick: () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(if (hovered) 1f else 0f)
    val color = if (selected) AppTheme.colors.primary100 else AppTheme.colors.neutral100.copy(alpha = alpha)

    Column(
        modifier
            .fillMaxSize()
            .background(color)
            .clickable(onClick = onClick)
            .padding(PaddingDefaults.Medium)
            .pointerMoveFilter(
                onEnter = {
                    hovered = true
                    false
                },
                onExit = {
                    hovered = false
                    false
                }
            )
    ) {
        Text(name, style = MaterialTheme.typography.subtitle1)
        Text(expiresOnText, style = AppTheme.typography.body2l)
        SpacerSmall()
        Text(prescribedOnText, style = AppTheme.typography.captionl)
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun IconHoverButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    content: @Composable BoxScope.() -> Unit
) {
    val colors = ButtonDefaults.buttonColors(
        backgroundColor = AppTheme.colors.neutral100,
        contentColor = AppTheme.colors.neutral400,
    )
    val contentColor by colors.contentColor(enabled)
    val coScope = rememberCoroutineScope()
    var size by remember { mutableStateOf(IntSize.Zero) }
    val press = remember(size) {
        PressInteraction.Press(Offset(size.width / 2f, size.height / 2f))
    }
    Surface(
        modifier = modifier
            .onSizeChanged {
                size = it
            }
            .pointerMoveFilter(
                onEnter = {
                    coScope.launch {
                        interactionSource.emit(press)
                    }
                    false
                },
                onExit = {
                    coScope.launch {
                        interactionSource.emit(PressInteraction.Release(press))
                    }
                    false
                }
            ),
        shape = CircleShape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        elevation = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp,
        onClick = onClick,
        enabled = enabled,
        role = Role.Button,
        interactionSource = interactionSource,
        indication = rememberRipple()
    ) {
        Box(Modifier.padding(PaddingDefaults.Small)) {
            content()
        }
    }
}

@Composable
private fun PrescriptionFlagChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    iconColor: Color,
    icon: ImageVector,
) {
    Row(
        Modifier
            .background(backgroundColor, CircleShape).height(22.dp)
            .padding(horizontal = PaddingDefaults.Small, vertical = PaddingDefaults.Small / 4)
    ) {
        Text(text, style = MaterialTheme.typography.subtitle1, color = textColor)
        SpacerTiny()
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = iconColor)
    }
}
