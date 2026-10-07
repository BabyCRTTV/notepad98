# Notepad 98 for Windows

Version 1.8.1 packages the same 17-theme editor as a standalone Electron app. The NSIS installer supports Windows 10/11 x64 (Intel/AMD), installs for the current user, creates shortcuts, and provides an uninstaller. The renderer is sandboxed, has no Node access, and loads bundled files rather than a remote website.

## Build

From a checkout of this repository, install Node.js 22 or newer and run:

```
cd windows
npm ci
npm run dist
```

The installer appears in `windows/dist/Notepad98-Setup-1.8.1-x64.exe`. Build on Windows for the supported release process. The prepare script copies the editor from the repository root and adjusts the desktop labels. Do not edit the generated `ui` directory.

## Release

The Windows installer workflow builds on a Windows runner, installs the EXE into an isolated temporary directory, launches that installed application, and verifies 17 themes, sandbox isolation, editing, draft/theme recovery, and text downloads before publishing a GitHub Release. Bump package.json and package-lock.json together for future Windows versions. Release tags are not overwritten. Update the website download link after the release succeeds.

The installer is unsigned. Windows may show an unknown-publisher warning. No signing keys or private credentials are stored in this project.

## Notes and files

File → Open reads local UTF-8 text. Save opens a Windows save dialog for a copy. Rename changes the suggested filename; it does not rename an existing file. Important notes should be saved as files. Local drafts are separate from browser and Android drafts. There is no automatic updater; install a later release manually.
