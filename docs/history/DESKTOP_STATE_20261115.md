> Архивный документ. Действующие инструкции: [AGENTS.md](../../AGENTS.md). Актуальное состояние: [STATE.md](../../STATE.md). Исторические пути/артефакты могут быть недоступны.

# Пауза внутри 3.4 — 15.11.2026 11:54:39 МСК

**Пользователь поручил записать последние изменения и остановить выполнение. Пункт3.4 НЕ завершён; OpenSpec12/43. Продолжать только после нового поручения; к3.5 не переходить.**

## Точка продолжения и незавершённые временные настройки

- **Glitch Lab сейчас в редакторе Adjust/Brightness со значением2 на собственном grid.** Эта последняя правка НЕ подтверждена. Попытка Cancel `resume-next-glitch-session-cancel` не вернула редактор в root: поверх приложения появилось системное уведомление; последнее сохранённое изображение всё ещё показывает Brightness2. Не считать Cancel успешным и не повторять координатный ввод вслепую.
- Последнее действие телефона — только inspect `resume-next-glitch-clean-editor.json`, XML private `ui-steps-20261115/1794732718420.xml`, screenshot `resume-next-glitch-clean-editor-screen/screen.png`. REA `resume-next-after-overlay-window.json` сообщает focus Glitch, но системная плашка перекрывает верхние кнопки. **Один focus guard не исключает overlay!** До нового ввода получить действительно открытый экран приложения. Уведомление/его содержимое не относятся к исследованию и не должны анализироваться или включаться в отчёты.
- Снимки `resume-next-glitch-session-cancel-screen` и `resume-next-glitch-clean-editor-screen` содержат постороннее уведомление: только private, исключить из поставки и публичных доказательств; не пересматривать ради содержания уведомления. Чужие сообщения/приложения не открывать.
- **В Glitch временно включены Video and Bookmarks user interface и Area of effect user interface(BETA). Их исходное значение — оба Deactivated; сейчас оба Activated. При следующем возобновлении после опытов ОБЯЗАТЕЛЬНО вернуть оба Deactivated и подтвердить свежим XML/screenshot.** Остальные настройки не менялись:4MP,PNG,QuickSaveoff,Use advanced effectsActivated,licenseMAXActivated(только предоставленныйmod).
- One Lab не возобновлялся в этой сессии; его Touch recording уже false по предыдущему checkpoint. lab.sf7b не запускалось,не обновлялось,не очищалось.
- Процессов observe.mjs/android-ui-resume15/screen-action на момент остановки не обнаружено. runtime_routes_low остановлен/завершён; старый runtime_routes с high не возобновлялся. После последнего запроса остановиться новых обращений к телефону не было.

## Новые обязательные правила делегирования

По явному решению пользователя15.11.2026:

- Прямые субагенты — модели не выше gpt-6.1-sol, reasoning не выше medium; предпочитать более простой вариант/low.
- Субагенты могут создавать своих субагентов. Субсубагенты — модели не выше gpt-6.1-terra, reasoning не выше medium; предпочитать low и передавать ограничения дальше.
- Это заменяет прежние sol-only/high/xhigh правила ДЕЛЕГИРОВАННЫХ агентов; параметры root этим решением не меняются. Старые экземпляры с high/xhigh не возобновлять с прежними параметрами.
- Записано в Desktop/AGENTS.md, явные overrides в OpenSpec design.md/proposal.md, а также отдельная memory note `.codex/memories_v2/extensions/ad_hoc/notes/2026-11-15-sf7b-delegation-caps.md`. Субагент runtime_routes_low запущен явно gpt-6.1-sol/low,forknone, работал только с report/coverage и собственными host-проверками, телефоном управлял root. Субсубагенты не запускались.
- OpenSpec list подтвердил существующий root; strict validation change complete-dual-app-functionality прошла0issues. Checkbox3.4 остался открытым.

## Что сделано после checkpoint03:37

1. Прочитаны STATE/notes и недавняя история действий; точные последние10phoneactions до возобновления сохранены private `last10-phone-actions-before-resume.json`. Это отдельный machine audit, не список всех toolcalls чата. Подключение A51 и REA CLI проверены заново.
2. Glitch после рестарта оказался START; прежний unsavedfixture не восстановился. Собственный PNG загружен повторно Gallery→PhotoPicker→Смотреть…→DocumentsUI→Скачанные. Выбран точный content-desc sf7b-task34-grid.png, не личные файлы. Свежий grid визуально подтверждён.
3. Overflow показывает Batch Process,Settings,Preset Packs,Tutorials,8Bit Photo Lab. Batch Process — обнаруженный отдельный маршрут, ещё не пройден. Settings baseline сохранён; штатно включены две панели с поясняющими OK-dialogs.
4. Save открыл PNG/1280×960/currentresolution. Из-за поведения ввода/суффикса actual имя стало `sf7b-task34-glitch-identity.pngpng.png`, а не введённое краткое имя. Это наблюдение составного helper/UI-ввода; причину не объявлять установленным дефектом оригинала. SAVE успешно, точный файл через REA выгружен.
5. Top Bookmark создал собственную закладку11:37:29; горизонтальная прокрутка нижней ленты показала Objects/Sort/Art/Retro/3D/Generate/Bookmarks/Adjust. В Bookmarks видна одна собственная thumbnailgrid — не чужая работа.
6. Adjust→Brightness:0→1 кнопкой+,Confirm,вторая собственная закладка11:41:12. Undo/Redo проверены содержательно: в фиксированном ROIхолста(130..1079,786..1595),769500pixels,Undo EXACTсовпал с до-правки,Redo EXACTсовпал сConfirm. Сама правка меняет758990pixels,maxRGB4,RGBA MAE2.1799886 по шкале0..255; поэтому совпадения не пустые. Private verification/glitch-undo-redo-roi.json+script.
7. Topbar horizontal swipe раскрыл Video. Диалог Interpolated animation имеет keyframes,easing,loopback,NEXT. NEXT без выбора требует минимум2. После выбора Adjust-keyframe исходная source-keyframe стала серой; tap по ней не выбрал второй кадр. Точная причина первоначального серого tile не установлена; найденный далее rendererror фиксируется отдельно.
8. Внутри новой Adjust-сессии созданы дополнительные собственные bookmarks A/B/C/D. A и B были сBrightness0: нажатие на шкалу800,2118 значение НЕ изменило, это явно подтверждено screenshot. Затем+дал1 и созданC; ещё+дал2 и созданD(последнее добавление как action,отдельный recall/D persistence ещё не проверен).
9. Вход Video из редактора автоматически пронумеровал3закладки текущей сессии. NEXT открыл outputconfig:800×600/0.48MP,4s,bitrate11.5Mb/s,имя261115_114832_Glitch.mp4. CREATE НЕ вывел видео, а показал явную ошибку: `The video cannot be rendered because the key frame images are not compatible. All key frames must have the same source image and effect stack.` ПослеOK вернулся effecteditor. Ошибка сохранена в resume-next-glitch-video-create.json/screen; **Glitchvideo output в этой сессии отсутствует.**
10. Гипотеза следующего опыта: проверить только два ненулевых состоянияBrightness1/2 одной сессии, без нулевыхA/B. Удаление нейтрального эффекта из stack — пока гипотеза, не установленная причина. На этапе попытки выйти из редактора появился overlay; дальнейший ввод приостановлен. Сейчас остановка по поручению пользователя, не продолжение опытов.

## Проверенный Glitch PNG

- Private `exports/glitch-identity-next/sf7b-task34-glitch-identity.pngpng.png`,9717bytes,SHA256 `d22cbe096b4c008fb50ce91fceddb3fc70b1f4a1002296a2c1532aadbe950e44`; REA `resume-next-glitch-export-pull.json`.
- System.Drawing decode1280×960 ARGB32. Все1228800alpha bytes точны исходному ownfixture:147840alpha0,220800partialalpha. Потери alpha в этом PNG не обнаружено.
- ExactRGBdiff302525pixels,max255; после premultiply RGB×A/255 отличаются155144pixels,max0.501960784 по шкале0..255. Точной pixelidentity нет; причину RGBdiff отдельно не утверждать. Это один нейтральный PNG предоставленногоmod, не паритет всего каталога.
- Private `verification/glitch-png-identity-decoded.json`, `inspect-glitch-png-identity.ps1`. Исходник `source/research/fixtures/sf7b-reference-grid.png`1280×960RGBA, прежнийSHA b197dfc7…31597b. One JPEG/video/.lab результаты из прежнего checkpoint остаются действующими и заново не выполнялись.

## Файлы и инструменты

- Android-source: `<repository>`. Канонические правила/план/state — Desktop/sf7b.lab. Рабочие report source/docs/verification/reference-task3.4-20261114.md и source/research/RUNTIME_ROUTE_COVERAGE.json: обновлены субагентом до части наблюдений; самые последние A/B/C/D и явная несовместимость могут ещё требовать синхронизации. 3.4не закрыт,featuredenominator3.5не создан.
- Private root прежний: `<local-private-path>`. Новые records начинаются resume-next-; XML в private/ui-steps-20261115. Полные APK/Evidence/сырые скриншоты/уведомления/exports/parsers/fixtures/backup вне поставки.
- observe.mjs использует private android-ui-resume15.mjs с ASCII type-text и action-attempts-resume15.jsonl (журнал ДОafterdump). Неудачный afterXML может быть после реального ввода; не верить автоматически тексту «no input sent». Для новых actions использовать новые label,wx не перезаписывать.
- ADB `<local-private-path>`; REA CLI `.codex/tools/rea/node_modules/rea-agents/scripts/rea.mjs`; process-local REA_ADB_PATH. Старый MCPpath по-прежнему отсутствовал, persistent config не менялся. Serial брать из freshdevices, не полагаться на hardcoded serial wrapper без проверки.
- Последний private lab.sf7b backup всё ещё lab-before/14.11T19:56:45.816Z,TAR44774912/SHA `3cde3d23a13261998a86962f7331e4729ed0c0a7932f45f299200b9815f7cc0e`. **Свежей post-сверки ещё нет.** Не заявлять сохранность актуальных данных только по историческому backup.
- Production/core/test source,APK и подпись sf7b.lab не менялись; сборок/установок/core/Android tests/эмулятора/benchmarks не было. Settings оригиналов менялись только описанными двумя временными переключателями Glitch; требуется восстановление.

## Следующий шаг после отдельного возобновления

1. Прочитать checkpoint/notes/правила. Проверить ADB/REA/currentfocus и получить экран без overlay перед вводом; не анализировать уведомления. Не нажимать отмену/создание/Video по старым координатам.
2. Уточнить effecteditorBrightness2/состояние собственной последней закладкиD. Закончить video-проверку только совместимой собственной пары1/2 либо честно сохранить ограничение; не называть ошибочныйCREATE успешнымэкспортом.
3. Довести Glitchcrop/eraser/AOE,bookmark recall/manage,эффектные семейства/presets/generator,Share вход+Cancel без отправки,Camera вход+Cancel без личных кадров,BatchProcess(новыймаршрут),форматы/настройки и boundedFRAME/devdiscovery. ОстаткиOne/coverage брать из рабочего checklist, не объявлять каждое название выполненнойфункцией.
4. **ВернутьVideo/BookmarksUI иAOEUI вDeactivated**, подтвердить; OneTouchrecording остаётсяfalse. Проверить текущий backup lab.sf7b и source/artifacts hashes, закончить report/coverage/evidence manifest. Только при фактическом завершении3.4 отметитьcheckbox и остановиться перед3.5 сSTATE/notes.

---

Ниже история, не текущий статус.
# Активно 3.4 — 15.11.2026 11:35:16 МСК

ВGlitchownPNG,VideoBookmarksUI+AOEUIвременноActivated—потомвосстановитьобaDeactivated. Прямыесубагентыsol/low(maxmedium),субсубterra/low(maxmedium);AGENTS+memoryобновлены. Прогресс12/43,3.5не начинать.

---

# Работа возобновлена — 15.11.2026 11:33:23 МСК

Продолжается только3.4 (12/43). Новые правила: прямые субагенты≤gpt-6.1-sol/medium,subsub≤gpt-6.1-terra/medium,предпочитатьlow;делегированиевнизразрешено. ЗаписаноAGENTS/OpenSpecoverride/memorynote;старыйhighагентне возобновлён. GlitchпослерестартаSTART,ownPNGсновазагружен. СейчасSettings:MAXActivated,Video&BookmarksUI=Deactivated,AOEUI=Deactivated,advanced=Activated;пока не переключались. Следующийшагисследоватьпанелиивконцевосстановитьисходныезначения. К3.5непереходить.

---

# Пауза внутри 3.4 — 15.11.2026 03:37:18 МСК

**Работа остановлена по прямому поручению пользователя. Пункт 3.4 НЕ завершён; OpenSpec 12/43. После нового поручения продолжить 3.4, к 3.5 автоматически не переходить.**

## Текущая точка

