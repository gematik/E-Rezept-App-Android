package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomNavigationDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.systemBarsPadding

@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        elevation = elevation,
        shape = RectangleShape
    ) {
        androidx.compose.material.TopAppBar(
            title,
            Modifier.statusBarsPadding(),
            navigationIcon,
            actions,
            backgroundColor,
            contentColor,
            elevation = 0.dp
        )
    }
}

@Composable
fun BottomAppBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    cutoutShape: Shape? = null,
    elevation: Dp = AppBarDefaults.BottomAppBarElevation,
    contentPadding: PaddingValues = AppBarDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        elevation = elevation
    ) {
        androidx.compose.material.BottomAppBar(
            Modifier.navigationBarsPadding(),
            backgroundColor,
            contentColor,
            cutoutShape,
            elevation = 0.dp,
            contentPadding,
            content
        )
    }
}

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = BottomNavigationDefaults.Elevation,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        elevation = elevation
    ) {
        androidx.compose.material.BottomNavigation(
            Modifier.navigationBarsPadding(),
            backgroundColor,
            contentColor,
            elevation = 0.dp,
            content
        )
    }
}

fun Modifier.minimalSystemBarsPadding() = Modifier.composed {
    val navBarInsetsPadding = rememberInsetsPaddingValues(
        insets = LocalWindowInsets.current.systemBars,
        applyBottom = true
    )

    if (navBarInsetsPadding.calculateBottomPadding() <= 16.dp) {
        statusBarsPadding()
    } else {
        systemBarsPadding()
    }
}
