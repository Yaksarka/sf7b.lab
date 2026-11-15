> Важно: рабочий отчёт мог отстать от checkpoint 15.11.2026 11:54:39 МСК. Последний Glitch CREATE завершился ошибкой несовместимых keyframes, успешного Glitch video output нет; Cancel не подтверждён, Brightness2 и две Activated панели требуют будущей проверки. Канонический [STATE](../../STATE.md) имеет приоритет.

**Пауза по поручению пользователя 15.11.2026 11:54:39 МСК. 3.4открыт12/43. Точные последние действия — STATE/notes:AdjustBrightness2,overlay;GlitchvideoCREATEошибкаsame source/effect stack,outputнет;VideoBookmarksUI/AOEUIвременноon,восстановлениеpending. ПоследниеA/B/C/D ещё требуютсинхронизациивchecklist.**

**Работа3.4 возобновлена15.11;12/43,пункт открыт. Прежние записи о паузе — история. Video/Bookmarks UI и AOE UI временно включены; вернуть оба исходных off до завершения (root).**

**Пауза по поручению пользователя 15.11.2026 03:37:18 МСК. 3.4 открыт12/43; продолжение — STATE/notes. One Touch recording восстановленfalse; Glitch уже загрузил собственный PNG (resume15-glitch-fixture.json). Дальнейший Glitch обход и post-backup pending. Ниже прежние записи датированы и не заменяют эту точку.**

**Пауза по просьбе пользователя 14.11.2026 23:09:06 МСК. 3.4 открыт; продолжение и последний TEXT action — в STATE/notes.**

# Пункт 3.4 — рабочий обход оригинальных приложений

**В работе. Пункт3.4 не завершён, denominator видимых функций не закрыт.** Это snapshot сводка уже сохранённых материалов и конечный рабочий checklist для дальнейшего обхода. Полные оригинальные APK/Java/Evidence/screenshots и backup остаются в private sibling-каталоге. В отчёте нет содержимого постороннего picker, личных проектов, имён файлов пользователя, аккаунтов или serial устройства.

Машинный coverage (материал отсутствует в этой копии), статические регистрации (материал отсутствует в этой копии), схемы (материал отсутствует в этой копии), [предыдущий отчёт3.3](reference-task3.3-20261114.md).

## Область и доказательства

Запись observed относится только к указанной части маршрута: видимый диалог не доказывает успешный экспорт/восстановление, а видимая кнопка не доказывает её действие. pending включает статические кандидаты и исторически видимые входы, ещё не подтверждённые свежим действием. blocked-gate означает наблюдённую границу доступа. Таблица не задаёт количество функций; регистрации effects/helpers/aliases не складываются с UI маршрутами.

Обозначения приватных Evidence: task3.4 root — sibling `.sf7b-private-reference-audits/20261114-task3.4`, task3.3 root — sibling `.sf7b-private-reference-audits/20261114-task3.3`. Все имена Evidence далее относительны соответствующему root; сами private файлы в поставку не включать. Исторические XML остаются историей, не свежей проверкой. Снимок coverage: 2026-11-15T00:34:18.366Z. Единственный actor телефона — root текущей задачи. Автор этой сводки читает уже записанные records и не управляет устройством.

## Точная идентичность и исходный backup

Повторно вычисленные SHA256 свежих pulls совпали с3.1:

- One Lab1.5.3 installed: `d4a93ba5e9a954a52b3e6af5a756cc485a621d71db22baa40e9c276dd8b1c7c3`, 32895049 bytes; `one-pull.json`, Evidence `ev_6ca60dff06f2d95ec81c1d7c0e63f41d07675323d2269dcbc2633933cf0f4882`. Supplied One отличается ресурсами; DEX совпадал в3.1, новая проверка содержимого DEX здесь не заявляется.
- Glitch Lab1.7.4 supplied mod/installed: `8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557`, 20130873 bytes; `glitch-pull.json`, Evidence `ev_1b7fe6378d3b677e51d4dcb18f35875d2f4c2161e5d0d913cd9214555b6c902f`. Официальное происхождение upstream не подтверждено.
- Свежий backup собственного lab.sf7b до опытов: `lab-before/backup.json`, время 2026-11-14T19:56:45.816Z; 44774912 bytes, SHA256 `3cde3d23a13261998a86962f7331e4729ed0c0a7932f45f299200b9815f7cc0e`. Сам TAR перепроверен на хосте; hash совпадает с checkpoint2.4. Это не постопытная сверка и не заявление о данных оригинальных приложений.

## One Lab: наблюдения14.11 до возобновления

Свежие settings records `one-initial.json`, `one-appearance-v2.json`, `one-ui-level-v2.json`, `one-menu-settings-v2.json`, `one-touch-settings-v2.json` со связанными `*-screen.json` показывают:

- Account FREE ·weekly free effect off; System theme,1.0×,Portrait; Advanced ·5of5.
- Theme Light/Dark/System, UI Scaling, Screen orientation.
- Show all parameters/effect tree/bookmarks/modifiers/parameter mapping. Видимость настроек не означает, что все их действия проверены.
- Expert menu on start, Include start module, Top Bar mode Overflow; дополнительные Touch recording/Randomize parameters/Snapping grid.
- Fling/Two-finger rotate/Two-finger move отдельно для Canvas и Parameters; Fling threshold0.03.
- Performance показывает preview target8ms, benchmark0.64,45effect groups,237/439,Low powered device: override restrictions. Это собственные строки UI, не измеренный benchmark этой задачи и не denominator видимых функций. GPULoad сюда не приравнивается.

