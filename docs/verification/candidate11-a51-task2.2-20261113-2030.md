# Пункт 2.2: точность и обновление кэша на A51

13.11.2026, завершено около 20:39 МСК. **2.2 выполнен; пауза перед 2.3.** Результаты, метрики и SHA (материал отсутствует в этой копии), успешный instrumentation (материал отсутствует в этой копии), [15 слоёв](candidate11-a51-task2.2-20261113-2030/mixed-preview-cache.csv), [source/mask/early effect](candidate11-a51-task2.2-20261113-2030/mixed-preview-cache-invalidation.csv), изменение теста (материал отсутствует в этой копии).

## Изменение и сборки

У прежнего MixedPreviewCacheDeviceTest был только сценарий upper blend. Для полного охвата 2.2 добавлен второй изолированный тест с реальными AndroidSources/Renderer: baseline cold/uncached/warm, mask, early effect и замена PNG по тому же пути. Проверяются все 512×512 premultiplied пиксели, реальное изменение результата и число provider calls. Из 73 проверенных файлов source/config/tools изменён только `app/src/androidTest/java/lab/sf7b/MixedPreviewCacheDeviceTest.kt`; production-код, конфигурация и app APK неизменны.

Первая сборка test APK прошла 20:28:50–20:31:48 МСК (BUILD SUCCESSFUL in 2m56s). Первый запуск: исходный 15-layer тест прошёл, дополнительный упал на validate mask fixture: в withLayer передан исходный Layer с пустым head. Исправлена одна строка — слой берётся из doc.layer(text.id). Это ошибка добавленного теста, не сбой production cache. Первый запуск (материал отсутствует в этой копии) и промежуточный test APK сохранены; не считать его проверенным артефактом для поставки.

Повторная сборка 20:33:34–20:34:44 МСК: BUILD SUCCESSFUL in 1m9s, exit0, 54 tasks/4 executed. Выполнялась только `:app:assembleDebugAndroidTest`; core tests, app assemble и Lint повторно не запускались. Два запуска Gradle шли последовательно. Полные журналы обеих сборок лежат рядом с result.json.

Команда из Android-source:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\gradlew.bat '-Psf7bBuildRoot=<local-private-path>' --no-daemon --max-workers=1 --no-parallel '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8 -XX:ActiveProcessorCount=2' '-Pkotlin.compiler.execution.strategy=in-process' :app:assembleDebugAndroidTest
```

Проверенный test-only APK: `artifacts/sf7b-lab-0.2.0-candidate11-cache-contract-tests-v2.apk`, 1177824 байта, SHA256 `950213f0a35336bd1766cfed2d9ee30da277c625ac5e3acbdf25ad154eff9ed1`. Подпись apksigner v2 verified, certSHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`, как у candidate11. Манифест APK (материал отсутствует в этой копии) связывает исходник теста, app SHA и result.json. Старые APK 1.4 не заменены.

Через REA install(replace=true) установлен только test package. Свежие REA pulls до/после подтвердили: app остаётся candidate11 SHA `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`, test после обновления соответствует новому SHA. Clear/uninstall и изменения телефона не выполнялись.

## Проверка на A51

Команда; `$serial` — доступный A51 из REA list_adb_devices:

```powershell
$adbTask='<local-private-path>'
& $adbTask -s $serial shell am instrument -w -r -e class lab.sf7b.MixedPreviewCacheDeviceTest lab.sf7b.test/androidx.test.runner.AndroidJUnitRunner
```

Успешный запуск 20:35:18.705–20:36:00.960 МСК: **OK (2 tests)**, ADB exit0, statuses 1→0/1→0, runner39,21с, host42,255с, без skip/failure. Итоговый instrumentation code -1 — штатный успех Android runner.

| Проверка | Подтверждено |
|---|---|
| 15 смешанных слоёв, правка верхнего blend/opacity | Точные пиксели относительно uncached, результат действительно изменился; image/text calls 0/0, hits2/miss1 |
| Двухслойный baseline и повторный warm | Cold совпадает с uncached; warm совпадает с cold, providers0/0 |
| Маска верхнего text-слоя | Точные пиксели и изменившийся результат; providers0/0, hits2/misses2 |
| Ранний adjust перед верхним слоем | Точные пиксели и изменившийся результат; providers0/0, hits2/misses2 |
| Замена PNG по тому же пути | Старые derived/decode данные не использованы; image прочитан1раз, text0; точное совпадение с uncached и отличие от прежних пикселей |
| Повтор после замены PNG | Точные пиксели, providers0/0 |
| Бюджет 15-layer node cache | Не более64MiB |

Документы и PNG-источник 2000×2000; рендер предпросмотра 512×512, CPU. Для source replacement намеренно меняется версия ресурса (length/mtime); подмена с сохранением обоих полей не проверялась. Mask/early effect/source сценарии изолированы в двухслойном документе; их нельзя выдавать за полный 15-layer сценарий.

## Одиночные измерения и ограничения

15-layer cold graph+decode **15408,53мс**, warm upper blend **218,77мс**, uncached graph с уже прогретым decode **11381,32мс**. Cold имеет дополнительные затраты записи кэша и пока медленный. Двухслойные: mask410,89мс, early effect543,89мс, source replacement1277,60мс, warm после замены32,80мс.

Это одиночные debug-замеры CPU render, не медианы, не пользовательский интервал UI/экспорта и не сравнение с оригиналами. Полноразмерный 15-layer export, память/PSS, FPS и повторные cold/warm измерения остаются будущими задачами. Корректность кэша не означает выполнение всех целей скорости. Общие TEST_REPORT/PERFORMANCE обновляются по отдельному 2.5.

## Сохранность и checkpoint

До тестов private backup фактически20:26:49МСК: `<local-private-path>`, 44790272байта, SHA256 `944c04b116b74fe1a959dcac5b1f172dbaa04eb3c3e280fc38867e6ad15b5f61`. Он побайтно совпал с post-test копией2.1. Все50файлов восстановлены изолированно и совпали с повторным чтением телефона. После установки test APK, первой попытки и итогового запуска **50/50 прежних файлов побайтно неизменны**, включая документы, last-good, assets, оба library indexes и prefs.

После тестов остановлено только lab.sf7b. Post-test backup20:36:19МСК: папка `20261113-2036-task2.2-post-test`,44793344байта,SHA256 `3d2d189398a284677b8321cc2ea4c9b55340cbe5b4457dc00015a8137f33d846`. Проверены все52файла относительно TAR и повторных SHA телефона; потерянных/изменённых прежних файлов0. Добавлены только два CSV test-results. Частные данные/распаковки/полные REA Evidence вне исходников и поставки. Личные проекты не открывались, архивы на телефон не восстанавливались.

**OpenSpec6/43. Пауза перед2.3.** Следующий после отдельного возобновления — damaged/missing current, last-good, идемпотентный save и RU/EN warning/repair с защитой чужих UUID/manifests/prefs. Текущий установленный test APK — cache-contract-tests-v2, он также содержит прежние unchanged recovery tests; app остаётся candidate11. 2.3/2.4 не запускались.
