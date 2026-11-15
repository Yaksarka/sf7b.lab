# Состояние соответствия эффектов

13.11.2026. Данная таблица относится к исследованию оригиналов. Проверки текущей реализации sf7b.lab должны добавляться по фактическим результатам другими этапами; отсутствие их здесь не означает отсутствие кода.

| Эффект | Найден механизм/связь | UI схема | Собственная реализация/проверка | Сравнение экспорта |
| --- | --- | --- | --- | --- |
| Patchwork Break | Glitch BreakDichotomic/PanDichotomic→named GLSL, seeded recursive partition + cell transforms; One Lab аналогичные классы, name parity не установлена | Ключи/modes0–10/count variants обнаружены; defaults/ranges неполны | Проверять отдельно, версии алгоритмов сохранять | Не выполнено |
| Random Pixelate | Glitch PixelateDichotomic→named GLSL, seeded partition, cell center sample/scale, palette | Ключи есть; defaults/ranges неполны | Проверять center sample и discontinuity intensity0; не равномерная сетка | Не выполнено |
| Low Freq Banding | Glitch ColorPickAngular→named GLSL; One Lab LowFreqBanding→embedded GLSL. Цвет выбирается лучами на границах ячеек | Ключи есть; угол обхода Glitch2π против OneLabπ, обвязку уточнить | Не заменять синусоидальной яркостью; версии отдельно | Не выполнено |
| Multi Distort | Glitch named GLSL, OneLab embedded GLSL; seeded composition marble/flower/ripples/swirl, OneLab layers/lighting | Шейдерная сигнатура+presets; defaults/ranges неполны | Не выдавать одно глобальное sine warp за этот механизм | Не выполнено |
| Bloom | OneLab REA Bloom.r подтверждает graph; DEX lum-select→2pass blur→combine. Glitch другая combine/gamma версия | OneLab граф и blend semantics, blur shared standardRange/default; сам Bloom default не подтверждён | Проверять target selection, alpha, normalization; простой blur недостаточен | Не выполнено |

Факты и математические наблюдения: [PRIORITY_BEHAVIOR.md](PRIORITY_BEHAVIOR.md). REA Evidence и ограничения: [REA_FINDINGS.md](REA_FINDINGS.md). Полный набор статических кандидатов: EFFECTS_CATALOG.json (материал отсутствует в этой копии).

Статусы различаются: класс/ресурс найден → зарегистрирован/доступен → механизм установлен → независимо реализован → функционально проверен → сравнен с конкретным оригинальным экспортом. Ни один эффект пока не прошёл последний этап в рамках данного исследования.
