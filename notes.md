# Журнал перенесённого проекта

## 15.11.2026 — перенос в отдельный private GitHub-репозиторий

Пользователь разрешил выполнить весь перенос самостоятельно. Собрана отдельная копия из source Android-проекта и Desktop OpenSpec/checkpoints. Исходные .git и история чатов не импортированы. Подготовлены README, AGENTS, STATE/CONTEXT, handoff, signing/private-materials и migration docs.

Сохранены исходные ограничения и пауза 3.4 (12/43). Продуктовые алгоритмы не менялись. Телефон/ADB/REA не использовались; подпись установленных APK, данные телефона и восстановление временных настроек заново не проверялись.

Исторические журналы: [Desktop](docs/history/DESKTOP_NOTES_20261115.md), [Source](docs/history/SOURCE_NOTES_20261115.md). Результаты текущих проверок: [MIGRATION_VALIDATION](docs/verification/MIGRATION_VALIDATION.md).


Публикация и свежий GitHub clone проверены: Windows build helper -Tests успешен, core70pass/1skip, lint0errors/17warnings (первая подготовленная копия:19warnings). OpenSpec strict validation прошла. A51/эмулятор не использовались. Завершающие коммиты меняют только документацию/план/отчёты.

## Продолжение по поручению владельца

Подтверждена публичная видимость. Согласованы требования по видео и исторические указатели 3.1; возобновление отражено в STATE/CONTEXT. Подготовлены JDK17/SDK36, выполнена успешная Linux-сборка с core70passed/1skipped и Lint. Телефон не подключён, 3.4 остаётся открыт. Отчёт: docs/verification/RESUME_CHECKLIST.md.

## 15.11.2026 — A51 в каноническом checkout
Remote/defaultbranch проверены;ff-only до7bb9645,новаяветкаcodex/task-3-4-runtime. Продолжается3.4,12/43. REA/A51доступны,ownPNGзагружен;crop/eraser/AOEобходначат. Свежийbackupиmetadata-onlycompare без потерьпрежнихdocuments/assets;новыйпроектнеисследован. ВременныеGlitchпанелипокаon,обязательновосстановитьoff. [Промежуточныйотчёт](docs/verification/A51_TASK3_4_RESUME_20261115.md).
