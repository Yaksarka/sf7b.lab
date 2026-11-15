#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
./gradlew --no-daemon --console=plain :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
mkdir -p artifacts
cp app/build/outputs/apk/debug/app-debug.apk artifacts/sf7b-lab-cloud-debug.apk
cp app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk artifacts/sf7b-lab-cloud-tests.apk
sha256sum artifacts/*.apk
echo 'Host tests, APK assembly and lint only. No A51/emulator/GPU verification implied.'
