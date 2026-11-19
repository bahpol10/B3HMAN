package com.b3hman.vpn.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// The B3HMAN palette — graphite + electric teal + warm amber.
//
// B3HMAN is a rebrand/fork of Aether Mobile (QW-AI-Code, AGPL-3.0). The
// upstream palette was a deep-navy ramp (hue 222 deg) with a blue primary
// (#5B93FF), mint and violet accents. This fork deliberately uses a DIFFERENT
// identity so it never looks like the original:
//
//   * base: warm graphite, NOT navy — the hue is green-grey, not blue;
//   * primary: electric teal #00E5C0, NOT blue;
//   * second accent: warm amber #FFB020 (used for the AI assistant and for
//     warnings), NOT violet;
//   * connected state: teal; idle state: warm off-white outline, NOT blue.
//
// The structure is the same as upstream (almost-black base, four elevation
// steps that add lightness, never transparency), so every surface keeps the
// elevation behaviour the original theme was built around — only the hues
// and the accent story changed.
// ---------------------------------------------------------------------------

/** The page behind everything. */
val Graphite950 = Color(0xFF0C0F0E)
/** Sheets, drawers, the settings surface. */
val Graphite900 = Color(0xFF131816)
/** Elevation 1: a settings group / card. */
val Graphite850 = Color(0xFF1A201D)
/** Elevation 2: a row inside a card, an input field. */
val Graphite800 = Color(0xFF222B26)
/** Elevation 3: a pressed row, the selected segment's track. */
val Graphite750 = Color(0xFF2C3831)
/** Hairlines and dividers. */
val Graphite700 = Color(0xFF3A473F)
/** Disabled strokes. */
val Graphite600 = Color(0xFF4D5C52)

// ---- Accents -------------------------------------------------------------

/** Primary action / selected state: electric teal. */
val B3hTeal = Color(0xFF00E5C0)
/** Container tint behind a primary icon. */
val B3hTealDim = Color(0xFF0E2E28)
/** The connected accent, shared with the card's travelling edge. */
val B3hTealBright = Color(0xFF4DF0D2)
val B3hTealDeep = Color(0xFF0A2E27)
/** Warm amber: the AI assistant's accent and the tertiary accent. */
val B3hAmberGlow = Color(0xFFFFB020)
val B3hAmber = Color(0xFFFFB020)
/** Container tint behind an AI icon, matched to the ramp's elevation steps. */
val B3hAmberDim = Color(0xFF33270A)
val B3hError = Color(0xFFFF6B6B)
val B3hErrorDim = Color(0xFF3A1717)

// ---- Text ----------------------------------------------------------------

/** Warm off-white, not the upstream cool #E8EEF9. */
val OnGraphite = Color(0xFFF2F5F1)
val OnGraphiteMuted = Color(0xFF9AA79E)
val OnGraphiteDim = Color(0xFF6B7A70)

// ---- Connection card ------------------------------------------------------

val B3hCardTop = Color(0xF01A201D)
val B3hCardBottom = Color(0xF60C0F0E)

/** Every sub-container inside the card (IP pill, speed strip, protocol strip). */
val B3hCardSub = Graphite800

val B3hCardTextPrimary = OnGraphite
val B3hCardTextMuted = OnGraphiteMuted
val B3hCardTextDim = OnGraphiteDim