- One Lab остановлен через REA stop_adb_app; собственный пробный нейтральный ADJUST отменён Undo, текущим снова стал TEXT. Три собственные закладки и импортированная тестовая палитра оставлены для воспроизведения; чужие работы не открывались/не удалялись.
- **Touch recording возвращён в исходное false!** В `resume15-touchrec-restored.json`/screen сводка `no extra buttons`. Историческое предупреждение ниже о временном true больше не актуально. Другие настройки не переключались; покупки/restore purchases/сетевые ссылки не выполнялись.
- **Сейчас Glitch Lab: успешно загружен собственный `sf7b-task34-grid.png`1280×960 через Gallery→PhotoPicker→Смотреть…→DocumentsUI→Скачанные.** Последняя операция уже закончилась: `resume15-glitch-doc-grid-v2.json`, затем `resume15-glitch-fixture.json` (inspect, без ввода). XML: private `ui-steps-20261115/1794702870297.xml`, screenshot+REA JSON: `resume15-glitch-fixture-screen/screen.png` и `resume15-glitch-fixture-screen.json`. Локальный screenshot просмотрен: виден собственный grid, верхние Gallery/Camera/Undo/Redo/Save/Share/overflow и нижние Presets/PP1 Color/PP2 Art/PP3 Mirror/PP4 Schematics/Color/Streak/Repeat. Accessibility содержит Your image и восемь O: для canvas-кнопок нужны свежие screenshot/focus/bounds, не угадывать названия по XML.
- **НЕ нажимать Gallery повторно вслепую:** собственный fixture уже в редакторе. Новых эффектов/экспорта/закладок/настроек Glitch в этом пункте ещё нет. Изображение с белыми прозрачными областями в preview не доказывает alpha экспорта.
- Процессов observe.mjs/android-ui-resume15/screen-action на момент остановки не обнаружено; runtime_routes прерван. После поручения остановиться новых обращений к телефону не было — только чтение сохранённых файлов и запись checkpoint.

## Подтверждённое за возобновление 15.11

1. ADB37.0.1/REA CLI работали, A51 device. One после долгой паузы вернулся в START; несохранённый TEXT не восстановился. Единственная собственная прежняя закладка1794686701752 сохранилась и Set as current вернул grid/alpha; weekly promo закрыт Not now.
2. TEXT: введены SF7B_TASK34, затем SF7B_TASK34_B, Confirm. Own bookmarks1794700486873/1794700678068 созданы и позже видны вместе с исходной — всего3. Font carousel и жестовый Transform просмотрены. Undo вернул SF7B_TASK34, Redo — SF7B_TASK34_B (визуально подтверждено).
3. Shape Circle с тенью дал реальный preview; Drawing Circle Brush нарисовал красный штрих — визуально проверено, оба Cancel. Combine→Blend with Mask→MASK показывает Load/Unspecified/собственные bookmarks, затем Cancel; реальная двухвходовая композиция не проверена. Adjust Brightness0→0.010 кнопкой+, затем Cancel; Rotate Mode и Crop Aspect Ratio просмотрены. Basics открыл Adjust. Eraser grey/no-op в TEXT и подтверждённом нейтральном ADJUST; static wrapperX=Plus установлен, но точный toolbar.enabled predicate не восстановлен. Пробный ADJUST затем Undo.
4. Root меню Geometry/Glitch/Transmogrify/Style/Generate просмотрены; Expert3pages, Generator→FLAT, RandomMode→StreakWaves и Discovery→Tokamak доступны, изменения отменены. Search Bloom found1 открыл PRO license required, Cancel. Favorites action привёл в TEXTeditor; семантика Favorites остаётся неизвестной. Параметры/все элементы каждого семейства не объявлены полностью runtime проверенными.
5. Account/About и прежние Settings панели просмотрены. CustomEffectsCreate/MyFontsAdd требуютPlus/Pro. Собственная GPL-палитра SF7B_TASK34(4colors) импортировалась Free, но Edit привёл наUpgrade; покупка не выполнялась. Системный Roboto-Regular.ttf прочитан только как private test fixture, на телефон не импортировался, в продукт/поставку не входит.
6. Video: синие tiles сами по себе не selected.0/1 selected→Create parentdisabled; при двух выбранных появляются оранжевые1/2 и enabled=true. Проверены2собственных TEXT состояния; More раскрываетLoopback/Easingstart/finish/30fps. Вывод успешно выполнен.
7. Touch recording: временно включён extra button, вход Record Video из effectedit, отдельная10sконфигурация, успешный файл. XML idle ошибки происходили и ПОСЛЕ реальных действий; фраза helper «no input sent» не доказывает отсутствия ввода. Create записан в action-attempts-resume15.jsonl. Во время этой ветви использован screenshot/focus-guarded fallback для собственного transform; жест произошёл после10s, поэтому motioncapture в файле не засчитывается. Поздний tap красной кнопки открыл новую CREATEVIDEO — имя stop в логах не доказывает Stop. Второй новый файл не создавался; configuration/effect Cancel, настройка false восстановлена.
8. Bookmarks Manage включает отдельный switch(нажатие одного слова Manage не меняло режим). Выбран только собственный1794700678068, Export в DocumentsUI. Toast Exported1bookmarkswith1mediafiles. Повторный Import того же файла: Imported0bookmarks,skipped1duplicates; три прежние свои записи остаются. Это duplicate/portable artifact проверка, не clean-profile восстановление.

## Собственные результаты и их границы

Private exports лежат в прежнем root `20261114-task3.4`:

- `exports/one-identity/onelab_1794686542343.jpg`:199243bytes,SHA256 `f0369b6dc4e59a71c60ed1fa19b0553c1c49c99a26b4faadcdb6a71f2865e07f`. System.Drawing:2000×1500,RGB24,нетalpha,прозрачныйcorner сталopaque black. Pixel identity/цветовойпаритет не проверен.
- `exports/one-video-actual/onelab_1794700611792.mp4`:3472822bytes,SHA `f518c6d21a570540c2d9573d97720bae17af6a7b53b57051366c3797b02ff100`. AVC/H2642000×1500,container4.03333s,121samples×30fps,noaudio track. NativeWPF/MediaFoundation декодировал выбранные start/mid/end,виден переход _B→исходныйTEXT. Все121кадр не декодировались; точные ordinal/timestamps показанныхsamples не установлены (player position послеencode не timestamp Render).
- `exports/one-touchrecord/onelab_1794702046189.mp4`:7460644bytes,SHA `e742d11a621bb76937bac58fe8e43f4d0175716de0e94bb8c8b5618289232d05`. AVC2000×1500,10s,300samples×30fps,noaudio;3native samples декодированы,на них SF7B_TASK34_B в прежнем месте. Запись движения не доказана.
- Важно: UI показывал1033×775/800KP иMovies/OneLab, но actualvideo MediaStore `_data` — `/sdcard/Movies/имя.mp4`,2000×1500. Два неудачных обращения поUIfolder сохранены какошибки. JPEG UI1280×960(3MP),actual2000×1500. Это найденные расхождения, не паритет.
- `exports/one-bookmark/onelab_export_20261115_032821.lab`:27144bytes,SHA `121e44e4ac65f1b1f98858a66d20267e38a4630702cc6be4d6adf07b7fad6cae`. Собственный ZIP/version1,3entries,CRC/SHA проверены:1bookmark AST TEXT(load-image),1embedded sourcePNG. Embedded PNG1280×960/RGBA8,131111bytes/SHA `b197dfc7979afbf797eb268931baa7477b36f2b5c9600e27b8b2d1e95a31597b` — точные bytes исходногоfixture. Следовательно исходник в own.lab сохраняется;JPEG/MP4 здесь upscale. FullrawURI/AST внепоставки.
- Private `verification/one-bookmark-archive.json`, `one-jpeg-decoded.json`, `one-media-container.json`, `one-video-native-playback.json`, `one-touchrec-container.json`, `one-touchrec-native-playback.json` и samples/scripts. Неудачные/stale WPF seek drafts сохранены отдельно, не засчитаны успешным decode. ffmpeg/ffprobe не нашли, ничего для этого не устанавливалось.

## Материалы, инструменты и сохранность

- Единственный Android-source: `<repository>`. План/tasks/канонический STATE — Desktop/sf7b.lab. Отчёт: source/docs/verification/reference-task3.4-20261114.md; coverage: source/research/RUNTIME_ROUTE_COVERAGE.json. Оба рабочие, не доказательство завершения; последняя Glitch загрузка может ещё не быть внесена в конкретную строку checklist, она зафиксирована здесь.
- Private root: `<local-private-path>`. Полные originals/Evidence/screens/exports/backup/parser/scripts только там, не включать в APK/публичные исходники. Некоторые screenshots имеют incidental notifications/picker; не копировать в публичный отчёт и не использовать личные данные.
- ADB: `<local-private-path>`;REA CLI: `.codex/tools/rea/node_modules/rea-agents/scripts/rea.mjs`. Старый MCP REA_ADB_PATH указывал на отсутствующийSDK/adb; persistent config не исправлялся, CLI с process-local env работает. Небольшой device-not-found во время Manage исчез при freshdevices(transport_id изменился), настройки/USB/драйверы не менялись.
- Последний lab.sf7b backup: `lab-before/sf7b-private-app-data.tar`,44774912bytes,SHA `3cde3d23a13261998a86962f7331e4729ed0c0a7932f45f299200b9815f7cc0e`,14.11T19:56:45.816Z — совпал с2.4. **Новой post/resume сверки телефона не было.** lab.sf7b не запускалось/не обновлялось/не очищалось в3.4. Проверить сохранность после возобновления, не заявлять её свежей без сверки.
- Private observe.mjs — wrapper REA+UI; serial закреплён прежним наблюдением и перед использованием сверяется. С15Nov вызывает private android-ui-resume15.mjs: новые XML в private/ui-steps-20261115, ASCII type-text только единственный EditText, action-attempts-resume15.jsonl пишет ввод ДО afterdump. Предыдущие14Nov XML — source/research/runtime/ui-steps.
- screen-action.mjs — только документированный fallback ownOne послеXMLidlefailure, жёсткий foreground/bounds; по умолчанию использовать полноценный свежий XML/screenshot. Не использовать для Glitch без отдельной проверки текущего экрана и корректировки package guard.
- Production/core/test source и APK sf7b.lab не менялись. Сборок, core/Android suites, установок, эмуляторных прогонов и замеров скорости не было. Original applications не uninstall/clear; собственные пробные артефакты/закладки/палитра остаются явно тестовыми.

## После отдельного поручения продолжить

1. Прочитать этот checkpoint/notes и актуальные правила. Проверить tools/ADB/A51/foreground и свежий XML/screenshot. Не повторять Gallery/загрузку без проверки: Glitch уже на собственномgrid.
2. Glitch: пройти top overflow/settings, доступный effect/group browser, adjustments/crop/eraser/AOE/bookmarks/UndoRedo, image Save/Share вход+Cancel,Camera вход+Cancel без чужих кадров; собственные2keyframes→Video→форматы/параметры/экспорт. Проверить FRAME/dev gate наличия без обхода. Raster buttons плохо представлены вXML; опираться на свежие screenshots и package/focus guards.
3. One: довести только явно оставшиеся route вопросы из рабочего coverage; не выдавать bbox-click/видимуюкнопку за успешный результат. Eraser Plus static/greyFree/runtime predicateunknown сохранять отдельно; отдельные Projects/Share в выбранных root/menu/save не найдены, не выдумывать их. При необходимости проверить portable restore в изолированном собственномсценарии; текущий duplicate-skip не freshrestore.
4. Завершить отчёт/RUNTIME_ROUTE_COVERAGE, актуальный evidence/backing hashmanifest и post-сверку lab backup; оставитьPRO/mod/неизвестное явным. Только при фактическом завершении3.4 отметить checkbox и остановиться перед3.5 с обновлёнными STATE/notes. FEATURE_COVERAGE/3.5 ещё не начинался.

Пример проверки подключения (из source; не запускать до возобновления):

```powershell
$env:REA_ADB_PATH='<local-private-path>'
node '<local-private-path>' list-adb-devices --json
# Private observe.mjs ui НОВАЯ_МЕТКА PACKAGE inspect
# Существующие метки/JSON/screens не перезаписывать; одновременно только один phone actor.
```

---

Ниже история, не текущий статус.
# Активный checkpoint 3.4 — 15.11.2026 03:21:02 МСК

3.4не завершён12/43;подробностиnotes/рабочийотчёт. СейчасOneRecordVideoвсобственномTEXTedit. Touchrecordingвременноtrue — послеопытавернутьfalse! MP4/JPEGметаданныеиnativeplaybackпроверены,Oneосновныемаршруты/гейтызаписаны,Glitchruntimeещёвпереди. Source/APK/core/testsпрежние.3.5не начинать.

---

# Работа возобновлена — 15.11.2026 02:55:23 МСК

Пользователь поручил продолжить3.4. OpenSpec12/43,пункт открыт;текущий ход в notes.md. One own bookmark recalled/TEXT SF7B_TASK34 confirmed/второйbookmark создан;сейчас Video с двумя собственными состояниями. Ниже checkpoint паузы исторический;работа активна.3.5 не начинать.