Собственный fixture открыт: `one-fixture-loaded.json`. В save dialog `one-save-dialog.json` видны Save image,Encoding,resolution,Cancel,Save to file. Раскрытый Encoding на просмотренном `one-encoding-visible/screen.png` предлагает JPEG; видны тарифные области PLUS12MP/PRO24MP. Размер/MP на UI не подменяет decoded image metadata. После Save to file получен собственный файл: `one-identity-pull.json`, 199243 bytes, SHA256 `f0369b6dc4e59a71c60ed1fa19b0553c1c49c99a26b4faadcdb6a71f2865e07f`. 15.11 System.Drawing декодировал2000×1500,Format24bppRgbбезalpha; UI1280×960 не совпал с actual dimensions. Pixel comparison ещё pending.

`one-overflow-v2.json` подтверждает Settings,Tutorials,One Lab PRO,Bookmarks,Custom Effects,Custom Palettes,My Fonts,Video,FAQ,More apps by Ilixa. `one-video-route.json` показывает CREATE VIDEO, duration4.0s,resolution800.0KP,More...,Cancel/Create и сообщение об отсутствии bookmarks для keyframes. На этом этапе14.11 экспорт видео не проверялся; результаты15.11 приведены ниже.

| Route | Вход | Статус | Проверенная область / остаток |
|---|---|---|---|
| one.input.image | START → LOAD / SOURCE IMAGE | observed | Собственный fixture открыт; содержимое постороннего picker в coverage не включено. |
| one.input.generator | START → GENERATOR / RECENTS & SAMPLES | observed | EXPERT MODE START→GENERATOR открылFLAT сCOLOR/ASPECT RATIO/VIEW/SOURCE; Cancel вернулEXPERT. Сохранённый generator output иsamples path пока не проверены. |
| one.output.save-dialog | Top bar → Save | observed | Диалог с Encoding, resolution, Cancel, Save to file; указанное UI число MP не измерение размера файла. |
| one.output.encoding | Save image → Encoding | observed | В раскрытом списке наблюдался JPEG; иные форматы текущим наблюдением не установлены. |
| one.output.resolution-upgrade | Save image → resolution | blocked-gate | На просмотренном кадре показаны тарифные области PLUS12MP/PRO24MP; эти разрешения не экспортировались. |
| one.output.jpeg-file | Save image → Save to file | observed | Собственный JPEG извлечён, SHA/bytes повторно проверены; System.Drawing декодировал2000×1500,RGB24безalpha. UI показывал1280×960(3.0MP); actual dimensions отличаются. Pixel comparison с исходником ещё pending. |
| one.output.share | UI route не установлен | pending | Ресурс строки не подтверждает установленный маршрут. |
| one.tools.adjust | Карточка категории → ADJUST | observed | ADJUST: BRIGHTNESS0.0→0.010 кнопкой+,затем Cancel. CONTRAST/SATURATION/HUE/LUMINOSITY/TINT видны; остальные параметры/экспорт не проверены. |
| one.tools.rotate | Карточка категории → ROTATE | observed | ROTATE и ROTATE MODE panel открыты, COLOR OUT/ASPECT RATIO/ROTATION видны; затем Cancel. Фактический поворот+Confirm/export ещё не проверены. |
| one.tools.crop | Карточка категории → CROP | observed | CROP → ASPECT RATIO открыт,Variable aspect ratio/±/SOURCE видны; затем Cancel. Изменённый crop+Confirm/export ещё не проверены. |
| one.tools.eraser | Карточка категории → ERASER | pending | ERASER визуально серый; tap не открыл инструмент ни на TEXT, ни на подтверждённом нейтральном ADJUST. Причина disabled не установлена. Статическая регистрация содержит Plus-wrapper, но связь с toolbar.enabled не найдена. |
| one.tools.basics | Карточка категории → BASICS | observed | BASICS открыл ADJUST с BRIGHTNESS/CONTRAST/SATURATION/HUE/LUMINOSITY/TINT; Cancel вернулTEXT. Другие возможные варианты BASICS ещё не установлены. |
| one.tools.markup | Старт → MARK UP | observed | SHAPE/DRAWING/TEXT/COMBINE вошли в собственные previews: shape shadow/drawing stroke просмотрены,Combine MASK panel; первые3нетекстовых маршрута отменены,TEXT подтверждён отдельно. Полная composition/persistence не заявляется. |
| one.history.undo-redo | Top bar → Undo / Redo | observed | Визуально проверена одна правка TEXT: Undo заменил SF7B_TASK34_B на SF7B_TASK34, Redo вернул SF7B_TASK34_B. История остальных операций и её сохранение после перезапуска не проверены. |
| one.bookmarks | Top bar → Create Bookmark; Menu → Bookmarks | observed | Исходный собственный bookmark восстановлен после запуска. Созданы два TEXT-состояния; поздний список показывает все три. Manage включён фактическим switch, выбран только собственный TEXT_B. Экспорт и повторный импорт того же состояния проверены отдельно. Новые bookmarks после ещё одного restart не открывались. |
| one.custom-effects | Menu → Custom Effects | observed | Открыта пустая библиотека CUSTOM EFFECTS. Создание требует Plus/Pro; Import виден. Импорт собственного .lab ещё не проверен. |
| one.palettes | Menu → Custom Palettes | observed | Пустая библиотека приняла собственный sf7b-task34.gpl в FREE: сообщение об импорте палитры SF7B_TASK34 с 4 цветами, затем count 1. Edit открыл страницу UPGRADE ONE LAB. |
| one.fonts | Menu → My Fonts | observed | MY FONTS пуст. Добавление TTF/OTF требует Plus/Pro. UI сообщает о переносе шрифтов при экспорте bookmarks/custom effects; фактическая упаковка не проверена. Чтение системного Roboto не считается импортом. |
| one.menu | Top bar → Menu | observed | Settings, Tutorials, One Lab PRO, Bookmarks, Custom Effects, Custom Palettes, My Fonts, Video, FAQ, More apps by Ilixa. |
| one.settings.overview | Menu → Settings | observed | Сводка Account/Appearance/UI level/Menus & top bar/Touch & gestures/Performance/About. |
| one.settings.appearance | Settings → Appearance | observed | Видны Theme Light/Dark/System, UI Scaling1.0×, Screen orientation Portrait. Изменение/перезапуск не проверены. |
| one.settings.ui-level | Settings → UI level | observed | Advanced ·5of5; Beginner/Intermediate/Advanced; Show all parameters/effect tree/bookmarks/modifiers/parameter mapping. |
| one.settings.menus | Settings → Menus & top bar | observed | Touch recording временно включили: сводка показала 1 extra button, в редакторе TEXT появился Record Video. После опыта вернули выключенное состояние: no extra buttons. |
| one.settings.gestures | Settings → Touch & gestures | observed | Fling/Two-finger rotate/Two-finger move отдельно Canvas/Parameters; threshold0.03. |
| one.settings.performance | Settings → Performance | observed | Preview target8ms, benchmark0.64,45effect groups,237/439,Low powered device: override restrictions. Это строки UI, не замеры и не denominator. |
| one.settings.account | Settings → Account | observed | Открыт раздел Account; наблюдалась лицензия FREE. Покупки и остальные элементы раздела не проверялись. |
| one.settings.about | Settings → About | observed | Открыт About: Version 1.5.3, Tap for change log, Acknowledgements, Licenses, Privacy Policy, Statistics. Внешние страницы и контакты не открывались. |
| one.time.video | Menu → Video | observed | Панель2/3bookmarks,More Loop back/Easing/30fps; selection0/1 Create disabled,selection2 enabled. Create дал UI saved и actual MP4, проверенный отдельно:2000×1500/4.03333s/30fps; полный паритет/все режимы не проверены. |
| one.time.touch-recording | Settings → Menus & top bar → EXTRA BUTTONS | observed | Record Video открыл CREATE VIDEO с длительностью 10 s и размером 1033×775 в UI. После Create получен собственный MP4: 2000×1500, 10 s, 300 samples, 30 fps. Движение от жеста в записи не доказано. Позднее нажатие красной кнопки открыло новую конфигурацию; семантика Stop не установлена. Оба диалога отменены; настройка выключена. |
| one.effects.navigation | Старт → карточка категории | pending | Свежие entry menus RANDOM/DISCOVERY/GEOMETRY/GLITCH/TRANSMOGRIFY/STYLE/MARKUP/GENERATE/EXPERT наблюдались;Random→STREAK WAVES,Discovery→TOKAMAK;SearchBloom→Unlock. Полный individual effect/selector/условия denominator не закрыт. |
| one.projects | Bookmarks; отдельный Projects route не установлен | pending | Наличие Bookmarks не доказательство отдельного проекта или формата sf7b.lab. |
| one.input.restart | Запуск One Lab → START | observed | После нового запуска виден START с LOAD,GENERATOR,BOOKMARKS,RECENTS & SAMPLES; собственный fixture восстановлен через Bookmarks. |
| one.tools.text | MARK UP → TEXT → TEXT → ввод → Confirm | observed | SF7B_TASK34 введён и Confirm; затем edit вновь открыл эффект, текст заменён на SF7B_TASK34_B и Confirm. Проверка exported pixels/persistence после restart ещё pending. |
| one.tools.text-font | TEXT → FONT | observed | Просмотрен resume15-font-screen/screen.png: carousel ROBOTO/ALIEN LINES и следующий частично видимый вариант. Смена font и импорт пользовательского TTF не проверены. Это отдельный маршрут от Menu → My Fonts. |
| one.tools.text-transform | TEXT → TRANSFORM | observed | В просмотренных screenshots переключился значок gesture mode в правом верхнем углу; numeric transform dialog не наблюдался. Перемещение/rotate/scale жестом ещё не подтверждены. |
| one.navigation.weekly-promo | Recall own bookmark → promotion → Not now | observed | Промо WIREFRAME free until Sun22Nov наблюдалось и закрыто Not now; premium effect не применялся, его право/экспорт не проверены. |
| one.time.video-selection | Video → tap own tiles1/2 → Create | observed | Синий фон tile не означает selected. XML clickable parent Create:0selected=false,1selected=false,2selected=true; после выбора двух variants появляются номера1/2 и1033×775(800KP). Это наблюдение selection UI, не доказательство static enabling predicate. |
| one.time.video-render | Video с2selected frames → Create | observed | Собственный MP4 извлечён exact SHA/3472822bytes. Контейнер:2000×1500,avc1,4.03333s,121samples×30fps,1video/noaudio track. Native playback открыл файл; просмотрены различимые start/middle/end samples. UI1033×775 отличается; exact frame ordinals/полный121frame decode/alpha/parity pending. |
| one.tools.shape | MARK UP → SHAPE | observed | SHAPE CIRCLE и shadow preview визуально просмотрены; SHAPE carousel открыт, затем Cancel вернул TEXT. Подтверждённый shape output/persistence не заявляется. |
| one.tools.drawing | MARK UP → DRAWING | observed | CANVAS CIRCLE BRUSH: свайп дал красный stroke, визуально просмотренный на собственном fixture; затем Cancel вернул TEXT. Export/persistence stroke не проверены. |
| one.tools.combine | MARK UP → COMBINE | observed | BLEND WITH MASK открыт; MASK panel показывает LOAD/UNSPECIFIED/own bookmark tiles/PRESETS. Затем Cancel. Реальная composition с выбраннымиSOURCE2/MASK не завершена. |
| one.navigation.style | Старт → STYLE | observed | PRESETS/COLOR/TEXTURE/LIGHT/BLUR/FRAME menu наблюдалось; Back без применения дочернего эффекта. |
| one.navigation.transmogrify | Старт → TRANSMOGRIFY | observed | PRESETS/3D/ALCHEMY/STATISTICS menu наблюдалось; individual effect outputs не проверены. |
| one.navigation.generate | Старт → GENERATE | observed | PRESETS/SIMPLE/PATTERNS/TEXTURE/OPTICAL/INTRICATE/FRACTALS menu наблюдалось; Back без выполнения каждого generator. |
| one.navigation.expert | Старт → EXPERT MODE → scroll | observed | START иPRESETS,группыDISTORT/COLOR/LIGHT/TEXTURE/MIRROR/BREAK/ALCHEMY/BLUR/RETRO/GLITCH/STREAK/COMBINE/POLAR/3D/FRACTAL/SHAPE/TEXT/DRAWING/FRAME/STATISTICS,SEARCH/FAVORITES иtools видны на3страницах. Содержимое каждого selector не обойдено. |
| one.navigation.search | EXPERT MODE → SEARCH → Bloom → Search | observed | Открыто search for effects,введеноBloom,found1; Search перешёл кBLOOM. Проверка поиска других строк иFAVORITES pending. |
| one.effects.bloom-gate | Search Bloom → BLOOM | blocked-gate | НаблюдалсяBLOOM сUnlock this effect. Effect result/параметры/экспорт не получены,доступне обходился. |
| one.custom-effects.create | Menu → Custom Effects → Unlock | blocked-gate | UI прямо сообщает, что создание Custom Effects требует Plus/Pro. Недоступный результат не заявляется. |
| one.custom-effects.import | Menu → Custom Effects → Import | pending | Кнопка Import видна. Успешный импорт собственного portable effect пока не проверен. |
| one.palettes.edit | Menu → Custom Palettes → Edit own palette | blocked-gate | Создание палитр помечено Plus/Pro. Edit собственной импортированной палитры открыл UPGRADE ONE LAB; покупки не выполнялись. |
| one.fonts.add | Menu → My Fonts → Unlock My Fonts | blocked-gate | UI прямо требует Plus/Pro для добавления шрифтов. Доступный маршрут импорта в FREE не обнаружен; push/import шрифта не выполнялся. |
| one.navigation.favorites | EXPERT → Favorites | pending | Action targetFavorites привёлTEXT editor,затемCancel; отдельнаяFavorites list иadd/removefavorite не подтверждены. Причина перехода неизвестна. |
| one.time.touch-recording-motion | Touch recording → own fixture | pending | Жест изменил положение текста в редакторе после окончания 10 s записи. Его захват в MP4 не подтверждён. Имена stop-intent/stop-sent не устанавливают действие Stop: наблюдалось повторное открытие CREATE VIDEO. |
| one.bookmarks.manage | Bookmarks → Manage switch → own row | observed | Нажатие подписи Manage не изменило UI; actual switch открыл Delete/Export/Import. После выбора собственного TEXT_B появился 1 selected и доступный Export. Удаление не выполнялось. |
| one.bookmarks.portable | Manage → select own bookmark → Export → DocumentsUI Save | observed | Toast: exported 1 bookmark with 1 media file. Полученный .lab 27144 bytes проверен по SHA, ZIP CRC32 и структуре: version 1, одно выражение text(load-image), один исходный PNG. Вложенные bytes PNG полностью совпали с собственным fixture. |
| one.bookmarks.import-duplicate | Manage → Import → same own .lab | observed | Сообщение Imported 0 bookmarks, skipped 1 duplicates; count остался 3. Это проверка дубликата, не восстановление нового ID/проекта после потери данных. |

