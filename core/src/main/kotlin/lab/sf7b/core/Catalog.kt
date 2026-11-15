package lab.sf7b.core

data class Param(val id: String,val ru: String,val en: String,val min: Float,val max: Float,val default: Float,val unit: String="",val integral: Boolean=false,val choices:List<String> = emptyList())
data class Definition(val id: String,val ru: String,val en: String,val category: Int,val params: List<Param>,val random: Boolean=false,val priority: Boolean=false,val algorithmVersion:Int=1)
object Catalog {
    val categoriesRu=listOf("Все","Глитч","Зеркала","Искажения","Цвет","Пиксели","Шум","Генераторы","3D","Свет")
    val categoriesEn=listOf("All","Glitch","Mirrors","Distort","Color","Pixels","Noise","Generate","3D","Light")
    private fun p(id:String,ru:String,en:String,min:Float,max:Float,default:Float,unit:String="",integer:Boolean=false)=Param(id,ru,en,min,max,default,unit,integer)
    private fun amount(v:Float=.3f)=p("amount","Сила","Amount",0f,1f,v)
    private fun size(v:Float=.06f)=p("size","Размер / сторона полотна","Size / canvas edge",.002f,.5f,v,"×")
    private fun angle()=p("angle","Угол","Angle",-180f,180f,0f,"°")
    private fun center()=listOf(p("cx","Центр X","Center X",0f,1f,.5f),p("cy","Центр Y","Center Y",0f,1f,.5f))
    private val legacyEffects=listOf(
        Definition("patchwork_break","Лоскутный разрыв","Patchwork Break",1,listOf(size(.12f),amount(.5f),p("density","Плотность","Density",0f,1f,.7f)),true,true),
        Definition("low_freq_banding","Низкочастотные полосы","Low Freq Banding",1,listOf(size(.1f),amount(.4f),p("shift","Сдвиг","Shift",0f,.5f,.05f)),true,true),
        Definition("multi_distort","Множественное искажение","Multi Distort",3,listOf(size(.15f),amount(.4f),p("layers","Число волн","Wave count",1f,8f,3f,"",true)),true,true),
        Definition("random_pixelate","Случайная пикселизация","Random Pixelate",5,listOf(size(.08f),p("variation","Разброс размеров","Size variation",0f,1f,.7f)),true,true),
        Definition("bloom","Мягкое свечение","Bloom",9,listOf(p("radius","Радиус / сторона полотна","Radius / canvas edge",.001f,.1f,.012f,"×"),p("threshold","Порог яркости","Luma threshold",0f,1f,.65f),p("strength","Свечение","Glow",0f,3f,.8f)),false,true),
        Definition("rgb_split","Разделение RGB","RGB split",1,listOf(amount(.04f),angle())),
        Definition("band_shift","Сдвиги полос","Band shift",1,listOf(size(.025f),amount(.15f)),true),
        Definition("scanlines","Строки развёртки","Scanlines",1,listOf(size(.006f),amount(.35f))),
        Definition("pixel_sort","Сортировка пикселей","Pixel sorting",1,listOf(p("low","Нижний порог","Low threshold",0f,1f,.2f),p("high","Верхний порог","High threshold",0f,1f,.8f))),
        Definition("mirror","Зеркало по осям","Axis mirror",2,listOf(p("axis","Ось: 0 X, 1 Y, 2 обе","Axis: 0 X, 1 Y, 2 both",0f,2f,0f,"",true))),
        Definition("radial_mirror","Радиальное зеркало","Radial mirror",2,listOf(angle())+center()),
        Definition("kaleidoscope","Калейдоскоп","Kaleidoscope",2,listOf(p("sectors","Секторы","Sectors",2f,32f,6f,"",true),angle())+center()),
        Definition("waves","Волны","Waves",3,listOf(size(.12f),amount(.12f),angle())),
        Definition("swirl","Закручивание","Swirl",3,listOf(amount(.5f),p("radius","Радиус","Radius",.01f,1f,.5f))+center()),
        Definition("displacement","Карта смещения","Displacement map",3,listOf(amount(.1f),size(.15f)),true),
        Definition("invert","Инверсия","Invert",4,emptyList()),
        Definition("threshold","Порог","Threshold",4,listOf(p("threshold","Порог","Threshold",0f,1f,.5f))),
        Definition("posterize","Постеризация","Posterize",4,listOf(p("levels","Уровни","Levels",2f,32f,5f,"",true))),
        Definition("gradient_map","Градиентная карта","Gradient map",4,emptyList()),
        Definition("palette","Палитра","Editable palette",4,emptyList()),
        Definition("pixelate","Пикселизация","Pixelate",5,listOf(size(.025f))),
        Definition("bayer","Дизеринг Bayer","Bayer ordered dither",5,listOf(p("levels","Уровни","Levels",2f,16f,3f,"",true),size(.002f))),
        Definition("floyd_steinberg","Дизеринг Floyd–Steinberg","Floyd–Steinberg dither",5,listOf(p("levels","Уровни","Levels",2f,16f,3f,"",true))),
        Definition("mono_noise","Монохромный шум","Monochrome noise",6,listOf(amount(.2f),size(.003f)),true),
        Definition("color_noise","Цветной шум","Color noise",6,listOf(amount(.2f),size(.003f)),true),
        Definition("grain","Зерно","Film grain",6,listOf(amount(.22f),size(.003f)),true),
        Definition("linear_gradient","Линейный градиент","Linear gradient",7,listOf(angle())),
        Definition("radial_gradient","Радиальный градиент","Radial gradient",7,listOf(p("radius","Радиус","Radius",.01f,1f,.65f))+center()),
        Definition("perlin","Шум Perlin","Perlin noise",7,listOf(size(.18f),p("octaves","Октавы","Octaves",1f,8f,4f,"",true)),true),
        Definition("voronoi","Ячейки Voronoi","Voronoi cells",7,listOf(size(.12f)),true),
        Definition("stripes","Полосы","Stripes",7,listOf(size(.08f),angle())),
        Definition("checker","Шахматный узор","Checkerboard",7,listOf(size(.1f))),
        Definition("sphere","Сферическая проекция","Sphere projection",8,listOf(p("radius","Радиус","Radius",.05f,.7f,.45f),angle())+center()),
        Definition("height_light","Освещённая карта высот","Lit height map",8,listOf(amount(.5f),angle())),
        Definition("emboss","Рельеф из яркости","Luminance emboss",8,listOf(amount(.5f),angle())),
        Definition("adjust","Цветокоррекция","Color adjustments",4,listOf(p("brightness","Яркость","Brightness",-1f,1f,0f),p("exposure","Экспозиция","Exposure",-4f,4f,0f,"EV"),p("contrast","Контраст","Contrast",-1f,1f,0f),p("shadows","Тени","Shadows",-1f,1f,0f),p("highlights","Светлые области","Highlights",-1f,1f,0f),p("white","Белая точка","White point",.01f,1f,1f),p("black","Чёрная точка","Black point",0f,.99f,0f),p("temperature","Температура","Temperature",-1f,1f,0f),p("tint","Оттенок ББ","WB tint",-1f,1f,0f),p("saturation","Насыщенность","Saturation",-1f,2f,0f),p("hue","Цветовой тон","Hue",-180f,180f,0f,"°"))),
        Definition("curves","Кривые RGB","RGB curves",4,emptyList()),
        Definition("color_swap","Замена цвета","Color Swap",4,listOf(
            p("tolerance","Допуск цвета","Color tolerance",0f,1f,.1f),p("hardness","Жёсткость выделения","Selection hardness",0f,1f,.5f))),
        Definition("lum_select","Выделение по яркости","Select by Luminance",4,listOf(
            p("target","Целевая яркость","Target luminance",0f,1f,.5f),p("tolerance","Допуск яркости","Luminance tolerance",0f,1f,.25f),
            p("hardness","Жёсткость выделения","Selection hardness",0f,1f,.5f))),
        Definition("channel_multiplier","Множитель каналов","Channel Multiplier",4,listOf(
            p("red","Множитель R","Red gain",-4f,4f,1f),p("green","Множитель G","Green gain",-4f,4f,1f),p("blue","Множитель B","Blue gain",-4f,4f,1f),
            p("hue","Множитель тона","Hue gain",-4f,4f,1f),p("saturation","Множитель насыщенности","Saturation gain",0f,4f,1f),
            p("luminance","Множитель светлоты","Lightness gain",0f,4f,1f),p("intensity","Интенсивность","Intensity",0f,1f,1f),
            options("style","Поведение за пределами","Range behavior",listOf("Wrap","Fold","Clamp"),2))),
        Definition("ink","Чернила","Ink",4,listOf(p("threshold","Порог","Threshold",0f,1f,.5f),p("hardness","Жёсткость","Hardness",0f,1f,1f),
            options("preserve_alpha","Прозрачность","Transparency",listOf("Opaque reference behavior","Keep source alpha"))))
    )
    // These v2 controls expose observed mechanisms. Their numeric ranges/defaults are sf7b
    // design choices until runtime/constructor evidence establishes the original UI mapping.
    private fun partitionParams()=listOf(
        p("depth","Глубина деления","Subdivision depth",0f,12f,6f,"",true),
        p("regularity","Регулярность","Regularity",0f,100f,25f),
        p("bias","Смещение деления","Split bias",-3f,3f,0f),
        p("thickness","Толщина границ","Border thickness",0f,20f,0f))
    private fun options(id:String,ru:String,en:String,values:List<String>,default:Int=0)=
        Param(id,ru,en,0f,(values.size-1).toFloat(),default.toFloat(),integral=true,choices=values)
    private val currentOverrides=mapOf(
        "patchwork_break" to Definition("patchwork_break","Лоскутный разрыв","Patchwork Break",1,
            partitionParams()+listOf(p("intensity","Интенсивность","Intensity",-100f,100f,20f),
                p("count","Повторы","Repeat count",1f,16f,1f,"",true),
                options("mode","Режим","Mode",listOf("Scale","Scale / fixed pattern","Shift X","Shift Y","Random axis","Long axis","Inverse scale","Cell shift","Cell shift / fixed pattern","Aspect shift","Rotation"))),true,true,2),
        "random_pixelate" to Definition("random_pixelate","Случайная пикселизация","Random Pixelate",5,
            partitionParams()+listOf(p("intensity","Содержимое ячейки","Cell content",-100f,100f,0f),
                options("palette","Палитра","Palette",listOf("Off","Nearest RGBA color"))),true,true,2),
        "low_freq_banding" to Definition("low_freq_banding","Низкочастотные полосы","Low Freq Banding",1,listOf(
            p("count","Число направлений","Ray count",2f,64f,12f,"",true),
            p("cell_size","Размер ячейки","Cell size",.01f,2f,.25f),
            p("angle","Направление","Direction",-180f,180f,0f,"°"),
            p("rotation","Поворот сетки","Grid rotation",-180f,180f,0f,"°"),
            p("offset_x","Смещение X","Offset X",-1f,1f,0f),p("offset_y","Смещение Y","Offset Y",-1f,1f,0f),
            options("reference","Вариант","Variant",listOf("Glitch Lab sweep","One Lab sweep"))),false,true,2),
        "bloom" to Definition("bloom","Мягкое свечение","Bloom",9,listOf(
            p("blurRadius","Радиус размытия","Blur radius",.001f,.1f,.012f),
            p("tolerance","Допуск яркости","Luminance tolerance",0f,1f,.35f),
            p("hardness","Жёсткость выделения","Selection hardness",0f,1f,.5f),
            p("intensity","Свечение","Glow intensity",0f,3f,.8f),
            p("balance","Баланс","Balance",0f,1f,.5f),
            p("normalization","Нормализация","Normalization",0f,1f,0f)),false,true,2),
        "multi_distort" to Definition("multi_distort","Множественное искажение","Multi Distort",3,listOf(
            p("intensity","Интенсивность","Intensity",0f,1f,.4f),
            p("variability","Разброс","Variability",0f,1f,.35f),
            p("lighting","Освещение","Lighting",-2f,2f,0f),
            p("layerCount","Число слоёв деформации","Distortion layers",1f,8f,3f,"",true),
            p("size","Размер области","Region size",.01f,1f,.15f),angle())+center(),true,true,2)
    )
    val effects:List<Definition> = legacyEffects.map{currentOverrides[it.id]?:it}
    fun byId(id:String):Definition=effects.firstOrNull{it.id==id}?:error("Unknown effect: $id")
    fun definition(id:String,algorithmVersion:Int):Definition=when(algorithmVersion){
        1->legacyEffects.firstOrNull{it.id==id}?:error("Unknown effect: $id")
        2->currentOverrides[id]?:throw IllegalArgumentException("Unsupported effect version: $id/$algorithmVersion")
        else->throw IllegalArgumentException("Unsupported effect version: $id/$algorithmVersion")
    }
    fun definition(effect:Effect):Definition=definition(effect.id,effect.algorithmVersion)
    fun newEffect(id:String):Effect=Effect(id,algorithmVersion=byId(id).algorithmVersion)
    fun builtinPresets()=listOf(
        Preset(name="Soft digital",effects=listOf(Effect("band_shift",mapOf("amount" to .025f)),Effect("rgb_split",mapOf("amount" to .008f)))),
        Preset(name="Hard glitch",effects=listOf(newEffect("patchwork_break"),newEffect("low_freq_banding"),Effect("rgb_split",mapOf("amount" to .045f)))),
        Preset(name="Mono grain",effects=listOf(Effect("adjust",mapOf("saturation" to -1f,"contrast" to .18f)),Effect("grain"))),
        Preset(name="Chromatic",effects=listOf(Effect("rgb_split",mapOf("amount" to .025f,"angle" to 45f)),Effect("gradient_map",mix=.25f))),
        Preset(name="Pixel print",effects=listOf(newEffect("random_pixelate").copy(params=mapOf("depth" to 7f,"regularity" to 60f)),Effect("bayer"))),
        Preset(name="Kaleidoscope",effects=listOf(Effect("kaleidoscope",mapOf("sectors" to 8f)),Effect("adjust",mapOf("saturation" to .35f)))),
        Preset(name="Abstract field",effects=listOf(Effect("perlin",seed=24),Effect("gradient_map"),newEffect("multi_distort").copy(params=mapOf("intensity" to .18f)))),
        Preset(name="Soft glow",effects=listOf(newEffect("bloom").copy(params=mapOf("intensity" to .65f,"blurRadius" to .018f)),Effect("adjust",mapOf("contrast" to -.08f))))
    )
}
