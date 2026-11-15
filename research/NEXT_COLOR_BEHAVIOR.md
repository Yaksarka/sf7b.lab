# Следующая ограниченная группа Color

13.11.2026. Источник — One Lab1.5.3, SHA256 aa90712cf7ff80ad8d2a3f36915002a5b26fb73c54355ecb05ca5720571ea7aa. Независимые DEX наблюдения, не REA Evidence и не runtime-паритет. Shader source не копировать в приложение.

## Общие величины

- DEX string38880: luma = 0.2989R + 0.587G + 0.114B, сумма коэффициентов0.9999. Не Rec709.
- DEX string49462: RGB→HSL использует hue в градусах0–360, S и L нормализованы. Активный helper берёт HCV, L=max−chroma/2, S=chroma/(1−abs(2L−1)+1e-6). Alpha проходит без изменения. HSL→RGB оборачивает hue в360, ограничивает результат RGB0–1 и сохраняет alpha.
- Общая gate-функция: hardness1 даёт точное условие distance≤1; иначе smoothstep от distance1 к hardness. На нулевой tolerance требуется определённая обработка; наличие исходного деления не разрешает в собственной реализации NaN/Infinity.
- UI min/max/defaults этих конкретных One Lab параметров пока не установлены. Собственные нормализованные диапазоны хранить как implementation tuning. У scalar mix-intensity в shader рабочая единица0–1, не число процентов; это не доказывает отображаемый диапазон UI.

## Color Swap

Класс `com.ilixa.pap2.effects.color.ColorSwap`, model registration ID `color-swap` подтверждён литералом в r→H2.h0. Own stable ID допускается `color_swap`.

Параметры: colorIn RGBA, colorOut RGBA, tolerance, hardness. DEXstring49210. Расстояние — длина RGB разности между pixel и colorIn, умноженной на max(pixelAlpha,colorInAlpha), делённая на tolerance×sqrt3. Gate выбирает область сходства; результат — линейная смесь исходного RGBA и colorOut по gate. Следовательно colorOut.alpha намеренно меняет alpha. Это не коррекция saturation и не случайный обмен каналов.

Нужен один source. Для собственного UI можно сопоставить palette[0]/[1] входному/выходному цвету, но проект должен хранить эти значения явно и устойчиво. Тесты: выбранный цвет меняется; далёкий остаётся; hardness/tolerance меняют область; alpha выхода соответствует смеси; нулевая tolerance определена.

## Select by Luminance / Lum Select

Класс `com.ilixa.pap2.effects.color.LumSelect`, registration ID `lum-select`. Точное видимое имя проверять по оригинальному UI; не выдавать собственный перевод за observed label.

Параметры: target, tolerance, hardness. DEXstring49380. distance=abs(luma(pixelRGB)−target)/tolerance. RGB остаётся исходным, alpha умножается на gate. Это генерация выделения/прозрачности, не просто перевод в серый и не mix чёрным цветом. Один source. Проверять target низкой/высокой яркости, мягкие края, исходную alpha и tolerance0.

## Channel Multiplier

Класс `com.ilixa.pap2.effects.color.ChannelMultiplier`. Registration r читает ChannelMultiplierKt.b; `<clinit>` присваивает этому полю `channel-multiplier` на dex code unit78. Это подтверждённый original ID, а `point-gradient` в старой graph-эвристике был **вспомогательным оператором**, не ID данного эффекта.

Параметры: intensity, style, red/green/blue gains, hue/saturation/luminance gains. DEXstring49181.

1. Умножить RGB по каналам.
2. Style0 оборачивает каждую компоненту fractional part; style1 использует треугольное folding с периодом2; остальные style clamp0–1.
3. Перевести в HSL, умножить H/S/L на соответствующие gains. Hue modulo360. S/L проходят wrap/fold/clamp согласно тому же style.
4. Перевести назад в RGB и смешать с исходным RGBA по intensity.

Alpha сохраняется через helpers и mix. Intensity0 — identity. При gains1/styleclamp/intensity1 также нужен практически identity с обоснованным округлением. При stylewrap чистый RGB1 оборачивается в0; это наблюдённая семантика wrap, её нельзя скрытно заменить clamp. Default style неизвестен, поэтому собственный безопасный нейтральный default отмечать как собственное решение.

## Ink

Класс `com.ilixa.pap2.effects.color.Ink`, registration ID `ink` подтверждён r→H2.h0. `ink-b` встречается как оператор preset graph; не подменять им original ID. Own ID `ink`.

Параметры выбранного unary shader: threshold, hardness. DEXstring49245. Используется luma. При hardness1: ниже threshold чёрный, иначе белый. Иначе границы smoothstep — hardness×threshold и 1+hardness×(threshold−1). Результат RGB=(g,g,g), alpha=1. Оригинальный shader **намеренно делает opaque**; если sf7b.lab решит сохранять alpha для этого инструмента, это следует записать как осознанное отличие, а не заявить совпадение. Внешняя маска слоя/эффекта остаётся частью собственного графа, а не новой скрытой intensity исходного shader.

## Границы этой части

Эти четыре механизма достаточно определены для независимой функциональной реализации. Не обещать совпадение defaults, UI-компоновки, shared color precision или preview/export до отдельной проверки. Не смешивать ColorSwap с legacy colorSwap другой сигнатуры изstring49209. Тестировать alpha и параметры на маленьких аналитических фикстурах; затем сравнить экспорт оригинала.
