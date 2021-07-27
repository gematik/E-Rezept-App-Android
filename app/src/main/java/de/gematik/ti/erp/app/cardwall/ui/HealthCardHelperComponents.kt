package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.model.InsuranceCompany
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.demo.ui.DemoBanner
import de.gematik.ti.erp.app.messages.ui.email
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HealthCardHelperScreen(
    viewModel: CardWallViewModel,
    demoMode: Boolean
) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val insuranceCompanies by produceState(
        initialValue = emptyList<InsuranceCompany>()
    ) {
        value = viewModel.loadInsuranceCompanies(context.applicationContext)?.entries ?: emptyList()
    }
    val selectedIndex = remember { mutableStateOf(-1) }
    val textState = remember { mutableStateOf(TextFieldValue()) }
    val errorState = remember { mutableStateOf(false) }
    val focus = remember { mutableStateOf(false) }
    val subject = stringResource(id = R.string.cdw_helper_mail_subject)
    val res = stringResource(id = R.string.cdw_helper_mail_body)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                mode = NavigationBarMode.Close,
                headline = stringResource(R.string.cdw_helper_top_title),
                onClick = { activity.onBackPressed() }
            )
        },
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val insuranceCompanyMail = insuranceCompanies[selectedIndex.value].email
                        val insuranceCompanyName = insuranceCompanies[selectedIndex.value].name
                        val securityNumber = textState.value.text
                        val emailBody = res.format(insuranceCompanyName, securityNumber)
                        email(insuranceCompanyMail, subject, emailBody, context)
                    },
                    enabled = textState.value.text.length == 10 && selectedIndex.value >= 0 && !errorState.value,
                ) {
                    Text(text = stringResource(id = R.string.cdw_helper_send).uppercase(Locale.getDefault()))
                }
                SpacerSmall()
            }
        }
    ) {
        var maxHeight = 0
        Column(
            modifier = Modifier
                .onSizeChanged {
                    if (focus.value) {
                        coroutineScope.launch {
                            scrollState.scrollTo(maxHeight)
                        }
                    }
                }
        ) {
            if (demoMode) {
                DemoBanner {}
            }
            Box(modifier = Modifier.verticalScroll(scrollState)) {
                Column(
                    modifier = Modifier
                        .onSizeChanged {
                            maxHeight = it.height
                        }
                        .padding(it)
                        .semantics(false) {}
                ) {
                    Text(
                        text = stringResource(id = R.string.cdw_helper_title),
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(id = R.string.cdw_helper_text),
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        textAlign = TextAlign.Center
                    )
                    SpacerMedium()
                    Text(
                        text = stringResource(id = R.string.cdw_helper_health_insurance_header),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(PaddingDefaults.Medium)
                    )
                    HealthInsuranceChooser(insuranceCompanies, selectedIndex)
                    SpacerMedium()
                    HealthCardOwnerData(textState, errorState, focus)
                    SpacerSmall()
                }
            }
        }
    }
}

@Composable
fun HealthInsuranceChooser(
    insuranceCompanies: List<InsuranceCompany>,
    selectedIndex: MutableState<Int>
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = PaddingDefaults.Medium, end = PaddingDefaults.Medium)
            .wrapContentSize(Alignment.TopStart)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.Small)
                .clickable { expanded = true }
                .semantics(true) {},
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val text = if (selectedIndex.value == -1) {
                stringResource(id = R.string.cdw_helper_dropdown_default)
            } else {
                insuranceCompanies[selectedIndex.value].name
            }
            Text(text, color = AppTheme.colors.primary600)
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    tint = AppTheme.colors.primary600,
                    contentDescription = null
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
        ) {
            insuranceCompanies.forEachIndexed { index, company ->
                DropdownMenuItem(
                    onClick = {
                        selectedIndex.value = index
                        expanded = false
                    }
                ) {
                    Text(text = company.name)
                }
            }
        }
    }
}

@Composable
fun HealthCardOwnerData(
    textState: MutableState<TextFieldValue>,
    errorState: MutableState<Boolean>,
    focus: MutableState<Boolean>
) {
    Text(
        text = stringResource(id = R.string.cdw_helper_kvnr),
        style = MaterialTheme.typography.h6,
        modifier = Modifier.padding(PaddingDefaults.Medium)
    )
    OutlinedTextField(
        value = textState.value,
        onValueChange = { textFieldValue ->
            textState.value = textFieldValue
            errorState.value = if (textFieldValue.text.length >= 10) {
                !isHealthInsuranceNumberValid(textState.value.text)
            } else {
                false
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium)
            .onFocusChanged {
                focus.value = it.isFocused
            },
        shape = RoundedCornerShape(8.dp),
        isError = errorState.value,
        label = {
            Text(
                if (errorState.value) stringResource(id = R.string.cdw_helper_error_input_security_number) else stringResource(
                    id = R.string.cdw_helper_label_input_security_number
                )
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            errorBorderColor = AppTheme.colors.red500,
            errorLabelColor = AppTheme.colors.red500,
            errorCursorColor = AppTheme.colors.red500
        )
    )
}

@Composable
fun HealthCardHelperButton(
    action: () -> Unit
) {
    val offset =
        ButtonDefaults.TextButtonContentPadding.calculateLeftPadding(LocalLayoutDirection.current)
    TextButton(
        modifier = Modifier.absoluteOffset(x = -offset),
        onClick = { action() },
        enabled = true,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(stringResource(R.string.learn_more_btn))
    }
}

private fun isHealthInsuranceNumberValid(nr: String): Boolean =
    if (nr.matches("""[A-Z]\d{9}""".toRegex())) {
        val letterNr = "%02d".format((nr.first().code - 64))
        val numbers = letterNr + nr.substring(1..8)
        val check = numbers.foldIndexed(0) { index, acc, c ->
            acc + if (index % 2 != 0) {
                val a = c.digitToInt() * 2
                if (a > 9) a - 9 else a
            } else {
                c.digitToInt()
            }
        }
        check % 10 == nr.last().digitToInt()
    } else {
        false
    }
