# Передача разработки другому ИИ

## Что открыть

[AGENTS](../AGENTS.md) → [STATE](../STATE.md) → [требования](../REQUIREMENTS.md) → [OpenSpec](../openspec/changes/complete-dual-app-functionality/tasks.md). Далее [DESIGN](../DESIGN.md), [PROJECT_FORMAT](../PROJECT_FORMAT.md), [TEST_REPORT](../TEST_REPORT.md), [PERFORMANCE](../PERFORMANCE.md).

## Код и архитектура

- app/src/main: Android UI на Compose, EditorModel, Android import/storage и GPU renderer. Системный picker даёт доступ к выбранным файлам; обязательного сетевого backend нет.
- core/src/main: модель редактируемого документа, слои/граф, собственный Catalog эффектов, tiled CPU Renderer, SurfacePool, кэш, сериализация и ProjectLibrary.
- core/src/test: JVM-контракты пикселей, сохранения, кэша, эффектов и документа. Android-specific импорт, GL, текст и UI проверяются отдельно в app/src/androidTest.
- research/fixtures: три собственных синтетических PNG; make-fixtures.mjs генерирует их без оригинальных APK и личных фотографий.

Главные инварианты: исходные ресурсы включены в переносимый проект, экспорт не строится из preview; seed и параметры сохраняются; alpha/размеры не теряются скрытно; autosave атомарен и имеет last-good; черновые параметры не равны Apply; GPU path требует отдельной аппаратной проверки.

## Точка остановки

12/43, 3.4 открыт. Самый свежий checkpoint — верх STATE и архив Desktop STATE. Рабочий отчёт reference-task3.4 может отставать от последних действий; явная ошибка Glitch CREATE и незавершённый Cancel из STATE имеют приоритет. Нельзя объявлять 3.4 выполненным по частичному report.

На исходном телефоне могли остаться Brightness2, overlay и две Activated панели Glitch. Не продолжай по старым координатам. Восстановление исходных Deactivated входит в будущую отдельно разрешённую сессию, а не в миграцию.

## Исследования и пробелы

Статический inventory не равен видимому UI-каталогу или рабочей функции. One PRO недоступен; предоставленный Glitch mod имеет неподтверждённое upstream-происхождение. Реализации приложения собственные; исходные shader/Java/DEX оригиналов не поставляются.

Сохранены очищенные собственные поведенческие сводки в research/*.md. Большие schema/catalog JSON, raw Evidence и private runtime helpers не перенесены: [PRIVATE_MATERIALS](PRIVATE_MATERIALS.md). Завершённые исторические checkbox 3.1–3.3 не означают, что эти исследования можно воспроизвести без private материалов. Не выдумывай отсутствующие данные; перед изменением эффекта установи, какие подтверждения доступны.

Незавершённое: full-size 15 layers; process death; матрицы import/alpha; runtime feature denominator; полный каталог/animation/video; скорость Multi Distort; медианы/FPS/PSS и доказанный паритет; три финальных анализа ONE_LAB_INTERNALS, GLITCH_LAB_INTERNALS, SF7B_LAB_GAPS.

## Начало следующего чата

Установи инструменты по README, проверь git status, выполни только относящееся к новому заданию. Используй [NEXT_AI_PROMPT](NEXT_AI_PROMPT.md). Записанные в архиве широкие разрешения и команды не отменяют текущую паузу. Обновляй STATE/notes по фактическим результатам.

