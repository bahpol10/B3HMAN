package com.b3hman.vpn.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.b3hman.vpn.R
import com.b3hman.vpn.ai.AiSession
import com.b3hman.vpn.core.IpEndpoint
import com.b3hman.vpn.model.ConnectionPreset
import com.b3hman.vpn.model.ConnectionProfile
import com.b3hman.vpn.model.ConnectionState
import com.b3hman.vpn.model.TransportBackend
import com.b3hman.vpn.model.isBusy
import com.b3hman.vpn.model.isConnected
import com.b3hman.vpn.ui.ai.AiHostContext
import com.b3hman.vpn.ui.ai.LocalAiHost
import com.b3hman.vpn.ui.components.ButtonMode
import com.b3hman.vpn.ui.components.ConnectButton
import com.b3hman.vpn.ui.components.HomeSettingsHost
import com.b3hman.vpn.ui.settings.SettingsHost
import com.b3hman.vpn.ui.settings.SettingsRoute
import com.b3hman.vpn.ui.theme.B3hTeal
import kotlinx.coroutines.delay

/**
 * B3HMAN home — minimal, Aether-only (simplified 2026-11-19).
 *
 * Deliberately only four things on screen:
 *   1. the app name, small, top-left
 *   2. one compact mode row — the six Aether engine modes as chips
 *   3. the big connect disc
 *   4. one status line (Disconnected / Connecting / Connected + session time)
 *
 * Everything else that used to live here is gone: the navigation drawer, the
 * preset selector and its import/export menu, the status card (pipeline, IP,
 * speed strip, latency), the tagline, the top AI/tune shortcuts and the
 * What's-New popup. Settings is one gear icon, top-right. The engine modes
 * themselves are unchanged — [TransportBackend] still carries all six.
 *
 * The preset parameters below are kept only so [com.b3hman.vpn.MainActivity]
 * keeps compiling unchanged; nothing on this screen uses them any more.
 */
@Composable
fun HomeScreen(
    state: ConnectionState,
    profile: ConnectionProfile,
    connectedSince: Long?,
    ipInfo: IpEndpoint?,
    ipLoading: Boolean,
    onProfileChange: (ConnectionProfile) -> Unit,
    onToggleConnection: () -> Unit,
    presets: List<ConnectionPreset> = emptyList(),
    activePresetId: String? = null,
    onSelectPreset: (String) -> Unit = {},
    onSavePresetAs: (String) -> Unit = {},
    onDeletePreset: (String) -> Unit = {},
    onRenamePreset: (String, String) -> Unit = { _, _ -> },
    onImportPresets: () -> Unit = {},
    onExportPresets: () -> Unit = {},
    presetBusy: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var route by remember { mutableStateOf<SettingsRoute?>(null) }
    val aiSettings by AiSession.settings.collectAsState()
    CompositionLocalProvider(
        LocalAiHost provides AiHostContext(profile, state, aiSettings.showHints,
            openAiSettings = { route = SettingsRoute.AI }, openConnection = { route = SettingsRoute.CONNECTION },
            openChat = { route = SettingsRoute.AI_CHAT }),
    ) {
        com.b3hman.vpn.ui.components.HomeSettingsHost(route == null, modifier.fillMaxSize(), home = {
            HomeSurface(state, profile, connectedSince, onProfileChange, onToggleConnection,
                onOpenSettings = { route = SettingsRoute.HOME })
        }, settings = {
            route?.let { SettingsHost(it, state, profile, onProfileChange, { route = null }) }
        })
    }
}

@Composable
private fun HomeSurface(
    state: ConnectionState,
    profile: ConnectionProfile,
    connectedSince: Long?,
    onProfileChange: (ConnectionProfile) -> Unit,
    onToggleConnection: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mode = when {
        state.isConnected -> ButtonMode.CONNECTED
        state.isBusy -> ButtonMode.BUSY
        state is ConnectionState.Error -> ButtonMode.ERROR
        else -> ButtonMode.IDLE
    }
    val locked = state.isConnected || state.isBusy

    // Session clock: ticks once a second only to repaint the status line.
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(connectedSince) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }
    val statusLine = when {
        state.isConnected && connectedSince != null -> {
            val secs = ((nowMs - connectedSince) / 1000).coerceAtLeast(0)
            "${stringResource(R.string.state_connected)} · %02d:%02d:%02d".format(secs / 3600, (secs / 60) % 60, secs % 60)
        }
        else -> stateTitle(state)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(R.string.settings_title),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // The six Aether engine modes, one compact scrollable chip row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TransportBackend.entries.forEach { backend ->
                val selected = profile.backend == backend
                Surface(
                    onClick = { if (!locked) onProfileChange(profile.copy(backend = backend)) },
                    enabled = !locked,
                    shape = RoundedCornerShape(50),
                    color = if (selected) B3hTeal else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = backend.pipelineLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        ConnectButton(
            mode = mode,
            onClick = onToggleConnection,
            stateText = stateTitle(state),
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = statusLine,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun stateTitle(state: ConnectionState): String = when (state) {
    is ConnectionState.Idle -> stringResource(R.string.state_idle)
    is ConnectionState.Selecting -> stringResource(R.string.state_selecting)
    is ConnectionState.Launching -> stringResource(R.string.state_launching)
    is ConnectionState.Connecting -> stringResource(R.string.state_connecting)
    is ConnectionState.Verifying -> stringResource(R.string.state_verifying)
    is ConnectionState.Connected -> stringResource(R.string.state_connected)
    is ConnectionState.Reconnecting -> stringResource(R.string.state_reconnecting)
    is ConnectionState.Disconnecting -> stringResource(R.string.state_disconnecting)
    is ConnectionState.Error -> stringResource(R.string.state_error)
}