---

# Пауза внутри 3.4 — 14.11.2026 23:09:06 МСК

**Остановлено по просьбе пользователя перед обновлением лимитов. Пункт 3.4 НЕ завершён, OpenSpec 12/43. После возобновления продолжать 3.4; к 3.5 и реализации автоматически не переходить.**

## Что сделано

- Прочитаны актуальные AGENTS/RTK/CONTEXT/WORKFLOW/CLEAN_ROOM, STATE/notes, OpenSpec proposal/design/specs/tasks и приложенные Downloads/tasks.md/design.md (исторические checkbox не источник текущего прогресса). Использованы openspec-apply-change и reverse-engineer-anything.
- REA MCP доступен, но list_adb_devices вернул capability_unavailable: старый `<local-private-path>` отсутствует. Scoped поиск локальной копии не нашёл. Официальный Platform-Tools загружен в `<local-private-path>`; ZIP SHA256 `45f4d63113e895ebde0c90f194099a4676b6ac653bd28d54314a9e022bbc1a99`. ADB37.0.1-15733141. MCP config/постоянные env/драйверы/сеть/питание телефона не менялись. Использован REA CLI6.8.0 с process-local REA_ADB_PATH. A51 был device; подключение при возобновлении проверить заново.
- Свежий private backup lab.sf7b `lab-before/sf7b-private-app-data.tar`: 44774912bytes, SHA256 `3cde3d23a13261998a86962f7331e4729ed0c0a7932f45f299200b9815f7cc0e`, время2026-11-14T19:56:45.816Z. Совпадает с checkpoint2.4. После опытов новый backup/сверка ещё НЕ выполнялись. Приложение lab.sf7b не запускалось и не обновлялось в3.4.
- Свежие REA package metadata и полные pulls: One1.5.3/code138, installed SHA `d4a93ba5e9a954a52b3e6af5a756cc485a621d71db22baa40e9c276dd8b1c7c3`; Glitch1.7.4/code999 SHA `8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557`, failures=[]/по одному base, прежние digest. One supplied отличается ресурсами; PRO нет; Glitch supplied mod/upstream не подтверждён.
- One: просмотрены Appearance/UI level/Menus & top bar/Touch & gestures/Performance. Настройки только раскрывались, значения/low-powered override не переключались. Delayed startup changelog закрыт штатным Ok; ранние guards отказали без ввода. Один uiautomator вызов пересёкся с ещё работающим предыдущим shell и завершился ошибкой; не считается успешным наблюдением. Далее все phone calls последовательно.
- Собственный `research/fixtures/sf7b-reference-grid.png`1280×960/SHA `b197dfc7979afbf797eb268931baa7477b36f2b5c9600e27b8b2d1e95a31597b` через REA push в `/sdcard/Download/sf7b-task34-grid.png`, digest телефона совпал; файл выбран через штатные PhotoPicker→Смотреть…→DocumentsUI→Скачанные. Чужие файлы не открывались; incidental picker XML/один снимок остаются только private, не использовать в поставке/отчётах.
- One Save→Encoding показывает JPEG; экспорт `/sdcard/Pictures/OneLab/onelab_1794686542343.jpg` успешно выгружен, 199243bytes/SHA `f0369b6dc4e59a71c60ed1fa19b0553c1c49c99a26b4faadcdb6a71f2865e07f`. Decoded dimensions/цвет/alpha/pixel comparison ещё pending; это НЕ подтверждение identity/паритета. Instagram prompt закрыт Later, внешние ссылки не открывались.
- One Menu→Video доступен: duration4.0s, resolution label800.0KP, no bookmarks message; More... был нажат, раскрытие не подтверждено, затем Cancel. Create Bookmark на собственной исходной картинке нажат один раз; реальное сохранение/повторное открытие ещё не подтверждено. Видео ещё не выводилось.
- Source edit/Modifiers просмотрены и Cancel; root прокручен до MARK UP/GENERATE/EXPERT MODE; MARK UP показывает shape/drawing/text/combine и нижние ADJUST/ROTATE/CROP/ERASER/BASICS.
- **Последний прерванный вызов успел выполниться:** `one-text-route.json`, performed=tap-description text, x602/y1954; XML `research/runtime/ui-steps/1794686814746.xml`, REA screenshot `one-text-route-screen/screen.png` и JSON сохранены. Открыт редактор собственного TEXT: TEXT/COLOR/FONT/TRANSFORM/MODIFIERS, Cancel/Repeat/Save/Create Bookmark/Confirm. Confirm не нажимался. Это точка продолжения, а не завершённый тест текста. На момент checkpoint процессов observe.mjs/android-ui.mjs больше нет; субагент runtime_routes остановлен.

## Где материалы

Единственный Android source: `<repository>`.
Private root: `<local-private-path>` — observe.mjs, полные REA JSON, screens, exports, APK pulls, lab-before. Всё это вне поставки. UI XML/actions собственного helper лежат в source/research/runtime/ui-steps; private wrapper JSON связывает точные пути.

Рабочие документы (НЕ итог закрытого обхода): source/docs/verification/reference-task3.4-20261114.md и source/research/RUNTIME_ROUTE_COVERAGE.json. Их read-only/static checklist подготовил gpt-6.1-sol/high; часть последних действий может ещё требовать внесения в строки coverage. FEATURE_COVERAGE.json/3.5 не создавался. Статика, доступный UI, успешное действие и результат учитываются отдельно.

## Следующий шаг после возобновления

1. Проверить ADB/REA/foreground и свежий XML+screen; не повторять вслепую tap text — он уже выполнен. Продолжить собственный TEXT либо Cancel с сохранённой исходной картинкой. Не заменять личные работы в оригиналах.
2. Завершить доступные One маршруты: shape/drawing/text/font/color/transform/combine, adjustments/rotate/crop/eraser/basics; root/expert/effect navigation, generators/random/presets; свои bookmarks create→recall/manage и custom effects/palettes/fonts. Подтвердить обнаруженные gates отдельно. Не открывать личные bookmarks/медиа ради исследования.
3. Создать минимум2собственных keyframes и проверить доступный One Video/More/экспорт; отдельно исследовать touch recording, если доступен. Не объявлять video working по одному видимому диалогу.
4. Glitch runtime в ЭТОМ пункте ещё не начинался (только metadata/APK pull). На том же собственном fixture пройти tools/crop/eraser/AOE/bookmarks/settings/export/video. Static подсказки в отчёте: movie требует≥2bookmarks; FRAME связан с dev/videoFrameRef, hasPresetPack в supplied mod=true — не upstream факт. Не путать bookmarks с полноценными portable projects.
5. Проверить собственные exports (размер/alpha/длительность/кадры по типу), сверить сохранность lab.sf7b и восстановить только свои изменённые настройки/следы при необходимости. Закончить RUNTIME_ROUTE_COVERAGE/отчёт/манифест Evidence и hashes. Только при фактическом закрытии3.4 отметить checkbox, обновить STATE/notes и остановиться перед3.5.

## Команды продолжения (из source; serial заново взять из devices)

```powershell
$env:REA_ADB_PATH='<local-private-path>'
node '<local-private-path>' list-adb-devices --json
# observe.mjs ui принимает: ui НОВАЯ_МЕТКА PACKAGE inspect|tap-text|tap-description|back|swipe TARGET
# observe.mjs screen принимает новую метку; существующие JSON/screens не перезаписывает.
# В observe.mjs serial закреплён историческим наблюдением: перед новым вводом сверить/обновить его по devices.
```

Production/core/test source и APK sf7b.lab не менялись; сборки/тесты/установки/эмулятор/измерения скорости не выполнялись. Оригинальные приложения не очищались/не удалялись. Полный обход/каталог/паритет не заявляется. Работа остановлена; продолжить только после нового поручения пользователя.

---

Ниже история, не текущий статус.
# Пауза после 3.3 — 14.11.2026, 22:48:45 МСК

**Пункт 3.3 выполнен; OpenSpec 12/43. Остановка перед 3.4 по поручению пользователя.**

- SCHEMA_COVERAGE.json (материал отсутствует в этой копии) schema3: базовые/наследуемые параметры, defaults/factories, три вида ranges, choices, объявленные единицы/преобразования, inputs, license wrappers и dispatch. Старые bytes сохранены отдельно в SCHEMA_COVERAGE_BEFORE_TASK3_3.json.
- One:214 прототипов;858 class profiles,531 со списками/5032 parameter positions;592 binding records/581operatorID.135 DSL headers/519rows,все414старых inheritance chains разрешены,parse errors0. В восстановленных native default/range expressions нет неразрешённых символов; constructor-dependent defaults отмечены. Два synthetic helper без standalone Java не зарегистрированы выбранными roots, их DEX metadata сохранена.
- Glitch:175 selector sites/174ID;380 непосредственных class graphs+1conditional profile;все120inlinegraphs.2239 class parameter slots;inline functional-string и legacy View arguments разделены. Factory defaults:1900values/377unset/30contextual source/1constructor-dependent/3runtime resource keys. Эти метрики не складываются в число функций.
- Все961registry slots сохранены, включая wrappers/aliases/условия. One licenses W=Free/X=Plus/Y=Pro, полный enum в контрактах. GPULoad не hardware gate. Числа без объявленной единицы не названы пикселями/процентами; unset не заменён midpoint; root-candidate не объявлен подтверждённым UI route.
- Контракты (материал отсутствует в этой копии), рядом JSON; отчёт и команды (материал отсутствует в этой копии); 17 passed host checks (материал отсутствует в этой копии), OpenSpec strict validation0issues. Это проверки extractor/evidence, не product/core/Android tests.
- Девять новых REA CLI Evidence канонически проверены, exactAPK digest/complete bodies/9-record bundle.2832export rows/backing Java сверены поSHA/bytes. ОбаAPK,84исторических rawJSON,11extractedDEX/GLSL прежние. Первый Glitch invalid_request и ошибочные/промежуточные выгрузки сохранены, не посчитаны успехом.
- REA MCP удалён из текущего набора; использовался CLI6.8.0/JDK17/JAR0.7.1,worker1,heap1024MiB. Старый AppData staging отсутствует. Durable private root: <local-private-path> Полные оригинальные Java/Evidence/драфты вне Android source и поставки. MCP import в3.3 не заявляется.
- One supplied/installed DEX совпадают, ресурсы различаются; One PRO недоступен. Glitch предоставленный mod, upstream не подтверждён. Static coverage не доказывает runtime видимость, пиксельный паритет или скорость.
- Android/core/test source,APK sf7b.lab,телефон и личные проекты не затрагивались. Сборок/установок/UI-ввода/runtime exports/эмуляторных опытов/измерений не было. Последний подтверждённый private backup проектов —2.4,01:18:29МСК/SHA3cde3d23…7cc0e; его актуальность сейчас не перепроверялась.

Следующий только после отдельного поручения — **3.4**: доступные маршруты оригиналов на собственных fixtures, свежие XML/screens/actions/exports с проверкой текущего подключения/backup. 3.4 не начат; к3.5/реализации автоматически не переходить.

Команды воспроизведения static анализа из source/research: node schema-task3.3-one.mjs → schema-task3.3-inheritance.mjs → schema-task3.3-glitch.mjs → schema-task3.3-coverage.mjs → schema-task3.3-contracts.mjs → schema-task3.3-verify.mjs. Требуются durable private snapshots; это не команды телефонного ввода.

---

Ниже история, не текущий статус.

## 14.11.2026, 22:36:59 МСК — 3.3, итоговая проверка

SCHEMA_COVERAGE.json schema3 создан; старые bytes сохранены в SCHEMA_COVERAGE_BEFORE_TASK3_3.json. One214 прототипов/531 параметризованный class profile/5032 параметр-позиции;592 уникальные binding records/581operatorID, все414 прежних цепочек наследования разрешены,135 DSL headers/519param rows,0parse errors и0неразрешённых defaults/ranges в восстановленных native профилях. Glitch175selector sites/174ID;380классов с графом,1с условными вариантами,120inlinegraphs. 961 регистрационная позиция сохранена. Состояние factory unset/context/resource отдельно отчисловыхdefaults; runtime3.4 не начат. Начата итоговая проверка REA, hashes, привязок и контрактов;3.3пока не отмечен. Android/core/APK/телефон не менялись.

# Пункт 3.3 выполняется — 14.11.2026 21:58 МСК

Пользователь поручил продолжить последние действия и остановиться перед 3.4. OpenSpec остаётся 11/43: 3.3 не отмечен. Используются openspec-apply-change и REA CLI; MCP недоступен после удаления инструментов. Данные One1889+8+2 и Glitch896 восстановлены в `<local-private-path>`, вне source/поставки; прежние AppData временные выгрузки отсутствуют. Hash-based filenames и export.tsv проверяются перед анализом.

One:214 прототипов; наследование пока в работе (последняя завершённая сверка398из414 старых inherited rows). Glitch:170 selector call sites;374 начальных графа из413 class candidates, включая helpers/группы — это не число видимых эффектов. Девять REA method Evidence завершились complete; первоначальный Glitch вызов по внешнему классу invalid_request сохранён, исправлен на Selectors$Utils. Черновики не являются runtime проверкой.

