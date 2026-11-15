param([switch]$Tests, [switch]$DeviceTests, [string]$BuildRoot)
$ErrorActionPreference='Stop'
$taskRoot=Split-Path -Parent $PSScriptRoot
if (!(Test-Path Env:JAVA_HOME)) {
    $taskCandidates=@('C:\Program Files\Android\Android Studio\jbr','C:\Program Files\Java\jdk-17')
    foreach($taskJava in $taskCandidates) { if(Test-Path -LiteralPath (Join-Path $taskJava 'bin\java.exe')){$env:JAVA_HOME=$taskJava;break} }
}
if (!$env:JAVA_HOME) { throw 'Set JAVA_HOME to JDK 17.' }
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$taskJavaVersion = & (Join-Path $env:JAVA_HOME 'bin\java.exe') -version 2>&1 | Out-String
if ($taskJavaVersion -notmatch 'version "17\.') { throw 'JDK 17 is required.' }
$taskOutput=if($BuildRoot){[System.IO.Path]::GetFullPath($BuildRoot)}else{Join-Path $env:LOCALAPPDATA ('sf7b-lab-build-'+(Split-Path $taskRoot -Leaf))}
$taskArgs=@('-p',$taskRoot,"-Psf7bBuildRoot=$($taskOutput.Replace('\','/'))",'--no-daemon','--console=plain',':app:assembleDebug')
if($Tests){$taskArgs+=':core:test';$taskArgs+=':app:assembleDebugAndroidTest';$taskArgs+=':app:lintDebug'}
if($DeviceTests){$taskArgs+=':app:connectedDebugAndroidTest'}
& (Join-Path $taskRoot 'gradlew.bat') @taskArgs
if($LASTEXITCODE -ne 0){throw 'Сборка не прошла. Сообщение об ошибке находится выше.'}
$taskArtifacts=Join-Path $taskRoot 'artifacts'
New-Item -ItemType Directory -Force -Path $taskArtifacts | Out-Null
Copy-Item -LiteralPath (Join-Path $taskOutput 'app\outputs\apk\debug\app-debug.apk') -Destination (Join-Path $taskArtifacts 'sf7b-lab-debug.apk') -Force
Write-Output "APK: $taskArtifacts\sf7b-lab-debug.apk"
