# Пункт 3.3 — восстановление схем оригиналов

Завершена статическая часть 3.3: базовые/наследуемые schemas, значения и выражения defaults, ranges, choices, явно объявленные единицы/преобразования, входы, license gates и dispatch. Coverage (материал отсутствует в этой копии), [поведенческие контракты](../../research/SCHEMA_BEHAVIOR_CONTRACTS.md), JSON контрактов (материал отсутствует в этой копии), результаты проверок (материал отсутствует в этой копии).

One: 214 прототипов; 531 параметризованный профиль / 5032 позиции; 592 binding records / 581 operator ID; 135 DSL headers / 519 parameter rows. Все 414 прежних inherited chains разрешены, parse errors=0. Glitch: 175 selector sites / 174 ID; 380 непосредственных class graphs плюс один conditional profile; все 120 inline graphs. Сохранены 961 registry slot. Эти метрики не складываются в число функций; runtime denominator остаётся задачей 3.4–3.5.

## Доказательства и проверка

17 host checks прошли. Это проверки собственного extractor и доказательств, не product/core/Android tests. Отдельно проверены:

- Девять новых полных REA method Evidence: p2 constructor/copy/random-default/три ranges; Glitch float/radians selectors и generic parameters. Canonical schemas/semantic IDs, exact APK digest, available/complete method body и Evidence bundle валидны. После удаления MCP использовался REA CLI 6.8.0; импорт в MCP в этом пункте не заявляется.
- 2832 export rows с backing Java проверены по SHA256/bytes; filename — хеш полного raw class name, исключающий Windows case collisions. Все использованные supplemental snapshots перечислены в result.json. Один неверный selector a.o отмечен missing; действительный predicate A7.o затем извлечён. Два synthetic helper имеют пустой standalone Java; их DEX metadata сохранена.
- Предоставленные APK, 84 исторических raw JSON и 11 извлечённых DEX/GLSL по-прежнему совпадают с recorded hashes. Пять DEX входят в эти 11 файлов. Исходные исследования не переписывались; предыдущий SCHEMA_COVERAGE сохранён побайтно.
- Проверены арифметика/casts/masks/generic constructors extractor, DEX denominator 214, все 414 inheritance chains, контрольные Adjust/Bloom/wrappers, сохранение 961 registration и license/alias metadata, radians conversion и contextual/unset defaults.

При разработке reader исправлены ошибки обхода скобок, inline static initializers, наследуемых конструкторов, qualified-name collision и специального сохранения source в wrapper predicate. Начальные Java snapshots с простыми именами не использовались после обнаружения Windows case collision. Первый REA Glitch вызов вернул invalid_request из-за внешнего Selectors вместо вложенного Selectors$Utils; три исправленных вызова завершились complete. Ошибки первых запусков verifier относились к самому проверочному скрипту (shadow variable, поле Evidence и квалификация имени); итоговый запуск прошёл. Неуспехи не посчитаны успешными product tests.

## Среда и команды

Private root: `<local-private-path>`. Старый AppData staging после возобновления не обнаружен; требуемые данные восстановлены в этом устойчивом sibling-каталоге вне Android source. Полные оригинальные тексты, APK/Evidence и промежуточные drafts исключаются из поставки.

JDK17, JADX JAR 0.7.1 SHA256 `6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75`; один JVM, heap1024MiB, ActiveProcessorCount=1. REA operation budget360000ms. Persistent config не менялся.

Из `<repository>/research`:

```powershell
$env:SF7B_REFERENCE_AUDIT_ROOT='<local-private-path>'
node .\schema-task3.3-one.mjs
node .\schema-task3.3-inheritance.mjs
node .\schema-task3.3-glitch.mjs
node .\schema-task3.3-coverage.mjs
node .\schema-task3.3-contracts.mjs
node .\schema-task3.3-verify.mjs
```

Для восстановления исходных static snapshots используются собственный `research/schema-task3.3-export.java`, выбранные `*-names.txt` в private root и сохранённый там SchemaSnapshot.class. Точная команда REA recovery находится в private `rea-glitch-recovery.ps1`; параметры и хеши успешных запросов — в Evidence и result.json. Скрипт не следует запускать поверх уже существующих Evidence: он специально запрещает overwrite.

## Ограничения и остановка

У One supplied SHA `aa90712cf7ff80ad8d2a3f36915002a5b26fb73c54355ecb05ca5720571ea7aa`; installed вариант SHA `d4a93ba5e9a954a52b3e6af5a756cc485a621d71db22baa40e9c276dd8b1c7c3` известен из 3.1. DEX одинаковый, ресурсы различаются. Glitch SHA `8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557`, предоставленный mod; upstream authenticity не установлена. One PRO недоступен.

Не объявленные единицы, null/contextual defaults, factory-unset и root-candidate bindings обозначены явно. Статические закрытые/legacy схемы не доказывают runtime доступность, корректный экран или визуальный паритет.

Android/core/test source, собственные APK, телефон и личные проекты не затрагивались. Новых сборок, установок, UI-ввода, exports, эмуляторных/телефонных опытов и измерений производительности нет. Актуальность private backup проектов повторно не проверялась; последняя подтверждённая копия относится к 2.4.

После фиксации STATE.md/notes.md — остановка перед 3.4 по поручению пользователя.
