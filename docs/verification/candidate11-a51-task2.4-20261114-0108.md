# Пункт 2.4: import/GPU/UI и свежие экраны A51

14.11.2026, checkpoint около01:22МСК. **2.4 выполнен; пауза перед2.5.** Результаты/SHA (материал отсутствует в этой копии), итоговый native log (материал отсутствует в этой копии), манифест снимков с датами/сборкой (материал отсутствует в этой копии), контактный лист (материал отсутствует в этой копии). Оригинальные PNG в screenshots/; контактный лист — производный обзор, исходные кадры не редактировались.

## Результат и границы

На A51 **20/20 выбранных native tests прошли, без skip/failure**; дополнительно повторён один тест тем/языков для чистых кадров. Это20различных контрактов, не21различный тест. Production-код и app APK candidate11 не менялись. Исправлен только test helper захвата экрана, чтобы семантически готовое UI успело отрисоваться перед uiAutomation.takeScreenshot.

| Класс | Пройдено | Охват |
|---|---:|---|
| ImportColorTest | 7 | PNG/JPEG/WebP без расширения, sRGB gray→PNG, P3→sRGB,16-bit RGBA alpha, adaptive EXIF/original bytes, изоляция shared decode cache и eviction |
| GpuRenderTest | 5 | Identity exact, цвет/curves и цепочка в допуске, Bloom legacy/v2/neutral/alpha, fallback до загрузки sources |
| GpuChainTest | 1 | Две цепочки,7префиксов,2000×64, сравнение всех premultiplied каналов CPU/GPU |
| EditorUiTest | 7 | Поиск/Apply/Undo, numeric comma/Cancel, text/activity recreation, независимая copy/seed, замена изображения/Undo/Redo, полный GPU PNG, RU/EN×dark/light |

Итоговый запуск на новом test APK: **01:13:41.617–01:14:54.893МСК**, ADBexit0,status1→0 для каждого из20,host73,276с. Дополнительный capture-only запуск: **01:16:22.363–01:16:51.486МСК**, OK(1),host29,123с. Итоговый instrumentation code -1 — штатный успех Android runner. Точные device epoch и UTC интервалы сохранены в JSON.

Команда полного набора из Android-source; `$serial` — текущий A51 из REA list_adb_devices:

```powershell
$adbTask='<local-private-path>'
& $adbTask -s $serial shell am instrument -w -r -e class 'lab.sf7b.ImportColorTest,lab.sf7b.GpuRenderTest,lab.sf7b.GpuChainTest,lab.sf7b.EditorUiTest' lab.sf7b.test/androidx.test.runner.AndroidJUnitRunner
```

Capture-only selector: `lab.sf7b.EditorUiTest#bothThemesAndLanguagesRenderEditorAndSettings` с тем же runner.

## Снимки и визуальная проверка

Сохранены9сырых PNG: editor/settings RU/EN×dark/light и numeric-keyboard-ru. Все рассмотрены на контактном листе: Settings/Настройки действительно видны, язык/тема соответствуют файлам; numeric поле с0,070 и Samsung IME видны, поле не перекрыто. Это проверка данных экранов, не полный UX/a11y аудит всего каталога.

Для каждого файла манифест содержит SHA256, размер/разрешение, device mtime в epoch и Москве, producer test/интервал и точные app/test APK SHA. Каждый mtime проверен внутри соответствующего успешного producer-run. Восемь theme/settings PNG относятся к capture-only запуску, numeric PNG и два GPU-отчёта — к полному20-test запуску. Названия папок приблизительные; фактические времена брать из манифестов.

Предыдущие попытки сохранены и разделены:

1. Первичный запуск оборвался по ADB transport во время UI; получены14успешных методовых статусов, общего итога нет. Связь восстановилась сама; новых concurrent instrumentation не запускалось. Его log/partial result не считаются полным проходом.
2. Полный повтор на старом test APK прошёл20/20. Но визуальная проверка показала, что settings PNG захватили прежний кадр редактора; эти кадры оставлены отдельно в attempt-2 и не считаются финальными экранами настроек.
3. В EditorUiTest.screenshot добавлены waitForIdleSync и350мс ожидания реального кадра. После test-only сборки ещё раз прошли все20. Настройки видны правильно, но на один PNG попал посторонний системный баннер; этот оригинал оставлен только в private TAR.
4. Повторён только тест тем/языков; восемь кадров пересняты без баннера и повторно визуально проверены. Пиксели исходных PNG не правились ради результата.

