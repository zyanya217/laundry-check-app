package io.github.zyanya217.laundrycheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.zyanya217.laundrycheck.ui.forecast.ForecastScreen
import io.github.zyanya217.laundrycheck.ui.forecast.ForecastViewModel
import io.github.zyanya217.laundrycheck.ui.settings.LocationSheet
import io.github.zyanya217.laundrycheck.ui.settings.SettingsSheet
import io.github.zyanya217.laundrycheck.ui.settings.SettingsViewModel
import io.github.zyanya217.laundrycheck.ui.theme.LaundryTheme
import io.github.zyanya217.laundrycheck.ui.theme.isDark
import io.github.zyanya217.laundrycheck.widget.WidgetRefresh

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val forecastViewModel: ForecastViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by forecastViewModel.uiState.collectAsStateWithLifecycle()
            // Kept across the activity restart that a language change causes, so the sheet stays open.
            var sheetOpen by rememberSaveable { mutableStateOf(false) }
            var locationOpen by rememberSaveable { mutableStateOf(false) }

            LaundryTheme(darkTheme = state.settings.theme.isDark()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ForecastScreen(
                        state = state,
                        onOpenSettings = { sheetOpen = true },
                        onOpenLocation = { locationOpen = true },
                        onReload = forecastViewModel::reload,
                        onStartHour = forecastViewModel::setStartHour,
                    )
                    if (locationOpen) {
                        LocationSheet(viewModel = settingsViewModel, onDismiss = { locationOpen = false })
                    }
                    if (sheetOpen) {
                        SettingsSheet(viewModel = settingsViewModel, onDismiss = { sheetOpen = false })
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Back in the foreground: a forecast older than 15 minutes is reloaded.
        forecastViewModel.refreshIfStale()
    }

    override fun onStop() {
        super.onStop()
        // Settings may have changed while the app was open: bring the home-screen widget up to date.
        WidgetRefresh.refreshNow(applicationContext)
    }
}
