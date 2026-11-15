> Архивный документ. Действующие инструкции: [AGENTS.md](../../AGENTS.md). Актуальное состояние: [STATE.md](../../STATE.md). Исторические пути/артефакты могут быть недоступны.

# Состояние sf7b.lab

13.11.2026 20:50МСК: **2.3 выполнен;OpenSpec7/43,пауза перед2.4.** Recovery native OK(1),безskip/failure:RU damaged/EN missing current,last-good repair,локализованные warning/no-repeat. Свежие3host EditorContractTest прошли идемпотентность bytes/mtime/revision/index,repair и защиту last-good. Generic coreXML теперь3filtered tests;исторические70pass/1skip1.3 сохранены отдельно. Исходники/APK в2.3 не менялись,app/testSHA подтверждены REA pulls;lab.sf7b остановлено.

Все user documents/last-good/assets/metadata/prefs/UUID/оба library indexes побайтно прежние. После удаления своих fixtures тест оставил1test UUID в backup index;только этот файл при остановленном app возвращён к проверенным pre-test bytes через проверенный temp/rename. Итог51/52 прежних файлов совпали,толькоdevice-info отличается,новых/потерянных0. Финальный private backup20261113-2047-task2.3-final-verified20:47:04МСК,SHA dd7feac628d009c596049ea46fbcd5cbbed64ae7e0fd98a9a17c35217007b795,все52файла сверены с телефоном. Промежуточные копии диагностические,private данные вне поставки. Отчёт2.3 (материал отсутствует в этой копии), JSON (материал отсутствует в этой копии). Следующий только2.4 после отдельного возобновления:import/GPU/UI contracts и свежие screenshots с проверкой A51/backup.

Канонический [STATE](../../STATE.md), [notes](../../notes.md). Ниже историческое состояние2.2.

13.11.2026 20:40МСК: **2.2 выполнен; OpenSpec6/43, пауза перед2.3.** Итоговый MixedPreviewCacheDeviceTest OK(2),безskip/failure:15-layer blend и двухслойные source/mask/early-effect контракты, exact pixels/provider calls/invalidation. Изменён только этот test source, production/app candidate11 прежний. Первая попытка выявила ошибку head в новом fixture; исправлена в тесте, оба запуска сохранены. Установлен только test APK `artifacts/sf7b-lab-0.2.0-candidate11-cache-contract-tests-v2.apk`,SHA950213f0a35336bd1766cfed2d9ee30da277c625ac5e3acbdf25ad154eff9ed1,прежняя подпись. AppSHA8fb06764…1a500 подтверждён REA pull.

Все50прежних private файлов сохранились побайтно, включая оба library indexes иprefs; post backup52файла повторно сверены, добавлены только2CSV. Lab.sf7b остановлено. Cold15-layer15,4с/warm blend219мс — одиночные CPU preview512² на документе2000², не полноразмерный/performance benchmark. Отчёт2.2 (материал отсутствует в этой копии), JSON (материал отсутствует в этой копии). Private копии вне поставки. Следующий после отдельного возобновления только2.3 recovery/save/RU-EN warnings с проверкой A51/backup. 2.4/2.5 pending.

Канонический [STATE](../../STATE.md), [notes](../../notes.md). Ниже историческое состояние2.1.

13.11.2026 20:23МСК: **2.1 выполнен; пауза перед 2.2. OpenSpec5/43.** Candidate11 app/testAPK установлены на A51 обновлением без clear/uninstall, точные SHA подтверждены REA pulls. Один 15-layer метод MixedDocumentDeviceTest прошёл **OK(1),безskip**: 15слоёв/50узлов,TTF/seed/crop/portable Document/ресурсы/точныеpixels/PNG130×154 alpha. Canvas192×144,CPU3921/2955мс,по1прогону;это не полноразмерный15-layer benchmark/process-death. Исходники/сборка не менялись.