## Возобновление15.11: собственный bookmark и TEXT

Новый запуск привёл к START. `resume15-bookmarks.json` показал один собственный bookmark предыдущего опыта; `resume15-bookmark-recall.json` записал Set as current. После закрытия недельного промо Not now (`resume15-dismiss-weekly.json`) просмотренный кадр подтверждает прежний собственный цветовой fixture и шахматную подложку. Это проверка восстановления собственного исходного состояния, не полного паритета прозрачности exported pixels.

Свежий MARK UP содержит SHAPE/DRAWING/TEXT/COMBINE. В TEXT введено `SF7B_TASK34` (`resume15-text-set.json`), затем Confirm; просмотренный `resume15-text-confirm-screen/screen.png` показывает текст в результате. После первого TEXT Create Bookmark (`resume15-bookmark-b.json`), повторного edit→SF7B_TASK34_B→Confirm и Create Bookmark (`resume15-bookmark-c.json`) Video показывает3собственных tiles. Повторное открытие новых состояний после нового запуска пока pending.

В `resume15-font-screen/screen.png` просмотрен carousel ROBOTO/ALIEN LINES; выбор другого шрифта и импорт TTF ещё не проверены. `resume15-font-open-screen/screen.png` уже показывает закрытый carousel: не использовать его как доказательство открытого списка. TRANSFORM переключил gesture mode; числовой transform dialog и действие жестов ещё не наблюдались. Menu → My Fonts остаётся самостоятельным pending маршрутом.

