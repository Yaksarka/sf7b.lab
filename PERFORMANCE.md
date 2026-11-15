# Производительность sf7b.lab

Актуальная сводка **candidate11**, пункт2.5,14.11.2026. Новых benchmark-запусков в2.5 нет: ниже сохранённые измерения2.1/2.2/2.4 на Galaxy A51, debug0.2.0. Все новые значения — **одиночные прогоны**, не медианы. Цели скорости всего приложения пока не подтверждены.

App SHA256 `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`. Разные test APK и даты перечислены в [TEST_REPORT](TEST_REPORT.md); актуальный подтверждённый в2.4 test APK — ui-capture-tests SHA52f98f1e…6b12f. Исторические медианы candidates8–10 не переносятся на candidate11 автоматически.

## Пятнадцать смешанных слоёв: CPU preview

Из [CSV2.2](docs/verification/candidate11-a51-task2.2-20261113-2030/mixed-preview-cache.csv),13.11.2026. Документ и PNG-источник2000×2000, **расчёт preview512×512**,15слоёв, AndroidSources/Renderer. Pool48MiB, тестовый node cache64MiB; бюджет кэша проверен, это не peak/PSS.

| Фаза | мс | Image/text calls | Hits/misses |
|---|---:|---:|---:|
| Cold graph + decode + заполнение node cache |15408,53|3/4|0/31|
| Правка верхнего blend/opacity, warm cache |218,77|0/0|2/1|
| Тот же изменённый граф без node cache, decode уже прогрет |11381,32|3/4|0/0|

Warm и uncached изменённого графа совпали по всем premultiplied пикселям; blend действительно изменил результат. Кэш источников и compositing **реализован и проверен** в этом сценарии. Холодный расчёт15,4с остаётся проблемой отзывчивости. Его интервал включает начальное decode/запись кэша и отличается от warm edit; нельзя выдавать их отношение за универсальное ускорение приложения.

## Инвалидация: отдельный двухслойный CPU fixture

[CSV контрактов2.2](docs/verification/candidate11-a51-task2.2-20261113-2030/mixed-preview-cache-invalidation.csv). Документ2000² → preview512², **2слоя**. Все cached outputs совпали с uncached; изменения mask/effect/source действительно изменили пиксели.

| Фаза | Cached, мс | Uncached, мс | Cached image/text calls |
|---|---:|---:|---:|
| Первый cold baseline |1601,48|1179,33|1/1|
| Неизменённый warm |65,10|—|0/0|
| Маска верхнего text-слоя |410,89|1413,32|0/0|
| Ранний adjust |543,89|1187,45|0/0|
| PNG заменён по тому же пути |1277,60|1177,62|1/0|
| Повтор после замены PNG |32,80|—|0/0|

Эти mask/early-effect/source cases не являются15-layer измерениями. Версия заменённого PNG изменилась по length/mtime; подмена с сохранением обоих полей не проверялась. Неизменённые image/text rasters переиспользованы, зависимые результаты пересчитаны. Одиночные cached случаи могут быть медленнее uncached из-за записи кэша; повторяемый выигрыш всех операций не установлен.

## Малый portable document и полный model PNG

| Сценарий | мс | Границы |
|---|---:|---|
| 2.1: исходный CPU render15слоёв |3921|Canvas192×144,PNG130×154 после crop/turn,source64×48;50узлов/TTF/alpha |
| 2.1: render после portable import/reopen |2955|Тот же малый документ, exact pixels/PNG;не полноразмерные15слоёв |
| 2.4: EditorModel GPU PNG2000×2000 |3225|Один image-layer с5эффектами,alpha128,Document не изменён |

[Отчёт2.1](docs/verification/candidate11-a51-task2.1-20261113-2021.md), [model export2.4](docs/verification/candidate11-a51-task2.4-20261114-0108/model-full-export-performance.txt). Интервал model export включает queue wait, original load, render, PNG и file URI delivery; исключает system picker. Это иной интервал, чем render-only/kernel/GPU-only. Full-size исходник использован для2000² PNG; preview не источник экспорта.

Собственная GPU-корректность отдельно: [7chain prefixes2000×64](docs/verification/candidate11-a51-task2.4-20261114-0108/gpu-chain-prefix-parity.csv), maxerror1/over3=0. Она не является измерением скорости и не доказывает исходный паритет или full-size15-layer память.

## Что означают результаты относительно целей

| Цель из design/SPEC | Статус |
|---|---|
| Open usable preview≤3с; cached project switch≤3с | Пользовательские интервалы и три повтора не измерены |
| Прогретая простая коррекция≤1с | Warm upper blend219мс наблюдён в одном fixture; общая UI-цель не подтверждена |
| Тяжёлое изменение≤3с, обоснованные случаи≤10с | Матрица эффектов не измерена; cold mixed preview15,4с остаётся открытым узким местом |
| Обычный full PNG ориентир≤15с | Один model case3225мс ниже ориентира; сложные цепочки/15слоёв и повторяемость не подтверждены |
| Pan/zoom≥30FPS, отзывчивость/отмена | FPS, input latency, PSS/peak/GPU memory не измерялись в2.x |

Нужны сопоставимые входы1980×1080/2000²/3000²/крупный stress, цепочки1/5/10 и полноразмерные15слоёв; минимум3повтора с медианой/разбросом, cold/warm и условиями температуры/зарядки. UI-suite duration не является slider/import/open/export latency. Small fixtures, preview512² и budget cache не заменяют эту проверку. Эмулятор и оригиналы не измерялись в этих прогонах; быстрее/медленнее оригиналов приложение не объявляется.

## Исторические данные

Прежний PERFORMANCE candidate10 сохранён **побайтно**: [PERFORMANCE_CANDIDATE10_20261113](PERFORMANCE_CANDIDATE10_20261113.md),SHA256 `9915a6a6682f9fe5dd649a5bf081be98eeffba88acc37a1eb5974f4c06b9be10`. Там находятся медианы3прогонов для перечисленных простых графов candidates8–10, GPU-only PNG1764мс и одиночный model export candidate10 4159мс. Интервалы/fixtures различаются; эти цифры не служат новым сравнением candidate11.

Multi Distort CPU9 около18789мс в preview1024² остаётся историческим незакрытым узким местом; свежего измерения candidate11 здесь нет. Старое утверждение о кэше «только active effects» относится к прежней версии; актуальные source/blend/mask/early-node контракты описаны выше. [Более ранняя история](PERFORMANCE_HISTORY_20261113.md), [Windows RAM/tiled](PERFORMANCE_RENDER.md) — отдельные данные, не A51.

[TEST_REPORT](TEST_REPORT.md), аудит доказательств2.5 (материал отсутствует в этой копии), [STATE](STATE.md). Следующий после отдельного возобновления —3.1; новые измерения сейчас не проводились.
