# Пункт 2.1: candidate11 на A51 и 15 смешанных слоёв

13.11.2026, завершено около 20:22 МСК. **2.1 выполнен; пауза перед 2.2.** Результаты и SHA доказательств (материал отсутствует в этой копии), instrumentation (материал отсутствует в этой копии), отчёт устройства (материал отсутствует в этой копии).

## Установка и идентичность

- A51 SM-A515F доступен через ADB 37.0.1. До обновления свежая REA-выгрузка установленного base.apk совпала с candidate10: SHA256 `065f2c85159e8a96102d665bdae7f49e32eb32355b65b996eabbb2672d9798ea`.
- Установлены сохранённые APK из пункта 1.4 через REA `install_adb_package(replace=true)`, эквивалент `adb install -r`. Clear/uninstall, новая сборка и изменения настроек телефона не выполнялись.
- App SHA256 `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`, 12621793 байта. Test SHA256 `05084940d2ec9827fd76562b31a0ed321b339b72fd484cfcb3aecc907f379a2a`, 1173964 байта. Повторные выгрузки обоих установленных APK подтвердили точные SHA и отсутствие splits.
- Перед установкой оба APK повторно прошли apksigner verify, v2, один signer; certSHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3` совпадает с candidate10. 73 выбранных файла source/config/tools совпали с checkpoint 1.2. Код не менялся.

## Проверка на телефоне

Запущен только метод `MixedDocumentDeviceTest#fifteenMixedLayersKeepTheirEditableResourcesMasksSeedsAndRenderedPngAcrossPortableCopies`. Второй метод этого класса проверяет recovery и относится к 2.3; он не запускался. MixedPreviewCacheDeviceTest относится к 2.2 и не запускался.

Команда из Android-source; `$serial` — серийный номер доступного A51 из REA list_adb_devices:

```powershell
$adbTask='<local-private-path>'
& $adbTask -s $serial shell am instrument -w -r -e class 'lab.sf7b.MixedDocumentDeviceTest#fifteenMixedLayersKeepTheirEditableResourcesMasksSeedsAndRenderedPngAcrossPortableCopies' lab.sf7b.test/androidx.test.runner.AndroidJUnitRunner
```

- 20:18:40.728–20:18:59.383 МСК, ADB exit 0, instrumentation statuses 1→0, итоговый code -1 (штатный успех Android runner), **OK (1 test)**, без skip/failure. Runner сообщил 14,88 с; полный host-интервал 18,655 с.
- Документ 192×144, 15 слоёв: по три image/generator/text/drawing/solid; 50 узлов, ветви, masks/blend/transforms, seed, встроенный системный TTF, crop и поворот. Итоговый PNG 130×154 с alpha.
- Исходный каталог ресурсов удалён до render; изображения и TTF сохранились внутри проекта. Правка seed дубликата не изменила оригинал. Portable export/import в отдельную библиотеку, удаление исходного проекта и повторное открытие новой библиотекой сохранили точный Document, SHA ресурсов, premultiplied пиксели и байты PNG.
- CPU AndroidSources/Renderer: исходный render 3921 мс, после переноса 2955 мс, по одному прогону. Это малый fixture, не полноразмерный benchmark 15 слоёв, не измерение GPU или скорости пользовательского экспорта. Повторное создание ProjectLibrary не является проверкой Android process death.

## Сохранность прежних данных

Перед обновлением остановлено только lab.sf7b и сделана новая private-копия в `<local-private-path>`. Фактическое время копии 20:16:55 МСК; метка папки приблизительная. TAR 44789248 байт, SHA256 `dd8ec2b0cf296bda54f661cc14ae0b5cd7d20a547898e9300f3abd670353998e`. Копия отличается от 19:45: 49 файлов, четыре document и четыре last-good (включая legacy), 13 JSON. Все файлы восстановлены в изолированную папку и совпали по SHA с повторным чтением телефона. Личные имена, UUID и содержимое не публиковались.

После установки все **49/49** прежних файлов сохранились побайтно. После теста приложение остановлено и снята отдельная post-test копия: `20261113-2021-task2.1-post-test`, фактическое время 20:20:10 МСК, 44790272 байта, SHA256 `944c04b116b74fe1a959dcac5b1f172dbaa04eb3c3e280fc38867e6ad15b5f61`. Все 50 файлов этой копии совпали с повторным чтением устройства.

После запуска **46/49** прежних файлов совпадают; пропавших нет. Все 16 файлов существующих проектов/ресурсов, девять файлов legacy workspace, document/last-good, current library.json и preferences XML сохранились побайтно. UUID, состав/порядок библиотеки и activeId не изменились. Единственное добавление — отчёт `files/test-results/mixed-document-15-layers.txt`; новые fixture-проекты не остались в пользовательской библиотеке.

Три изменения названы явно:

- `device-info.txt` перезаписан при запуске EditorModel; `profileInstalled` обновлён Android profile installer.
- `projects/library-last-good.json` теперь побайтно равен **исправному current library.json до запуска**; от прежнего резервного индекса отличаются только updatedAt/revision одной записи. `ProjectLibrary.recoverPublishedDirectories()` вызывает replaceIndex при инициализации, а writeJsonWithBackup обновляет резервную копию. Document/last-good каждого проекта не менялись. Нельзя утверждать, что все файлы private data неизменны. Поведение идемпотентного сохранения/восстановления отдельно проверяется в 2.3.

Частные TAR, восстановленные данные, списки файлов и полные REA Evidence хранятся вне исходников и поставки. На телефон архивы не восстанавливались, личные изображения визуально не открывались. Полные сценарии миграции/повреждения/process death остаются незавершёнными.

## Следующий шаг

OpenSpec **5/43**. Выполнен только 2.1; 2.2–2.5 не закрыты. Следующий после отдельного возобновления — MixedPreviewCacheDeviceTest по 2.2 с повторной проверкой доступности A51 и актуальности backup. Candidate11 остаётся установленным; lab.sf7b остановлено после проверки. Исторические core70pass/1skip и Lint0errors/18warnings относятся к сборке 1.3, повторно не запускались.
