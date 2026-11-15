# Пункт 3.1 — идентичность оригиналов и пригодность Evidence

14.11.2026, завершено в 01:50 МСК. Выполнен только 3.1. Android/core/test source и APK sf7b.lab не менялись. Сборок, тестов продукта, установки, запуска оригиналов, ввода на телефоне и изменения данных/настроек не было. После фиксации — обязательная пауза перед 3.2.

## Основания и охват

Прочитаны актуальные AGENTS/RTK, CONTEXT, STATE, notes, WORKFLOW, CLEAN_ROOM, REQUIREMENTS и OpenSpec proposal/specs/design/tasks; применены openspec-apply-change и reverse-engineer-anything. Используется `complete-dual-app-functionality` / `spec-driven`. Внутренний анализ разрешён владельцем; материалы оригиналов не переносятся в продукт.

В Downloads найдены приложенные tasks.md/design.md. Tasks совпадает с активным по тексту после исключения checkbox/переводов строк. Design отличается только строками 7 и 122: реальные пути заменены на `<repository>`; функциональных расхождений нет. SHA обеих пар сохранены в аудите (материал отсутствует в этой копии). Приложения Downloads не изменялись. Единственный Android-source остаётся `<repository>`; Desktop содержит правила и OpenSpec.

## Проверенные артефакты

| Вариант | Package / version / code | Размер APK | min / target SDK | Нативные ABI |
|---|---|---:|---|---|
| One Lab, предоставленный APKPure | com.ilixa.onelab / 1.5.3 / 138 | 32813131 | 24 / 36 | arm64-v8a |
| One Lab, свежий pull A51 | com.ilixa.onelab / 1.5.3 / 138 | 32895049 | 24 / 36 | arm64-v8a |
| Glitch Lab, предоставленный mod | com.ilixa.glitch / 1.7.4 / 999 | 20130873 | 21 / 36 | arm64-v8a, armeabi-v7a |
| Glitch Lab, свежий pull A51 | com.ilixa.glitch / 1.7.4 / 999 | 20130873 | 21 / 36 | arm64-v8a, armeabi-v7a |

Точные входы:

- `<local-private-path>`, SHA256 `aa90712cf7ff80ad8d2a3f36915002a5b26fb73c54355ecb05ca5720571ea7aa`.
- One Lab A51 base.apk, SHA256 `d4a93ba5e9a954a52b3e6af5a756cc485a621d71db22baa40e9c276dd8b1c7c3`.
- `<local-private-path>` и Glitch A51 base.apk: SHA256 `8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557`.

REA pull обоих packages вернул complete, failures=[], каждый установленный набор содержит только один base.apk; splits не обнаружены в этом текущем `pm path` наблюдении. В ZIP One Lab 1666 файлов, Glitch 1489; повторяющихся путей нет. ABI получены по реальным `lib/<abi>/*.so`, не по названию файла и не из каталогов. Пустое значение ABI в старом onelab-inventory.json было артефактом учёта каталога `lib/`; текущий ABI только arm64-v8a. Старый raw не исправлялся.

### Расхождение One Lab

SHA предоставленного и установленного APK различаются. Из 1666 одноимённых распакованных файлов 1662 побайтно совпадают, включая оба DEX, AndroidManifest.xml, все native-библиотеки и assets. Отличаются:

- `resources.arsc`: предоставленный 141748 байт / SHA `3bef3599089187d6da38832f3238e1d1d2ab0b6fdcf7e13934920b575bf9a87a`; установленный 226204 байт / SHA `4804008bea5783185739d6e35d0604cdd0c3a65f27463c2b4aa9f6f738498bc3`.
- `META-INF/CERT.RSA`, `META-INF/CERT.SF`, `META-INF/MANIFEST.MF`.

Эта проверка относится к распакованным членам ZIP; она не объявляет совпадающими прочие метаданные/структуру контейнера. Причина и семантика различий таблицы ресурсов в 3.1 не установлены. Общие статические утверждения о совпавшем коде допустимо переносить с явной связью по member SHA; ресурсные строки/defaults/UI и поведение целого приложения автоматически не приравниваются. В следующих исследованиях хранить оба variant digest.

