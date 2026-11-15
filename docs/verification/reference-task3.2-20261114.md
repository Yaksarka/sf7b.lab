# Пункт 3.2 — ordered groups и реестр регистраций

14.11.2026, результаты проверены в 02:28 МСК. Выполнен только3.2. Это статическое исследование двух выбранных APK через REA с независимой сверкой DEX. На телефоне действий не было; Android/core/tests/APK sf7b.lab не менялись. После фиксации требуется пауза перед3.3.

## Результат и границы

Созданы REGISTRY_COVERAGE.json (материал отсутствует в этой копии) и UNRESOLVED_REGISTRATIONS.json (материал отсутствует в этой копии). Первый содержит каждый корень, порядок, позицию, класс/фабрику, wrappers, строковые аргументы конструктора, условные альтернативы и связанные Evidence. Второй сохраняет список неопознанных статических регистраций: **0 в выбранных корнях**. Это не утверждение об отсутствии неизвестных функций во всём приложении.

| Объект учёта | One Lab | Glitch Lab |
|---|---:|---:|
| Места создания групп | 51 | 24 |
| Номинальные позиции в этих массивах | 587 | 374 |
| Позиции главного menu array | Отдельный UI порядок не исследовался | 21 |
| В том числе позиции, отмеченные preset-контекстом | 90 | 193 |
| Остальные позиции эффектов/инструментов | 497 | 181 |
| Успешные полные REA methods | 19 | 1 |

**587/374 — позиции регистраций с повторениями, preview packs, hidden/dev и условными значениями, не число уникальных эффектов.** Всего961уникальный row ID. 186 наборов повторных ссылок и 10 именованных variant aliases хранятся отдельно; их не прибавлять к числу алгоритмов. Тип preset означает явную регистрацию в preset-группе, класс preset или вызов Presets factory; реальный граф набора этим пунктом не восстановлен.

## One Lab: полный охват выбранного корня

Извлечены H2.<init> и все17 вызываемых им методов G–W, создающих группы, а также H2.E. Конструктор содержит34группы; остальные17создаются helper-методами. Все51site связаны с конкретными DEX offsets и полными REA method Evidence.

| Вызов регистрации | Число sites |
|---|---:|
| D | 29 |
| F | 16 |
| Прямой E | 6 |

Старый onelab-static-groups-ordered.json учитывал только D и пропускал22site через E/F. Старые raw не перезаписывались. В новом файле сохранён порядок создания в H2.<init>, включая реальный порядок вызова helper-методов, а не алфавит G–W. Для каждой позиции сохранены group ID, method/offset и индекс.

Имена `generator_basics`, `generator_fractals`, `generator_patterns`, `generator_texture`, `generator_intricate`, `fractal_mapping`, `optical`, `local`, `global`, `displacement`, `hue`, `black&white` и preset-группы больше не выпадают. Исходное имя **gltich_presets** сохранено с его написанием. Пять пустых начальных массивов `all`, `mapping`, `custom`, `mosaic`, `minimal` явно присутствуют; пустое объявление не доказывает пустой runtime после последующих динамических действий.

H2.E по декомпилированному коду может повторно использовать maker ID через cache, пропускать null-результат и направлять результат разными путями регистрации. Поэтому статический список makers не равен готовому видимому меню.

Wrappers классифицированы отдельно:

- M0 — делегирование base maker с license metadata; это не самостоятельный механизм.
- K0/L0 — делегирование с изменением регистрационных метаданных; не отдельные эффекты.
- T0 — именованная variant-ссылка `(base ID, variant name)`, а не новый класс алгоритма. Сохранены все10пар; действительный dispatch/граф остаётся3.3.
- Повторный singleton отличается от другого поля того же класса: canonical key включает owner и field. Повторные ссылки не сливаются только по имени класса.

Учитывается наследование интерфейсов: например, CircleListPainter реализует U0, наследующий l8. Старое сопоставление только прямого l8 ошибочно относило его к helper; новый классификатор проходит superclass и interface graph.

## Glitch Lab: initGlitchTypes

Полный REA source `FragmentGlitch.initGlitchTypes` получен без fallback, markers=[]; source complete. Восстановлен21-slot `effectGroups`, а не прежний13-entry subset:

