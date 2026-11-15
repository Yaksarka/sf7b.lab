> Исторический журнал/постановка. Действующие разрешения и точка остановки — [AGENTS](../AGENTS.md) и [STATE](../STATE.md). Архив не отменяет паузу разработки.

# Project storage — checkpoint

13.11.2026, блок после candidate10. Разработка активна; эти новые изменения ещё не собраны и не запущены на A51. Gradle/JADX/ADB выполняет только root.

- Прочитаны актуальные AGENTS/RTK/CONTEXT/STATE и karpathy-guidelines. .codegraph отсутствует.
- Recovery раньше оставался в английском `ProjectLibrary.recoveryMessages`, а EditorModel его не показывал. Добавлены typed RecoveryKind/RecoveryNotice с сохранением старого строкового API; новые предупреждения переводятся RU/EN и передаются существующему Snackbar через message при startup/после операции. Повторное чтение одного notice не создаёт уведомления заново. EditorApp не менялся.
- Обнаружена потеря полезной recovery-копии при повторном unchanged flush: save ротировал last-good и менял modified/revision даже без изменения документа. Исправлены idempotent ProjectFiles.save и ProjectLibrary.save. Повреждённый current с валидным backup всё равно ремонтируется при save, даже если загруженная модель равна сохраняемой. Добавлены два host regression-теста в EditorContractTest с проверкой actual disk bytes/metadata, а не только памяти.
- MixedDocumentDeviceTest готов: изолированный native-документ 192×144 с 15 слоями (по 3 image/generator/text/drawing/solid), масками, seed, TTF и ветвью; crop/turn даёт PNG 130×154. Проверяются exact premultiplied pixels/PNG bytes после удаления внешних исходников, независимая копия с изменённым seed, перенос в отдельную библиотеку и reopen новым экземпляром. Это небольшой тест корректности, не 15-слойный benchmark 2000px.
- Отдельный UI-тест создаёт только свои UUID: повреждение current (RU) и потеря current (EN), fallback к известному backup, реальные локализованные Snackbar, ремонт current при flush, отсутствие повторного предупреждения. До/после проверяются SHA всех document/last-good существовавших проектов. Фикстуры не используют личные файлы: собственный RGBA PNG и системный TTF; prefs/selection восстанавливаются, при неуспешном setup cleanup не проводится.

Код заморожен для общей сборки candidate11. Централизованно проверить host-класс `lab.sf7b.core.EditorContractTest` и native-класс `lab.sf7b.MixedDocumentDeviceTest` (два сценария). Текущие результаты пока pending: наличие тестов не означает их прохождение. ADB/Gradle этим агентом не запускались.
