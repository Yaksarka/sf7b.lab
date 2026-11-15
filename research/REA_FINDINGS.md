# REA: состояние исследования оригиналов

## 14.11.2026 02:31 МСК — пункт 3.2 завершён

Актуальные ordered registration факты: REGISTRY_COVERAGE.json (материал отсутствует в этой копии), список неизвестных (материал отсутствует в этой копии), [отчёт и ограничения](../docs/verification/reference-task3.2-20261114.md). Получены 20 полных REA Evidence через CLI с наблюдённым heap989MiB; фактический импорт в MCP принят. One: 51 group site/587slots; Glitch: 24 constructor sites/374slots и21menu slot. Независимая Java/DEX сверка75/75 прошла; неопознанных статических регистраций в этих корнях нет. Сохранены wrappers, aliases, preview packs и условные альтернативы. Эти числа не равны числу уникальных или видимых эффектов. Старые raw и EFFECTS_CATALOG сохранены; для регистраций3.2 использовать новый registry.

В3.2 не было Android/product changes, сборок, тестов или действий на телефоне. One supplied/installed DEX совпадают при разных ресурсах; One PRO отсутствует, Glitch — только предоставленный mod. CLI успех с heap1024 не доказывает готовность прежнего MCP heap512. Следующий только3.3 после нового поручения: схемы, параметры, входы, gates/dispatch и поведенческие контракты. Общая ревизия REA_FINDINGS/EFFECT_PARITY из3.6 не закрыта.

---

## 14.11.2026 01:52 МСК — 3.1, актуальная идентичность и Evidence

Выполнен только3.1; пауза перед3.2. [Отчёт](../docs/verification/reference-task3.1-20261114.md), варианты/подписи/ограничения (материал отсутствует в этой копии), аудит (материал отсутствует в этой копии).

- One Lab 1.5.3/code138: предоставленный APKPure — SHA256 `aa90712cf7ff80ad8d2a3f36915002a5b26fb73c54355ecb05ca5720571ea7aa`, 32813131 байт; установленный APK — `d4a93ba5e9a954a52b3e6af5a756cc485a621d71db22baa40e9c276dd8b1c7c3`, 32895049 байт. Из 1666 распакованных файлов 1662 совпали, включая DEX, AndroidManifest.xml, native и assets. Отличаются resources.arsc и три META-INF signing files. Сертификат одинаковый, apksigner v2 verified. Совпадение кода позволяет переиспользовать связанные static facts; равенство ресурсов/UI/runtime не установлено. Причина различия resources.arsc неизвестна.
- Glitch 1.7.4/code999: предоставленный mod и установленный APK совпали побайтно — SHA256 `8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557`, 20130873 байта, 1489 файлов; apksigner v1/v2/v3 verified. Официальный контроль и список mod-изменений отсутствуют. One Lab PRO недоступен по прежнему ответу владельца; закрытый runtime не проверен.
- Оба свежих REA package pulls завершились complete, failures=[]: у каждого один base.apk, splits не обнаружены. One: minSdk24/target36, arm64-v8a; Glitch: minSdk21/target36, arm64-v8a и armeabi-v7a. Пустой ABI в старом One inventory относился к каталогу lib/, не архитектуре. Package Manager сообщает installer com.android.vending для One и com.google.android.packageinstaller для Glitch; эти метаданные и подпись не аутентифицируют upstream.
- Проверены 84 JSON: 27 файлов содержат 28 уникальных REA Evidence, все прошли текущие schema/semantic-ID validation и повторный импорт в MCP; 47 JSON — host-derived, 10 — error-only. Пять static records содержат exact APK digest; 23 targetless records включают 17 capture, 3 start, 2 device-list, 1 client inspection. Текущие pulls не устанавливают exact digest исторической установки задним числом.
- Все 17 backing screenshots совпали с recorded SHA/bytes, 11 извлечённых DEX/GLSL — с соответствующими ZIP members; input/output SHA Glitch identity export также совпали. Отдельно инвентаризован 21 исторический runtime PNG/XML. Все 84 исходных исследовательских JSON остались прежними. Новый pixel-diff не выполнялся; полнота registry/schema этим аудитом не подтверждена.
- Свежий One inspect_android_package успешен: manifest complete, classes10801/resources1666, JADX JAR0.7.1 и bridge7 прежних SHA. Glitch package scan завершился provider_timeout120000ms при heap512; это ошибка/partial diagnostic, не успешный Evidence. Исторический успешный Glitch constructor использовал heap989MiB, One static — 494MiB, worker1. Неизменённый тяжёлый вызов не повторялся, настройки REA не менялись. Fresh bundle13 + historical import28 = combined41 records/0unknowns; каноническая целостность проверена. Импорт не исполняет старые операции.

