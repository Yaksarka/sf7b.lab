# Пять приоритетных эффектов: статические наблюдения

13.11.2026. Это спецификация наблюдённых механизмов, не подтверждение runtime-паритета. UI-диапазоны и defaults не угадывать по shader uniforms. Полные байты исходных шейдеров находятся только в исследовательских raw и не должны включаться в sf7b.lab. В реализации использовать собственный код.

## Источники

- One Lab 1.5.3 / code138, SHA256 `aa90712cf7ff80ad8d2a3f36915002a5b26fb73c54355ecb05ca5720571ea7aa`.
- Glitch Lab 1.7.4 / code999: предоставлен файл с `mod` в названии, SHA256 `8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557`. Он не является аутентифицированным официальным эталоном. aapt2 сообщает отсутствующие ресурсы.
- One Lab DEX: строки (материал отсутствует в этой копии), классы и инструкции (материал отсутствует в этой копии). Индексы строк ниже — индексы в DEX string_ids, не номера строк JSON.
- Glitch DEX: методы приоритетов (материал отсутствует в этой копии), регистры и вызовы конструкторов (материал отсутствует в этой копии), классы (материал отсутствует в этой копии), выбранные GLSL (материал отсутствует в этой копии).
- DEX parser — дополнительный независимый анализ, не REA Evidence. REA-подтверждения отдельно в REA_FINDINGS.md.

## Patchwork Break

Glitch: `effect.glitch.BreakDichotomic`, также `PanDichotomic` имеет такое же отображаемое имя; не объединять варианты автоматически. Filter → `glitch_break_dichotomic.glsl` / `glitch_pan_dichotomic.glsl`.

Сначала исходный aspect-normalized прямоугольник делится рекурсивными случайными разрезами. Начальная ось Y, далее оси чередуются. Seed и путь по дереву задают разрез; regularity влияет на выбор длинной стороны и на степень случайности. Model origin задаёт bias разрезов; после шага bias уменьшается вдвое. Model scale задаёт глубину; разбиение останавливается также при размере ячейки меньше пикселя. Aspect ratio округляется до 0.01 для согласованности preview. Ось и seed должны быть глобальными для документа, не локальными для тайла.

После выбора ячейки деформируются координаты семплирования. Count повторяет процесс. Перед деформацией signed intensity преобразуется как знак × квадрат × 0.01. Наблюдённые ветви shader mode:

| Mode | Действие |
| --- | --- |
| 0/1 | Масштабирование вокруг центра ячейки; множитель 1.05 в степени преобразованной интенсивности |
| 2/3 | Случайный сдвиг по X/Y |
| 4 | Выбор оси по большей абсолютной случайной компоненте |
| 5 | Сдвиг по длинной стороне ячейки |
| 6 | Инвертированное относительно центра масштабирование |
| 7/8 | Сдвиг, зависящий от размера длинной стороны |
| 9 | Сдвиг, зависящий от отношения сторон |
| 10 и выше | Поворот вокруг центра |

В 1/8/9 разбиение сохраняется между итерациями count, в остальных seed меняется с номером итерации. Thickness создаёт линии разрезов; RGB границы смешивается с выбранным цветом по его alpha, alpha исходника сохраняется. Locus/mask может управлять интенсивностью до деформации либо финальным смешиванием в зависимости от режима.

UI конструктор обращается к `Parameters.Utils.getMODE_0_10`, count 1–10 PRO либо 1–100 developer, INTENSITY/INTENSITY_RELATIVE, RANDOMSEED, REGULARITY, THICKNESS, COLOR1A_PRO и LOCUS_MODE_WITH_CD. Название getter — статическое доказательство связи, полная схема его числовых диапазонов пока не интерпретирована. Точные defaults не подтверждены. Mode names UI не подтверждены.

One Lab содержит `effects.glitch.DichotomicBreak`, `effects.glitch.legacy.BreakDichotomicGL` и другие варианты; соответствие их visible name этому Glitch эффекту требует проверки регистрации/выполнения.

## Random Pixelate

Glitch: `effect.glitch.PixelateDichotomic` → filter того же имени → `glitch_pixelate_dichotomic.glsl`.

Та же seeded recursive partition, а не равномерная сетка. В каждой ячейке при intensity=0 используется **цвет центра**, не среднее по области. При ненулевой intensity выбирается координата `центр + (позиция − центр) × 1.01^intensity`. Особая ветвь в нуле означает, что эту intensity нельзя бездумно переименовывать в общий blend. Palette при наличии более одного цвета выбирает ближайший по RGBA цвет. Thickness/Color задают линии разрезов, их alpha сохраняет исходную alpha. Model transform управляет глубиной/bias; seed и regularity сохраняются отдельно. Финальный mix задаёт locus/mask.

Конструктор: PALETTES_8BIT, RANDOMSEED, REGULARITY, THICKNESS, COLOR1A, LOCUS_MODE_WITH_CD; затем привязывает intensity/count и transform. Числовые диапазоны/дефолты пока не подтверждены. One Lab классы `PixelateDichotomicGL` и новые pixelate-варианты не считать автоматически тем же visible effect.

## Low Freq Banding