Glitch supplied и installed совпали целиком, включая все 1489 членов; сравнение остаётся сравнением предоставленного mod.

### Подпись и происхождение

REA `inspect_apk_signature` прочитал One supplied, One installed и Glitch supplied. Это **структурные наблюдения**, а не криптографическая проверка подписи. Дополнительно Android SDK apksigner 35.0.0 проверил все четыре файла, exit0:

- One supplied/installed: v2 verified; одинаковый certificate SHA256 `4610edf9d68580fcf4940a98f54959fc7748e45787a2a2cfd75e6fa13e276564`. V1 присутствует по REA, но apksigner сообщил v1=false; не заявлять успешную v1-проверку.
- Glitch supplied/installed: v1/v2/v3 verified; certificate SHA256 `a7fe9ac49dccdfb99f06b6a39a43fc16ce046d10288b41a2bc7c4c288499b00b`. REA отмечает 4 неинтерпретированных байта signing records; это само по себе не ошибка подписи, apksigner exit0.

Название APKPure описывает предоставленный файл, а не доказанную цепочку загрузки. Package Manager сообщает installer `com.android.vending` у установленного One и `com.google.android.packageinstaller` у Glitch. Это метаданные установки, не аутентификация upstream. Криптографическая целостность и общий сертификат не устанавливают официальный статус сертификата/издателя. Полного списка изменений Glitch относительно официального APK нет. One Lab PRO отсутствует по ранее данному ответу владельца; покупать/повторно запрашивать его в 3.1 не требовалось.

Старый `uptodown-com.ilixa.glitch.apk` соответствует com.uptodown 7.45/code745 и исключён: сохранённые raw относятся к магазину, не Glitch. Повторно он как оригинал не исследовался.

## Пригодность сохранённых материалов

Машинный аудит (материал отсутствует в этой копии) проверил 84 JSON из research/raw и выбранных Glitch/One runtime путей:

| Класс | Результат / допустимое использование |
|---|---|
| 27 файлов с REA records | 28 уникальных Evidence, все прошли текущие REA6.8.0 schema + semantic-ID validation и фактический импорт в живой MCP; ошибок идентификаторов нет |
| 5 static Evidence с subject digest | One class-search + 3 One methods + 1 успешный Glitch Patchwork constructor; SHA APK точные, JADX JAR и bridge SHA совпали с текущим провайдером |
| 23 Evidence без subject APK digest | 17 capture, 3 start, 2 device-list и 1 client inspection; полезны как исторические наблюдения, не обязательство точной исторической сборки |
| 10 error-only файлов | Старые provider_timeout, resource_constraint, capability_unavailable; не результаты успешного анализа |
| 47 host-derived JSON | Каталоги, DEX/string/schema/registry разборы и результаты host-сценариев; не REA Evidence и не завершённый видимый каталог |
| 5 raw DEX + 6 извлечённых GLSL | 11/11 SHA совпали с соответствующими членами текущих предоставленных APK; материалы оригиналов остаются вне поставки |
| 17 REA screenshot backing files | 17/17 совпали с recorded SHA и byte size; всего отдельно инвентаризован 21 PNG/XML в выбранных исторических runtime путях |
| Glitch identity export | Сохранённые input/output SHA совпали с comparison.json; повторного pixel comparison/нового экспорта не было |

Методы имеют доступное полное тело. JAR0.7.1 SHA `6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75`, bridge7 SHA `e5044a8312dd0731f53af3f095508b3e5cff54df6b971c95d4305e2f39b4fa30`. One static использовал наблюдённый heap494MiB, успешный Glitch constructor989MiB; worker1. Это разные ресурсные условия, не единый повторный прогон.

Исторический screenshot сам по себе не фиксирует эффект, полные параметры или экспорт. Текущие APK pulls не устанавливают точный digest исторической установки задним числом. Сохранённый Glitch PNG — один немодифицированный эффектом проход 1280×960 с ранее отменённым draft Streak, не сравнение эффекта и не доказательство официального upstream.