Ниже историческая запись13.11, не доказательство текущего состояния провайдера. Следующий только после нового поручения — **3.2**: закрыть One Lab ordered groups и Glitch FragmentGlitch.initGlitchTypes; классифицировать effects/helpers/aliases/presets и сохранить unresolved registrations. Учитывать оба One variant digest и ограничения PRO/mod/Glitch provider. Схемы3.3 и обход3.4 автоматически не начинать.

---

13.11.2026. Внутренний анализ разрешён пользователем; это не исследование по прежним ограничениям чистой комнаты. Никакие оригинальные шейдеры/значки в приложение не переносились этим исследованием.

## Идентичность APK

| Материал | Подтверждённая идентичность | SHA-256 | Ограничение |
| --- | --- | --- | --- |
| One_Lab_1.5.3_APKPure.apk | com.ilixa.onelab,1.5.3,code138,minSdk24,target36,arm64-v8a | aa90712cf7ff80ad8d2a3f36915002a5b26fb73c54355ecb05ca5720571ea7aa | Подпись не аутентифицирована как upstream |
| 1787155315_glitch-lab-v1_7_4-mod-5mod_ru.apk | com.ilixa.glitch,1.7.4,code999,minSdk21,target36,arm64-v8a/armeabi-v7a | 8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557 | mod-кандидат, aapt2 missing resources warnings; не официальный контроль |
| uptodown-com.ilixa.glitch.apk | com.uptodown,7.45,code745 | 918aae7b5e38647d933d50e90525f29512d393a91e03ce8a494d58ba530b21a5 | Это Uptodown App Store; исключён из анализа эффектов. Позднее файл перестал быть доступен; ранние raw сохранены |