Новые helper XML хранятся в private `ui-steps-20261115/`; точные root-relative XML/screen records привязаны к маршрутам в coverage. APK pulls и исходный backup выше сохранены14.11; этот resume не переименовывает старые проверки в новые.

### Video: selection и результат Create15.11

`resume15-video-two.json` и `resume15-video-three.json` показывают2/3собственных bookmark tiles. Раскрыты Loop back/Easing at start/Easing at finish/Frame rate30fps. До явного выбора frames Create был серым. Проверен XML clickable parent текста Create: `resume15-video-status.json` — enabled=false, `resume15-video-select-first.json` — false, `resume15-video-select-second.json` — true. Дочерний TextView имеет enabled=true во всех3случаях и не служит индикатором доступности кнопки. После выбора двух собственных TEXT variants появились номера1/2 и разрешение1033×775(800KP). Синий tile background сам по себе не означает выбор.

`resume15-video-create-selected.json` вернул редактор и сообщение Video saved to /external/video/media. Позднее извлечён actual MP4 и выполнена ограниченная host verification ниже; сообщение UI само по себе не служит её доказательством. Ранние попытки list/pull Movies/OneLab завершились ошибкой; это не доказательство отсутствия результата в MediaStore. Причина ранней disabled Create не названа license/topology gate.

