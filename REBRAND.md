# B3HMAN — Rebrand of Aether Mobile

> **B3HMAN یک ری‌برند (فورک) از Aether Mobile است، نه یک اپ از صفر.**
> B3HMAN is a rebrand (fork) of Aether Mobile, not an app written from scratch.

## Upstream / بالادست

- **Upstream project:** Aether Mobile by QW-AI-Code — https://github.com/QW-AI-Code/Aether
- **Cloned commit:** `249cc903b225de4280b58e50e0c298834e6d4285` (main, 2026-11-19)
- **Upstream version:** app **1.6.0** (versionCode 19), engine core **2.4.0**
- **License:** **GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)**
  — see `LICENSE`. The upstream copyright and credit are kept intact:
  `THIRD_PARTY_NOTICES.md`, `SOURCE_MANIFEST.sha256`, `MODIFICATIONS.md`,
  and the "Aether core — Cluvex Studio" credit in the About screen were NOT
  removed.

## What this fork is / این فورک چیست

B3HMAN keeps the **Aether core and all of its prerequisites** exactly as
upstream: the native Aether engine (`native/aether`, core 2.4.0), the Android
`VpnService`, the six engine modes (Aether, Aether → Psiphon, Tor,
Aether → Tor, Tor → Psiphon, Tor → Aether), per-app split tunnelling, kill
switch, DNS / MTU / fragmentation / ECH settings, the Gemini assistant
(user's own API key), Quick Settings tile, home-screen widget, and the
encrypted diagnostics log. (The custom-config / Xray UI was removed in
the 2026-11-19 simplification — see "Simplified 2026-11-19" below.)

Only the **brand and the look** changed. There is deliberately **no**
sales content in this app: no prices, no servers of any seller, no purchase
bot, no channel, no payment method. The only "B3HMAN" is the app name itself.

## AGPL obligations / تعهدات AGPL

If you distribute B3HMAN (APK or any modified form), the AGPL-3.0 requires
you to make the complete corresponding source of this fork available to the
recipients under the same license, including the `native/aether` engine
sources as vendored here. Do not remove `LICENSE`,
`THIRD_PARTY_NOTICES.md`, or the upstream credits. This file, `REBRAND.md`,
must stay with the fork.

## What changed (rebrand) / چه چیزی عوض شد

| Area | Change |
|---|---|
| App name | `Aether` → **B3HMAN** (`app_name`, notification, QS tile, widget, onboarding, crash screen, AI title, tagline "One tap, your tunnel.") — English + Persian strings |
| Package | `studio.cluvex.aether` → **`com.b3hman.vpn`** — all Kotlin sources (main + test), `AndroidManifest.xml`, `app/build.gradle.kts` (`namespace` + `applicationId`), `settings.gradle.kts` (`rootProject.name = "B3hman"`), `scripts/test-*.py` paths, `.github/removed-sources.txt` paths |
| JNI bridge | `scripts/aethertun-jni.c` + `scripts/build-natives.sh`: symbols `Java_studio_cluvex_aether_core_TProxyService_*` → `Java_com_b3hman_vpn_core_TProxyService_*`, matching the moved `TProxyService.kt`. The native libraries are built by CI from these sources (not vendored), so natives **must be rebuilt** — see README-B3HMAN.md |
| Theme | Upstream navy/blue (`#070B14`, `#5B93FF`, mint, violet) → **B3HMAN graphite + electric teal + warm amber**: background `#0C0F0E`, surfaces `#131816`/`#1A201D`/`#222B26`, primary teal `#00E5C0`, amber `#FFB020`, warm off-white text `#F2F5F1`. Files: `ui/theme/Color.kt` (rewritten), `ui/theme/Theme.kt`, `res/values/colors.xml`, `res/values/themes.xml` (+ `values-v27`), `Theme.AetherMobile` → `Theme.B3hman` |
| Connect button | Upstream: blue glowing halo + additive core + animated "A" mark (`AetherMark.kt`). B3HMAN: **flat graphite disc, no halo, no glow**; connected = solid teal disc with a bold dark **"B"**; idle = off-white power icon. `AetherMark.kt` deleted. `ui/components/ConnectButton.kt` rewritten |
| Travelling edge | `GlowCycle` palette: red/green/blue/yellow → **teal / amber / off-white / deep teal** (no blue) |
| Home layout | Upstream: title → preset selector → connect button → status card. B3HMAN: title → preset selector → **status card → connect button** (button low, at thumb reach), wider gaps |
| Launcher icon | Upstream stylised blue "A" shield → **bold geometric teal "B" with one amber dot**, graphite background (`ic_launcher_foreground.xml`, `ic_launcher_background.xml`); TV banner redrawn with the same mark (`tv_banner.png`) |
| Signer pin | `.github/expected-signer-ci.txt` (the **upstream Aether release certificate**) was **removed** from this fork. A B3HMAN build signed with your own key would otherwise report "signed by someone else". Builds now report UNPINNED until you pin your own fingerprint — see README-B3HMAN.md |

## What was deliberately NOT changed — and why

- **Internal engine class names** (`AetherController`, `AetherProcess`,
  `AetherEngine`, `AetherStage`, `AetherVpnService`, `AetherTileService`,
  `AetherWidgetProvider`, `AetherFa`): they name Aether-core components,
  are not user-visible, and renaming them adds compile risk for zero user
  benefit. The user-visible brand is B3HMAN everywhere it matters.
- **Engine/mode strings** ("Aether", "Aether → Psiphon", "Aether/WARP",
  "Aether core — Cluvex Studio"): these name the engine itself, not the
  app. Renaming them would misdescribe the core the app runs on.
- **Upstream docs** (`README.md`, `README.fa.md`, `CHANGELOG.md`, `docs/`):
  left as the upstream record of the code this fork is built on.
- **`.github/ci-keystore.jks.b64` (upstream public CI keystore): REMOVED
  from this fork (2026-11-19).** It was the key anyone can use to sign an
  APK that installs over the upstream Aether app, so it must never sign a
  B3HMAN release. The workflow now generates a fresh B3HMAN key on the
  first build and persists it to the repo (keep the repo private). The
  local opt-in fallback (`-PaetherAllowPublicCiKey=true`) is inert without
  the file. Generate your own keystore for real distribution — see
  README-B3HMAN.md.
- **`.github/workflows/build.yml`**: the upstream CI workflow, with B3HMAN
  release/artifact names and the fresh-key signing flow above. Its CI
  guard scripts were updated for the simplification (the tests that
  asserted the deleted custom-config / clean-IP UI and the upstream pinned
  key were removed or rewritten — see README-B3HMAN.md).

## What is NOT in this app / چه چیزی در این اپ نیست

Zero trace of any sales operation: no prices, no seller servers, no
purchase bot, no channel, no support contact, no payment method, no panel
or subscription URLs. Verified by grep — see README-B3HMAN.md.

## Simplified 2026-11-19 / ساده‌سازی

The user said the screen was too crowded and he does not want Xray and
those things either — just Aether itself. So this fork was simplified:

**Home screen now contains only four things:** the app name B3HMAN (small),
one compact chip row with the six Aether engine modes, the big connect disc,
and one status line (Disconnected / Connecting / Connected + session time).
Removed from home: the navigation drawer, the preset selector and its
import/export menu, the status card (pipeline, IP, speed strip, latency),
the tagline, the top AI/tune shortcuts, and the What's-New popup on launch.
(`ui/HomeScreen.kt` rewritten, 391 lines → ~230.)

**Xray / custom configs removed from the user-facing app (deleted, not
just hidden):**
- Deleted UI screens: `ui/settings/CustomConfigsPage.kt` (import by
  paste-link / subscription / file / QR, config tabs, ping table, chains),
  `ui/settings/CleanIpSettingsPage.kt` + `CleanIpScanNotice.kt` +
  `CleanIpScanResultCard.kt`, `ui/settings/ConfigDataTable.kt`,
  `ui/settings/ConfigTabStrip.kt`, and `ConfigImportActivity.kt` (+ its
  manifest entry, so no other app can open a config file into B3HMAN).
- Deleted the 9 UI tests that composed those screens.
- Removed the `CUSTOM_CONFIGS` / `CLEAN_IP_SETTINGS` routes from
  `SettingsRoute` and their rows from Settings; removed the Upstream,
  Share, Diagnostics, Engine, Zero-Trust, Announcements rows and the
  Display & Sound section.
- Removed 148 unused `cc_*` / `cip_*` strings from `strings.xml` (en + fa):
  `vless` / `vmess` / `trojan` / `Xray` now occur 0 times in user-visible
  strings, except 2 engine error strings (`cc_err_no_core`,
  `cc_err_core_failed`) still referenced by `AetherVpnService` for the
  unreachable custom-config path.
- `PresetSelector.kt` and `ConnectionCard.kt` are now unused (kept in the
  tree, nothing references them from the UI).

**What remains in code but is unreachable from the UI** (documented, not
deleted, because the engine/service layer still references it and
removing it would break the build without a compiler here): the
`custom/` package (config store/parser/Xray process), the `scanner/`
clean-IP engine, the `AetherVpnService` custom-config error paths, and
the `AiTopic.CUSTOM_CONFIGS` help topics. No button, menu, drawer, tile,
widget, deep link, or file association in the app can reach any of it.
To finish the removal, delete `custom/` + `scanner/` and the custom
branches in `AetherVpnService`/`MainActivity` once a compiler is
available to verify the build.

**What remains (Aether core only):** the native Aether engine, the
`VpnService`, the six engine modes, and the core settings — Protocol,
Scan mode, IP version, Obfuscation, MTU/keepalive, DNS, kill switch,
per-app tunnel, and the Gemini assistant as ONE row in Settings (no
home tab). Navigation is now just Home + Settings.
