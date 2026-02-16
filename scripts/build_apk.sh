#!/usr/bin/env bash
set -euo pipefail

# Build debug APK using local Gradle installation (wrapper JAR is intentionally not tracked).
JAVA_HOME_DEFAULT="$HOME/.local/share/mise/installs/java/17.0.2"
export JAVA_HOME="${JAVA_HOME_OVERRIDE:-$JAVA_HOME_DEFAULT}"
export PATH="$JAVA_HOME/bin:$PATH"

# Use sdk auto-download mode in clean environments.
gradle assembleDebug -Pandroid.builder.sdkDownload=true

echo "APK: app/build/outputs/apk/debug/app-debug.apk"
