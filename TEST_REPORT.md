# Проверки sf7b.lab — candidate11

Сводка пункта **2.5**, 14.11.2026. Она объединяет сохранённые результаты 13–14 ноября; новых сборок, тестов, установок или обращений к телефону в 2.5 не было. Этап проверки уже написанных изменений завершён в указанном ниже охвате. Полный каталог, паритет оригиналам и готовность всего приложения этим не установлены. Следующий пункт после отдельного возобновления — 3.1.

## Идентичность артефактов

Последнее подтверждение установленных APK — свежие REA pulls в 2.4. App debug0.2.0/code2, lab.sf7b, candidate11:

`8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`

App собран в 1.3; production-код с тех пор не менялся. В 2.2 добавлены cache-контракты, в 2.4 исправлен helper захвата кадров; менялись только test sources и test package.

| Test APK | SHA256 | Где использован |
|---|---|---|
| candidate11-tests (материал отсутствует в этой копии) | `05084940d2ec9827fd76562b31a0ed321b339b72fd484cfcb3aecc907f379a2a` | 2.1 |
| cache-contract-tests-v2 (материал отсутствует в этой копии) | `950213f0a35336bd1766cfed2d9ee30da277c625ac5e3acbdf25ad154eff9ed1` | 2.2–2.3 |
| ui-capture-tests (материал отсутствует в этой копии) | `52f98f1ec63a869cee603c413edc555833f5f811861c0237485717567536b12f` | итог 2.4, последний подтверждённый установленный test package |

