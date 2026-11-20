package com.b3hman.vpn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
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
import com.b3hman.vpn.ui.components.ConnectionCard
import com.b3hman.vpn.ui.components.HomeSettingsHost
import com.b3hman.vpn.ui.settings.SettingsHost
import com.b3hman.vpn.ui.settings.SettingsRoute
import com.b3hman.vpn.ui.theme.B3hAmber
import com.b3hman.vpn.ui.theme.B3hTeal
import com.b3hman.vpn.ui.theme.B3hTealBright
import com.b3hman.vpn.ui.theme.Graphite700
import com.b3hman.vpn.ui.theme.Graphite850
import com.b3hman.vpn.ui.theme.Graphite950
import com.b3hman.vpn.ui.theme.OnGraphite

/**
 * BAHMAN home — modern redesign (2026-11-20), Aether-only.
 *
 * Layout, top to bottom:
 *   1. the app name + settings gear
 *   2. the six Aether engine modes as selectable cards (one scrollable row)
 *   3. the big connect disc (pulsing rings + glow while connected)
 *   4. the connection card: status, session timer, exit-IP pill, live
 *      download/upload speeds, pipeline and latency
 *
 * The backdrop is a static graphite gradient with two soft radial glows
 * (teal behind the connect area, a whisper of amber low) — drawn once,
 * never animated, so it costs no frame budget. The custom-config / Xray UI
 * stays removed; the engine modes themselves are unchanged —
 * [TransportBackend] still carries all six.
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
            HomeSurface(state, profile, connectedSince, ipInfo, ipLoading, onProfileChange, onToggleConnection,
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
    ipInfo: IpEndpoint?,
    ipLoading: Boolean,
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

    val caption = when {
        state.isConnected -> stringResource(R.string.tap_to_disconnect)
        state is ConnectionState.Error -> state.message
        state.isBusy -> ""
        else -> stringResource(R.string.tap_to_connect)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Graphite950, Color(0xFF0E1512), Graphite950),
                ),
            )
            .drawBehind {
                // Static ambient glows: a teal wash behind the connect area
                // and a whisper of amber low on the screen. Drawn once per
                // layout — no animation, no per-frame cost.
                val tealCenter = Offset(size.width / 2f, size.height * 0.40f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(B3hTeal.copy(alpha = 0.13f), Color.Transparent),
                        center = tealCenter,
                        radius = size.width * 0.80f,
                    ),
                    radius = size.width * 0.80f,
                    center = tealCenter,
                )
                val amberCenter = Offset(size.width * 0.88f, size.height * 0.96f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(B3hAmber.copy(alpha = 0.05f), Color.Transparent),
                        center = amberCenter,
                        radius = size.width * 0.65f,
                    ),
                    radius = size.width * 0.65f,
                    center = amberCenter,
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
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

            Spacer(Modifier.height(20.dp))

            // The six Aether engine modes as selectable cards.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TransportBackend.entries.forEach { backend ->
                    val selected = profile.backend == backend
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) {
                                    Brush.linearGradient(listOf(B3hTeal, B3hTealBright))
                                } else {
                                    SolidColor(Graphite850)
                                },
                            )
                            .border(
                                width = 1.dp,
                                color = if (selected) Color.Transparent else Graphite700,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable(enabled = !locked) {
                                onProfileChange(profile.copy(backend = backend))
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) Color(0xFF0C0F0E) else B3hTeal.copy(alpha = 0.55f),
                                    ),
                            )
                            Text(
                                text = backend.pipelineLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color(0xFF0C0F0E) else OnGraphite,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            ConnectButton(
                mode = mode,
                onClick = onToggleConnection,
                stateText = stateTitle(state),
            )

            Spacer(Modifier.height(24.dp))

            ConnectionCard(
                connected = state.isConnected,
                statusTitle = stateTitle(state),
                statusCaption = caption,
                connectedSince = connectedSince,
                ipInfo = ipInfo,
                ipLoading = ipLoading,
                error = state is ConnectionState.Error,
                backend = profile.backend,
            )

            Spacer(Modifier.height(12.dp))
        }
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