Read-only static поиск не нашёл Create.enabled predicate в доступных snapshots. FAQ `onelab-v2/66640802df513a4f4e42cd94.java:22` (m6.A0) советует same source+same effects same order для keyframes; `onelab-v2/470407bac16ecd8c68d5b58a.java:1833` (T5.q.d) требует минимум2nodes для analyzeInterpolation. Эти сведения не связывают UI.enabled и не заменяют runtime selection. Нового JADX/телефонного действия для поиска не было.

### Собственные exports: host verification15.11

`resume15-video-pull-correct.json` — успешный REA pull собственного результата после точечного MediaStore lookup. Фактический parent directory Movies отличается от показанного UI Movies/OneLab. Проверенный файл3472822bytes,SHA256 `f518c6d21a570540c2d9573d97720bae17af6a7b53b57051366c3797b02ff100`. Ошибочные прежние pull/list сохранены и не посчитаны успехом. Собственный bounded ISO-BMFF parser `verification/inspect-own-mp4.cjs` дал `verification/one-media-container.json`:1video track,avc1,2000×1500,121samples,30samples/s,duration4.033333s; audio track нет. Это declared container/sample fields, не декодирование121кадра.

Windows System.Drawing действительно декодировал предыдущий JPEG:2000×1500,Format24bppRgb,alpha channel отсутствует (`verification/one-jpeg-decoded.json`). В точке0,0 RGB0/0/0,alpha255. Его UI показывал1280×960, а video UI1033×775; оба фактических файла2000×1500. Причина несоответствия UI/выхода не установлена. Pixel parity с original fixture не проверена.

Штатный WPF MediaPlayer/MediaFoundation открыл MP4:2000×1500,HasVideo=true,HasAudio=false,reported duration4.0s. Последовательное воспроизведение с выключенным звуком сохранило sample captures `verification/one-video-live-frame-0000.png`, `one-video-live-frame-2000.png`, `one-video-live-frame-3800.png`; player positions после PNG encode/count:0.328/2.612/4.0s; exact timestamp raster.Render не записан. Просмотрены start со строкой SF7B_TASK34_B, middle crossfade и end со строкой SF7B_TASK34. Это выборочные displayed frames, не exact ordinal0/60/120. Canonical result `verification/one-video-native-playback.json`.

Paused-seek attempt при4s повторил middle frame,изолированные3.9s attempts дали stale first frame; они сохранены в `one-video-native-decoded.json`/`one-video-native-near-end*.json` и не используются как last-frame proof. UTF8 BOM/JSON read encoding первого WindowsPowerShell запуска исправлены. Pbgra32 у PNG captures не доказывает video alpha. ffmpeg/ffprobe не найдены в ограниченном поиске доступных tools/runtime dirs; загрузок/установок не было. Все parser/scripts/decoded PNG остаются private.

### One tools: preview и отмена15.11

SHAPE CIRCLE+shadow (`resume15-shape-options-screen/screen.png`) и красный stroke CANVAS CIRCLE BRUSH (`resume15-drawing-stroke-screen/screen.png`) просмотрены визуально, затем Cancel. COMBINE→BLEND WITH MASK→MASK показал источники/UNSPECIFIED/PRESETS,затем Cancel; завершённая composition не заявляется. ADJUST→BRIGHTNESS изменён0.0→0.010,затемCancel. ROTATE MODE и CROP ASPECT RATIO открыты и отменены; реальные rotate/crop outputs не созданы. BASICS ведёт в ADJUST. ERASER выглядит disabled в текущемTEXT context,а tap не меняет экран; причина и другие contexts неизвестны,PRO gate из этого не выводится.

