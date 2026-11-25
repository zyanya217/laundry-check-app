package io.github.zyanya217.laundrycheck.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyanya217.laundrycheck.R
import io.github.zyanya217.laundrycheck.logic.hh
import io.github.zyanya217.laundrycheck.model.ThemeChoice
import io.github.zyanya217.laundrycheck.ui.components.ChevronDownIcon
import io.github.zyanya217.laundrycheck.ui.components.LocateIcon
import io.github.zyanya217.laundrycheck.ui.theme.LaundryFonts

private val START_HOURS = (17..23).toList()
private val COLLECT_HOURS = (5..10).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings
    val context = LocalContext.current
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    // The permission prompt appears only here, when the person taps the button.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.useMyLocation() else viewModel.onPermissionDenied()
    }
    val onUseLocation = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.useMyLocation() else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.settings), fontFamily = LaundryFonts.Display, fontSize = 22.sp)
                Button(onClick = onDismiss) { Text(stringResource(R.string.done), fontWeight = FontWeight.Bold) }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel(stringResource(R.string.loc))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DropdownField(
                        label = stringResource(R.string.county),
                        value = settings.county,
                        options = state.counties,
                        optionText = { it },
                        onSelect = viewModel::onCounty,
                        modifier = Modifier.weight(1f),
                    )
                    DropdownField(
                        label = stringResource(R.string.district),
                        value = settings.district,
                        options = state.districts,
                        optionText = { it },
                        onSelect = viewModel::onDistrict,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedButton(
                    onClick = onUseLocation,
                    enabled = !state.gps.busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                ) {
                    LocateIcon(color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        stringResource(if (state.gps.busy) R.string.locating else R.string.use_gps),
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.Medium,
                    )
                }
                state.gps.message?.let { Text(it.text(), color = muted, fontSize = 13.sp) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DropdownField(
                    label = stringResource(R.string.start_def),
                    value = settings.startHour,
                    options = START_HOURS,
                    optionText = ::hh,
                    onSelect = viewModel::onStartHour,
                    modifier = Modifier.weight(1f),
                    fontFamily = LaundryFonts.Mono,
                )
                DropdownField(
                    label = stringResource(R.string.collect_def),
                    value = settings.collectHour,
                    options = COLLECT_HOURS,
                    optionText = ::hh,
                    onSelect = viewModel::onCollectHour,
                    modifier = Modifier.weight(1f),
                    fontFamily = LaundryFonts.Mono,
                )
            }

            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.covered))
                    Text(stringResource(R.string.covered_hint), color = muted, fontSize = 12.sp)
                }
                Switch(checked = settings.covered, onCheckedChange = viewModel::onCovered)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.lang), color = muted, fontSize = 12.sp)
                Segmented(
                    options = AppLanguage.entries.map { it to it.label },
                    selected = currentAppLanguage(context),
                    onSelect = { setAppLanguage(context, it) },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.theme), color = muted, fontSize = 12.sp)
                Segmented(
                    options = listOf(
                        ThemeChoice.AUTO to stringResource(R.string.th_auto),
                        ThemeChoice.LIGHT to stringResource(R.string.th_light),
                        ThemeChoice.DARK to stringResource(R.string.th_dark),
                    ),
                    selected = settings.theme,
                    onSelect = viewModel::onTheme,
                )
            }
        }
    }
}

@Composable
private fun GpsMessage.text(): String = when (this) {
    is GpsMessage.Located -> stringResource(R.string.gps_ok, county, district)
    GpsMessage.Denied -> stringResource(R.string.gps_denied)
    GpsMessage.Unavailable -> stringResource(R.string.gps_unavailable)
    GpsMessage.Timeout -> stringResource(R.string.gps_timeout)
    GpsMessage.Outside -> stringResource(R.string.gps_outside)
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, letterSpacing = 0.06.em)
}

/** A labelled field that opens a menu of choices. Plain Material pieces, so it does not depend on experimental APIs. */
@Composable
private fun <T> DropdownField(
    label: String,
    value: T,
    options: List<T>,
    optionText: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = LaundryFonts.Sans,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Box {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        optionText(value),
                        modifier = Modifier.weight(1f),
                        fontFamily = fontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ChevronDownIcon(color = MaterialTheme.colorScheme.onSurface)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionText(option), fontFamily = fontFamily) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

/** Pill-shaped single choice, like the web app's segmented buttons. */
@Composable
private fun <T> Segmented(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(3.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(value) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    textAlign = TextAlign.Center,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