## 14.11.2026 22:18 МСК — 3.3, продолжается статическая сверка

One:214 прототипов,135 заголовков/519 параметров DSL (131именованный+4анонимных),412из414старых inherited rows разрешены;2ссылки ещё проверяются. Dossier восстановлен из конкатенации, случайный palette default оставлен динамическим. Ошибок разбора заголовков0; identity lambda/2фрагмента отдельно. Поддержаны inline static initializers, родительские конструкторы, typed wrapper/lambda dispatch; исправлены ошибки собственного symbolic reader в скобках/числовых выражениях и конфликте x2.i. Glitch175 selector call sites/413 class candidates,374графа; это static draft, не runtime и не итоговый знаменатель. Девять новых полных REA Evidence, первая invalid_request сохранена. Один ошибочный selector a.o отсутствует, отмечен в supplemental export.tsv;25 доступных классов проверены, отсутствие не скрыто. Все материалы3.3 в durable sibling private root. 3.3 открыт; перед3.4 остановиться. Телефон/product/APK не трогались.

Сейчас: завершить static schemas/defaults/units/choices/inputs/gates/dispatch и coverage/контракты3.3, затем обновить документы и остановиться. Телефон, Android/core/tests/APK и личные проекты не затрагивались. 3.4 не начинать.

---

# Пауза после пункта 3.2 — 14.11.2026 02:31 МСК

**Пункт 3.2 выполнен; OpenSpec 11/43. Пауза перед 3.3 по указанию пользователя.**

- Через REA CLI получены 20 полных успешных Evidence: One H2.<init>, H2.E и 17 helper-методов G–W; Glitch FragmentGlitch.initGlitchTypes. Каноническая целостность проверена, живой MCP принял импорт 20 записей (total61, unknowns0).
- One: 51 место создания групп — D29/F16/E6; 587 позиций. Сохранён фактический порядок 34 групп конструктора и 17 helper-групп, включая пять пустых начальных массивов. Исходное имя gltich_presets не исправлялось.
- Glitch: 24 constructor sites (23 списка и одно promotion action), 374 позиции, 21 menu slot. Из позиций 75 относятся к четырём preview packs, 299 — к menu constructors. Учитываются разные порядки Schematics, два separator, шесть nullable menu slots, семь nullable effect slots и beta-альтернатива FractalSolid/Simplified. Условия не выполнялись и не обходились.
- Независимая сверка полных Java bodies с DEX подтвердила **75/75 списков** по порядку типов, строковым аргументам и условным альтернативам. Неопознанных статических регистраций в этих корнях нет. Сохранены 186 наборов повторных ссылок и 10 именованных variant aliases; wrappers K0/L0/M0 не посчитаны новыми алгоритмами.
- Классифицированы все 1270 строк прежнего class-selector. One: 706 effect-type, 83 preset-type, 55 helper, 12 abstract; Glitch: 384 effect-type, 7 helper, 2 abstract, 21 group-container. Пять типов за пределами старого selector и три регистрационных wrapper перечислены отдельно. Интерфейсное наследование U0→l8 учитывается, ADJUSTMENTS Glitch выделен вне групп. Эти числа, позиции и aliases не складываются в число уникальных функций.
- REGISTRY_COVERAGE.json (материал отсутствует в этой копии) — актуальная карта регистраций 3.2. Рядом UNRESOLVED_REGISTRATIONS.json содержит пустой список в этом scope. Старые EFFECTS_CATALOG.json и raw сохранены. Отчёт и точные команды (материал отсутствует в этой копии); рядом result.json, java-dex-check.json и manifest.json.
- Повторно сверены два предоставленных APK, пять DEX member SHA и 84 исторических JSON. One supplied SHAaa90712c…ea7aa; установленный вариант SHAd4a93ba5…b1c7c3 известен из 3.1 и имеет другие ресурсы при одинаковом DEX. Glitch SHA8899fb87…69557 остаётся предоставленным mod без официального контроля. One PRO отсутствует. Нового phone pull в 3.2 нет.
- REA6.8.0/JDK17/JAR0.7.1: heap1024, наблюдённый989MiB, worker1, budget360000ms; одновременно только одна тяжёлая JVM. Переменные заданы в дочерних shell, persistent config не менялся. Успех CLI не доказывает успех MCP Glitch package scan с heap512. Полные оригинальные Evidence/Java/CFG находятся в LOCALAPPDATA/sf7b-lab-reference-audits/20261114-task3.2 и исключаются из поставки.
- Android/core/tests/APK sf7b.lab и телефон не затрагивались; сборок, product tests и runtime опытов не было. Личные проекты не читались. Последний подтверждённый private backup — 2.4, 01:18:29 МСК, SHA3cde3d23…7cc0e; в 3.2 его актуальность не проверялась.

Схемы и числовые параметры, внутренние preset collections/recipes, runtime видимость, полный feature denominator и паритет остаются открытыми.

Следующий только после нового поручения — **3.3**: базовые и наследуемые схемы, ranges/defaults/units/choices, входы, gates и dispatch; сохранить schema coverage и поведенческие контракты. К 3.4 и реализации автоматически не переходить.

---

Ниже история, не текущий статус.

# Пауза после пункта 3.1 — 14.11.2026 01:52 МСК

**3.1 выполнен; OpenSpec 10/43. Пауза перед 3.2 по указанию пользователя.** На A51 выполнялось только чтение package metadata и APK через REA. Android/core/test source и APK sf7b.lab не менялись; сборок, тестов, установки, запуска оригиналов, UI-ввода и изменения их данных/настроек не было.

- One Lab 1.5.3/code138: предоставленный APKPure — SHA256 `aa90712cf7ff80ad8d2a3f36915002a5b26fb73c54355ecb05ca5720571ea7aa`, 32813131 байт; установленный APK — `d4a93ba5e9a954a52b3e6af5a756cc485a621d71db22baa40e9c276dd8b1c7c3`, 32895049 байт. Из 1666 распакованных файлов 1662 совпали, включая DEX, AndroidManifest.xml, native и assets. Отличаются resources.arsc и три META-INF signing files. Сертификат одинаковый, apksigner v2 verified. Совпадение кода позволяет переиспользовать связанные static facts; равенство ресурсов/UI/runtime не установлено. Причина различия resources.arsc неизвестна.
- Glitch 1.7.4/code999: предоставленный mod и установленный APK совпали побайтно — SHA256 `8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557`, 20130873 байта, 1489 файлов; apksigner v1/v2/v3 verified. Официальный контроль и список mod-изменений отсутствуют. One Lab PRO недоступен по прежнему ответу владельца; закрытый runtime не проверен.
- Оба свежих REA package pulls завершились complete, failures=[]: у каждого один base.apk, splits не обнаружены. One: minSdk24/target36, arm64-v8a; Glitch: minSdk21/target36, arm64-v8a и armeabi-v7a. Пустой ABI в старом One inventory относился к каталогу lib/, не архитектуре. Package Manager сообщает installer com.android.vending для One и com.google.android.packageinstaller для Glitch; эти метаданные и подпись не аутентифицируют upstream.
- Проверены 84 JSON: 27 файлов содержат 28 уникальных REA Evidence, все прошли текущие schema/semantic-ID validation и повторный импорт в MCP; 47 JSON — host-derived, 10 — error-only. Пять static records содержат exact APK digest; 23 targetless records включают 17 capture, 3 start, 2 device-list, 1 client inspection. Текущие pulls не устанавливают exact digest исторической установки задним числом.
- Все 17 backing screenshots совпали с recorded SHA/bytes, 11 извлечённых DEX/GLSL — с соответствующими ZIP members; input/output SHA Glitch identity export также совпали. Отдельно инвентаризован 21 исторический runtime PNG/XML. Все 84 исходных исследовательских JSON остались прежними. Новый pixel-diff не выполнялся; полнота registry/schema этим аудитом не подтверждена.
- Свежий One inspect_android_package успешен: manifest complete, classes10801/resources1666, JADX JAR0.7.1 и bridge7 прежних SHA. Glitch package scan завершился provider_timeout120000ms при heap512; это ошибка/partial diagnostic, не успешный Evidence. Исторический успешный Glitch constructor использовал heap989MiB, One static — 494MiB, worker1. Неизменённый тяжёлый вызов не повторялся, настройки REA не менялись. Fresh bundle13 + historical import28 = combined41 records/0unknowns; каноническая целостность проверена. Импорт не исполняет старые операции.

- Приложенные Downloads/tasks.md и design.md прочитаны: tasks отличается только checkbox/переводами строк, design — заменой путей на <repository> в двух строках. Downloads не менялись. Единственный Android-source остаётся в Documents/ChatGPT/Сту/sf7b.lab.
- Отчёт и точные команды (материал отсутствует в этой копии); рядом result.json, evidence-audit.json и manifest.json. Private root: `<local-private-path>`. Оригинальные APK, полные Evidence и извлечённые материалы исключать из поставки. Личные проекты не читались.
- Candidate11 и результаты2.1–2.5 прежние. Последний подтверждённый private backup проектов —2.4, 01:18:29 МСК, SHA3cde3d23…7cc0e. В3.1 его актуальность не проверялась; новая сохранность проектов не заявляется.

Следующий только после нового поручения — **3.2**: закрыть One Lab ordered groups и Glitch FragmentGlitch.initGlitchTypes; классифицировать effects/helpers/aliases/presets и сохранить unresolved registrations. Учитывать оба One variant digest и ограничения PRO/mod/Glitch provider. Схемы3.3 и обход3.4 автоматически не начинать.

---

Ниже история, не текущий статус.

# Пауза после пункта 2.5 — 14.11.2026 01:36 МСК

**2.5 выполнен; OpenSpec 9/43. Пауза перед 3.1 по указанию пользователя.** В этом пункте обновлена только документация; новых Gradle/core/A51/эмулятор/REA-запусков, установок и действий на телефоне не было.

- Создан TEST_REPORT (материал отсутствует в этой копии) и обновлён PERFORMANCE (материал отсутствует в этой копии). На Desktop — полные читаемые копии со ссылками к каноническим доказательствам source. Прежний candidate10 PERFORMANCE сохранён побайтно в source/PERFORMANCE_CANDIDATE10_20261113.md, SHA9915a6a6682f9fe5dd649a5bf081be98eeffba88acc37a1eb5974f4c06b9be10.
- Локальный аудит доказательств (материал отсутствует в этой копии): 88 уникальных файлов / 107 проверок SHA/связей, включая APK4, манифесты, сохранённый build/Lint/core XML, этапные JSON/logs/CSV и9PNG. Это проверка файлов, не новые тесты продукта.
- В сводке разделены исторический полный core70pass/1skip/Lint0errors18warnings из1.3, три filtered core contracts2.3 (подмножество70), A51 2.1=1 / 2.2=2 / 2.3=1 / 2.4=20 и повтор одного UI-теста для кадров. Не выдавать отдельные запуски с тремя test APK за единый24/24 suite; старые ошибки fixtures, обрыв ADB и пересъёмки не скрыты.
- 15-layer portable:192×144→PNG130×154,source64×48,50узлов/TTF/seed/alpha,exact pixels/PNG. 15-layer cache:document2000²→preview512²,blend edit; source/mask/early-effect проверены отдельно в2слоя. **Полноразмерные15смешанных слоёв остаются pending.** Activity recreation не считается process death; свой CPU/GPU parity не считается паритетом оригиналов.
- PERFORMANCE больше не называет source/compositing cache нереализованным: v4-контракты подтверждены. Cold mixed preview15408,53мс остаётся узким местом; warm blend218,77мс и model GPU PNG2000²/alpha128/3225мс — одиночные прогоны, не медианы/UI-цели. Последний full PNG — один image-layer с5эффектами, не15слоёв. Исторический MultiDistort CPU9~18,8с не переименован в новое измерение candidate11. FPS/PSS/peak/cold-warm матрица открыты.
- Последнее подтверждение APK —2.4: app candidate11 SHA8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500; test ui-capture-tests SHA52f98f1ec63a869cee603c413edc555833f5f811861c0237485717567536b12f, certdd65119b…ea6d3. В2.5 их сохранённые байты повторно сверены, установленное состояние телефона заново не проверялось. Production и test source не менялись в2.5.
- Последний проверенный private checkpoint —14.11 01:18:29МСК,20261114-0118-task2.4-final-verified:44774912байт,SHA3cde3d23a13261998a86962f7331e4729ed0c0a7932f45f299200b9815f7cc0e,52файла. User manifests/assets/prefs/UUID/selection/обаindexes были побайтно сохранны после явного host cleanup только backup index;40файлов прежние,10собственных отчётов обновлены,2служебных изменились. Не заявлять сохранность обоих индексов без этого вмешательства. Private данные/ключи/полные Evidence вне поставки; актуальность backup позже01:18 не подтверждалась в2.5.

