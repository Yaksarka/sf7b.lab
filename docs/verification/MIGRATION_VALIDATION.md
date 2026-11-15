# Проверка переноса — 15.11.2026

Windows x64; Oracle JDK 17.0.12, Gradle 8.13, AGP 8.13.2, Kotlin 2.2.21, SDK Platform36/Build-Tools35.0.0. SDK установлен официальным Windows setup с проверкой SHA256 архива.

Команда из чистой подготовленной копии:

~~~powershell
gradlew.bat --no-daemon --console=plain :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
~~~

**BUILD SUCCESSFUL**, 8m21s, 83 задачи выполнены. Core: 70 passed, 1 skipped, 0 failures, 0 errors. Lint: 0 errors, 19 warnings. Собраны app APK и Android test APK; тесты на устройстве не запускались.

[Машиночитаемая сводка, suites, lint IDs и APK SHA256](migration-20261115/build-summary.json). APK/сырые build logs не включены в Git.

[Контроль исходников](migration-20261115/integrity.json): все 178 Desktop и 234 source файла исходного аудита неизменны. Ни одного app/core/Gradle source не пропущено; от source отличаются только внешняя signing-конфигурация и независимый от Windows-фотографии host fixture. Более свежий Android screenshot helper сохранён из source.

Состав проверен на секреты/ключи, персональные пути, device serial и запрещённые raw-артефакты; обнаружений в итоговой копии нет. Markdown-ссылки на имеющиеся файлы относительные; ссылки на исключённые материалы превращены в текст с оговоркой. OpenSpec12/43, 3.4 открыт, 3.5 не начат.

Bash-скрипты проверяются синтаксически, PowerShell scripts разобраны штатным parser. Linux workflow подготовлен для ручного запуска; его успешное выполнение не заявляется. A51, эмулятор, GL pixels, исходные приложения, подпись установленного APK и данные телефона заново не проверялись.

## Свежий GitHub clone

Новый private-репозиторий создан через GitHub API (201), push и clone успешны. GitHub-плагин подтвердил private-видимость, владельца Yaksarka и права admin/push/pull текущего аккаунта. Default branch: codex/project-transfer.

Свежий clone revision fe8f47a41c3655b01e8f9f3bbd84b5fc56fd4aa0: git fsck --full успешен, git status чистый. Документированная Windows-команда tools/build-local.ps1 -Tests завершилась BUILD SUCCESSFUL за 7m 22s; 70 core passed, 1 skipped, 0 failures, 0 errors; lint 0 errors/17 warnings. App и test APK собраны из этого clone в новом внешнем build root, без файлов исходных папок.

[Сводка clone и SHA256](migration-20261115/clone-summary.json). Последующий коммит завершения переноса содержит только документацию/OpenSpec и отчёты; app/core/Gradle/tools не изменяются относительно проверенного revision. OpenSpec strict validation успешна, 12/43 сохранены.

Другой аккаунт и внешние ИИ-сервисы не подключались: их логины/названия не предоставлены. Порядок доступа описан в [MIGRATION](../MIGRATION.md).

У собранного app APK проверены разрешения через SDK aapt: разрешение INTERNET отсутствует. Это проверка manifest, а не новый тест runtime offline-first на A51.
