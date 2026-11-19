package com.b3hman.vpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.b3hman.vpn.R
import com.b3hman.vpn.ui.theme.B3hError
import com.b3hman.vpn.ui.theme.B3hTeal
import com.b3hman.vpn.ui.theme.Graphite800
import com.b3hman.vpn.ui.theme.OnGraphite

enum class ButtonMode { IDLE, BUSY, CONNECTED, ERROR }

/**
 * B3HMAN connect button — deliberately NOT the upstream Aether button.
 *
 * Upstream (Aether) had: a circular disc with a soft blue glowing halo that
 * "breathed" while connected, an additive glow core, and the animated "A"
 *    animated "A" brand mark as the connected glyph. This fork replaces all
 * three:
 *
 *  - **No halo, no glow.** The button is a flat graphite disc with a hairline
 *    ring. It does not bloom, breathe or light up the screen around it.
 *  - **Connected = solid teal disc + a bold dark "B".** The glyph is plain
 *    text, not an animated mark; the state reads from the fill colour, not
 *    from motion.
 *  - **Idle = graphite disc + off-white power icon.** No blue anywhere: the
 *    idle accent is warm off-white, the busy/connected accent is electric
 *    teal (#00E5C0), the error accent is soft red. The old animated "A"
 *    brand mark (AetherMark.kt) was deleted with this change.
 *
 * The busy sweep (a 90-degree arc that spins while connecting) is kept: it
 * is functional feedback, not decoration, and it is the only animation on
 * this button. It follows ONLY whether the home screen is on screen, so
 * switching the home animations off can never freeze the button into
 * looking hung. An idle screen subscribes to no frame callbacks at all.
 */
@Composable
fun ConnectButton(
    mode: ButtonMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    stateText: String? = null,
) {
    val connected = mode == ButtonMode.CONNECTED
    val busy = mode == ButtonMode.BUSY
    val accent = when (mode) {
        ButtonMode.IDLE -> OnGraphite
        ButtonMode.BUSY -> B3hTeal
        ButtonMode.CONNECTED -> B3hTeal
        ButtonMode.ERROR -> B3hError
    }
    val animatedAccent by animateColorAsState(accent, tween(600), label = "accent")

    val homeVisible = LocalHomeVisible.current
    val spin = if (busy && homeVisible) rememberSpin() else null

    val interaction = remember { MutableInteractionSource() }
    val actionLabel = stringResource(
        when (mode) {
            ButtonMode.CONNECTED -> R.string.a11y_disconnect
            ButtonMode.BUSY -> R.string.a11y_cancel_connecting
            ButtonMode.IDLE, ButtonMode.ERROR -> R.string.a11y_connect
        },
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(BUTTON_BOX)) {
        // Flat disc: solid teal when connected, graphite otherwise, with a
        // thin hairline ring in the accent colour. No halo, no glow.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(DISC)
                .clip(CircleShape)
                .background(
                    if (connected) B3hTeal else Brush.linearGradient(
                        listOf(Graphite800, Color(0xFF131816)),
                    ),
                )
                .border(
                    width = if (connected) 0.dp else 1.dp,
                    color = if (connected) Color.Transparent else animatedAccent.copy(alpha = 0.35f),
                    shape = CircleShape,
                )
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics {
                    contentDescription = actionLabel
                    role = Role.Button
                    if (stateText != null) stateDescription = stateText
                    liveRegion = LiveRegionMode.Polite
                },
        ) {
            // Progress sweep while busy.
            if (spin != null) {
                Canvas(modifier = Modifier.size(SWEEP)) {
                    rotate(degrees = spin.value) {
                        drawArc(
                            color = animatedAccent,
                            startAngle = 0f,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
            }

            if (connected) {
                // The B3HMAN mark: a bold dark "B" on the solid teal disc.
                Text(
                    text = "B",
                    color = Color(0xFF0C0F0E),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                )
            } else {
                val icon = when (mode) {
                    ButtonMode.BUSY -> Icons.Rounded.Autorenew
                    else -> Icons.Rounded.PowerSettingsNew
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animatedAccent,
                    modifier = Modifier
                        .size(ICON_SIZE)
                        .then(
                            if (spin != null) {
                                Modifier.graphicsLayer { rotationZ = spin.value }
                            } else {
                                Modifier
                            },
                        ),
                )
            }
        }
    }
}

/** The busy sweep. Composed only while busy. */
@Composable
private fun rememberSpin(): androidx.compose.runtime.State<Float> =
    rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1_200, easing = LinearEasing)),
        label = "spinAngle",
    )

// ---------------------------------------------------------------- geometry

private val BUTTON_BOX = 190.dp
private val DISC = 132.dp
private val SWEEP = 116.dp
private val ICON_SIZE = 52.dp