RANDOM MODE открыл STREAK WAVES,DISCOVERY —TOKAMAK; оба возвращеныBack. GEOMETRY иGLITCH семейные меню наблюдались с группами,но individual effect denominator не закрыт. Точные actions/XML/screens привязаны к соответствующим routes в coverage.

### Root menus, generator и поиск15.11

STYLE,TRANSMOGRIFY,GENERATE menu и3страницыEXPERT MODE наблюдались; точные видимые группы перечислены вcoverage. GENERATOR внутриSTART/EXPERT открылFLAT,затемCancel. Это входы/preview,не все generator outputs. SEARCH→Bloom показалfound1; Search открылBLOOM сUnlock this effect (`resume15-search-bloom.json`). Этот gate наблюдён вOneFREE; недоступный эффект не применялся. FAVORITES икаждый effect selector отдельно ещё не проверены.

### Libraries и Undo/Redo15.11

CUSTOM EFFECTS иMY FONTS initially empty; UI явно требуетPlus/Pro для созданияeffects/добавленияfonts. MY FONTS сообщаетTTF/OTF ипереносfonts вместе сbookmark/customeffect export; фактическая portable font inclusion не проверена. SystemRoboto был прочитан какprivatefixture,но доступногоfontimportroute не было,поэтому import не заявляется.

CUSTOM PALETTESinitially empty; собственный4colorGIMPpalette изsf7b-task34.gpl успешно импортирован вFREE (`resume15-palette-select.json`, `resume15-palette-result.json`),listcount1. Edit этогоownpalette открылUPGRADE ONE LAB (`resume15-palette-edit.json`); создание/редактирование иimport — разные доступности. Чужиеpalette/документы не использовались.

Независимо просмотрены `resume15-undo-screen/screen.png` и `resume15-redo-screen/screen.png`:UndoB→безB,Redo→B длястрокиSF7B_TASK34. ЭтооднаизменённаяTEXTпараметризация,не общаяпроверка истории всехинструментов. FAVORITES action привёлTEXTeditor; отдельнаяFavorites list/semantics остаютсяunknown.

### Touch recording и ошибки наблюдения 15.11

В Menus & top bar включили Touch recording; сводка показала 1 extra button, а редактор TEXT получил Record Video. Нажатие открыло CREATE VIDEO с 10 s и 1033×775. После Create последующий XML/idle завершился ошибкой, однако отдельные кадры и собственный файл подтверждают изменение состояния. `resume15-touchrec-pull.json`: 7460644 bytes, SHA256 `e742d11a621bb76937bac58fe8e43f4d0175716de0e94bb8c8b5618289232d05`; хеш перепроверен.

`verification/one-touchrec-container.json`: 2000×1500, avc1, 10 s, 300 samples при 30 fps, только video track. `verification/one-touchrec-native-playback.json`: файл открыт штатным декодером; три samples получены при Render positions 0.024/5.656/10 s. На просмотренных кадрах SF7B_TASK34_B остаётся на прежнем месте. Это выборочное декодирование, а не проверка каждого из 300 frames.

Жест transform выполнен после истечения 10 s, поэтому его захват в видео не доказан. Позднее нажатие красной кнопки, несмотря на имена stop-intent/stop-sent, открыло новую CREATE VIDEO. Семантика Stop не установлена. Конфигурация и редактор отменены; Touch recording выключили обратно (`resume15-touchrec-restored.json`: no extra buttons).

Ошибки `resume15-touchrec-dialog-error.json`, `resume15-touchrec-start-error.json`, `resume15-touchrec-stopped-error.json` содержат текст helper «No fresh UI hierarchy; no input sent». В составном действии ошибка after-dump может возникнуть после реального ввода; эта строка не доказывает его отсутствия. `action-attempts-resume15.jsonl` содержит попытку Create; экран, конфигурация и output подтверждены отдельно. Fallback gesture/tap сохранён в intent/sent после проверки foreground; это не равнозначно успешной проверке последующего XML.

Открыты Account с FREE и About с Version 1.5.3, Acknowledgements, Licenses, Privacy Policy, Statistics. Цены, контакты и учётные данные не переносятся в отчёт.

### Portable bookmark и исходный PNG 15.11

Bookmarks показывает три собственных состояния. Подпись Manage не переключала режим; actual switch открыл Delete/Export/Import. Выбран только собственный TEXT_B. Export через DocumentsUI Save дал сообщение о 1 bookmark и 1 media file; `resume15-bookmark-export-pull.json` сохранил 27144 bytes, SHA256 `121e44e4ac65f1b1f98858a66d20267e38a4630702cc6be4d6adf07b7fad6cae`. Удаление не выполнялось.

Собственный format-aware reader `verification/inspect-own-lab.cjs` проверил ZIP local/central headers, raw deflate, CRC32/SHA всех трёх entries и разобрал S-expression: version 1; один bookmark с text(load-image) и SF7B_TASK34_B; один cached PNG. Полные URI/исходные выражения остаются private. `verification/one-bookmark-archive.json` подтверждает, что PNG bytes полностью совпали с исходным `research/fixtures/sf7b-reference-grid.png` и `fixture-push.json`: 131111 bytes, SHA256 `b197dfc7979afbf797eb268931baa7477b36f2b5c9600e27b8b2d1e95a31597b`.