Подтверждение идентичности: Android SDK aapt2 35.0.0 и независимая ZIP-инвентаризация, не выдаваемые за REA Evidence. См. raw/*-inventory.json и *-aapt-badging.txt. Факт версии не доказывает аутентичность/полноту поведения модифицированного APK.

## REA и провайдер

Живой MCP inspect_android_package первоначально сообщил REA_JADX_MCP_JAR not_configured. Raw: oneInspection-mcp-2026-11-13.json / glitchInspection-mcp-2026-11-13.json. Затем официальный upstream JAR0.7.1 установлен в `.codex/tools/jadx-headless-mcp-0.7.1/`; SHA256 `6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75` совпал с release checksum и audited identity REA. Источник: [upstream v0.7.1](https://github.com/1013503897/jadx-headless-mcp/releases/tag/v0.7.1).

Успешная проверка проведена через **отдельный REA CLI6.8.0**, не через старый процесс MCP: JDK17, JAVA_HOME `C:\Program Files\Java\jdk-17`, REA_JADX_HEAP_MIB512, REA_JADX_ACTIVE_PROCESSOR_COUNT1, JDK_JAVA_OPTIONS `-XX:ActiveProcessorCount=1 -Xmx512m`. Наблюдённый heap494MiB, worker1. Настройку постоянного MCP выполняет root; CLI успех не доказывает обновление его окружения.

| REA Evidence | Операция | Подтверждённое |
| --- | --- | --- |
| ev_b5c79da344a831b6126afc9d18049beddf19ea0eb3457e9f6c2729154fb228e9 | search_android_classes | One Lab complete class search по com.ilixa.pap2.effects; top-level total10801, повторяющихся имён не найдено |
| ev_d49cb692b65631fab250e5e30f83e780f431b0ad5efad6113f05ba0ae81a6680 | inspect_android_method Bloom.r | Декомпилированный метод строит `(bloom source1)`, complete source, без fallback |
| ev_30e7dfb0263b5b0b42057a8d0ebf002cb48b9639ea732e669fb6291786bcd721 | inspect_android_method LowFreqBanding.H | Возвращает new s(5,100,100); значение полей s пока не интерпретировано |
| ev_032dd44577facc15a217fb47e8526c9067a7df73d24b681aaa4ad41761509050 | inspect_android_method MultiDistort.H | Возвращает new s(400,1); значение полей s пока не интерпретировано |

Raw *_rea.json / *-rea.json и onelab-rea-cli-classes.json содержат Evidence целиком. Target-free CLI сам завершал принадлежащие ему процессы; shared native session исследователем не открывалась.

JDK21 module probe первоначально не уложился в10s из-за задержки startup. С JDK17 повторный класс-поиск успешен. Четыре Glitch constructor decompile достигли120000ms operation deadline, пятый не прошёл30000ms bridge startup. Их JSON — ошибки, **не найденные доказательства кода**. Remediation разрешает REA_JADX_OPERATION_TIMEOUT_MS; повторное обращение должно менять этот бюджет и учитывать доступную память. Нельзя считать статическую декомпиляцию runtime-тестом.

## Каталог и механизмы

EFFECTS_CATALOG.json (материал отсутствует в этой копии):856 One Lab и414 Glitch top-level кандидатов effect/preset/helper; каждый связан с точным APK, классом, методами, DEX string references, схемами и shader declarations при наличии. Это полный результат **селектора классов**, не завершённый каталог видимых эффектов: registration, helpers, legacy и entitlement ещё требуют классификации. `visible_catalog_complete=false`, `parameter_schema_complete=false`.

Необфусцированные имена классов и строки позволили связать все пять приоритетов с реальными механизмами. См. [поведенческую спецификацию](PRIORITY_BEHAVIOR.md) и [паритет](EFFECT_PARITY.md). В One Lab значительная часть обработки — GLSL-строки внутри DEX, а не отдельная алгоритмическая native.so. В Glitch найдены связанные GLSL в res/raw. Эта находка не доказывает отсутствие иных путей выполнения.

## UI: доступные статические сведения

Android launch: `com.ilixa.onelab.MainActivity`; также существует LivePreviewActivity. Compose .cvr строки декодированы из предоставленного APK: raw/pap2mp-compose-strings.json (119 entries), raw/onelab-compose-strings.json (74 entries). Подтверждены собственные settings labels expert-mode menu, effect tree, theme, image export format, custom effects. Они свидетельствуют о настройках/ресурсах; не доказывают точную компоновку, цвета, размеры, доступность экранов или UI transitions. Карточки activity_start/expert/discovery/generate/geometry/glitch/style присутствуют как assets. Не использовать оригинальные изображения в sf7b.lab.

Runtime-снимки, маршруты экранов и измерения на эмуляторе собирает root отдельно. Пока точная UI parity непроверена. Нельзя выдать самостоятельно придуманный экран за установленный дизайн One Lab.

## Продолжение

1. Классифицировать registration и схемы всех кандидатов: actual effects, presets, helpers, legacy, debug-only.
2. Установить min/max/defaults и mapped parameter semantics из function definitions / Parameters.Utils; ключи и shader declarations не равны UI schema.
3. Точечно декомпилировать Glitch с увеличенным разрешённым operation budget; не запускать несколько512MiB JVM параллельно.
4. Сохранить runtime inputs→exports, settings и снимки UI; сравнивать каждую целевую версию отдельно. Данные модифицированного Glitch не выдавать за доказанный официальный upstream.
