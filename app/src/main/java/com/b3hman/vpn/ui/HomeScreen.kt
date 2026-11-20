package com.b3hman.vpn.ui

import android.os.SystemClock
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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.b3hman.vpn.R
import com.b3hman.vpn.ai.AiSession
import com.b3hman.vpn.core.HevTunnel
import com.b3hman.vpn.core.IpEndpoint
import com.b3hman.vpn.core.NetProbe
import com.b3hman.vpn.core.PingMonitor
import com.b3hman.vpn.core.ShareBridge
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
import com.b3hman.vpn.ui.components.LocalHomeVisible
import com.b3hman.vpn.ui.settings.SettingsHost
import com.b3hman.vpn.ui.settings.SettingsRoute
import com.b3hman.vpn.ui.theme.B3hAmber
import com.b3hman.vpn.ui.theme.B3hError
import com.b3hman.vpn.ui.theme.B3hTeal
import com.b3hman.vpn.ui.theme.B3hTealBright
import com.b3hman.vpn.ui.theme.B3hTealDeep
import com.b3hman.vpn.ui.theme.Graphite700
import com.b3hman.vpn.ui.theme.Graphite850
import com.b3hman.vpn.ui.theme.Graphite900
import com.b3hman.vpn.ui.theme.Graphite950
import com.b3hman.vpn.ui.theme.OnGraphite
import com.b3hman.vpn.ui.theme.OnGraphiteMuted
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * BAHMAN home — modern redesign (2026-11-20), Aether-only.
 *
 * Layout, top to bottom:
 *   1. the app name + settings gear
 *   2. the six Aether engine modes as selectable cards (one scrollable row)
 *   3. the big connect disc (gradient disc; pulsing rings + glow while connected)
 *   4. the BAHMAN stats card: status, live download/upload speeds, exit IP
 *      with country flag, session timer and latency — BAHMAN's own compact
 *      card, NOT the upstream Aether connection card.
 *
 * The backdrop is a static graphite-to-deep-teal gradient with two soft
 * radial glows (teal behind the connect area, amber low) — drawn once,
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
                    listOf(Graphite950, B3hTealDeep, Graphite950),
                ),
            )
            .drawBehind {
                // Static ambient glows: a strong teal wash behind the connect
                // area and a warm amber glow low on the screen. Drawn once
                // per layout — no animation, no per-frame cost.
                val tealCenter = Offset(size.width / 2f, size.height * 0.38f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(B3hTeal.copy(alpha = 0.30f), Color.Transparent),
                        center = tealCenter,
                        radius = size.width * 0.85f,
                    ),
                    radius = size.width * 0.85f,
                    center = tealCenter,
                )
                val amberCenter = Offset(size.width * 0.88f, size.height * 0.96f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(B3hAmber.copy(alpha = 0.12f), Color.Transparent),
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

            BahmanStatsCard(
                connected = state.isConnected,
                statusTitle = stateTitle(state),
                statusCaption = caption,
                connectedSince = connectedSince,
                ipInfo = ipInfo,
                ipLoading = ipLoading,
                error = state is ConnectionState.Error,
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

// ------------------------------------------------------------ stats card

/**
 * BAHMAN's own stats card (2026-11-20): one compact card with the status,
 * live speeds, exit IP + country, session timer and latency in a 2x2 grid.
 * Deliberately NOT the upstream Aether connection card — different layout,
 * different hierarchy, same underlying live data sources.
 */
@Composable
private fun BahmanStatsCard(
    connected: Boolean,
    statusTitle: String,
    statusCaption: String,
    connectedSince: Long?,
    ipInfo: IpEndpoint?,
    ipLoading: Boolean,
    error: Boolean,
    modifier: Modifier = Modifier,
) {
    val stats = rememberTrafficStats(connectedSince = connectedSince, connected = connected)

    // Session clock.
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(connectedSince) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }
    val elapsedSecs = if (connectedSince == null) 0L else ((nowMs - connectedSince) / 1000).coerceAtLeast(0)
    val timerText = String.format(
        Locale.US, "%02d:%02d:%02d",
        elapsedSecs / 3600, (elapsedSecs / 60) % 60, elapsedSecs % 60,
    )

    // Latency: same probe loop the old card used, through the live tunnel.
    val ping by PingMonitor.state.collectAsState()
    val visible = LocalHomeVisible.current
    LaunchedEffect(connected, visible) {
        while (connected && visible) {
            PingMonitor.pingOnce(viaTunnel = true)
            delay(4_000L)
        }
    }
    var lastMs by remember { mutableLongStateOf(-1L) }
    LaunchedEffect(connected, ping.ms) {
        lastMs = when {
            !connected -> -1L
            ping.ms >= 0L -> ping.ms
            else -> lastMs
        }
    }
    val latencyText = when {
        !connected -> "—"
        lastMs >= 0L -> "$lastMs ms"
        else -> "…"
    }

    val flag = NetProbe.flagEmojiOrNull(ipInfo?.countryCode)
    val ipValue = when {
        ipLoading && ipInfo == null -> stringResource(R.string.ip_checking)
        ipInfo != null -> if (flag != null) "$flag ${ipInfo.ip}" else ipInfo.ip
        else -> stringResource(R.string.ip_unavailable)
    }
    val ipLabel = stringResource(
        if (connected) R.string.ip_server_label else R.string.ip_your_label,
    )

    val titleColor = when {
        error -> B3hError
        connected -> B3hTealBright
        else -> OnGraphite
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(Graphite850, Graphite900)),
                shape = RoundedCornerShape(24.dp),
            )
            .border(
                width = 1.dp,
                color = if (connected) B3hTeal.copy(alpha = 0.35f) else Graphite700,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = statusTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (statusCaption.isNotEmpty()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = statusCaption,
                        fontSize = 13.sp,
                        color = OnGraphiteMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCell(
                    label = stringResource(R.string.traffic_download),
                    value = formatRate(stats.downRate),
                    sub = stringResource(R.string.traffic_total, formatBytes(stats.downTotal)),
                    icon = Icons.Rounded.ArrowDownward,
                    iconTint = B3hTealBright,
                    modifier = Modifier.weight(1f),
                )
                StatCell(
                    label = stringResource(R.string.traffic_upload),
                    value = formatRate(stats.upRate),
                    sub = stringResource(R.string.traffic_total, formatBytes(stats.upTotal)),
                    icon = Icons.Rounded.ArrowUpward,
                    iconTint = B3hAmber,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCell(
                    label = ipLabel,
                    value = ipValue,
                    modifier = Modifier.weight(1f),
                )
                StatCell(
                    label = stringResource(R.string.connected_for),
                    value = timerText,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.meta_latency),
                    fontSize = 12.sp,
                    color = OnGraphiteMuted,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = latencyText,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = OnGraphite,
                )
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    sub: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = B3hTeal,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                color = OnGraphiteMuted,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = OnGraphite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (sub != null) {
            Text(
                text = sub,
                fontSize = 10.sp,
                color = OnGraphiteMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ------------------------------------------------------------ traffic feed

/** Instantaneous rates + session totals, polled once per second. */
private data class TrafficStats(
    val downRate: Long = 0L,
    val upRate: Long = 0L,
    val downTotal: Long = 0L,
    val upTotal: Long = 0L,
)

/**
 * Sums BOTH possible traffic paths so the meter works in every mode:
 * hev-socks5-tunnel's counters (system-VPN mode) plus ShareBridge bytes
 * (proxy mode / LAN clients). Rates come from deltas against a monotonic
 * clock; negative deltas clamp to zero and the baseline rebases itself.
 */
@Composable
private fun rememberTrafficStats(connectedSince: Long?, connected: Boolean): TrafficStats {
    var stats by remember(connectedSince) { mutableStateOf(TrafficStats()) }

    val visible = LocalHomeVisible.current
    LaunchedEffect(connectedSince, connected, visible) {
        if (!connected || !visible) return@LaunchedEffect
        var lastDown = -1L
        var lastUp = -1L
        var lastAt = 0L
        while (true) {
            val hev = HevTunnel.traffic()
            val share = ShareBridge.traffic()
            if (hev != null || ShareBridge.active.value) {
                val down = (hev?.downloadBytes ?: 0L) + share.downloadBytes
                val up = (hev?.uploadBytes ?: 0L) + share.uploadBytes
                val at = SystemClock.elapsedRealtime()
                var downRate = stats.downRate
                var upRate = stats.upRate
                if (lastAt > 0L && at > lastAt) {
                    val dt = at - lastAt
                    downRate = ((down - lastDown).coerceAtLeast(0L) * 1000L) / dt
                    upRate = ((up - lastUp).coerceAtLeast(0L) * 1000L) / dt
                }
                stats = TrafficStats(downRate, upRate, down, up)
                lastDown = down
                lastUp = up
                lastAt = at
            }
            delay(1_000L)
        }
    }

    return stats
}

private fun formatBytes(v: Long): String {
    if (v < 1024L) return "$v B"
    val kb = v / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024.0) return String.format(Locale.US, "%.1f MB", mb)
    return String.format(Locale.US, "%.2f GB", mb / 1024.0)
}

private fun formatRate(v: Long): String = formatBytes(v) + "/s"

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