**Исходный fixture — 1280×960, RGBA8 (PNG color type 6).** Фактические JPEG и два MP4 — 2000×1500, то есть увеличены относительно исходника. Portable .lab сохранил исходные PNG bytes и выражение, а не только итоговую картинку. Это не доказывает восстановление в новой библиотеке или pixel parity.

Повторный импорт того же .lab сообщил Imported 0 bookmarks, skipped 1 duplicates (`resume15-bookmark-import-select.json`); count остался 3. Новое состояние, новый ID, восстановление без исходного Downloads URI и очистка библиотеки этим опытом не проверялись.

### ERASER: две проверки и статическая граница

После TEXT проверен и подтверждённый нейтральный ADJUST (`resume15-eraser-adjust.json`): кнопка серая, tap не открыл ERASER. В доступных snapshots не найден actual toolbar.enabled predicate. Найдена отдельная регистрация: `onelab-v2/479a7e473bea8a2d357d2806.java:570` задаёт l6.k.X; строка 921 оборачивает Eraser через l8.l. `onelab-v2/8ec1877dd7a3456b8f323919.java:7` создаёт M0 и сохраняет license metadata; `onelab-more/bcfb19d0199c82e70aa0066a.java:7` именует X как Plus. Это статическое ограничение зарегистрированного инструмента, а не доказанная причина текущей серой кнопки. PRO-gate по одному виду UI не выводится.

## Glitch Lab: runtime и статические кандидаты

Private task3.3 `glitch-main/f628807fd53ecd3f7b3f5972.java` — FragmentMain; `glitch-dispatch/29dddd1df553544efe67601d.java` — FragmentGlitch; `glitch-v2/abb04f8cb797f7b146d6cb1b.java` — Settings; `glitch-v2/d6c635b5d3ff19341d0f8bb1.java` — GlitchSettings. Подробные line pointers записаны в coverage.

В supplied mod hasPresetPack(String) буквально возвращает true; requiredPackagesAreInstalled тоже true. Это не upstream и не fresh runtime enforcement. Top movie/add bookmark зависят от showBookmarkUi; минимум2bookmarks проверяется при входе в video. FRAME — отдельный dev-only video-import путь: canHandleVideoImport→hasDev, начальная visibility GONE. Eraser и AOE add/subtract/intersect проверяют hasFull. Статические Settings задают video keyframes/duration/resolution по full/max/dev; текущий результат на mod ещё требуется наблюдать.

| Route | Вход | Статус | Проверенная область / остаток |
|---|---|---|---|
| glitch.load | Gallery → PhotoPicker → Browse → Download → own PNG | observed | Собственный PNG1280×960 открыт после START; preview grid подтверждён. Alpha экспорта pending. |
| glitch.camera | Top bar → Camera | pending | Статический путь; свежего runtime в этой редакции coverage нет. |
| glitch.history | Top bar → Undo / Redo | observed | После Brightness0→1+Confirm выполнены Undo иRedo. Canvas ROI x130..1079,y786..1595(769500pixels):Redo точен Confirm,Undo точен before;Confirm отличается от before758990pixels,maxRGB4,RGBA meanabs2.1799886. Это screenshot ROI одной правки,не полная история/экспорт/persistence. |
| glitch.save | Top bar → Save → PNG/current resolution → SAVE | observed | Собственный PNG1280×960 декодирован; alpha точна исходнику, RGB имеет различия. Подробности ниже. |
| glitch.share | Top bar → Share | pending | Статический путь; свежего runtime в этой редакции coverage нет. |
| glitch.menu | Top bar → overflow → Settings | observed | Batch Process/Settings/Preset Packs/Tutorials/8Bit Photo Lab видны; Settings открыт. Остальные действия pending. |
| glitch.tools.adjustments | Bottom toolbar → Adjustments | observed | Adjust открыт,BRIGHTNESS0→1 кнопкой+,Confirm. Реальное изменение preview подтверждено ROI comparison; остальные параметры/экспорт не проверены. |
| glitch.tools.crop | Bottom toolbar → Crop | pending | Статические ±90°,free,2:1,16:9,16:10,3:2,4:3,5:4,1:1 и обратные ratios. |
| glitch.tools.eraser | Bottom toolbar → Eraser | pending | Статические invert и hard/smooth brushes3sizes; текущий mod gate не наблюдался. |
| glitch.tools.aoe | Bottom toolbar / effect parameters → AOE | pending | Статические shapes/bookmarks,clear/invert/perturbate/soften,replace/add/sub/intersect,normal/intensity. |
| glitch.bookmarks | Top bar Add; Bottom Bookmarks; long press | observed | Add own1 выполнен;Bookmarks panel показал одну собственную thumbnail. После Brightness+Confirm выполнен Add own2;повторная панель/recall/restart persistence own2 пока не проверены. |
| glitch.time.video | Topbar horizontal swipe → Movie | observed | Interpolated animation/easing/loopback/NEXT;2own states видны,но только новый Adjust выбирается. Исходныйgrey,tap не выбирает;NEXT требует≥2frames. Причина неизвестна;рендер pending. |
| glitch.time.frame | Bottom toolbar → FRAME after video import | pending | Селектор создаётся GONE; runtime доступность не установлена. |
| glitch.projects | Bookmarks; отдельный Projects route не установлен | pending | Не доказан отдельный редактор проектов. |
| glitch.custom-effects | Маршрут не установлен | pending | Наличие самостоятельного соответствующего UI route не установлено; отсутствие не доказано. |
| glitch.palettes | Маршрут не установлен | pending | Наличие самостоятельного соответствующего UI route не установлено; отсутствие не доказано. |
| glitch.fonts | Маршрут не установлен | pending | Наличие самостоятельного соответствующего UI route не установлено; отсутствие не доказано. |
| glitch.effects.navigation | Bottom toolbar → группа → effect selector | pending | 21menu slot,включая separators и conditional alternatives; это не21видимый маршрут и не число функций. |