Host каталоги можно продолжать как производные от подтверждённых DEX; их parsers и полнота классификации не сертифицированы проверкой исходного хеша. 1270 статических кандидатов, registry refs, instances и schemas остаются разными метриками, не числом законченных пользовательских функций. Закрытие ordered groups, FragmentGlitch.initGlitchTypes и inherited schema относится к 3.2–3.3.

## Свежие REA результаты и ошибка

Новый One Lab inspect_android_package успешен: manifest complete, package1.5.3/code138, classes10801/resources1666, точный supplied digest и текущий JADX JAR. Его полный parent Evidence: `ev_402b52bce706d4cec8bb0f224a1718ff1384e08f92407f2d76ed0df1518869f8`.

Новый Glitch inspect_android_package при текущем heap512 достиг provider operation deadline120000ms на загрузке APK. Получены диагностика/частичное наблюдение провайдера, но **успешного нового package Evidence нет**. Полный ответ сохранён private `glitch-package-attempt.json`. Не повторялся неизменённый вызов и не менялись настройки REA: для идентичности этого пункта достаточно свежего точного pull, REA signature inspection и независимых aapt/apksigner/ZIP проверок. При следующем анализе учитывать успешный исторический constructor с heap1024 и текущие ограничения среды, не выдавая их за текущую готовность полного разбора.

Свежий канонический bundle содержит13records; исторический импорт добавил28, итоговый export41records/0unknowns. Повторный импорт — проверка сохранённых записей, не повтор исполнения старых start/capture операций. Полные bundle проверены текущим `parseEvidenceBundle`; все84 исходных исследовательских JSON после работы сохранили прежние SHA.

## Доказательства и команды

- result.json (материал отсутствует в этой копии): варианты, digest, ABI, подписи, provenance и открытые ограничения.
- evidence-audit.json (материал отсутствует в этой копии): каждый проверенный raw, Evidence ID/coverage/engine, файл снимка и результат сверки.
- manifest.json (материал отсутствует в этой копии): SHA локальных доказательств/проверочных скриптов. Все raw bundles, оригинальные APK и логи — private, не вложены в публичный результат.

Private root: `<local-private-path>`. `installed/<package>/base.apk`, `apk-inventory.json`, `*-aapt*.txt`, `*-apksigner.txt`, `fresh-evidence-final.json`, `historical-authenticated-evidence.json`, `combined-evidence.json`, `glitch-package-attempt.json`. Здесь нет копий личных проектов.

Фактические REA операции: list_adb_devices; inspect_adb_package и pull_adb_package для com.ilixa.onelab/com.ilixa.glitch; inspect_apk_signature для трёх различных digest; inspect_android_package supplied One (успех) и Glitch (timeout); import/export Evidence. На телефоне только чтение package metadata/APK. Ни UI-input, ни start/stop originals в этом пункте не выполнялись.

Проверочные команды с хоста:

```powershell
& '<repository>\docs\verification\reference-task3.1-inventory.ps1'
node '<repository>\docs\verification\reference-task3.1-audit.mjs'
node '<repository>\docs\verification\reference-task3.1-finalize.mjs'
```

Inventory script использует aapt2 dump badging, apksigner verify --verbose --print-certs и SHA256 каждого распакованного ZIP member; SDK35.0.0/JDK17. В audit.mjs локально использованы поставляемые REA6.8.0 `parseEvidence` / `createEvidenceBundle` / `parseEvidenceBundle`, без LLM-вызовов. Начальный запуск audit исправлен с Windows ESM path на file URL; первая попытка не анализировала файлы. SDK поле скрипта уточнено до minSdkVersion; сохранённый inventory обновлён из тех же уже полученных badging logs, повторной проверки телефона не было.

Это проверки исследовательских файлов, не новые core/native/UI/performance тесты sf7b.lab. Последняя проверка пользовательских проектов остаётся checkpoint2.4, актуальность private backup в3.1 повторно не проверялась. Полный реестр функций, PRO runtime, официальный Glitch parity, скорость и анимация не закрыты.

**Пауза после 3.1 по указанию владельца. Следующий 3.2 только после нового поручения:** завершить ordered groups One Lab и FragmentGlitch.initGlitchTypes, разделить actual effects/helpers/aliases/presets и явно сохранить неразрешённые регистрации. Не начинать схемы3.3 или обход3.4 автоматически.
