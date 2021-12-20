package de.gematik.ti.erp.app.profiles.ui

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.db.entities.ProfileColors
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.ui.AddProfileDialog
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.ProfileNameInputField
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer40
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.firstCharOfForeNameSurName
import kotlinx.coroutines.flow.collect
import java.util.Locale

@Composable
fun EditProfileScreen(
    state: SettingsScreen.State,
    profile: ProfilesUseCaseData.Profile,
    settingsViewModel: SettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    onBack: () -> Unit,
    mainNavController: NavController,
) {
    val navController = rememberNavController()

    EditProfileNavGraph(
        state = state,
        navController = navController,
        onBack = onBack,
        profile = profile,
        settingsViewModel = settingsViewModel,
        onRemoveProfile = onRemoveProfile,
        mainNavController = mainNavController
    )
}

@Composable
fun EditProfileScreen(
    profileId: Int,
    settingsViewModel: SettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    onBack: () -> Unit,
    mainNavController: NavController,
) {
    val state by produceState(initialValue = SettingsScreen.defaultState) {
        settingsViewModel.screenState().collect {
            value = it
        }
    }

    state.profileById(profileId)?.let {
        EditProfileScreen(
            state = state,
            onBack = onBack,
            profile = it,
            settingsViewModel = settingsViewModel,
            onRemoveProfile = onRemoveProfile,
            mainNavController = mainNavController
        )
    }
}

@Composable
fun EditProfileScreenContent(
    onBack: () -> Unit,
    selectedProfile: ProfilesUseCaseData.Profile,
    state: SettingsScreen.State,
    settingsViewModel: SettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    onClickToken: () -> Unit,
    ssoTokenValid: Boolean = false,
    onClickLogIn: () -> Unit
) {
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.edit_profile_title),
        listState = listState,
        onBack = onBack,
    ) {
        var showAddDefaultProfileDialog by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier.testTag("edit_profile_screen"),
            state = listState,
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.systemBars,
                applyBottom = true
            )
        ) {
            item {
                ColorAndProfileNameSection(selectedProfile, state, settingsViewModel)
            }
            item { SecuritySection(onClickToken, ssoTokenValid) }
            item {
                if (ssoTokenValid) {
                    LogoutButton(onClick = {
                        settingsViewModel.logout(selectedProfile)
                    })
                } else
                    LoginButton(
                        onClick = { onClickLogIn() }
                    )
            }
            item {
                RemoveProfileSection(
                    onClickRemoveProfile = {
                        if (state.uiProfiles.count() == 1) {
                            showAddDefaultProfileDialog = true
                        } else {
                            onRemoveProfile(null)
                        }
                    }
                )
            }
        }

        if (showAddDefaultProfileDialog) {
            AddProfileDialog(
                state = state,
                wantRemoveLastProfile = true,
                onEdit = { showAddDefaultProfileDialog = false; onRemoveProfile(it) },
                onDismissRequest = { showAddDefaultProfileDialog = false }
            )
        }
    }
}

@Composable
fun SecuritySection(onClick: () -> Unit, tokenAvailable: Boolean) {
    SecurityHeadline()
    SecurityTokenSubSection(tokenAvailable, onClick)
}

@Composable
fun SecurityTokenSubSection(tokenAvailable: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val noTokenAvailableText = stringResource(R.string.settings_no_active_token)

    val iconColor = if (tokenAvailable) {
        AppTheme.colors.primary500
    } else {
        AppTheme.colors.primary300
    }

    val textColor = if (tokenAvailable) {
        AppTheme.colors.neutral999
    } else {
        AppTheme.colors.neutral600
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    if (tokenAvailable) {
                        onClick()
                    } else {
                        Toast
                            .makeText(context, noTokenAvailableText, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            )
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {},
    ) {
        Icon(Icons.Outlined.VpnKey, null, tint = iconColor)
        Text(
            stringResource(
                R.string.settings_show_token
            ),
            style = MaterialTheme.typography.body1, color = textColor
        )
    }
}

@Composable
private fun SecurityHeadline() {
    Column {
        Column(
            modifier = Modifier.padding(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_appprotection_headline),
                style = MaterialTheme.typography.h6
            )
        }
    }
}

@Composable
private fun LoginButton(onClick: () -> Unit) {
    LoginLogoutButton(
        onClick = onClick,
        buttonText = R.string.login,
        buttonDescription = R.string.login_description,
        contentColor = AppTheme.colors.primary700
    )
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    var dialogVisible by remember { mutableStateOf(false) }

    if (dialogVisible) {
        CommonAlertDialog(
            header = stringResource(id = R.string.logout_detail_header),
            info = stringResource(R.string.logout_detail_message),
            actionText = stringResource(R.string.logout_delete_yes),
            cancelText = stringResource(R.string.logout_delete_no),
            onCancel = { dialogVisible = false },
            onClickAction = {
                onClick()
                dialogVisible = false
            }
        )
    }

    LoginLogoutButton(
        onClick = { dialogVisible = true },
        buttonText = R.string.logout,
        buttonDescription = R.string.logout_description,
        contentColor = AppTheme.colors.red700
    )
}

