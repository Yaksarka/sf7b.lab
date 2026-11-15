> Исторический журнал/постановка. Действующие разрешения и точка остановки — [AGENTS](../AGENTS.md) и [STATE](../STATE.md). Архив не отменяет паузу разработки.

# Исследование каталога — review_prompt

## 13.11.2026 11:55

- Зона владения: research metadata/scripts/MD, этот журнал. app/core и телефон не изменяются. Root управляет runtime; heavy JADX только в согласованное окно.
- RLM353e54d51deb expired. Его результаты воспроизведены локально без LLM:131named+6anonymous literal schemas,509params,414inherit rows; static release selector397One/84Glitch. Это не visible catalog. `catalog-coverage.mjs` сохраняет CATALOG_RELEASE.json и SCHEMA_COVERAGE.json.
- Новый точечный scope: base declarations adjust,height-map,dichotomic-sampling,hex-kaleidoscope,dichotomic-break,charts,polar,globe. Требуется связывать DEX ctor→schema fields→реальные ID/params/defaults/ranges, неизвестное оставлять явным. Имена/кандидаты не являются реализацией.
- Разрешённый ранее REA Glitch1024MiB завершился успешно: Patchwork ctor, Evidence ev_31ce9a7fd53d5197550c24d1c6bc3ebe3d49ec0eddeda9367259727aea1097e4; observed989MiB/worker1. Raw glitch-patchwork-constructor-rea-1024m.json. На11:52 своих JVM не осталось.
- Root runtime11:53: SettingsPerformance показывает45groups,237/439 и notice low-powered device; следующий экран Effect Statistics оказался статистикой использования. Смысл237/439 ещё не установлен, НЕ называть числом enabled effects. Overridefalse. Raw1794560012072.xml и1794560063478.xml. Отдельно факт device restriction notice.
- Light DEX анализ уже имеет все app effects, m6,T5,V5,S5,g6 и выбранные UI refs; parse-dex.mjs теперь извлекает static fields, wide constants, calls/registers. Инструкция const-wide и field opcode известны. Вспомогательные графовые operator calls отделены от internal_id_declared: например Ink registration `ink`, а `ink-b` — другой referenced operator.
- Для уточнения T5.p2 constructor метаданных: immutable builder o заменяет третьеObject-поле (кандидат default), a — поле32 (возможно neutral); это нельзя обозначать подтверждённой семантикой без mapping. T5.w2 содержит literal definitionRange/standardRange, m6.U2 использует те же ключи. Следующий шаг: адресно связать их с полями p2 и ctor восьми missing bases, затем построить coverage.
- Последние файлы: EFFECTS_CATALOG.json9.5MB, BASE_DECLARATIONS.json ещё не создан, CATALOG_COVERAGE.md ещё не завершён. Не перезаписывать catalog без сохранения его версии/дополнений root.

## Полезные готовые материалы

- research/PRIORITY_BEHAVIOR.md и NEXT_COLOR_BEHAVIOR.md — собственная текстовая спецификация наблюдённых формул; runtime-parity не заявлен.
- raw/onelab-literal-schemas.json:137literal DSL схем/517params (из них509вnamed).
- raw/onelab-named-presets.json:1654construction instances,1545безpreview; не равны distinctvisiblepresets.
- raw/glitch-slider-schemas.json:107declared FloatSelectors с min/max, подтверждённый путь initGenericParameters→FloatSelector field stores. Defaults отдельноunknown.
- raw/onelab-registration-candidates.json, onelab-ui-string-locations.json, onelab-ui-color-candidates.json; candidate bytes не равны verified styling. x6.P — оранжево/бирюзовые UI constants; x6.a5 — отдельный LabCode/export dialog palette, не глобальная тема. MainActivity.onCreate→S6.J.k switchcase8→g5.a.a→x6.W0.J — реальная UI entry chain, детали layout ещё требуется исследовать.
- APK-derived raw DEX/GLSL/assets не включать в APK или публичный source archive. Сохранять links/hash/provenance и различать независимый DEX-анализ от настоящего REA Evidence.
