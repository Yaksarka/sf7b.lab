#!/usr/bin/env bash
set -euo pipefail
# Run with bash tools/cloud/setup.sh. Requires JDK 17, unzip and curl.
root="$(cd "$(dirname "$0")/../.." && pwd)"
sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/.local/share/sf7b-android-sdk}}"
export ANDROID_HOME="$sdk" ANDROID_SDK_ROOT="$sdk"
if ! java -version 2>&1 | head -1 | grep -q '"17\.'; then
  echo 'JDK 17 required. Set JAVA_HOME and PATH (e.g. /usr/lib/jvm/java-17-openjdk-amd64).' >&2
  exit 1
fi
if [[ ! -x "$sdk/cmdline-tools/latest/bin/sdkmanager" ]]; then
  tmp="$(mktemp -d)"
  trap 'rm -rf -- "$tmp"' EXIT
  curl --fail --location --retry 3 https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip -o "$tmp/tools.zip"
  unzip -q "$tmp/tools.zip" -d "$tmp"
  mkdir -p "$sdk/cmdline-tools"
  mv "$tmp/cmdline-tools" "$sdk/cmdline-tools/latest"
fi
manager="$sdk/cmdline-tools/latest/bin/sdkmanager"
# Accept Android SDK licenses for this explicitly requested build environment.
set +o pipefail
yes | "$manager" --sdk_root="$sdk" --licenses >/dev/null
license_status=${PIPESTATUS[1]}
set -o pipefail
[[ "$license_status" -eq 0 ]]
"$manager" --sdk_root="$sdk" 'platform-tools' 'platforms;android-36' 'build-tools;35.0.0'
printf 'sdk.dir=%s\n' "$sdk" > "$root/local.properties"
chmod +x "$root/gradlew"
cd "$root"
./gradlew --no-daemon --console=plain --version
echo 'Android build environment ready. Run bash tools/cloud/verify.sh'