### Glitch: возобновление15.11

`resume-next-fixture.json` и `resume-next-fixture-screen.json` фиксируют повторную загрузку собственного PNG после START. `resume-next-glitch-menu.json` показывает overflow из5пунктов. `resume-next-glitch-settings.json`:4Megapixels,PNG,Quick save off,Video and Bookmarks UI Deactivated,AOE UI Deactivated,Use advanced effects Activated,MAX Activated. Это наблюдения supplied mod,не доказательство upstream лицензии.

Video/Bookmarks UI временно включён,explanatory OK подтверждён (`resume-next-bookmark-ok.json`). Затем включён AOE UI и подтверждён OK; `resume-next-aoe-ok.json` показывает оба Activated. **Исходное состояние обоих — off; восстановление ещё pending,root управляет телефоном.** Видимость интерфейсов не засчитывается как применение AOE,создание bookmark или video render.

### Glitch: собственный PNG export и decoded comparison

Save dialog показывал1280×960,current resolution,PNG. После SAVE извлечён собственный PNG9717bytes,SHA256 `d22cbe096b4c008fb50ce91fceddb3fc70b1f4a1002296a2c1532aadbe950e44`. Ввод helper намеревался дать имя `sf7b-task34-glitch-identity.png`,но field/output содержал `sf7b-task34-glitch-identity.pngpng.png`; причина неизвестна,это не установленный дефект приложения.

Private `verification/inspect-glitch-png-identity.ps1`/`glitch-png-identity-decoded.json` сравнили все1228800pixels через Windows System.Drawing. Оба1280×960/Format32bppArgb. Alpha отличается в0pixels:147840полностью прозрачных и220800частично прозрачных у обоих. Exact RGBA отличается в302525pixels,R/G/B maxabs255,RGBA meanabs6.5800212. После математического premultiply RGB×A/255 (alpha unchanged,double,tolerance1e-9) отличается155144pixels,maxabs0.5019608,meanabs0.0239935 в шкале0–255. Alpha loss здесь нет,но точное pixel identity отсутствует; причина RGB различий не установлена. Это один собственный fixture/PNG export; metadata/color-profile и поведение остальных эффектов не проверены. Первый запуск script безUTF8BOM не разобран WindowsPowerShell; исправленныйBOM запуск успешен.

### Glitch: Adjust, собственные bookmarks и Undo/Redo

Создан own1; Bookmarks panel показал одну собственную thumbnail. Adjust→BRIGHTNESS изменён0→1 кнопкой+,Confirm;затем Add own2. Вторая запись ещё не подтверждена повторным открытием panel,recall илиrestart. Остальныеbookmark actions не проверены.

Private `verification/glitch-undo-redo-roi.json`/script сравнили только заданный canvas ROI x130..1079,y786..1595 inclusive(950×810=769500pixels),исключая clock/toolbars. Redo vs Confirm:0различий. Undo vs before:0различий. Контроль Confirm vs before:758990из769500pixels отличаются,maxRGB4,RGBA meanabs2.1799886 по шкале0–255. Следовательно совпадения Undo/Redo относятся к двум действительно различным previews одной Brightness правки. Это screenshot comparison,не проверка full-resolution export или истории послеrestart.

### Glitch: первый вход Interpolated animation

Movie стал виден после горизонтального свайпа topbar. Панель показывает2own bookmark tiles,easing/loopback/NEXT. NEXT безselection явно сообщает At least2frames. После reopening выбор нового Adjust даёт номер1;исходный tile остаётсяgrey,повторныйtap не выбирает его,NEXT снова сообщает минимум2frames. Это не сводится к отсутствию попытки выбрать второй tile;причина недоступности неизвестна. Compatibility/topology gate по видуUI не установлен. Scoped поиск уже имеющихся task3.3glitch snapshots не нашёл телаVideoDialog/predicate;новыйJADX не запускался. Собственный video render ещё pending.

## Что остаётся

Обходить только собственные fixtures; для инструментов сохранять вход/параметры/подтверждение или отмену/результат. Bookmarks проверить как сохранённое редактируемое состояние, не переименовывать их в доказанный Projects route. Отдельные Projects/Open project/Save project в просмотренном наборе не установлены. Custom Effects/Palettes/Fonts проверять по реальному UI; там, где маршрут ещё не найден, отсутствие функции не утверждать.

Нужны fresh Glitch screens/actions и exports, One инструменты и сохранённые состояния, navigation по доступным группам/условиям, video/touch-recording в доступных границах, непроверенные output pixels/video alpha и постопытная сверка backup собственного приложения. Завершение ограниченного обхода 3.4 и закрытие feature denominator в 3.5 — разные решения. Статические кандидаты и ненайденные отдельные Projects/Share routes не объявлять реально существующими функциями. К 3.5 автоматически не переходить. Android/core/тесты/APK sf7b.lab этой подзадачей не менялись; новых build/core/device test claims здесь нет.