Свежая private копия доupdate49файлов проверена;послеinstall49/49 совпали. После теста все прежние document/last-good/assets/prefs/current library/UUID сохранны,потерь0. Изменились device-info/profileInstalled и backup index (теперь точная копия исправного pre-current),46/49 прежних файлов неизменны;только1новый test-report. Post-test TAR50файлов повторно сверены. Полные пути/SHA/ограничения в отчёте2.1 (материал отсутствует в этой копии) и JSON (материал отсутствует в этой копии). Private backup/REA Evidence вне поставки. Lab.sf7b остановлено. Следующий после отдельного возобновления только2.2 MixedPreviewCacheDeviceTest с проверкой A51/актуальности backup;recovery2.3/import-GPU-UI2.4/отчёты2.5 pending.

Канонический [STATE](../../STATE.md), [notes](../../notes.md). Ниже история1.4,не текущий статус.

13.11.2026 20:10МСК: **1.4 завершён; пауза перед2.1**. OpenSpec4/43. Candidate11 app/test APK сохранены отдельно, SHA/сертификаты проверены и связаны с успешной сборкой1.3. Следующий2.1 — проверка A51/актуального private backup, update и MixedDocumentDeviceTest. Установки/native tests ещё не выполнялись.

App APK (материал отсутствует в этой копии),12621793bytes, SHA256 `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`. Test APK (материал отсутствует в этой копии),1173964bytes, SHA256 `05084940d2ec9827fd76562b31a0ed321b339b72fd484cfcb3aecc907f379a2a`. Оба apksigner verified v2, общий certSHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`, как candidate10. Манифест19evidence SHA (материал отсутствует в этой копии), SHA256SUMS (материал отсутствует в этой копии), отчёт1.4 (материал отсутствует в этой копии). Public PEM отдельно, private key/backup не включены.

Отчёт1.3 (материал отсутствует в этой копии), JSON результатов (материал отсутствует в этой копии), команда/время (материал отсутствует в этой копии):19:54:22–20:00:55МСК,6м32с/exit0; core70pass/1skip/0fail/error, app/testAPK собраны, Lint0errors/18warnings. Исторические result1.3 не редактировались. В1.4 новых сборок/тестов/обращений к телефону не было. Native/A51/Mixed tests/миграции остаются2.x.

Канонический checkpoint: [STATE.md](../../STATE.md).
Хронология: [notes.md](../../notes.md).

В1.2 A51 снова доступен: выгруженный установленный APK побайтно candidate10. Свежая private копия app data19:45:26МСК:44470272bytes, SHA256 `a7b3195b9b2f67d6821193a136761cf64383350b464016406ac2f2562b5c4727`; все44 файла восстановлены на хосте и совпали по SHA с повторным чтением устройства. Новый частный source checkpoint91файл: SHA256 `a0b2e7a2b02bd4c831d70045f83f6de671b6c0003782611899344464d7fee7c5`; отдельные docs23файла и private key. Старые копии также проверены. Копия ключа подписала изолированный probe; apksigner подтвердил прежний сертификат. Только force-stop lab.sf7b; install/clear/uninstall и изменения app data не выполнялись.

Отчёт/восстановление1.2 (материал отсутствует в этой копии), SHA/агрегаты (материал отсутствует в этой копии). Private checkpoint/backup/restore folders исключать из поставки. Android-открытие восстановленных проектов/миграции остаются2.x. Код Android не менялся; build/core/Lint проверены в1.3, native ещё pending. Среда1.1: manifest (материал отсутствует в этой копии); статус devices=[] в нём исторический.

Команда выполненной1.3 из этой папки (для воспроизводимости; сейчас не запускать повторно):

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\gradlew.bat '-Psf7bBuildRoot=<local-private-path>' --no-daemon --max-workers=1 --no-parallel '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8 -XX:ActiveProcessorCount=2' '-Pkotlin.compiler.execution.strategy=in-process' :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

Последняя наблюдавшаяся установленная/проверенная на A51 версия — candidate10 из1.2. Candidate11 собран, архив/SHA/cert/core/Lint подтверждены; новая установка и native/A512.x ещё впереди. Перед2.1 проверить, не менялись ли проекты после backup19:45:26МСК; при изменениях снять новую копию, не clear/uninstall.
