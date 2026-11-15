# Пункт 2.3: восстановление, идемпотентный save и предупреждения

13.11.2026, завершено около20:50МСК. **2.3 выполнен; пауза перед2.4.** Результаты/SHA (материал отсутствует в этой копии), native log (материал отсутствует в этой копии), три core contracts (материал отсутствует в этой копии), core run (материал отсутствует в этой копии), очистка тестового индекса (материал отсутствует в этой копии).

## Проверка на A51

Свежие REA pulls до/после подтвердили неизменные установленные APK: app candidate11 SHA256 `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`, test cache-contract-tests-v2 SHA256 `950213f0a35336bd1766cfed2d9ee30da277c625ac5e3acbdf25ad154eff9ed1`. Новых установок, Android-сборок, правок кода, clear/uninstall или изменений телефона не было. 73 выбранных source/config/tools файла соответствуют финалу2.2; recovery test source не изменялся.

Запущен только метод `MixedDocumentDeviceTest#aDamagedOwnedProjectShowsItsLocalizedRecoveryWarningWithoutChangingOtherDocuments`. 20:43:33.707–20:43:52.182МСК: **OK(1 test)**, без skip/failure, ADBexit0,status1→0,runner15,242с/host18,475с. Итоговый instrumentation code -1 — штатный успех.

| Сценарий | Результат |
|---|---|
| RU: усечённый document.json в собственном fixture | Загружен ожидаемый last-good, документ совпал полностью; локализованное предупреждение найдено в Compose semantics |
| EN: отсутствующий document.json в другом собственном fixture | Тот же контракт восстановления и английского предупреждения прошёл |
| Repair после flushSaves | Current и last-good декодируются в ожидаемый Document |
| Повторное открытие без изменений | Повторное предупреждение отсутствует |
| Защита чужих проектов | @After проверил SHA всех прежних document/last-good; итоговый host контроль также сверил ресурсы, metadata, prefs и индексы |

Повреждались только созданные тестом документы32×24. Сначала fixture выведен из active selection и очередь сохранений завершена; реальные документы не повреждались. RU+damaged и EN+missing — два конкретных сочетания, не вся матрица повреждений и локалей. Скриншоты не делались: после теста восстанавливается прежняя пользовательская работа. Проверка предупреждений основана на реальном Compose UI, а не только строковой функции.

Команда; `$serial` — текущий A51 из REA list_adb_devices:

```powershell
$adbTask='<local-private-path>'
& $adbTask -s $serial shell am instrument -w -r -e class 'lab.sf7b.MixedDocumentDeviceTest#aDamagedOwnedProjectShowsItsLocalizedRecoveryWarningWithoutChangingOtherDocuments' lab.sf7b.test/androidx.test.runner.AndroidJUnitRunner
```

## Свежие host-контракты сохранения

Одним отдельным Gradle-запуском выполнены три существующих EditorContractTest:

- `unchangedSaveKeepsActualRecoveryBytesAndModificationMetadata`: три повторных сохранения сохраняют точные current/last-good, metadata/index, mtime, updatedAt/revision и прежний recovery Document.
- `saveOfRecoveredDocumentRepairsTheDamagedCurrentFileEvenWhenItsModelIsUnchanged`: повторное сохранение восстановленной модели действительно исправляет повреждённый current; после открытия recovery-флаг снят.
- `damagedCurrentManifestCannotOverwriteTheLastGoodRecoveryFile`: повторно повреждённый current не заменяет исправный last-good.

20:43:55–20:44:10МСК, BUILD SUCCESSFUL in14s, exit0; свежий XML20:44:08UTC+3: **3passed/0skipped/0failures/0errors**, суммарное время тестов1,687с. Это JVM-проверки байтов/metadata, отдельно от A51 UI. Старые70pass/1skip из1.3 не выдаются за новый полный запуск. В публичном XML опущен необязательный hostname; SHA исходного XML сохранён в result.json, исходник — private.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\gradlew.bat '-Psf7bBuildRoot=<local-private-path>' --no-daemon --max-workers=1 --no-parallel '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8 -XX:ActiveProcessorCount=2' '-Pkotlin.compiler.execution.strategy=in-process' :core:test --tests lab.sf7b.core.EditorContractTest.unchangedSaveKeepsActualRecoveryBytesAndModificationMetadata --tests lab.sf7b.core.EditorContractTest.saveOfRecoveredDocumentRepairsTheDamagedCurrentFileEvenWhenItsModelIsUnchanged --tests lab.sf7b.core.EditorContractTest.damagedCurrentManifestCannotOverwriteTheLastGoodRecoveryFile
```

## Данные до и после, очистка следов теста

Pre-test private backup20:43:10МСК: `<local-private-path>`,44793344байта,SHA256 `3d2d189398a284677b8321cc2ea4c9b55340cbe5b4457dc00015a8137f33d846`. Он побайтно совпал с итогом2.2; все52файла распакованы изолированно и совпали с повторным чтением телефона.

Сразу после native **50/52** прежних файлов совпали, потерянных/добавленных0. Изменились device-info.txt и library-last-good.json. Current library.json, три пользовательских UUID/activeId/порядок, все прежние project manifests/last-good, assets, metadata, legacy workspace и prefs совпали побайтно. @After удалил все свои проекты, но штатное обновление backup index при удалении оставило там metadata последнего уже удалённого fixture (3user+1fixture); пользовательские записи сохранились.

Этот след теста удалён отдельно и явно: при остановленном lab.sf7b повторно проверены все52файла post-snapshot, затем **только library-last-good.json** возвращён к точным pre-test bytes через проверенный временный файл и rename в той же папке. Документы, assets, prefs, current index, APK не восстанавливались и не заменялись. Это cleanup собственного теста, не доказательство автоматического сохранения резервного индекса без вмешательства.

Первая staging-команда `adb exec-in/run-as/dd` передала512из605байт; проверка заблокировала rename, целевой индекс не менялся. Вариант `shell -T/run-as/sh/cat` передал605байт; проверка совпадения выполнена до rename. Свой неудачный temp после проверки удалён. Диагностические копии `20261113-2045-task2.3-post-test` и `20261113-2047-task2.3-final` содержат промежуточное состояние, последняя — временный файл; их нельзя считать финальным checkpoint.

**Финальная проверенная копия**20:47:04МСК: `20261113-2047-task2.3-final-verified`,44793344байта,SHA256 `dd7feac628d009c596049ea46fbcd5cbbed64ae7e0fd98a9a17c35217007b795`. Все52файла совпали с повторными SHA телефона; **51/52** прежних файлов совпали с pre-test, единственное отличие — служебный device-info.txt. Оба library indexes, UUID/selection, все пользовательские manifests/resources/prefs побайтно прежние. Новых/пропавших файлов0, временных файлов cleanup не осталось. App остановлено, full private archive на телефон не восстанавливался; личные имена/UUID/изображения не публиковались. Копии/распаковки/полные REA Evidence исключать из поставки.

## Следующий шаг

OpenSpec **7/43**. Только2.3 завершён. Пауза по правилу пользователя. Следующий после отдельного возобновления —2.4: затронутые import/GPU/UI контракты и свежие screenshots точной сборки, с проверкой A51/актуального backup. 2.5 общие отчёты, process death, вся матрица миграций и полноразмерные сценарии остаются будущими задачами.