**Следующий только после отдельного возобновления — 3.1:** сверить идентичность двух оригинальных APK и пригодность сохранённых Evidence, версии/происхождение/ABI и ограничения PRO/mod. Исследование оригиналов в2.5 не начиналось. Этап2 закрыт только в указанном охвате; полный каталог/анимация/миграции/скорость/паритет проекта не объявлены завершёнными.

---

Ниже история checkpoint2.4 и более ранних этапов.

# Пауза после пункта 2.4 — 14.11.2026 01:22 МСК

**2.4 выполнен; OpenSpec 8/43. Пауза перед 2.5 по указанию пользователя.** App candidate11 прежний; обновлён только test package. Lab.sf7b остановлено.

- Итоговый A51 запуск: **20/20, без skip/failure** — ImportColorTest7, GpuRenderTest5, GpuChainTest1, EditorUiTest7. 01:13:41.617–01:14:54.893 МСК, runner68,636с/host73,276с. Дополнительный capture-only повтор одного теста тем/языков: OK(1),01:16:22–01:16:51 МСК. Это20различных контрактов, не21.
- **9 свежих сырых PNG** editor/settings RU/EN×dark/light и numeric keyboard визуально просмотрены. Настройки действительно видны, numeric поле не перекрыто IME, постороннего баннера нет. Каждый кадр связан с device mtime внутри своего producer-run и точными APK SHA; PNG не редактировались, contact-sheet — производный обзор.
- Первый запуск оборвался по ADB после14успешных статусов. Полный повтор20/20 обнаружил визуально преждевременный захват settings. Изменён только helper EditorUiTest.screenshot: waitForIdleSync+350мс. На новом test APK ещё раз прошли20/20; один кадр с баннером переснят повтором тем/языков. Прежние логи/неверные settings frames сохранены отдельно, баннерный оригинал только private. Production-код не менялся.
- Test-only сборка BUILD SUCCESSFUL4m2s/exit0. Текущий test APK: source/artifacts/sf7b-lab-0.2.0-candidate11-ui-capture-tests.apk,1177921байт,SHA256 `52f98f1ec63a869cee603c413edc555833f5f811861c0237485717567536b12f`; apksigner v2 verified, cert `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`. AppSHA `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500` подтверждён свежим REA pull; app не пересобирался/не переустанавливался, clear/uninstall не было. Из73source/config/tools файлов71совпали с1.2; отличия — только tests2.2 и2.4.
- GPU chain fixture2000×64:7префиксов,maxerror1,over3=0. Model GPU PNG2000×2000,alpha128,Document неизменен:3225мс,один прогон без system picker. Не медиана/не15-layer benchmark. Новый core suite/Lint/эмулятор не запускались; Generic core XML по2.3 содержит3filtered tests,70pass/1skip1.3 сохранены отдельно.
- Согласованный pre baseline: `<local-private-path>`,01:02:12МСК,44793344байта,SHA `97bde4446096090ee0a695f4a8974bad809832cb4330ea2de6dab03ae3af787a`,52файла восстановлены и сверены. Копия01:00 устарела по activeId/device-info; старый выбор2.3 поверх нового не возвращался.
- User documents/last-good/assets/metadata/legacy/prefs/current index/UUID/selection совпали с актуальным baseline. Только backup index после удаления своих fixtures возвращён к точным pre-test bytes через проверенный temp/rename при остановленном app. **Финальная private копия**01:18:29МСК: `<local-private-path>`,44774912байт,SHA `3cde3d23a13261998a86962f7331e4729ed0c0a7932f45f299200b9815f7cc0e`. Все52файла сверены с телефоном:40прежних совпали,10собственных отчётов обновлены,2служебных файла(device-info/profileInstalled) изменились; новых/пропавших0. Все пользовательские данные и оба library indexes побайтно сохранны. Private копии/распаковки/Evidence не включать в поставку.
- Отчёт2.4/команды (материал отсутствует в этой копии), JSON/SHA (материал отсутствует в этой копии), даты снимков (материал отсутствует в этой копии), обзор (материал отсутствует в этой копии). APK manifest связывает app/test/source/result SHA.

**Следующий только после отдельного возобновления — 2.5:** внести факты и ограничения2.1–2.4 в TEST_REPORT, PERFORMANCE и STATE. Различать host/native/visual, fixtures/full export и одиночные замеры/медианы. Полноразмерные15слоёв, process death, вся матрица миграций/каталога/UX/скорости не закрыты. 2.5 сейчас не выполнялся.

---

# Пауза после пункта 2.3 — 13.11.2026 20:50 МСК (история)

**2.3 выполнен; OpenSpec7/43. Пауза перед2.4 по указанию пользователя.** App candidate11/test cache-contract-tests-v2 остаются прежними, подтверждены свежими REA pulls до/после. Новой Android-сборки, установки и правок исходников не было. Lab.sf7b остановлено.

- Recovery-метод MixedDocumentDeviceTest: **OK(1),безskip/failure**,20:43:33–20:43:52МСК,runner15,242с/host18,475с. RU damaged current, EN missing current; ожидаемый last-good/repair/localized warning/no-repeat после повторного открытия прошли. Повреждались только собственные fixture32×24; реальные документы не повреждались. Это два конкретных сочетания, не вся матрица ошибок.
- Свежие host EditorContractTest: **3pass/0skip/0fail/0error**, Gradle14с/exit0,20:43:55–20:44:10МСК. Проверены неизменность bytes/mtime/revision/updatedAt/index при повторном save, repair восстановленной модели и защита last-good от повреждённого current. Generic core XML теперь содержит эти3filtered tests; полный70pass/1skip остаётся историей1.3 в сохранённых отчётах.
- Pre-test private backup20:43:10МСК: `<local-private-path>`,44793344bytes,SHA `3d2d189398a284677b8321cc2ea4c9b55340cbe5b4457dc00015a8137f33d846`; совпал с post2.2,52файла восстановлены и сверены с телефоном.
- После native50/52 прежних файлов совпали, потерянных/новых0. Изменены только служебный device-info и backup library index: @After оставил metadata последнего удалённого fixture UUID (3user+1fixture), current index/все прежние project manifests/assets/prefs неизменны. При остановленном приложении все52post-файла проверены; **только backup index** возвращён к точным pre-test bytes через проверенный temp/rename. Первая staging exec-in/dd передала512из605байт и была остановлена до rename; shell -T/cat прошёл проверку605байт, свой неудачный temp удалён. Это host cleanup следа теста, не заявление о полной неизменности индексов без вмешательства.
- **Финальный проверенный backup**20:47:04МСК: `<local-private-path>`,44793344bytes,SHA `dd7feac628d009c596049ea46fbcd5cbbed64ae7e0fd98a9a17c35217007b795`. Все52файла повторно совпали с устройством;51/52 прежних файлов побайтно прежние, единственное отличие device-info.txt. Оба library indexes, UUID/activeId/порядок, все прежние documents/last-good/resources/metadata/prefs сохранны. Файлов не добавлено/не потеряно. Промежуточные2045-post-test и2047-final — диагностические, не финальный checkpoint. Private копии/распаковки/Evidence не включать в поставку.
- Отчёт2.3/точные команды (материал отсутствует в этой копии), JSON/SHA (материал отсутствует в этой копии), рядом native/core logs,3-test XML без hostname,cleanup result. AppSHA `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`; testSHA `950213f0a35336bd1766cfed2d9ee30da277c625ac5e3acbdf25ad154eff9ed1`.

**Следующий только после отдельного возобновления —2.4:** проверить A51 и актуальность финального backup, выполнить затронутые import/GPU/UI contracts, сохранить новые screenshots с реальными датами и точной сборкой. 2.4 и2.5 не начинались; process death/вся матрица миграций/полноразмерные сценарии не закрыты. Android app/core production код не менялся со сборки1.3; Lint18warnings из1.3 не повторялся.

---

# Пауза после пункта 2.2 — 13.11.2026 20:40 МСК (история)

**2.2 выполнен; OpenSpec 6/43. Пауза перед 2.3 по указанию пользователя.** Candidate11 app остаётся установленным без изменений; обновлён только test package. После проверки lab.sf7b остановлено.

- MixedPreviewCacheDeviceTest: итоговый запуск 20:35:18–20:36:00 МСК, **OK (2 tests), без skip/failure**; runner39,21с/host42,255с. Exact pixels и provider calls подтверждены: 15 смешанных слоёв с upper blend, отдельный двухслойный fixture для source replacement/mask/early effect. Вход2000², preview512²; не полноразмерный export.
- Изменён только `app/src/androidTest/java/lab/sf7b/MixedPreviewCacheDeviceTest.kt`: добавлены недостающие native-контракты. 72/73 проверенных source/config/tools файла совпали с checkpoint1.2; production-код/app APK неизменны. Первая попытка: 15-layer тест прошёл, новый mask fixture упал из-за пустого head исходного Layer. Исправлен тест (`doc.layer(text.id)`), не renderer. Первая попытка/промежуточный test APK сохранены и не выданы за успех. Две последовательные test-only сборки успешны (2м56с и1м9с); core/app assemble/Lint не повторялись.
- Текущий проверенный test APK: source/artifacts/sf7b-lab-0.2.0-candidate11-cache-contract-tests-v2.apk,1177824bytes,SHA256 `950213f0a35336bd1766cfed2d9ee30da277c625ac5e3acbdf25ad154eff9ed1`. Apksigner v2 verified, прежний certSHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`. Повторные REA pulls подтвердили этот test APK и прежний appSHA `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`. Исходные APK1.4 сохранены отдельно. Clear/uninstall и изменения телефона не выполнялись.
- 15-layer cold15408,53мс, upper blend warm218,77мс, uncached/warm-decode11381,32мс. Mask410,89мс/early effect543,89мс/source replacement1277,60мс (двухслойные). Все замеры одиночные debug CPU, не медианы/UI-интервал/паритет. Холодный расчёт пока медленный; это не закрытие целей производительности. Source replacement проверен при изменении length/mtime.
- Private backup до тестов: `<local-private-path>`, фактически20:26:49МСК,44790272bytes,SHA `944c04b116b74fe1a959dcac5b1f172dbaa04eb3c3e280fc38867e6ad15b5f61`; совпал с post2.1, все50файлов восстановлены и повторно сверены с телефоном. После обеих попыток **50/50 прежних файлов неизменны**, включая оба library indexes, все manifests/resources/prefs.
- Post-test private TAR20:36:19МСК: `20261113-2036-task2.2-post-test`,44793344bytes,SHA `3d2d189398a284677b8321cc2ea4c9b55340cbe5b4457dc00015a8137f33d846`; все52файла сверены с устройством, потерь0/изменений прежних0, добавлены только2CSV. Private TAR/распаковки/Evidence не включать в поставку.
- Отчёт2.2 (материал отсутствует в этой копии), JSON/SHA/оба запуска (материал отсутствует в этой копии), рядом CSV/logs/test-change.patch. Манифест нового test APK связывает его с sourceSHA и resultSHA.

**Следующий только после отдельного возобновления — 2.3:** проверить A51 и актуальность private backup, затем damaged/missing current, last-good, идемпотентный save и RU/EN warning/repair, сохраняя чужие UUID/manifests/prefs. Установленный cache-contract-tests-v2 содержит неизменённый recovery-метод MixedDocumentDeviceTest; он пока не запускался. 2.4 и2.5 не начинались. Исторические core70pass/1skip/Lint0errors18warnings — сборка1.3.

---

# Пауза после пункта 2.1 — 13.11.2026 20:23 МСК (история)

**2.1 выполнен; OpenSpec 5/43. Следующий — 2.2, ещё не начат.** По указанию пользователя работа приостановлена после подпункта. Candidate11 установлен обновлением на A51 без clear/uninstall; точные app/test SHA подтверждены повторной REA-выгрузкой. После теста lab.sf7b остановлено.

- Установлены сохранённые artifacts 1.4: app SHA256 `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`, test `05084940d2ec9827fd76562b31a0ed321b339b72fd484cfcb3aecc907f379a2a`. Повторная apksigner v2 проверка обоих успешна; cert `dd65119b…ea6d3` совпадает с candidate10. До установки свежая выгрузка подтверждала candidate10. Новой сборки/правок Android-кода не было; 73 выбранных source/config/tools файла совпали с checkpoint1.2.
- Только метод `MixedDocumentDeviceTest#fifteenMixedLayersKeepTheirEditableResourcesMasksSeedsAndRenderedPngAcrossPortableCopies`: **OK (1 test)**, без skip/failure, 20:18:40–20:18:59 МСК; runner14,88с, host18,655с. 15 смешанных слоёв/50 узлов, embedded TTF, seed/copy isolation, masks/blend/ветви, crop/turn и portable Document/ресурсы/точные пиксели и PNG прошли. Canvas192×144→PNG130×154 alpha; CPU render3921мс/после переноса2955мс, по одному прогону. Это не полноразмерный15-layer benchmark и не Android process death.
- Новая private копия **до update**: `<local-private-path>`, фактически20:16:55МСК,44789248bytes,SHA `dd8ec2b0cf296bda54f661cc14ae0b5cd7d20a547898e9300f3abd670353998e`;49файлов/4document/4last-good (включая legacy), изолированная распаковка и повторные SHA телефона совпали. Копия19:45 уже отличалась, поэтому не использовалась как актуальная.
- После install49/49 прежних файлов совпали. После теста46/49 совпали, пропавших0; все16файлов пользовательских projects/resources,9legacy workspace, document/last-good и prefs неизменны. Current library.json, UUID/activeId/состав библиотеки неизменны. Три отличия: служебные device-info.txt/profileInstalled и library-last-good.json. Последний стал точной копией исправного pre-current index (только updatedAt/revision одной записи относительно прежнего backup); startup recoverPublishedDirectories обновляет резервный индекс. Не утверждать полную неизменность private data; идемпотентность/recovery остаются2.3. Единственное добавление — mixed-document-15-layers.txt.
- Post-test private TAR: `20261113-2021-task2.1-post-test`, фактически20:20:10МСК,44790272bytes,SHA `944c04b116b74fe1a959dcac5b1f172dbaa04eb3c3e280fc38867e6ad15b5f61`;все50файлов повторно сверены с телефоном. Private TAR/распаковки/индексы/REA Evidence не включать в поставку. На телефон данные не восстанавливались.
- Отчёт 2.1 (материал отсутствует в этой копии), JSON/связь с SHA (материал отсутствует в этой копии), рядом instrumentation.log и свежий device report. Личные имена/UUID/изображения не публиковались.

