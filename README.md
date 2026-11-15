# sf7b.lab

Нативный офлайн Android-редактор изображений: Kotlin, Jetpack Compose, собственное ядро CPU и OpenGL ES. Целевое устройство — Samsung Galaxy A51; minSdk 26, compile/targetSdk 36. Приложение не требует сети, аккаунта, рекламы или водяных знаков. Интернет нужен для первоначальной загрузки инструментов сборки и зависимостей.

Проект находится в разработке. План выполнен на 12/43; пункт 3.4 открыт, 3.5 не начат. Полное покрытие One Lab/Glitch Lab, паритет и целевая скорость не подтверждены. Перенос репозитория не возобновляет остановленную разработку или опыты на телефоне.

## Начало работы

1. Клонируйте репозиторий через свой авторизованный GitHub-аккаунт.
2. Прочитайте [AGENTS.md](AGENTS.md), [STATE.md](STATE.md), [AI_HANDOFF](docs/AI_HANDOFF.md).
3. Установите JDK 17 и задайте JAVA_HOME на его каталог. Android SDK Platform 36 и Build-Tools 35.0.0 устанавливаются через SDK Manager или setup-скрипт ниже. Gradle 8.13 загружается Wrapper; AGP 8.13.2 и Kotlin 2.2.21 закреплены в конфигурации.
4. Укажите SDK через ANDROID_HOME/ANDROID_SDK_ROOT или локальный, не коммитящийся local.properties.

Windows PowerShell, из корня проекта:

~~~powershell
.\tools\setup-android.ps1
.\tools\build-local.ps1 -Tests
~~~

Linux (JDK 17, bash, curl, unzip):

~~~bash
bash tools/cloud/setup.sh
bash tools/cloud/verify.sh
~~~

Прямая проверка без установки/запуска приложения на телефоне:

~~~bash
./gradlew --no-daemon --console=plain :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
~~~

В Windows используйте gradlew.bat. При кириллице в пути JDK 17 используйте tools/build-local.ps1: вывод направляется в отдельную папку; при необходимости задайте ASCII-путь через -BuildRoot. На Linux выходы стандартные: app/build, core/build; скрипт verify.sh копирует APK в artifacts/. Результаты текущего переноса: [проверка](docs/verification/MIGRATION_VALIDATION.md).

## Подпись и данные

Обычная debug-сборка на новом компьютере получает локальную debug-подпись. Она может не обновить уже установленный APK. Для обновлений используйте прежний ключ вне репозитория: [SIGNING.md](docs/SIGNING.md). Не удаляйте установленное приложение и не очищайте данные ради установки; сначала отдельно сохраните редактируемые проекты.

## Документация

- [Инструкция редактора](README_RU.md), [требования](REQUIREMENTS.md), [архитектура](DESIGN.md), [формат проекта](PROJECT_FORMAT.md).
- [Исторические проверки](TEST_REPORT.md), [производительность](PERFORMANCE.md), [ограничения перенесённых доказательств](docs/PRIVATE_MATERIALS.md).
- [OpenSpec: 43 задачи](openspec/changes/complete-dual-app-functionality/tasks.md).
- [Перенос, доступ другого аккаунта и ИИ](docs/MIGRATION.md), [стартовый промпт](docs/NEXT_AI_PROMPT.md).

GitHub Actions подготовлен для ручного workflow_dispatch. Автоматические запуски выключены; платные услуги и настройки оплаты не подключались. Ручные запуски подчиняются лимитам и условиям аккаунта GitHub.

Лицензия на собственный проект владельцем пока не выбрана. Новая лицензия при переносе не добавлялась. Материалы оригинальных приложений и закрытый ключ не входят в репозиторий.