@Composable
private fun LoginLogoutButton(
    onClick: () -> Unit,
    @StringRes buttonText: Int,
    @StringRes buttonDescription: Int,
    contentColor: Color
) {
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp,
                bottom = 16.dp
            )
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.neutral050,
            contentColor = contentColor
        )
    ) {
        Text(
            stringResource(buttonText).uppercase(Locale.getDefault()),
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
        )
    }
    Text(
        stringResource(buttonDescription),
        modifier = Modifier.padding(
            start = PaddingDefaults.Medium,
            end = PaddingDefaults.Medium,
            bottom = PaddingDefaults.Small
        ),
        style = AppTheme.typography.body2l,
        textAlign = TextAlign.Center
    )
}

@Composable
fun ColorAndProfileNameSection(
    profile: ProfilesUseCaseData.Profile,
    state: SettingsScreen.State,
    settingsViewModel: SettingsViewModel
) {

    val colors = profileColor(profileColors = profile.color)
    var currentSelectedColors by remember { mutableStateOf(colors) }

    val name = rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var nameDuplicated by remember { mutableStateOf(false) }
    val initials = remember(profile.name) { firstCharOfForeNameSurName(profile.name) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
    ) {
        Spacer40()
        Surface(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.CenterHorizontally),
            shape = CircleShape,
            color = currentSelectedColors.backGroundColor
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    initials, style = MaterialTheme.typography.body2,
                    color = currentSelectedColors.textColor,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 60.sp,
                )
            }
        }

        Spacer40()
        ProfileNameInputField(
            modifier = Modifier
                .testTag("editProfile/profile_text_input")
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = name.value,
            onValueChange = {
                focusRequester.requestFocus()
                name.value = it.trimStart()
                nameDuplicated = false
            },
            onSubmit = { editedName ->
                if (!state.containsProfileWithName(editedName)) {
                    settingsViewModel.updateProfileName(profile, name.value)
                    focusManager.clearFocus()
                } else {
                    nameDuplicated = true
                }
            },
            isError = name.value.isEmpty() || nameDuplicated
        )

        val errorText = if (name.value.isEmpty()) {
            stringResource(R.string.edit_profile_empty_profile_name)
        } else {
            stringResource(R.string.edit_profile_duplicated_profile_name)
        }

        if (name.value.isEmpty() || nameDuplicated) {
            Spacer4()
            Text(
                text = errorText,
                color = AppTheme.colors.red600,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = PaddingDefaults.Medium)
            )
        }
        Spacer40()
        Text(
            stringResource(R.string.edit_profile_background_color),
            style = MaterialTheme.typography.h6
        )

        Spacer24()
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            ProfileColors.values().forEach {
                val currentValueColors = profileColor(profileColors = it)
                ColorSelector(
                    profileColors = it,
                    selected = currentValueColors == currentSelectedColors
                ) { newColors ->
                    settingsViewModel.updateProfileColor(profile.name, it)
                    currentSelectedColors = newColors
                }
            }
        }
        Spacer16()
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                currentSelectedColors.colorName,
                style = MaterialTheme.typography.body2,
                color = AppTheme.colors.neutral600
            )
        }
    }
}

@Composable
fun createProfileColor(colors: ProfileColors): ProfileColor {
    return profileColor(profileColors = colors)
}

@Composable
fun ColorSelector(
    profileColors: ProfileColors,
    selected: Boolean,
    onSelectColor: (ProfileColor) -> Unit,
) {

    val colors = createProfileColor(profileColors)
    val contentDescription = annotatedStringResource(
        R.string.edit_profile_color_selected,
        profileColors.name
    ).toString()

    Surface(
        modifier = Modifier
            .size(40.dp),
        shape = CircleShape,
        color = colors.backGroundColor
    ) {
        Row(
            modifier = Modifier.clickable(onClick = { onSelectColor(colors) }),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    Icons.Outlined.Done,
                    contentDescription,
                    tint = colors.textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun RemoveProfileSection(onClickRemoveProfile: () -> Unit) {
    var dialogVisible by remember { mutableStateOf(false) }
    if (dialogVisible) {

        CommonAlertDialog(
            header = stringResource(id = R.string.remove_profile_header),
            info = stringResource(R.string.remove_profile_detail_message),
            actionText = stringResource(R.string.remove_profile_yes),
            cancelText = stringResource(R.string.remove_profile_no),
            onCancel = { dialogVisible = false },
            onClickAction = {
                onClickRemoveProfile()
                dialogVisible = false
            }
        )
    }

    Button(
        onClick = { dialogVisible = true },
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp,
                bottom = 16.dp
            )
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.red600,
            contentColor = AppTheme.colors.neutral000
        )
    ) {
        Text(
            stringResource(R.string.remove_profile).uppercase(Locale.getDefault()),
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
        )
    }
}
