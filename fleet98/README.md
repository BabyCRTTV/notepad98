# Fleet98 by 8BitTrade

A complete first playable release of a classic naval strategy game in a Windows 98-inspired interface. Original top-down procedural ship artwork, dark steel-and-radar combat styling, and local-only saves.

## Play and downloads

- Browser: https://babycrttv.github.io/notepad98/fleet98/web/
- Android: Fleet98-1.0.0-debug.apk (Android 8+; test build).
- Windows: extract the entire Fleet98-1.0.0-Windows-x64.zip, then run Fleet98.exe. Keep all files together. This is a portable app, not an installer. The EXE is unsigned.
- In this repository, **Actions → Fleet98 builds** produces downloadable Android and Windows artifacts for every Fleet98 source update. Extract the Android artifact ZIP to find the APK.

Fleet98 lives in its own project folder alongside Notepad98 for this initial release. All app code is self-contained here and can move to a dedicated repository. The existing Notepad98 app is not changed.

## Features

- 10 × 10 classic fleet, five ships / 17 occupied cells; turn-based single shots.
- Cadet random AI, Captain hunt/target AI, Admiral placement-density AI. The AI receives only its shot history and remaining ship sizes, never the player's hidden positions.
- Manual positioning, rotation and randomized deployment.
- Per-ship callsign, three silhouettes, two colors, four paint schemes, three insignias plus none, and engine wake toggle. Designs are cosmetic and apply directly to board sprites.
- Radar sweep, hit/miss impact animations, optional synthesized sound, reduced-motion support.
- Automatically saved operation, designs, options and win/loss record on each device. Browser/native saves are separate.
- Mouse, touch, and keyboard grid controls. Arrow keys select a coordinate; Enter acts; R rotates during deployment.
- Offline browser caching after successful first online load. Close all game tabs and reopen online to activate a newly downloaded browser version.

## Development

Requires Node.js 22+ for tests. The web app has zero third-party runtime dependencies.

```sh
npm test
npm run serve
```

Open http://localhost:8080. You can also open web/index.html directly, without offline service-worker support.

- `web/engine.js`: pure rules, placement and observation-only AI; CommonJS compatible for testing.
- `web/app.js`: match lifecycle, persistence, UI and ship/board renderers.
- `web/style.css`: retro theme and responsive layout.
- `tests/engine.test.cjs`: placement, shot results, AI completion and targeting tests.
- `android/`: native offline WebView shell and command-line build.
- `desktop/`: sandboxed Electron shell with pinned dependencies and lockfile.

## Android build

Install JDK 17 and Android SDK command-line tools, accept SDK licenses, and install `platforms;android-35` and `build-tools;35.0.0`. Set ANDROID_SDK_ROOT, then run:

```sh
bash android/build.sh
```

The default produces `android/build/Fleet98-debug.apk`, using a locally generated debug key. It requests no internet permission and loads only bundled game assets. This is a sideloadable test build, not a Play Store release. New debug keys cannot update an existing installation; uninstalling erases local saves.

For stable releases, provide your permanent private keystore via FLEET98_KEYSTORE, FLEET98_STORE_PASS and FLEET98_KEY_PASS. The output becomes `android/build/Fleet98.apk`. Back up that key privately; never commit it. No release key is included in this source bundle. Increment Android versionCode for every Android update.

## Windows build

Copy `web/` to `desktop/web/`, then:

```sh
cd desktop
npm ci
npm run package:win
```

The complete portable application is created at `dist/Fleet98-win32-x64/`. Zip the entire folder for distribution. Running on Windows was not tested in the Linux build environment. Signing and a Windows installer can be added later without changing the game engine.

## Automated development

The included `.github/workflows/fleet98.yml` belongs at the repository root. It detects whether Fleet98 is nested in `fleet98/` or at the repository root. It tests the rules and builds both native packages. The existing repository's GitHub Pages publishing serves the web directory.

For a dedicated new repo, enable GitHub Pages using GitHub Actions and use `pages.yml.example` as `.github/workflows/pages.yml`. Then update the README and in-game source links to the new repo. Avoid adding that Pages workflow to the shared Notepad98 repo because it would replace its existing site.

For web updates, bump the cache name in `web/sw.js`. Match-save schema is versioned separately via `game.version`; migrate saves if changing it. Keep visual customization independent from combat rules. No analytics, ads or external runtime assets.

## Validation for 1.0.0

1,000 randomized placements and 450 AI games passed. Browser smoke checks passed for rendering, ship edits, saved designs, a full player/enemy turn, match restoration and 390px layout without horizontal overflow. APK signature verified; Windows PE executable packaged. Native device execution remains to be checked on actual Android and Windows devices.

## Ownership

Original Fleet98 source and artwork: 8BitTrade, 2026. Third-party desktop components retain their bundled licenses. Fleet98 is an independent naval strategy game, unaffiliated with Microsoft, id Software or Hasbro.
