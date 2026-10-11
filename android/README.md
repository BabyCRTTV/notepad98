# Notepad 98 for Android 1.7.4

A plain-text editor by 8BitTrade with 17 themes, file opening/saving/renaming, draft recovery, fonts, word wrap, and find/replace.

Help → Check for updates contacts the official HTTPS version feed only when requested. It offers newer Android releases, verifies the downloaded APK checksum, package identity, version code, and signing certificate, then opens Android’s installation confirmation. Android may first ask you to allow updates from this app. Notes are never uploaded. Editing works offline.

## One-time signing change

1.7.4 uses a new permanent signing key. Save your unfinished note as a text file, uninstall any older version, then install 1.7.4 and reopen your file. Preferences and unsaved drafts do not transfer through uninstall. Future releases signed with this permanent key update in place.

The private key and passwords are deliberately excluded from this source. Reuse the separately saved Notepad98-Android-signing-backup.zip for all future signed releases; never commit it publicly.

## Build

Requires Java 17 and Android SDK platform 35/build-tools 35.0.0. Set ANDROID_SDK_ROOT, NOTEPAD_KEYSTORE, NOTEPAD_STORE_PASS, and NOTEPAD_KEY_PASS, then run bash build.sh. The signed APK is Notepad98.apk. Run bash build.sh --unsigned for CI compilation only.

## Release and update feed

Keep the same package name and permanent signing key. Increase the manifest versionCode and versionName; build and verify the signed APK. Publish it as Notepad98-vVERSION.apk alongside a sanitized source ZIP. Update the android entry in the root updates.json with the version, versionCode, exact HTTPS download URL, SHA-256 of the signed APK, and short release notes. Publish the file and metadata together. Do not advertise a release until its APK is available. The Windows entry is maintained by its release workflow.