| Индекс | Группа/действие | Статическая условность |
|---:|---|---|
| 0 | Presets | — |
| 1 | Color preset pack | hasPresetPack(color) или null |
| 2 | Art preset pack | hasPresetPack(PP_ART) или null |
| 3 | Mirror preset pack | hasPresetPack(mirror) или null |
| 4 | Schematics pack / promotion / null | hasPresetPack(PP_SCHEMATICS), isPPSchematicsPromoSeen |
| 5 | PP Unsorted | hasDev или null |
| 6 | separator | helper, не эффект |
| 7 | Color | — |
| 8 | Streak | — |
| 9 | Repeat | — |
| 10 | Break | отдельная условная позиция |
| 11 | Corrupt | две условные позиции |
| 12 | Distort | — |
| 13 | Objects | setPro metadata у отдельной позиции |
| 14 | Sort | — |
| 15 | Art | отдельная условная позиция |
| 16 | Retro | — |
| 17 | 3D | три nullable позиции и beta-альтернатива |
| 18 | Test | hasDev или null |
| 19 | separator | helper, не эффект |
| 20 | Generate | — |

Здесь названия через ресурсы сопоставлены с resource keys точного APK; runtime локализация не исследовалась. Условия только прочитаны, **не вычислялись и не обходились**. Даже наличие вызова hasPresetPack в предоставленном mod не доказывает фактическое лицензионное поведение.

24constructor sites включают23списка эффектов и одно promotion action. Четыре отдельных preview fields `ppColorGroup`, `ppArtGroup`, `ppMirrorGroup`, `ppSchematicsGroup` сохраняют16/13/28/18позиций —75суммарно. Они не являются ещё четырьмя независимыми видимыми packs. Остальные299номинальных slots связаны с menu constructors. При Schematics сохранены **два разных порядка** preview и menu pack, без ложного объединения по title.

Сохранены15branch sites, шесть nullable menu slots, семь nullable effect slots и обе альтернативы FractalSolid/FractalSolidSimplified в 3D по useBetaFilters. Это branch alternatives, не одновременно доступные функции. Fluent-вызовы `addScaling`, `addTranslation`, `setPro` и другие сохраняют идентичность получателя и metadata; они не превращаются в потерянные значения или новые эффекты.

Отдельно сохранён `ADJUSTMENTS` с DefaultEffectType/label Adjustments: инструмент создаётся вне group arrays. Presets factory calls, DynamicEffectType, anonymous subclasses и именованные constructor variants остаются различимыми. Например, разные ShapesGL со строковыми аргументами Stripes/Circular Stripes/Square Stripes не склеены по классу.

## Классификация исходного class-selector

Сохранены все1270строк прежнего class-selector; классификация пересчитана по типам с учётом наследования. Это классификация классов, а не процент покрытия пользовательских возможностей:

| App | Effect-type classes | Preset-type classes | Function/helper | Abstract-helper | Group-container |
|---|---:|---:|---:|---:|---:|
| One | 706 | 83 | 55 | 12 | 0 |
| Glitch | 384 | 0 | 7 | 2 | 21 |

У каждой строки указаны ссылки на новые registry rows либо `not-referenced-by-selected-roots`; последнее не означает недоступность/отсутствие в runtime. Ещё5типов, найденных за пределами старого selector, сохранены в supplemental_types: m6.o2, m6.T0, Glitch Presets factory и два anonymous EffectType. Три One registration wrappers записаны отдельно.

Старый EFFECTS_CATALOG.json и его сырые inputs сохранены; для фактов ordered registration/classification3.2 использовать новый REGISTRY_COVERAGE.json. Итоговые feature IDs, полный знаменатель и объединение сценариев относятся к3.5; ни один из этих пунктов не отмечен выполненным сейчас.

## Независимая проверка

Host-анализ идёт по DEX control-flow graph, а не по линейному списку ссылок. Он декодирует недостающие move/16, сохраняет альтернативы на branch joins, array sizes/indexes, constructor identity и fluent returns. Во всех выбранных методах нет неучтённых инструкций или недостижимых записей decoder; исключения и поведение исполняемой среды этим не доказываются.

Второй скрипт отдельно читает полные Java bodies из20REA Evidence. Лексически восстанавливает массивы, локальные переменные, ресурсные title references, constructor/factory signatures, наследование anonymous classes, строковые constructor arguments и условные null/beta варианты. Сравнение с DEX прошло **75/75 списков без расхождений**, включая все51One и24Glitch, а не только размеры массивов. Явные string constants, например Filter.INLINE, разрешены из static metadata. Это не сравнение всех числовых параметров, лицензий, shader semantics или пикселей.