**После отдельного возобновления выполнять только 2.2:** проверить A51/актуальность private backup, запустить MixedPreviewCacheDeviceTest и подтвердить pixels/provider calls/invalidation. Recovery-метод MixedDocumentDeviceTest относится к2.3, не запускался; import/GPU/UI2.4 и отчёты2.5 также pending. Core70pass/1skip и Lint0errors/18warnings — исторические результаты сборки1.3, сейчас не повторялись.

---

# Пауза после пункта1.4 — 13.11.2026 20:10 МСК (история)

**1.4 завершён:** отдельные APK/testAPK candidate11, SHA256 и публичные сертификаты сохранены и связаны с успешной сборкой1.3. OpenSpec `complete-dual-app-functionality`: **4/43**. Следующий **2.1 — update на A51 и MixedDocumentDeviceTest**; ещё не начат. Пауза по указанию пользователя после подпункта.

- App: source/artifacts/sf7b-lab-0.2.0-candidate11.apk,12621793bytes, SHA256 `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`.
- Test: source/artifacts/sf7b-lab-0.2.0-candidate11-tests.apk,1173964bytes, SHA256 `05084940d2ec9827fd76562b31a0ed321b339b72fd484cfcb3aecc907f379a2a`.
- Сохранённые копии совпали с originals до/после копирования. Размеры/mtime/output-metadata совпали с доказательствами единственной сборки1.3 (19:54:22–20:00:55МСК,6м32с/exit0).71файл source/config/tools не изменён относительно checkpoint1.2. Новая сборка/тесты не запускались; старые APK не заменялись.
- Оба сохранённых APK проверены apksigner:exit0/verified v2/один signer; общий certSHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`, совпадает с candidate10 из1.2. Для каждого сохранён полный public PEM, DER SHA проверен независимо. Keystore/закрытый ключ/пароли/private backup не включены в поставку.
- Манифест1.4 (материал отсутствует в этой копии), SHA256SUMS (материал отсутствует в этой копии), отчёт (материал отсутствует в этой копии). В манифесте SHA19файлов доказательств (log/coreXML/Lint/run/result/metadata/signatures/badging/testmanifest); подписи/PEM относятся к точным байтам APK. Исторические отчёты1.3 не редактировались.
- Приложение debug0.2.0/code2,package lab.sf7b; test package lab.sf7b.test,target lab.sf7b/AndroidJUnitRunner. Сборочные результаты:core70pass/1skip,0fail/error; app/testAPK собраны; Lint0errors/18warnings. Это не подтверждает native/A51/15слоёв/cache/recovery/миграции candidate11.
- Телефон в1.4 не затрагивался; последнее наблюдение установленного APK — candidate10 в1.2. При следующем возобновлении2.1 сначала проверить подключение/версию/актуальность private backup; если проекты редактировались после19:45:26МСК, снять свежую копию. Затем установить **сохранённые** app/test APK update-r без clear/uninstall и выполнить только MixedDocumentDeviceTest по2.1; MixedPreviewCache относится к2.2.

---

# Пауза после пункта1.3 — 13.11.2026 20:04 МСК (история)

**1.3 завершён:** единственная сборка candidate11 успешна. OpenSpec `complete-dual-app-functionality`: **3/43**. Следующий **1.4 — сохранить отдельные APK/testAPK, SHA и сертификаты**; ещё не выполнялся. Пауза по правилу пользователя после каждого подпункта.

- Один Gradle-запуск точной командой design.md:19:54:22–20:00:55МСК, **BUILD SUCCESSFUL in6m32s, exit0**,83 actionable tasks/20executed/63up-to-date. Исходники и ключ перед запуском совпали с checkpoint1.2; Android-код/инструменты не менялись.
- Свежие coreXML19:56:46: **70passed/1skipped/0failures/0errors**. Core:test действительно исполнен заново: прежние результаты сохранены отдельно. Пропущен только штатно выключенный `GraphicsTest.largeCanvasBenchmarks` (benchmark-флаг в команде отсутствует).
- `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `:app:lintDebug` прошли. Новые build-root APK: app12621793bytes (19:58:53), test1173964bytes (19:59:14). Основной Android-код и новые Mixed tests скомпилированы; на устройстве не запускались.
- Свежий Lint20:00:54: **0errors,18warnings** (8GradleDependency,2NewerVersionAvailable,2UsableSpace,6UseKtx). Имеются также предупреждения overridePathCheck/SDK XML3↔4/Kotlin daemon sessions; они не остановили сборку. После выхода действующих Java/Gradle/JADX не обнаружено.
- Отчёт1.3 (материал отсутствует в этой копии), JSON результатов (материал отсутствует в этой копии), run metadata/команда (материал отсутствует в этой копии). Журнал source/candidate11-build-20261113-1953.log; его копия/coreXML/Lint/output-metadata сохранены в source/docs/verification/candidate11-build-20261113-1953/.
- Телефон/данные/настройки в1.3 не затрагивались; последний наблюдавшийся установленный APK — candidate10 из1.2. Новые APK ещё не скопированы в artifacts и не связаны с проверенными SHA/сертификатами: делать это в1.4 без новой сборки. Native/A51/15слоёв/cache/recovery/миграции остаются2.x; сборка не доказывает сохранность проектов candidate11 или паритет.

После нового поручения выполнять только1.4: проверить вывод именно этого успешного запуска, сохранить APK/testAPK отдельно, SHA/сертификаты и связь с журналом; затем снова STATE/notes и пауза. Не начинать установку до2.1 и не выдавать старые testAPK за новые.

---

# Пункт1.3 выполняется — 13.11.2026 19:57 МСК (история)

По новому поручению пользователя идёт единственная сборка candidate11 точной командой design.md, session69984. Новый журнал source/candidate11-build-20261113-1953.log; после выхода будет source/docs/verification/candidate11-build-20261113-1953-run.json. Сборку не дублировать. Прежние XML ядра сохранены в build-root/core/test-results/test-before-candidate11-20261113-1953, чтобы core:test выполнился заново. Свежие XML19:56:46МСК:71case/70pass/1skip/0fail/0error. Android assemble/testAPK/Lint пока pending; APK и готовность1.3 ещё не подтверждены. Исходники/ключ перед запуском совпали с checkpoint1.2. Телефон не затрагивается. После завершения1.3 — STATE/notes и пауза перед1.4.

# Пауза после пункта 1.2 — 13.11.2026 19:50 МСК (история)

Выполнен **1.2: checkpoint исходников, private backup проектов и подпись**. OpenSpec `complete-dual-app-functionality`: **2/43**. Следующий **1.3 — единственная сборка candidate11 по design.md**, ещё не начинался. Пауза после подпункта по указанию пользователя.

