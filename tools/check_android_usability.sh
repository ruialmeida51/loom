#!/usr/bin/env bash
# Keep the emulator alive while collecting evidence, including after an instrumentation failure.
set -euo pipefail
cd "$(dirname "$0")/.."
collect_device_evidence() {
    local usability_status=$?
    mkdir -p build/device-captures
    timeout 15s adb pull /sdcard/Pictures/Loom-usability build/device-captures || true
    timeout 15s adb logcat -d > build/device-captures/logcat.txt || true
    exit "$usability_status"
}
trap collect_device_evidence EXIT
./gradlew :androidApp:connectedDebugAndroidTest --no-parallel --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8'