Первый запуск catalog до завершения всех CLI jobs был остановлен на ещё пустом JSON; итоговые файлы тогда не созданы. В verifier исправлены распознавание Java identifier как inherited Object property, вложенного массива внутри L0, локальных String titles и статических String constants. После исправлений выполнена окончательная независимая сверка. Промежуточные результаты не выдавались за успех.

20Evidence прошли текущие parseEvidence/parseEvidenceBundle и фактический импорт в живой MCP: imported20, total61, unknowns_added0. Повторный импорт не запускает код оригиналов. Все84исследовательских JSON из аудита3.1 сохранили SHA; все5raw DEX совпали с ZIP member SHA3.1. Точные предоставленные APK повторно хешированы.

## Происхождение, инструменты и ограничения

One supplied SHA `aa90712cf7ff80ad8d2a3f36915002a5b26fb73c54355ecb05ca5720571ea7aa`,1.5.3/code138. Из3.1 известно, что установленный One имеет SHA `d4a93ba5e9a954a52b3e6af5a756cc485a621d71db22baa40e9c276dd8b1c7c3`, одинаковые DEX и отличающуюся resources.arsc; текущий пункт не повторял phone pull и не приравнивает ресурсный/runtime вариант.

Glitch supplied mod SHA `8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557`,1.7.4/code999. Он совпадал с установленным в3.1; официальный upstream не аутентифицирован. One PRO по прежнему ответу отсутствует. Закрытые runtime и entitlement не проверялись.

Использован REA6.8.0 CLI, JDK17, JADX headless JAR0.7.1 SHA `6e5eacf500b64292bfb73c49797c1958f6ee44646e43e868039ae7feb573ff75`, bridge7, REA_JADX_HEAP_MIB1024, наблюдённый heap989MiB, worker1, operation budget360000ms. Выполнялся только один тяжёлый REA/JVM job одновременно. Параметры установлены в дочерних shell, persistent MCP/Codex config не менялся. CLI успех Glitch с этим heap не доказывает успех текущего MCP package scan с heap512 из3.1.

Основные Evidence:

- Glitch initGlitchTypes: `ev_077cf6a87881494691ebb9a39104dde39917c34f60a7e4b539e25cd421d9788d`.
- One H2.<init>: `ev_18c43b2af88b3876d373b9e6c665b1207dc1788fc41322552a7933968609a0b3`.
- One H2.E: `ev_0927434c9adb9ed227fc6079e88fe9951e02998f31319455eb0501a36430ec42`.
- Все17helper method IDs, точные engine/target identities: result.json (материал отсутствует в этой копии).

Полные original Java/DEX traces/Evidence остаются private: `<local-private-path>`. Оригинальный код/GLSL/ресурсы не включать в APK или поставляемый собственный source archive. Публичный registry содержит описательные факты и references, без тела исходного кода оригиналов. Личные проекты не читались.

## Файлы и повторяемость

- REGISTRY_COVERAGE.json (материал отсутствует в этой копии), UNRESOLVED_REGISTRATIONS.json (материал отсутствует в этой копии).
- Java/DEX comparison (материал отсутствует в этой копии), result.json (материал отсутствует в этой копии), SHA manifest (материал отсутствует в этой копии).
- Проверочные скрипты: research/registry-task3.2.mjs, registry-task3.2-catalog.mjs, registry-task3.2-verify.mjs, registry-task3.2-finalize.mjs. Запускать по порядку после завершения всех20REA method jobs; CLI helper runner сохранён private как rea-one-helpers.ps1.

```powershell
node '<repository>\research\registry-task3.2.mjs'
node '<repository>\research\registry-task3.2-catalog.mjs'
node '<repository>\research\registry-task3.2-verify.mjs'
node '<repository>\research\registry-task3.2-finalize.mjs'
```

Новые сборки/core/native/A51/runtime/performance проверки sf7b.lab не выполнялись. Последний подтверждённый private backup проектов остаётся2.4,01:18:29МСК; актуальность в3.2 не проверялась. Полноразмерные15слоёв, скорость, точные ranges/defaults/units, графы наборов и сравнения оригиналов остаются открытыми.

**Пауза после3.2 по указанию владельца. Следующий только3.3 после нового поручения:** базовые/наследуемые schemas, ranges/defaults/units/choices, входы, gates и dispatch с поведенческими контрактами. Не переходить автоматически к3.4 runtime или реализации редактора.