- A51 снова доступен. REA выгрузил установленный APK: это candidate10, SHA256 `065f2c85159e8a96102d665bdae7f49e32eb32355b65b996eabbb2672d9798ea`, полный base без splits. Для согласованной копии остановлено только lab.sf7b; оно не перезапускалось. Установок/очистки/записи данных приложения не было.
- Свежая private копия19:45:26 МСК: `%LOCALAPPDATA%\sf7b-lab-device-backups\20261113-1945-pre-candidate11\sf7b-private-app-data.tar`,44470272bytes, SHA256 `a7b3195b9b2f67d6821193a136761cf64383350b464016406ac2f2562b5c4727`. Все44 файла восстановлены в изолированную папку хоста; SHA совпали. Затем SHA всех44 файлов повторно прочитаны на телефоне и совпали с копией.3 current/3 last-good (включая legacy),2 записи library,6 ссылок ресурсов и1 preferences XML прошли структурные проверки. JSON/проектные имена/UUID/изображения не публиковались.
- Старый private backup073222 (34928640bytes, SHA942ff5de…e3ea8c) также восстановлен и проверен по всем12 файлам. Старый source checkpoint061951:60 файлов, CRC/SHA и совпадение с сохранённой распаковкой подтверждены.
- Новый частный checkpoint: `%LOCALAPPDATA%\sf7b-lab-checkpoints\20261113-1945-pre-candidate11`. `candidate11-source.zip`:91файл,254471bytes, SHA256 `a0b2e7a2b02bd4c831d70045f83f6de671b6c0003782611899344464d7fee7c5`; `project-docs.zip`:23файла, SHA256 `0d07bf4ac8ebce35e76f531befb2e0291d95023fda3fd68da1a63d101424d3a4`. Исходные и восстановленные файлы сверены; это pre-1.2-report снимок, последующие STATE/отчёты в него не входят. Signing/research/artifacts/caches исключены из source ZIP; ключ сохранён отдельно в private-signing. Частные архивы/restore-check папки не включать в поставку.
- Keystore совпадает с baseline; его новая частная копия реально подписала отдельный probe APK, apksigner verify успешен. Сертификат выданного старого APK/candidate10/установленного/probe одинаков: SHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`. Probe не устанавливался и не является candidate11.
- Отчёт и порядок восстановления (материал отсутствует в этой копии), JSON с полными SHA/агрегатами (материал отсутствует в этой копии). Хостовая распаковка/байты/ресурсы проверены; открытие восстановленных проектов на Android, миграции и новые Mixed tests остаются2.x. Извлечение TAR поверх живых данных и слепой downgrade не проверены и не выполнялись.

При следующем возобновлении: выполнять только1.3, один Gradle по команде design.md (она приведена ниже), сохранить новый отдельный журнал. Перед1.4/2.x снова проверить актуальность подписи/backup; при новых правках проектов после19:45 снять новую копию. Android source не менялся, сборка/core tests/Lint/native в1.2 не запускались.

---

# Пауза после пункта 1.1 — 13.11.2026 19:40 МСК (история)

Пользователь явно возобновил работу и потребовал останавливаться после каждого подпункта tasks.md с записью STATE/notes. Выполнен только **1.1**; **1.2 ещё не начинался**. Состояние ниже — история предыдущих остановок, а не запрет текущего возобновления. Сейчас снова пауза по новому правилу пользователя.

- OpenSpec `complete-dual-app-functionality`, spec-driven: 1/43 подпунктов. Приложенные Downloads/tasks.md и design.md перед изменениями побайтно совпали с OpenSpec (SHA в манифесте); рабочая отметка ведётся в tasks (материал отсутствует в этой копии). Приложения сохранены без правок.
- Метаданные **текущего** чата: `gpt-6.1-sol`, reasoning `high`; субагенты не запускались. Прочитаны текущие правила, checkpoint и последние notes, оба приложения, proposal и три delta-specs. Прежний planning-only относится к старому запросу.
- JDK17.0.12, Gradle wrapper8.13 (`--version`, exit0), SDK36/revision2, Build Tools35.0.0, ADB37.0.1, REA6.8.0/skill49, OpenSpec2.0.0 и RTK1.2.0 доступны. JAVA_HOME в shell исходно не задан: перед будущей сборкой явно установить JDK17. MCP REA имеет собственный REA_ADB_PATH; shell ADB в PATH не видит.
- Живые REA `inspect_adb_client` и `list_adb_devices` успешны, **devices=[]**: A51 сейчас не обнаружен. Питание/сети/драйверы/настройки телефона не менялись. Новых действий в приложениях не было.
- Android-source остаётся единственным: `<repository>`. Git/.codegraph отсутствуют в обеих папках; индекс не создавался. Наличие файлов патчей candidate11 и двух Mixed device tests проверено, их Android-корректность пока не подтверждена.
- Candidate10 APK и текущий app-debug.apk имеют SHA256 `065F2C85159E8A96102D665BDAE7F49E32EB32355B65B996EABBB2672D9798EA`; текущий APK — старый candidate10. Отдельного candidate11 APK нет; app/testAPK и Lint остаются старыми. Перечитанные XML: исторические70pass/1skip/0fail/0error, не новый тестовый запуск. Установленная версия сейчас не проверялась без A51.
- Компактный environment manifest (материал отсутствует в этой копии) и свежие REA Evidence (материал отсутствует в этой копии) сохранены в исходниках. Секреты, ключи и личные проекты не читались/не включены.

**Следующий подпункт после возобновления — 1.2:** проверить checkpoint исходников, private backup проектов, сохранённую подпись и восстановимость; записать SHA. Сборка относится к1.3, установка/защита проектов на A51 — к2.x. При отсутствующем телефоне не заявлять свежую копию данных устройства или сохранность его проектов подтверждённой.

Точная команда будущей единственной сборки из Android-source (в1.1 не выполнялась):

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\gradlew.bat '-Psf7bBuildRoot=<local-private-path>' --no-daemon --max-workers=1 --no-parallel '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8 -XX:ActiveProcessorCount=2' '-Pkotlin.compiler.execution.strategy=in-process' :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

---

# План подготовлен; разработка остаётся остановленной (история)

Checkpoint 13.11.2026 19:30 МСК. Новый запрос выполнен как планирование: план (материал отсутствует в этой копии), задачи (материал отсутствует в этой копии), proposal и три delta-specs. OpenSpec strict validation прошла; два read-only review на gpt-6.1-sol/high не нашли существенных замечаний. Реализация, сборки, установка и опыты на телефоне не возобновлялись.

Решения текущего чата: только gpt-6.1-sol; medium/high/xhigh, не low/max/ultra; экономить выходные токены. Внутренний анализ APK явно разрешён, код/шейдеры/ресурсы sf7b.lab создавать самостоятельно; действующий режим отражён в docs/CLEAN_ROOM.md. План охватывает все обнаруженные возможности выбранных версий, включая доступные анимационные/видеорежимы.

Фактическая база остаётся прежней: candidate10 — последняя проверенная установленная сборка; candidate11 — 70 core pass/1 skip, Android assemble/Lint/новые native pending. Проверен только REA inspect_adb_client: ADB37.0.1 выбран по REA_ADB_PATH, без обращения к устройствам. Текущее подключение A51 не проверялось. RLM использован без LLM-вызовов и закрыт.

Первое действие при отдельном поручении реализовывать план: проверить текущую среду/защиту проектов и завершить candidate11; далее задачи 1–2, параллельно конечный REA-каталог. Завершённое планирование не означает завершённый функционал.

---

# Работа остановлена по указанию пользователя — предыдущий checkpoint

Checkpoint 13.11.2026 12:15. Пользователь: «Остановись и сделай заметки в notes и state». Разработку, исследование, ADB-ввод и сборки НЕ продолжать до явного нового поручения. Это состояние имеет приоритет над историческими указаниями «работа активна» и «продолжать» ниже.

## Завершение записей по последнему запросу

13.11.2026 19:13: последние10 существенных действий перед остановкой перечитаны и внесены в notes.md (материал отсутствует в этой копии). Точка остановки и незавершённое зафиксированы; дальнейшую работу не возобновлять без нового поручения. Сверка только читала журналы/метаданные и обновляла документацию. Активных субагентов и Java/Gradle-процессов сейчас нет. CoreXML подтверждают 70passed+1skipped,0failures/0errors; candidate11 APK/Lint/native ещё не проверены. Последняя проверенная установленная версия — candidate10.

## Что остановлено

- Общая сборка candidate11 прервана Ctrl+C (session3543, exit1). На момент проверки Java/Gradle-процессов не осталось. Неполный build не считать ошибкой кода или готовым APK. Журнал: source/current-candidate11-build.log.
- Последний уже начатый Glitch UI swipe завершился, его XML1794561150420.xml сохранён. Новых действий с телефоном после остановки не выполнялось.
- review_prompt прерван; render_import и project_storage уже завершили свои патчи и не работают. RLM353e54d51deb ранее истёк. Сессий CUA/REA native, требующих дополнительного завершения, нет.

## Последнее проверенное

- Последняя собранная и установленная версия — candidate10: artifacts/sf7b-lab-0.2.0-candidate10.apk, SHA256065F2C85159E8A96102D665BDAE7F49E32EB32355B65B996EABBB2672D9798EA. Native8/8; GPU-only controlled benchmark прошёл, preview fivecolors238мс/full2000PNG1764мс, медианы3. Подробности PERFORMANCE.md исходников.
- До отмены candidate11 успели завершиться core tests: XML13.11 12:12:41, всего71case =70passed+1skipped,0failures/0errors. EditorContract29/29, NodeRasterCache15/15; Graphics9pass/1skip, ReferenceColor8/8, ReferenceEffects9/9. App/testAPK11 и Lint НЕ завершены; новые native tests НЕ запускались. APK11 НЕ сохранён и НЕ установлен.
- В исходниках сохранены новые патчи11: source/blend/mask preview-cachev4 (Renderer.kt,NodeRasterCache.kt), idempotent ProjectFiles/ProjectLibrary save с защитой last-good/modified, typed recovery notices и RU/EN EditorModel warnings. Hostregressions прошли, Android интеграция ещё не проверена.
- Новые pending native: MixedPreviewCacheDeviceTest (15слоёв,512preview,cached/uncached pixels+providercalls+CSV); MixedDocumentDeviceTest2 (15mixedlayers192×144,TTF/маски/ветвь/seed/portable exactPNG/crop130×154; повреждение/потеря current→RU/EN warning/repair). Собственные UUID/cacheDir, защита старых манифестов/backup SHA и prefs/selection в teardown. Проверить это наA51 после полной сборки.
- REA MCP теперь действительно использует REA_ADB_PATH/ADB37.0.1 и видел A51device. Полные Evidence: research/raw/rea-adb-mcp-verified-20261113.json. Не повторять старый setup/adb-not-found как текущую проблему.
- Пользователь: OneLabPRO нет, продолжать имеющиеся APK. OneLab временный lowpoweredoverride возвращён в исходныйfalse (freshXML1794560590147.xml), изменений настроек не осталось. Телефонные питание/сеть/драйверы не менялись; originals не очищались.
- Glitch1.7.4 предоставленныйmod: Gallery→собственный grid13ноя07:49→PNG1280×960 без подтверждённого эффекта. Export/REA Evidence+comparison.json в research/runtime/glitch-identity-export. Результат сравнения сохранён, полный анализ вчат не публиковать. Screenshot glitch-fixture-confirmed и ui-steps есть. Текущий экран после horizontal category swipe не перепроверять без возобновления.
- review_prompt исследует полный registry, нашёл главный Glitchregistry FragmentGlitch.initGlitchTypes (прежний84Glitchsubsetнеполон), восстановил ordered OneLab groups/license metadata. Его CATALOG_COVERAGE/BASE_DECLARATIONS ещё не считать завершёнными. Notes: Desktop/docs/notes_review_prompt.md; cacheagent Desktop/docs/notes_render_import.md; storageagent source/docs/notes_project_storage.md.

## Продолжение только после разрешённого возобновления

1. Прочитать этот checkpoint и notes, не начинать аудит заново. Проверить текущие инструменты/A51 перед использованием.
2. Завершить единую candidate11 сборку из source, не использовать оставшийся app-debug.apk как11 без успешного assemble. Команда: JDK17; gradlew.bat -Psf7bBuildRoot=<local-private-path> --no-daemon --max-workers=1 --no-parallel '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8 -XX:ActiveProcessorCount=2' '-Pkotlin.compiler.execution.strategy=in-process' :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug. Один тяжёлый Gradle/JADX одновременно.
3. Сохранить APK11 отдельно/hash/cert, update-r с прежней подписью без clear/uninstall. Запустить новые MixedDocumentDeviceTest/MixedPreviewCacheDeviceTest и подходящие прежние контракты. Ошибки исправить, реальные15layer timings/report сохранить с размером и интервалом.
4. Продолжить finitecatalog/schema evidence и реальныеoriginalruntime/пятьpriority, ускорениеMultiDistort, весьSPEC. Полный каталог/паритет ещё не выполнены. После разработки три подробных MD: docs/analysis/ONE_LAB_INTERNALS.md,GLITCH_LAB_INTERNALS.md,SF7B_LAB_GAPS.md; пока не созданы/не финализированы. Не выдавать этот checkpoint за завершение задачи.

<!-- HISTORICAL_STATE -->

## Исторические checkpoints до остановки

# sf7b.lab — активная реконструкция

Checkpoint 13.11.2026 09:25. Полный SPEC ещё не выполнен. Работу продолжать, не выдавать промежуточные APK/пять эффектов за весь результат. Хронология — notes.md (материал отсутствует в этой копии), компактный контекст — docs/CONTEXT.md (материал отсутствует в этой копии).

## Текущая работа 13.11.2026 12:06

- Candidate10 остаётся последней собранной/установленной версией; native8/8 и GPU benchmark подтверждены ниже. Новые изменения ещё НЕ собраны: expanded source/blend/mask preview-cache v4+15JVMcontracts (render_import); typed RU/EN recovery notices и idempotent save/last-good protection+hostregressions+native15mixedlayers (project_storage). Идёт единая candidate11 сборка :core:test +app/testAPK+Lint, current-candidate11-build.log/session3543; итог проверить перед установкой. Не путать изменения исходников с проверенной установленной версией.
- Пользователь подтвердил: OneLab PRO APK/доступа нет, продолжать имеющиеся APK. Закрытые эффекты/runtime параметры остаются с явным ограничением; недоступность не останавливает независимую реализацию/анализ.
- OneLab override lowpowered временно false→true→false; восстановление исходного false доказано freshXML research/runtime/ui-steps/1794560590147.xml. Никаких незавершённых изменений настроек телефона/оригинала не оставлено. Effects237/439 открыл статистику, значение counter ещё не интерпретировано; не выдавать его за видимое число фильтров.
- Root начал Glitch1.7.4 mod runtime: штатный Gallery→PhotoPicker→собственный grid13ноя07:49→Done, screenshot glitch-fixture-confirmed сохранён. Исходные работы не удалялись. Для canvas-only controls в research/android-ui.mjs добавлен tap-point: fresh package/focus/bounds guard, координаты только по просмотренному screenshot, actionlogs. Это research helper, не функция продукта.
- review_prompt восстановил static ordered groups и license tags; продолжает missing base schemas/finite catalog, без heavyJADX и phoneinput. Полный анализ вчат не выводить, финальные3MD после разработки ещё впереди. Notes не реже5мин/после значимого, STATE/CONTEXT заранее при смене этапа и перед переполнением.

## Актуальный checkpoint 13.11.2026 11:50

Работа активна; полный SPEC ещё не выполнен. Этот checkpoint заменяет противоречащие ему старые указания ниже.

- Candidate9: full build, оба APK и Lint успешны; core60pass/1skip. Native22/22. Pointwise CPU9 сохранил все72 SHA (4 718 592пикселя), MultiDistort12 SHA (43 884координаты). CPU full2000 PNG14581мс (1прогон), five-color preview7438мс (медиана3).
- Candidate10: app/test build2м30с, GPU transfer bulk IntBuffer; математика шейдеров/CPU не менялась. Native8/8 (39.756с), включая identity/alpha/цепочку/полный PNG/EditorModel. Реальный Model GPU export2000x2000, alpha128, документ неизменен,4159мс (1прогон).
- Контролируемый GPU-only benchmark10 OK(1),12.956с: медианы3прогонов preview identity187мс,adjust223мс,five-colors238мс,Bloom465мс; full2000 PNG1764мс. Candidate9 соответственно849/879/841/1101/4124мс. CSV и ограничения в PERFORMANCE.md исходников. Это debug, не3холодных запуска, не измерение original apps.
- APK: artifacts/sf7b-lab-0.2.0-candidate10.apk; SHA256065F2C85159E8A96102D665BDAE7F49E32EB32355B65B996EABBB2672D9798EA. На A51 установлен update с прежней подписью без очистки. Последний test-only APK отдельно candidate10-gpu-benchmark-tests.apk. Старый выданный debug.apk сохранён.
- ADB сейчас device после переподключения владельцем. Не менять USB/питание/сети/драйверы. При старом idle-offline независимую работу продолжать. Из test-results выгружены screenshots в candidate10-ui, но сами UI-снимки сделаны тестами candidate8 (10:08), не новая визуальная проверка10. Light checker/viewport controls и Samsung IME визуально просмотрены, перекрытия нет.
- REA49 CLI и живой MCP проверены11:50: inspect_adb_client выбрал env ADB37.0.1, list_adb_devices видит A51device; Evidence research/raw/rea-adb-mcp-verified-20261113.json. Старое adb-not-found больше неактуально. RLM session353e54d51deb read-only,0LLMcalls:131named+6anonymous schemas,509namedparams;414inherit chains требуют base definitions. Не равно полному видимому каталогу/паритету.
- Дальше: 15 смешанных слоёв/native persistence, расширить кэш на дорогие источники/композитинг с проверкой invalidation; recovery UI; продолжить REA registry/OneLab+Glitch runtime и весь static-image каталог/наборы. MultiDistort CPU~18.8с пока медленный. Новые кодовые изменения ещё не начаты.
- Последние требования: нужные скиллы применять, конкретную ссылку спрашивать только если отсутствующий действительно нужен. После разработки три подробных docs/analysis/ONE_LAB_INTERNALS.md,GLITCH_LAB_INTERNALS.md,SF7B_LAB_GAPS.md; полный анализ вчат не выводить. Ни полный каталог, ни финальные3документа пока не завершены.
- Notes после важных изменений/не реже5мин; STATE/CONTEXT до переполнения. Агенты ранее quota до11:51, после этого можно дать независимые задачи. Один тяжёлый Gradle/JADX одновременно; телефон контролирует root.

## Предыдущий checkpoint candidate6b (исторический)

- Candidate6b0.2.0/code2 собран2m52s, app/testAPK/Lint; установлен на A51 обновлением с прежней подписью, без очистки данных. Артефакт `artifacts/sf7b-lab-0.2.0-candidate6b.apk`, SHA256 `C70F8F0A1FE37386015BC3D566477B828333F89E640F811FEFBDAC5EAE07FE32`. Старый выданный APK не заменён.
- Ядро:60pass/1skip (60+1=61 cases); новый кэш7/7. Последняя полная native-серия candidate5 на A51:19/19,12.826s, native-a51-candidate5.log. GPU5/5 прошёл ещё candidate3 и снова входит в эти19.
- Сейчас PreviewCacheDeviceTest candidate6b на телефоне: source bypass/exact cached-cold pixels и full-size2000x2000 GPU/CPU/PNG. Лог native-a51-candidate6b-cache-export.log, session11190; итог проверить. GPU export в EditorModel пока НЕ включён. EditorUiTest5 ещё НЕ исполнен.
- Новый UI в candidate6b: постоянная нижняя панель с handle/drag/resize, Source/Random/category cards, teal+neutral light/dark, быстрые инструменты, ribbon реальных узлов, верхние Cancel/Apply/Variant для эффекта. Это первая реализация по наблюдениям, до просмотра/проверки телефона сходство не объявлять.
- Source replacement сохраняет image-layer ID, graph, seed, canvas и старый asset для Undo. Импорт проверяет маленький decode перед commit. Устранён cancellation race удаления уже подтверждённого image/font asset. Эти новые пути проверяются новыми UI-тестами, не только компиляцией.
- Добавлен bounded NodeRasterCache192MiB для CPU preview: SHA256 по структуре/версии/ресурсам/seed/маскам/размеру/project namespace; lookup до посещения входов. Unknown resourceVersion отключает reuse. Export/thumbnail этот кэш не используют. Без активных эффектов preview обходит EGL.
- MultiDistort ускорен cache списка пересекающих ячейку окружностей.12 own-field SHA snapshots/43884 coordinates до/после совпали. На A51 uncontrolled3run benchmark median28525→18767ms (candidate4→5), ~1.52x. Это ещё далеко от1–3s для нового тяжёлого узла. Runtime-паритет оригиналу не доказан.

## Задание и правила

Выполнить PROMPT (материал отсутствует в этой копии) и SPEC (материал отсутствует в этой копии), нативный Android offline. OneLab1.5.3 — главный ориентир функций/компоновки, Glitch1.7.4 дополняет. Внутренний анализ APK разрешён явно, обязательная чистая комната отменена. Извлечённые shaders/code/assets оригиналов в собственный APK не включать. Различать static evidence, runtime, гипотезы и неизвестное.

Пользователь разрешил подключённый A51; прежнее «без телефона» отменено. notes.md — после важных изменений и минимум5мин. STATE/CONTEXT checkpoint до переполнения контекста; точный процент недоступен. Общаться по-русски, кратко, продолжать доступную работу самостоятельно.

## Пути и защита данных

- Правила/документы: `<repository>`.
- Единственный Android source: `<repository>`. Git/.codegraph отсутствуют. Не создавать второй проект/индексацию автоматически. Source STATE/AGENTS — ссылки сюда.
- Baseline checkpoint: `%LOCALAPPDATA%\sf7b-lab-checkpoints\20261113-061951`, source zip/extracted copy, delivered APK и private signing отдельно. Baseline build:54tasks,28m16s, `%LOCALAPPDATA%\sf7b-lab-baseline-build`.
- Приватная копия app data ПЕРЕД update: `%LOCALAPPDATA%\sf7b-lab-device-backups\20261113-073222\sf7b-private-app-data.tar`,34928640bytes; SHA256 `942ff5deac1c11cb02b171e3fc4dff4f1d92766cdf7f9267dc0a681cf0e3ea8c`. Не публиковать/не включать в архив исходников.
- Подпись новых update совпадает со старой: certSHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`. Не распространять signing keystore в public source archive.
- Старый APK `artifacts/sf7b-lab-debug.apk`11.11:11446573bytes, SHA256 `386C926C0DCBCA14973A2EB46F04862FF50D4B998D51FAEE816DAEFB21F3DF30` не соответствует текущему source. Промежуточные candidate1–6b сохранены отдельно.

