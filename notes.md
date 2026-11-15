# Журнал перенесённого проекта

## 15.11.2026 — перенос в отдельный private GitHub-репозиторий

Пользователь разрешил выполнить весь перенос самостоятельно. Собрана отдельная копия из source Android-проекта и Desktop OpenSpec/checkpoints. Исходные .git и история чатов не импортированы. Подготовлены README, AGENTS, STATE/CONTEXT, handoff, signing/private-materials и migration docs.

Сохранены исходные ограничения и пауза 3.4 (12/43). Продуктовые алгоритмы не менялись. Телефон/ADB/REA не использовались; подпись установленных APK, данные телефона и восстановление временных настроек заново не проверялись.

Исторические журналы: [Desktop](docs/history/DESKTOP_NOTES_20261115.md), [Source](docs/history/SOURCE_NOTES_20261115.md). Результаты текущих проверок: [MIGRATION_VALIDATION](docs/verification/MIGRATION_VALIDATION.md).


Публикация и свежий GitHub clone проверены: Windows build helper -Tests успешен, core70pass/1skip, lint0errors/19warnings. OpenSpec strict validation прошла. A51/эмулятор не использовались. Завершающий коммит меняет только документацию/план/отчёты.
