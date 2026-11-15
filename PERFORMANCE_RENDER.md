# Производительность рендера — 13.11.2026

Новая архитектура использует отдельный decode-cache рабочего разрешения, ограниченные RAM-плоскости с spill на диск, identity/neutral fast paths и фильтрацию PNG Sub/Up. Исходники и разрешение экспорта сохраняются.

JVM-проверка `GraphicsTest.residentAndTiledRasterPathsMatchAndRecordHostTimings` сравнивает одинаковую цепочку RGB split → nonneutral adjust, 1024×1024, по три запуска в resident-plane и tiled режимах одной новой реализации. CSV: `%LOCALAPPDATA%\sf7b-lab-build\core\verification\raster-path-host.csv`. Это сравнение путей хранения, а не замер старого APK против нового, и не показатель Galaxy A51.

Этот файл сохраняет host-замер раннего этапа. Актуальные реальные A51-метрики и ограничения: [PERFORMANCE.md](PERFORMANCE.md). Полный экспорт >16MP пока использует CPU/region decode и дисковые плоскости.

## Фактический JVM замер 13.11.2026, 07:12 МСК

Одинаковая цепочка RGB split→nonneutral adjust,1024×1024; по3 запуска:

| Путь | Прогоны,с | Медиана,с |
|---|---|---|
| tile-cache без RAM-плоскостей |0.476207;0.4885176;0.4747571|0.476207|
| RAM-плоскости с ограниченным spill и25%reserve подtile-cache |0.2950871;0.2967683;0.2968233|0.2967683|

Отношение медиан≈1.60. Замер охватывает render+readRows и исключает исходное декодирование, PNG, закрытиеpool/flush, UI и устройство. Это не end-to-end preview, не старый APK против нового и не метрика A51. Пиксели путей совпали в отдельной фикстуре.

Команда общего v2 build (один процесс, результатapp/Lint отдельно влоге):

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\gradlew.bat '-Psf7bBuildRoot=<local-private-path>' '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process' --no-daemon --max-workers=1 --no-parallel --console=plain :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

Лог исторической сборки: `current-v2-build.log`. Позднее пользователь подключил A51, GPU-паритет5/5 выполнен на candidate3, GPU preview включён в candidate4. Полный документ/экспорт и v2 эффекты не подменяются legacy-алгоритмом1. Следующие итоги ведутся в PERFORMANCE.md и каноническом STATE.