## Среда и REA

JDK `C:\Program Files\Java\jdk-17`; SDK `%LOCALAPPDATA%\Android\Sdk`. Хост8GB, один тяжёлый Gradle/JADX. Node `%USERPROFILE%\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe`; REA CLI `%USERPROFILE%\.codex\tools\rea\node_modules\rea-agents\scripts\rea.mjs`6.8.0; skill reverse-engineer-anything49.

REA_ADB_PATH и официальный REA_JADX_MCP_JAR0.7.1, JAVA_HOME, heap512/oneCPU прописаны в `.codex/config.toml`; backup `config.toml.sf7b-20261113-062142.bak`. Живой MCP всё ещё старого env (adb not found). Одноразовые REA CLI с правильным env успешно анализируют/снимают A51 и возвращают Evidence. Не объявлять MCP переподключённым. Glitch JADX method с heap1024MiB успешен (raw/glitch-patchwork-constructor-rea-1024m.json);512 OOM сохранён как ограничение.

A51 SM-A515F Android13/API33 сейчас device; иногда USB ADB исчезал, MTP оставался. Причина обрыва не установлена. Не менять WiFi/power/developer settings и не очищать apps. Три попытки эмулятора не загрузили рабочий ADB; свой QEMU остановлен, повтор без новой причины не нужен.

## Материалы исследования

- OneLab `<local-private-path>`: com.ilixa.onelab1.5.3/code138 arm64; SHA256 `aa90712cf7ff80ad8d2a3f36915002a5b26fb73c54355ecb05ca5720571ea7aa`.
- Первый uptodown-com.ilixa.glitch.apk — магазин com.uptodown7.45, исключён.
- Исправленный Glitch `<local-private-path>`: com.ilixa.glitch1.7.4/code999, SHA256 `8899fb87d25877630b333dfd5013a3268304fd312b1aabda9f2253282dd69557`. Mod-кандидат, официальный upstream не подтверждён.
- research/REA_FINDINGS.md, EFFECT_PARITY.md, PRIORITY_BEHAVIOR.md, NEXT_COLOR_BEHAVIOR.md, EFFECTS_CATALOG.json (~9.5MB).1270 static candidates не являются видимым завершённым каталогом;438One/88Glitch registry refs,137literal schemas/517params,1654construction instances — разные метрики, не сумма готовых эффектов.
- research/raw содержит извлечённые оригинальные материалы; исключить из поставляемого собственного source archive вместе с APK оригиналов/секретами.

## Реальное наблюдение оригинала

OneLab был на пустом START; выбран только собственный grid через PhotoPicker, существующая пользовательская работа не заменялась. `research/runtime/onelab-fixture-confirmed/screen.png` доказывает загрузку; SEND до этого не загрузил image. UI_PARITY.md (материал отсутствует в этой копии) описывает light Advanced toolbar/right actions, persistent dock, source edit, Glitch subcategories/Break, effect carousel. Снимки REA/raw сохранены. Dichotomic Break locked (Unlock this effect); полный entitlement не подтверждён. Остальные5priority/runtime/экспорт оригинала ещё исследовать. Glitch runtime пока не исследован в этой сессии.

`research/android-ui.mjs`: foreground package + свежий XML перед каждым вводом, unique text/description, clickable ancestor для0bounds; action.json. Custom carousel swipe только по наблюдённому screenshot. При null root от uiautomator уникальный remote tempfile исключает чтение старого XML; сбой останавливает ввод. Не автоматизировать ChatGPT на телефоне.

## Код, совместимость и метрики

Import: sRGB conversion для P3/16bit, EXIF один раз, lazy working-size decode, bounded384MiB cache поpath/size/mtime/size. SurfacePool RAM+spill, scratch persistOnClose=false; cached decoded source явно persist. ProjectLibrary independent UUIDs, atomic save/backup, migration old workspace без его удаления, integrity portable projects; gallery15+All, копии/rename/deleteconfirm. Render tagged project/revision, latest wins; save actor+flush barrier; lifecycle checkpoint. Crop/brush transforms/colors/TTF controls исправлены.

Пять приоритетов algorithmVersion2 сохраняются отдельно от legacy1. Catalog.newEffect создаёт новуюверсию, Catalog.definition(effect) редактирует нужную. Старый JSON безversion означает1; old readers strict reject новые поля. Реализованы также ColorSwap/LumSelect/ChannelMultiplier/Ink с реальным dispatch. Собственные диапазоны/PRNG/Gaussian approximation не считать доказанными оригинальными.

PERFORMANCE.md (материал отсутствует в этой копии) содержит A51 controlled3run:2000PNG→1024preview. Candidate5 CPU/GPU medians:identity18/829ms,adjust3578/869,five colors13630/1142,Bloomv22650/1079,MultiCPU18767. FullCPU2000PNG38446ms. Нет picker/UI/autosave в интервале; heap sample не peak/PSS/GPU memory. OneLab foreground, charging69%,25.7→25.8°C; не полный холодный launch. Сравнение CPU/GPU новой версии не сравнение со старым APK/OneLab.

Реального проблемного изображения пользователь сейчас не имеет. Synthetic profile/EXIF/16bit tests прошли, но исходный частный случай сильного затемнения не воспроизведён.

## Следующие действия

1. Получить результат session11190 native cache/fullGPU, исправить ошибки без ослабления допусков. Затем EditorUiTest5 на candidate6b, сохранить/просмотреть скриншоты обоих языков/тем/keyboard. Test teardown восстанавливает прежние prefs/active project, удаляет только UUID, записанные самими тестами; если setup не завершён, очистки нет.
2. Проверить draft cancellation при переходе из effect через node ribbon/viewport pill: нужно сбрасывать preview перед editNode/tree (логически найдено, ещё не исправлено). Добавить проверку numeric keyboard/Cancel и header Apply callback. После правки новый APK, не приписывать ему старые результаты.
3. После full-size GPU proof подключить подходящий GPU export из originals, fallback CPU; повторить полный PNG/размер/alpha. Дальше ускорять CPU/GPU MultiDistort и другие тяжёлые узлы; preview cache не ускоряет новый изменённый upstream сам по себе.
4. Продолжить original runtime5priority, UI numeric/tree/export/themes, Glitch, полный конечный видимый каталог и наборы. Реализовывать реальные механизмы/графы, не label stubs. Большой SPEC не сужать до текущих ~40 эффектов.
5. Вывести recovery warning ProjectLibrary в UI, проверить15layers/маски/offline first launch, очереди/import failure/process recreation/portable ресурсов, всю документацию и конечный APK/source archive. Финальные ROOT_CAUSES/TEST_REPORT/DESIGN/BUILD_AND_INSTALL/LICENSE/полная карта ещё нужны.
6. notes после важного/5мин, STATE/CONTEXT перед заполнением. Не останавливать доступную работу из-за недоступных агентов или телефона.

## Команды

Из папки исходников, один Gradle:
```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\gradlew.bat '-Psf7bBuildRoot=<local-private-path>' --no-daemon --max-workers=1 --no-parallel '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8 -XX:ActiveProcessorCount=2' '-Pkotlin.compiler.execution.strategy=in-process' :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```
Output `%LOCALAPPDATA%\sf7b-lab-build\app\outputs\apk\debug\app-debug.apk`, testAPK в apk/androidTest/debug. Установка только `adb -s <actual-serial> install -r ...` и test `-r -t`; не uninstall/clear.

Native: `adb -s <actual-serial> shell am instrument -w -r -e class lab.sf7b.PreviewCacheDeviceTest lab.sf7b.test/androidx.test.runner.AndroidJUnitRunner`. Для UI заменить class на lab.sf7b.EditorUiTest. Логи сохранять отдельно для каждой сборки. REA CLI `capture-adb-screen <serial> --output-directory <new-absolute-directory> --format json` с правильным REA_ADB_PATH.

## Агенты

Все3 existing agents quota errored до11:51AM; не повторять неизменённые вызовы. Root один сейчас. review_prompt — research, render_import — render/GPU/import, project_storage — document/storage/effects; когда доступны, дать конечные независимые задачи и не править одновременно их файлы. Пользователь явно разрешил делегирование. Старые checkpoints: история (материал отсутствует в этой копии).


