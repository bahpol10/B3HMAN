# B3HMAN — Build Guide / راهنمای ساخت

B3HMAN is a rebrand/fork of **Aether Mobile 1.6.0** (QW-AI-Code, AGPL-3.0).
Read `REBRAND.md` first — it records the upstream commit, the license, and
exactly what was rebranded.

## Prerequisites / پیش‌نیازها

- **JDK 17** (`java -version` must print 17)
- **Android Studio** (Ladybug or newer) **or** the Android SDK command-line
  tools with `ANDROID_HOME` / `ANDROID_SDK_ROOT` set
- Android SDK: `compileSdk 35`, `minSdk 26`, `targetSdk 35`
- For the native engine: Android NDK + Rust toolchain — the CI workflow
  (`.github/workflows/build.yml`) runs `scripts/build-natives.sh`, which
  cross-compiles the Aether engine (core 2.4.0), `libhev-socks5-tunnel.so`
  and the `libaethertun.so` JNI bridge. The `.so` files are **not** in this
  repo; they are built, then packaged into `app/src/main/jniLibs`.

> This VM has no JDK and no Android SDK, so no APK was built here. The
> project was verified with static checks only (identifier renames, XML
> validity, bracket balance on edited files, forbidden-content grep).

## Option A — Android Studio (recommended) / اندروید استودیو

1. Open this folder (`~/workspace/b3hman`) in Android Studio.
2. Let Gradle sync finish (first sync downloads the AGP/Kotlin plugins and
   the Compose BOM — needs internet).
3. **First run needs the natives:** either run the GitHub Actions workflow
   once (Option B) and copy the produced `jniLibs`, or run
   `bash scripts/build-natives.sh aether` on a machine with the NDK + Rust
   installed. Without the natives the app installs but the tunnel cannot
   start (the app reports this in diagnostics — it never pretends to be
   connected).
4. Run ▶ (debug) on a phone/emulator, or **Build → Build App Bundle(s) /
   APK(s) → Build APK(s)**.
5. Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Option B — Command line / خط فرمان

```bash
export ANDROID_HOME=$HOME/Android/Sdk   # adjust to your SDK path
./gradlew assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

Per-ABI splits are produced for release builds (arm64-v8a, armeabi-v7a,
x86_64 + universal).

## Option C — GitHub Actions / گیت‌هاب اکشن

Push this fork to your own GitHub repository — **make the repo private**,
because the first build generates a fresh B3HMAN signing key and stores it
in the repo (`.github/ci-keystore.jks.b64`), so later builds sign with the
same key and updates install in place. The workflow in
`.github/workflows/build.yml` builds and tests the app and publishes the
APKs as a GitHub Release (named `B3HMAN-1.6.0-<abi>.apk`). It takes a while
on the first run (the native engine is cross-compiled) — roughly 30–60
minutes. **Before publishing a release to anyone else:** generate your own
keystore (`bash scripts/generate-keystore.sh`) and put it in the repo
Secrets as `KEYSTORE_BASE64` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` /
`KEY_PASSWORD` — the workflow prefers Secrets over the repo key.

## Signing a release / امضای نسخه انتشار

```bash
bash scripts/generate-keystore.sh   # creates keystore.properties (git-ignored)
./gradlew assembleRelease
```

Then pin your own certificate so the About screen can verify it:

```bash
keytool -list -v -keystore <your.keystore> | grep SHA-256
# put the 64-hex fingerprint (lowercase, no colons) into
# .github/expected-signer.txt
```

Without a pinned fingerprint the About card reports "cannot be checked
here" — that is the honest state for an unpinned build, not an error.

## Static verification done in this workspace / بررسی‌های انجام‌شده

- The six CI guard scripts in `scripts/` now pass here
  (`test-security-ci`, `test-core-sync-disabled`, `test-functional-wiring`,
  `test-ai-settings-coverage`, `test-auto-signing`, `test-ci-preflight`) —
  the three that asserted the deleted custom-config / clean-IP UI and the
  upstream pinned key were updated for the 2026-11-19 simplification and
  for the fork (no bundled upstream key; first build mints a fresh B3HMAN
  key). The key-generation test needs a JDK, so it skips here and runs on
  the GitHub runner. Gradle unit tests and the native build can only be
  proven by the first real GitHub Actions run.
- `grep`: 0 occurrences of `studio.cluvex` in app code, Gradle files and
  the JNI bridge sources; 0 occurrences of the old blue palette
  (`#5B93FF`, `#4C8DFF`, `#9B8CFF`, `#070B14`); 0 forbidden sales terms
  (no bot/channel/price/server/panel references — see `REBRAND.md`).
- XML validity: `strings.xml` (en + fa), `AndroidManifest.xml`,
  `themes.xml` (values + values-v27), `colors.xml`, launcher icons — all
  parse.
- Bracket balance on every file edited by the rebrand (ConnectButton,
  Color, Theme, HomeScreen, GlowCycle, ConnectionCard, TProxyService):
  balanced. (A naive whole-tree count flags pre-existing brackets inside
  strings/regex in untouched upstream files; those counts are identical
  to upstream HEAD.)
- JNI consistency: `TProxyService.kt` (`com.b3hman.vpn.core`),
  `scripts/aethertun-jni.c` and `scripts/build-natives.sh` all use
  `Java_com_b3hman_vpn_core_TProxyService_*`.

## What is in the app / امکانات

Six engine modes (Aether, Aether → Psiphon, Tor, Aether → Tor,
Tor → Psiphon, Tor → Aether), per-app split tunnelling, kill switch, DNS/MTU/fragmentation/ECH,
Gemini assistant (your own free API key from Google AI Studio), Quick
Settings tile, home-screen widget, encrypted diagnostics log. (Custom
configs / Xray UI were removed in the 2026-11-19 simplification — see the
section at the end of this file.)

## Simplified 2026-11-19 — Aether only, minimal home

The home screen now has only the app name, one chip row with the six
Aether engine modes, the connect disc, and one status line. All Xray /
custom-config UI was deleted (import screens, config tabs, ping table,
chains, clean-IP scan, file-import activity + manifest entry, and their
tests); the `CUSTOM_CONFIGS` / `CLEAN_IP_SETTINGS` settings routes and
the Upstream / Share / Diagnostics / Engine / Zero-Trust / Announcements
rows are gone. Settings keeps only the Aether-core groups: Tunnel
(Connection / Transport / DNS), Device (Apps / Security), Assistant
(Gemini, one row), App (Language / About). See `REBRAND.md` for the full
list, including what remains in code but is unreachable from the UI
(`custom/` + `scanner/` packages and the service error paths — kept only
because removing them needs a compiler to verify the build).

Verification (static, no JDK/SDK in this VM — still no APK): XML parses
(manifest, strings en+fa, themes, colors); edited Kotlin files
bracket-balanced; 0 references to the deleted screens/routes anywhere in
`app/src/main` or `app/src/test`; 0 `vless`/`vmess`/`trojan` in
`strings.xml`; 0 sales identifiers (`B3Hvpn`, `jize`, `behmangt`,
`pnl.b3h`) in the app and docs.
