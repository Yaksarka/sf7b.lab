param([string]$SdkRoot)
$ErrorActionPreference = 'Stop'
if (!$env:JAVA_HOME -or !(Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) { throw 'Set JAVA_HOME to JDK 17 before setup.' }
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$taskJavaVersion = & (Join-Path $env:JAVA_HOME 'bin\java.exe') -version 2>&1 | Out-String
if ($taskJavaVersion -notmatch 'version "17\.') { throw 'JDK 17 is required.' }
$taskSdk = if ($SdkRoot) { $SdkRoot } elseif ($env:ANDROID_HOME) { $env:ANDROID_HOME } elseif ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$taskSdk = [System.IO.Path]::GetFullPath($taskSdk)
$taskDownloads = Join-Path $env:TEMP 'sf7b-android-setup'
New-Item -ItemType Directory -Force -Path $taskDownloads,$taskSdk | Out-Null
$taskArchive = Join-Path $taskDownloads 'commandline.zip'
if (!(Test-Path -LiteralPath (Join-Path $taskSdk 'cmdline-tools\latest\bin\sdkmanager.bat'))) {
    Invoke-WebRequest 'https://dl.google.com/android/repository/commandlinetools-win-15859902_latest.zip' -OutFile $taskArchive
    $taskHash = (Get-FileHash -LiteralPath $taskArchive -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($taskHash -ne '90ae805d20434428bffcb699c290860f19bb5f66a67e6b330067e3de801fb04a') { throw 'SDK archive checksum mismatch' }
    Expand-Archive -LiteralPath $taskArchive -DestinationPath (Join-Path $taskDownloads 'unpacked') -Force
    New-Item -ItemType Directory -Force -Path (Join-Path $taskSdk 'cmdline-tools') | Out-Null
    Move-Item -LiteralPath (Join-Path $taskDownloads 'unpacked\cmdline-tools') -Destination (Join-Path $taskSdk 'cmdline-tools\latest')
}
$taskManager = Join-Path $taskSdk 'cmdline-tools\latest\bin\sdkmanager.bat'
1..20 | ForEach-Object { 'y' } | & $taskManager "--sdk_root=$taskSdk" --licenses
& $taskManager "--sdk_root=$taskSdk" 'platform-tools' 'platforms;android-36' 'build-tools;35.0.0'
if ($LASTEXITCODE -ne 0) { throw 'SDK installation failed' }
$taskProject = Split-Path -Parent $PSScriptRoot
$taskSdkProperty = $taskSdk.Replace('\','/').Replace(':','\:')
"sdk.dir=$taskSdkProperty" | Set-Content -LiteralPath (Join-Path $taskProject 'local.properties') -Encoding UTF8
Write-Output "SDK ready: $taskSdk"
