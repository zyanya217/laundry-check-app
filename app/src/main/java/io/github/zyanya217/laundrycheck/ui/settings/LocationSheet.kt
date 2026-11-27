package io.github.zyanya217.laundrycheck.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.zyanya217.laundrycheck.R
import io.github.zyanya217.laundrycheck.ui.components.CloseIcon
import io.github.zyanya217.laundrycheck.ui.components.LocateIcon
import io.github.zyanya217.laundrycheck.ui.theme.LaundryFonts

/** Opens from the place chip at the top of the main screen. Changes apply as soon as they are made. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSheet(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
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
                Text(stringResource(R.string.loc), fontFamily = LaundryFonts.Display, fontSize = 22.sp)
                val closeDescription = stringResource(R.string.close_settings)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.semantics { contentDescription = closeDescription },
                ) {
                    CloseIcon(color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