Glitch: `effect.glitch.ColorPickAngular` → `filter.glitch.ColorPickAngular` → `glitch_color_pick_angular.glsl`. One Lab: `effects.glitch.LowFreqBanding`; DEX строка с `vec4 lowFreqBanding` содержит аналогичный механизм.

Это **не периодические тёмные полосы**. Для исходного пикселя берётся enclosing rectangle повторяющейся прямоугольной сетки. Сетка создаётся transform и размерами source; она не является recursive partition предыдущих двух эффектов. Из пикселя направляются count лучей под разными углами до ближайшей положительной точки пересечения с границей этого прямоугольника. Берутся цвета исходника или optional source2. Выбирается ближайший к исходному пикселю цвет по Euclidean RGBA distance; результат смешивается с исходным.

В Glitch угловой обход — phase + i×2π/count; в найденном One Lab shader — angle + i×π/count. Поэтому версии нельзя считать идентичными. В Glitch u_Mode объявлен, но в выбранном shader body не используется. Полный смысл режима через обвязку/варианты требует проверки. Source2 меняет источник выборки и размеры сетки. Для границы используется half-pixel inset по размеру источника. Нулевые размеры transform требуют собственной корректной обработки.

REА One Lab H(): `new s(5,100,100)`. Семантика класса `s` пока не установлена; эти числа **не являются доказанными диапазонами ползунков**. UI Glitch связывает intensity, source, mode, count, phase, model scale/position; min/max/defaults пока не подтверждены.

## Multi Distort

Glitch: `effect.distort.MultiDistort` → `filter.distort.MultiDistort` → `distort_multi.glsl`. One Lab: `effects.distort.MultiDistort`, DEX shader declaration `multiDistort(pos,outPos,modelTransform,intensity,variability,randomSeed,lighting,layerCount)`.

Координаты переходят в локальную систему model transform. Seed задаёт поле круговых очагов на решётке. Внутри очага последовательно работают: многомасштабное noise-смещение (marble), лепестковое масштабирование по углу (flower), радиальные волны (ripples), закручивание (swirl). Они зависят от одних стабильных случайных величин, а не представляют независимый random на каждом пикселе. При variability=0 проверяется один очаг; при ненулевой — соседство ±2 ячейки по обеим осям. Variability также смещает центры и влияет на радиусы.

One Lab добавляет layerCount с последовательными affine преобразованиями поля и increment seed между слоями. Lighting — **числовая интенсивность освещения**, не enum: из производных длины смещения получается градиент, влияющий на RGB; alpha семплированного источника сохраняется. Glitch и One Lab отличаются константами jitter, преобразованиями и слоями; алгоритмы хранить раздельно при необходимости.

One Lab presets: default, chaotic, lighted (строки схем в raw). REA H(): `new s(400,1)`, назначение этих полей не установлено. Glitch ctor keys: count, random, intensity, dampening, variability, model_scale/model_x/model_y. Полные defaults/UI limits не установлены; не назначать их по имени uniform.

## Bloom

One Lab REA `Bloom.r` подтверждает вызов графа `(bloom source1)`. DEX strings 2677 задают композицию: выделение яркости `lum-select` → `gaussian-blur2` → `bloom-combine`. Параметры: blurRadius, tolerance, hardness, intensity, balance, normalization.

Из string49380: яркость luma RGB; расстояние = abs(luma−target)/tolerance. При hardness=1 — жёсткий порог, иначе smoothstep от расстояния1 к hardness. RGB не меняется, alpha умножается на коэффициент выделения. В Bloom target=1. Необходима определённая обработка tolerance=0; наблюдённая формула сама не доказывает корректную runtime-ветвь нуля.

Из string49160: ветвь added = baseRGB + bloomRGB×intensity×bloomAlpha; ветвь blended смешивает baseRGB и bloomRGB с коэффициентом min(intensity×bloomAlpha,1). Balance смешивает эти две ветви. Normalization смешивает единичный коэффициент и обратный maxValue, где maxValue интерполируется от 1+intensity к1 по balance. Итоговая alpha равна baseAlpha. Следовательно размытие само по себе не реализует Bloom; важны выделение яркости, нелинейности alpha и normalize.

String3628 описывает gaussian-blur2 как вертикальный затем горизонтальный проход; radius standardRange0–1, default0.05 для общей функции blur. Это **не подтверждённый default самого Bloom**. Точный kernel/radius mapping ещё не установлен.

Glitch BloomGL отличается: параметры blur/radius, intensity, normalize, darken/hardness, gamma, transform. Его combine shader использует порог яркости размытого входа, усиленное добавление, деление на normalization и gamma в HSL. Не подменять его One Lab формулой.

## Что ещё нужно для паритета

Полная регистрация visible effects и preset-ветвей; фактические UI min/max/defaults; точное shared luma/random/masking/wrap поведение; Gaussian kernel и radius mapping; одинаковые тестовые входы и экспорт оригиналов; сохранение alpha/seed/масштаба; сравнение preview/full export; режимы и случайность на нескольких значениях. До этих проверок использовать статус «самостоятельный v2 по статической спецификации, runtime-паритет не подтверждён».