## Точная сборка и измерения

App candidate11: SHA256 `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`,12621793байта. Он не пересобирался и не переустанавливался. Свежие REA pulls до/после подтвердили те же байты.

Новый test-only APK: sf7b-lab-0.2.0-candidate11-ui-capture-tests.apk (материал отсутствует в этой копии),1177921байт,SHA256 `52f98f1ec63a869cee603c413edc555833f5f811861c0237485717567536b12f`. Apksigner v2 verified, certSHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`, как прежде. Установлен только test package через REA install(replace=true), без clear/uninstall. Старые test APK сохранены отдельно; текущий установленный — ui-capture-tests. Манифест APK (материал отсутствует в этой копии) связывает app/test/source/result SHA.

Единственная test-only сборка: BUILD SUCCESSFUL in4m2s,exit0,54tasks/4executed. Журнал (материал отсутствует в этой копии), metadata (материал отсутствует в этой копии), delta теста (материал отсутствует в этой копии). Из73source/config/tools файлов71совпали с checkpoint1.2; два отличия — MixedPreviewCacheDeviceTest из2.2 и новый helper EditorUiTest. Production-код прежний.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\gradlew.bat '-Psf7bBuildRoot=<local-private-path>' --no-daemon --max-workers=1 --no-parallel '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8 -XX:ActiveProcessorCount=2' '-Pkotlin.compiler.execution.strategy=in-process' :app:assembleDebugAndroidTest
```

[GPU chain CSV](candidate11-a51-task2.4-20261114-0108/gpu-chain-prefix-parity.csv): все7префиксов прошли, максимальное наблюдённое отклонение1уровень канала, over3=0. Этот максимум относится именно к fixture2000×64. Отдельные GpuRenderTest используют свои допуски0/2/3; точные максимумы каждого из них не выводятся.

[Model export report](candidate11-a51-task2.4-20261114-0108/model-full-export-performance.txt): GPU,PNG2000×2000,alpha128,**3225мс**, редактируемый Document не изменился. Один прогон, включает queue wait/original load/render/PNG/file URI delivery, исключает системный picker. Не медиана, не полноразмерный15-layer benchmark. Activity recreation не равна Android process death. Новые core suite/Lint/эмулятор не запускались; их прежние результаты учитывать отдельно. Общие TEST_REPORT/PERFORMANCE и свод ограничений относятся к2.5.

## Сохранность

Первая копия01:00:24МСК совпала с финалом2.3, но последующее чтение обнаружило изменённый activeId и device-info при запущенном приложении; entries/metadata проектов совпали. Для тестов использована **новая согласованная** private копия: `<local-private-path>`, фактически01:02:12МСК,44793344байта,SHA256 `97bde4446096090ee0a695f4a8974bad809832cb4330ea2de6dab03ae3af787a`. Все52файла распакованы изолированно и совпали с телефоном. Старый activeId из2.3 поверх нового выбора не возвращался.

После всех запусков current library.json/selection/UUID/порядок, прежние документы/last-good/assets/metadata/legacy/prefs совпали с этим baseline. Ожидаемо изменены собственные test-results и device-info/profileInstalled. Backup index после удаления своих fixtures содержал metadata последнего уже удалённого test UUID; при остановленном app все52post-файла повторно проверены, затем **только library-last-good.json** возвращён к точным pre-test bytes через проверенный temp/rename. Cleanup явно отделён от поведения продукта; личные документы/assets/prefs/current index не заменялись.

**Финальная проверенная private копия**01:18:29МСК: `20261114-0118-task2.4-final-verified`,44774912байт,SHA256 `3cde3d23a13261998a86962f7331e4729ed0c0a7932f45f299200b9815f7cc0e`. Все52файла повторно сверены с телефоном; потерянных/добавленных0. Из52прежних файлов40побайтно прежние,10собственных test-report файлов обновлены,2служебных файла изменились. Все пользовательские данные и оба library indexes побайтно сохранны. Lab.sf7b остановлено. Private TAR/распаковки/полные Evidence и диагностические промежуточные копии не включать в поставку.

**OpenSpec8/43. Пауза перед2.5.** После отдельного возобновления внести факты и ограничения2.1–2.4 в TEST_REPORT/PERFORMANCE/STATE, сохранив границы fixtures, host/A51/GPU/визуальной проверки. Полнота каталога, все UI-маршруты, process death и все цели скорости не объявлены проверенными.