Сохранённые подписи APK verified v2; общий certSHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`. App обновлялся без clear/uninstall, позднее обновлялись только test APK. Манифест 1.4 (материал отсутствует в этой копии), cache-манифест (материал отсутствует в этой копии), UI-манифест (материал отсутствует в этой копии).

## Что проверено и когда

| Этап | Среда и результат | Реальный охват и границы |
|---|---|---|
| 1.3, 13.11 | Host: core **70 pass / 1 skip**, fail/error0; app/test assemble успешны; Lint **0 errors / 18 warnings** | Полный тогдашний core-запуск. Пропущен largeCanvasBenchmarks; это не измерение большого полотна |
| 2.1, 13.11 20:18 МСК | A51: **1 pass**, без skip/failure | 15 слоёв, 50 узлов, TTF/seed/masks/blend/ветви/crop/portable Document и ресурсы; exact pixels/PNG. Canvas **192×144 → PNG130×154**, ресурс64×48. Не полноразмерные15слоёв |
| 2.2, 13.11 20:35–20:36 | A51: **2 pass**, без skip/failure | 15-layer blend, документ2000² → **preview512²**; exact cached/uncached pixels, providers0/0 после blend. Source replacement/mask/early effect — отдельный **двухслойный** документ |
| 2.3, 13.11 20:43 | A51: **1 pass**, без skip/failure | Собственные fixture32×24: RU повреждённый current, EN отсутствующий current; last-good/repair/видимый warning/no-repeat. Это два сочетания, не вся матрица |
| 2.3, 13.11 20:44 | Host: **3 pass**, без skip/fail/error | Повторный save сохраняет bytes/mtime/revision/updatedAt/index; repair восстановленной модели и защита last-good. Эти методы входят в прежний core-набор; не прибавлять их к70 как новые уникальные тесты |
| 2.4, 14.11 01:13–01:14 | A51: **20 pass**, без skip/failure | Import7, GpuRender5, GpuChain1, EditorUi7. GPU chain2000×64; full model export2000² — image chain из5эффектов, **не15слоёв** |
| 2.4, 14.11 01:16 | A51: **1 pass**, повтор | Пересъёмка RU/EN×dark/light. Повтор одного уже учтённого UI-метода, не новый21-й тест |

Результаты A51 получены отдельными запусками и разными test APK; это не один общий запуск «24/24». Полные журналы, точные интервалы и команды: [1.3](docs/verification/candidate11-build-20261113-1953.md), [2.1](docs/verification/candidate11-a51-task2.1-20261113-2021.md), [2.2](docs/verification/candidate11-a51-task2.2-20261113-2030.md), [2.3](docs/verification/candidate11-a51-task2.3-20261113-2048.md), [2.4](docs/verification/candidate11-a51-task2.4-20261114-0108.md).

Generic core XML после 2.3 содержит только три filtered tests. Архив полного70/1: result[1.3](docs/verification/candidate11-build-20261113-1953.md). Эмулятор в этой проверке не запускался; исторический неудачный старт не является пройденным тестом.

## Import/GPU/UI

- Import: gray PNG round-trip, P3→sRGB,16-bit RGBA alpha, PNG/JPEG/WebP без расширения, EXIF/adaptive preview с сохранением исходных bytes, shared decode isolation и eviction. Это конкретные fixtures, не вся матрица профилей/форматов/ошибок.
- GPU: identity exact, pointwise/curves/Bloom legacy/v2/alpha/neutral в установленных допусках0/2/3, unsupported fallback до загрузки sources. Семь chain prefixes дали максимум **1** уровень premultiplied канала, over3=0; этот максимум относится к2000×64 fixture, не ко всем эффектам.
- UI: поиск/Apply/Undo, numeric comma/Cancel без commit, text и independent copy/seed после activity recreation, замена изображения/Undo/Redo, full PNG2000²/alpha128 без изменения Document, RU/EN и две темы. Activity recreation не является Android process death.

Сохранены **9 сырых PNG**: editor/settings RU/EN×dark/light и numeric keyboard. Визуально проверены нужные панели и ввод с Samsung IME; PNG не редактировались. Манифест дат/SHA/producer (материал отсутствует в этой копии), контактный лист (материал отсутствует в этой копии). Это проверка этих экранов, не полный UX/a11y аудит.

## Сохранность и вмешательство при cleanup

Повреждались и удалялись только свои fixtures. Pre/post private copies сверялись по SHA с устройством; реальные documents/last-good/assets/metadata/legacy/prefs/UUID/selection сохранены. В 2.3 и2.4 после удаления тестовых проектов в backup index оставалась запись последнего удалённого fixture. При остановленном app **только library-last-good.json** возвращён к проверенным pre-test bytes через проверенный temp/rename. Поэтому сохранность обоих индексов в финале подтверждена **после явного host cleanup**, а не без вмешательства.

В 2.4 выбран согласованный baseline после изменения activeId; старый выбор2.3 поверх нового не возвращался. Итоговая копия14.11 01:18:29МСК:52файла,44774912байт,SHA256 `3cde3d23a13261998a86962f7331e4729ed0c0a7932f45f299200b9815f7cc0e`. Все52файла совпали с повторным чтением телефона. По сравнению с baseline40файлов прежние,10собственных отчётов обновлены,2служебных файла(device-info/profileInstalled) изменились; новых/пропавших0. Private TAR/распаковки/ключи/полные Evidence не включаются в поставку. Актуальность этих данных позже01:18 отдельно не проверялась в2.5.

## Неуспешные попытки и пределы вывода

В2.2 исправлен собственный test fixture с пустым Layer.head, затем итоговые2теста прошли. В2.4 первый ADB transport оборвался после14успешных статусов без общего итога; он не засчитан полным проходом. Преждевременные settings screenshots обнаружены визуально и пересняты после исправления helper. Один баннерный оригинал оставлен только private, финальные кадры чистые. В2.3 передача512из605байт была остановлена до rename; успешный вариант проверен до замены backup index. Эти события не скрыты подробными успешными итогами: их журналы и ограничения сохранены в этапных отчётах.

Остаются открытыми полноразмерные15смешанных слоёв/память/отмена, Android process death, вся матрица миграций и import failures, все функции и временные режимы оригиналов, полные RU/EN/темы/UX-маршруты, end-to-end latency/FPS/PSS и воспроизводимые сравнения оригиналов. Собственная CPU/GPU согласованность не доказывает паритет One Lab/Glitch Lab. Скорость и её ограничения — [PERFORMANCE](PERFORMANCE.md).

В2.5 локально проверены SHA сохранённых APK/логов/XML/CSV/PNG и связи манифестов: аудит доказательств (материал отсутствует в этой копии). Это проверка файлов, а не новые A51/core tests. [STATE](STATE.md) содержит checkpoint; следующий после отдельного возобновления —3.1.
