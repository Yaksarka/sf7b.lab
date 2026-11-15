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

# Состояние sf7b.lab — 14.11.2026 02:31 МСК

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

Канонический STATE (материал отсутствует в этой копии), notes (материал отсутствует в этой копии).

---

Ниже история, не текущий статус.

# Состояние sf7b.lab

14.11.2026 01:52 МСК: **3.1 выполнен; OpenSpec10/43, пауза перед3.2.** Отчёт и команды (материал отсутствует в этой копии), варианты/происхождение (материал отсутствует в этой копии), аудит (материал отсутствует в этой копии), manifest (материал отсутствует в этой копии).

One supplied SHAaa90712c…ea7aa и installed SHAd4a93ba5…b1c7c3 различаются resources.arsc и3META-INF при1.5.3/code138; совпали1662/1666 файлов, включая DEX/manifest/native/assets. Cert общий/v2 verified; равенство UI/resource/runtime не подтверждено. Glitch supplied mod/installed exact SHA8899fb87…69557,1.7.4/code999,v1/v2/v3 verified; официальный контроль отсутствует. One PRO нет. У каждого package один base без splits; ABI One arm64, Glitch arm64/armv7.

84JSON проверены:28 старых Evidence валидны и импортированы,5 exact static/23 targetless,10 error-only/47 host-derived. Совпали17 screenshot backing hashes и11 DEX/GLSL hashes. Fresh One package scan успешен; Glitch120000ms timeout не считается успешным Evidence. Fresh13 + import28 = combined41/0unknowns. Android/core/tests/APK не менялись; на A51 только чтение package/APK, без запуска originals/UI/установок/изменения данных. Private originals/Evidence находятся в LOCALAPPDATA/sf7b-lab-reference-audits/20261114-task3.1, вне поставки. Backup проектов последний2.4/SHA3cde3d23…7cc0e, в3.1 не проверялся.

Следующий только после нового поручения — **3.2**: закрыть One Lab ordered groups и Glitch FragmentGlitch.initGlitchTypes; классифицировать effects/helpers/aliases/presets и сохранить unresolved registrations. Учитывать оба One variant digest и ограничения PRO/mod/Glitch provider. Схемы3.3 и обход3.4 автоматически не начинать. Канонический STATE (материал отсутствует в этой копии), notes (материал отсутствует в этой копии). Ниже история2.5.

# Состояние sf7b.lab

14.11.2026 01:36 МСК: **2.5 выполнен; OpenSpec9/43, пауза перед3.1.** Создан TEST_REPORT (материал отсутствует в этой копии), обновлён PERFORMANCE (материал отсутствует в этой копии), прежний candidate10 сохранён в архиве (материал отсутствует в этой копии). Desktop-копии ссылаются к source. Аудит доказательств (материал отсутствует в этой копии): 88 уникальных файлов / 107 проверок SHA/связей; новых запусков/сборок/телефона не было.

Сводка разделяет core70pass/1skip1.3 и3filtered2.3, native1/2/1/20 поэтапно и capture-repeat,9свежихPNG2.4. Small15-layer192×144 и preview512² при document2000² не доказывают full-size15layers. Model GPU PNG2000²/alpha128/3225мс — один image-layer+5effects,одинпрогон. Source/compositing cache подтверждён; cold preview15,4с, полная speed/FPS/PSS матрица открыты. Исторические candidate10 медианы отделены от candidate11.

Последние подтверждённые app/test APK и private backup —2.4; их сохранённые доказательства сверены, телефон в2.5 не проверялся. User данные/оба library indexes сохранены после явного cleanup только backup index. Private ключи/копии/Evidence вне поставки. Следующий только3.1 после отдельного возобновления: оригинальные APK, версии/происхождение/ABI и PRO/mod. Канонический STATE (материал отсутствует в этой копии), notes (материал отсутствует в этой копии). Ниже история2.4.

# Состояние sf7b.lab

14.11.2026 01:22МСК: **2.4 выполнен; OpenSpec8/43, пауза перед2.5.** A51 import7/GPU6/UI7:20/20 безskip/failure, дополнительно1capture-only повтор. Девять свежих PNG RU/EN×dark/light+numeric визуально проверены, даты/device mtime/producer/APK SHA в manifest. Изменён только EditorUiTest screenshot helper(waitForIdleSync+350мс); production/app candidate11 прежние. Текущий test APK `artifacts/sf7b-lab-0.2.0-candidate11-ui-capture-tests.apk`,SHA52f98f1ec63a869cee603c413edc555833f5f811861c0237485717567536b12f, прежняя подпись. AppSHA8fb06764…1a500 подтверждён REA pull.

GPU chain7префиксов maxerror1; model GPU PNG2000²/alpha128/3225мс — один прогон, не полноразмерный15-layer benchmark. Все user manifests/assets/metadata/prefs/UUID/selection/оба library indexes побайтно прежние. Cleanup вернул только backup index после своих fixtures. Финальная private копия0118-task2.4-final-verified01:18:29МСК,SHA3cde3d23a13261998a86962f7331e4729ed0c0a7932f45f299200b9815f7cc0e;52файла сверены. Обновлены10собственных отчётов и2служебных файла, новых/пропавших0. App остановлено, private копии вне поставки. Отчёт2.4 (материал отсутствует в этой копии), JSON (материал отсутствует в этой копии), снимки/даты (материал отсутствует в этой копии).

Следующий только после отдельного возобновления2.5 — общие TEST_REPORT/PERFORMANCE/STATE с фактами и ограничениями2.1–2.4. Новый core suite/Lint/эмулятор не запускались. Канонический STATE (материал отсутствует в этой копии), notes (материал отсутствует в этой копии). Ниже история2.3.

13.11.2026 20:50МСК: **2.3 выполнен;OpenSpec7/43,пауза перед2.4.** Recovery native OK(1),безskip/failure:RU damaged/EN missing current,last-good repair,локализованные warning/no-repeat. Свежие3host EditorContractTest прошли идемпотентность bytes/mtime/revision/index,repair и защиту last-good. Generic coreXML теперь3filtered tests;исторические70pass/1skip1.3 сохранены отдельно. Исходники/APK в2.3 не менялись,app/testSHA подтверждены REA pulls;lab.sf7b остановлено.

Все user documents/last-good/assets/metadata/prefs/UUID/оба library indexes побайтно прежние. После удаления своих fixtures тест оставил1test UUID в backup index;только этот файл при остановленном app возвращён к проверенным pre-test bytes через проверенный temp/rename. Итог51/52 прежних файлов совпали,толькоdevice-info отличается,новых/потерянных0. Финальный private backup20261113-2047-task2.3-final-verified20:47:04МСК,SHA dd7feac628d009c596049ea46fbcd5cbbed64ae7e0fd98a9a17c35217007b795,все52файла сверены с телефоном. Промежуточные копии диагностические,private данные вне поставки. Отчёт2.3 (материал отсутствует в этой копии), JSON (материал отсутствует в этой копии). Следующий только2.4 после отдельного возобновления:import/GPU/UI contracts и свежие screenshots с проверкой A51/backup.

Канонический STATE (материал отсутствует в этой копии), notes (материал отсутствует в этой копии). Ниже историческое состояние2.2.

13.11.2026 20:40МСК: **2.2 выполнен; OpenSpec6/43, пауза перед2.3.** Итоговый MixedPreviewCacheDeviceTest OK(2),безskip/failure:15-layer blend и двухслойные source/mask/early-effect контракты, exact pixels/provider calls/invalidation. Изменён только этот test source, production/app candidate11 прежний. Первая попытка выявила ошибку head в новом fixture; исправлена в тесте, оба запуска сохранены. Установлен только test APK `artifacts/sf7b-lab-0.2.0-candidate11-cache-contract-tests-v2.apk`,SHA950213f0a35336bd1766cfed2d9ee30da277c625ac5e3acbdf25ad154eff9ed1,прежняя подпись. AppSHA8fb06764…1a500 подтверждён REA pull.

Все50прежних private файлов сохранились побайтно, включая оба library indexes иprefs; post backup52файла повторно сверены, добавлены только2CSV. Lab.sf7b остановлено. Cold15-layer15,4с/warm blend219мс — одиночные CPU preview512² на документе2000², не полноразмерный/performance benchmark. Отчёт2.2 (материал отсутствует в этой копии), JSON (материал отсутствует в этой копии). Private копии вне поставки. Следующий после отдельного возобновления только2.3 recovery/save/RU-EN warnings с проверкой A51/backup. 2.4/2.5 pending.

Канонический STATE (материал отсутствует в этой копии), notes (материал отсутствует в этой копии). Ниже историческое состояние2.1.

13.11.2026 20:23МСК: **2.1 выполнен; пауза перед 2.2. OpenSpec5/43.** Candidate11 app/testAPK установлены на A51 обновлением без clear/uninstall, точные SHA подтверждены REA pulls. Один 15-layer метод MixedDocumentDeviceTest прошёл **OK(1),безskip**: 15слоёв/50узлов,TTF/seed/crop/portable Document/ресурсы/точныеpixels/PNG130×154 alpha. Canvas192×144,CPU3921/2955мс,по1прогону;это не полноразмерный15-layer benchmark/process-death. Исходники/сборка не менялись.

Свежая private копия доupdate49файлов проверена;послеinstall49/49 совпали. После теста все прежние document/last-good/assets/prefs/current library/UUID сохранны,потерь0. Изменились device-info/profileInstalled и backup index (теперь точная копия исправного pre-current),46/49 прежних файлов неизменны;только1новый test-report. Post-test TAR50файлов повторно сверены. Полные пути/SHA/ограничения в отчёте2.1 (материал отсутствует в этой копии) и JSON (материал отсутствует в этой копии). Private backup/REA Evidence вне поставки. Lab.sf7b остановлено. Следующий после отдельного возобновления только2.2 MixedPreviewCacheDeviceTest с проверкой A51/актуальности backup;recovery2.3/import-GPU-UI2.4/отчёты2.5 pending.

Канонический STATE (материал отсутствует в этой копии), notes (материал отсутствует в этой копии). Ниже история1.4,не текущий статус.

13.11.2026 20:10МСК: **1.4 завершён; пауза перед2.1**. OpenSpec4/43. Candidate11 app/test APK сохранены отдельно, SHA/сертификаты проверены и связаны с успешной сборкой1.3. Следующий2.1 — проверка A51/актуального private backup, update и MixedDocumentDeviceTest. Установки/native tests ещё не выполнялись.

App APK (материал отсутствует в этой копии),12621793bytes, SHA256 `8fb06764875f9782240d0f8d41a91a1cfa681cdacdd5c1057b411f5c89a1a500`. Test APK (материал отсутствует в этой копии),1173964bytes, SHA256 `05084940d2ec9827fd76562b31a0ed321b339b72fd484cfcb3aecc907f379a2a`. Оба apksigner verified v2, общий certSHA256 `dd65119b5cea90780660de67721250bc54516fe9c4aedcc43bc90abf729ea6d3`, как candidate10. Манифест19evidence SHA (материал отсутствует в этой копии), SHA256SUMS (материал отсутствует в этой копии), отчёт1.4 (материал отсутствует в этой копии). Public PEM отдельно, private key/backup не включены.

Отчёт1.3 (материал отсутствует в этой копии), JSON результатов (материал отсутствует в этой копии), команда/время (материал отсутствует в этой копии):19:54:22–20:00:55МСК,6м32с/exit0; core70pass/1skip/0fail/error, app/testAPK собраны, Lint0errors/18warnings. Исторические result1.3 не редактировались. В1.4 новых сборок/тестов/обращений к телефону не было. Native/A51/Mixed tests/миграции остаются2.x.

Канонический checkpoint: [STATE.md](../../STATE.md).
Хронология: notes.md (материал отсутствует в этой копии).

В1.2 A51 снова доступен: выгруженный установленный APK побайтно candidate10. Свежая private копия app data19:45:26МСК:44470272bytes, SHA256 `a7b3195b9b2f67d6821193a136761cf64383350b464016406ac2f2562b5c4727`; все44 файла восстановлены на хосте и совпали по SHA с повторным чтением устройства. Новый частный source checkpoint91файл: SHA256 `a0b2e7a2b02bd4c831d70045f83f6de671b6c0003782611899344464d7fee7c5`; отдельные docs23файла и private key. Старые копии также проверены. Копия ключа подписала изолированный probe; apksigner подтвердил прежний сертификат. Только force-stop lab.sf7b; install/clear/uninstall и изменения app data не выполнялись.

Отчёт/восстановление1.2 (материал отсутствует в этой копии), SHA/агрегаты (материал отсутствует в этой копии). Private checkpoint/backup/restore folders исключать из поставки. Android-открытие восстановленных проектов/миграции остаются2.x. Код Android не менялся; build/core/Lint проверены в1.3, native ещё pending. Среда1.1: manifest (материал отсутствует в этой копии); статус devices=[] в нём исторический.

Команда выполненной1.3 из этой папки (для воспроизводимости; сейчас не запускать повторно):

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\gradlew.bat '-Psf7bBuildRoot=<local-private-path>' --no-daemon --max-workers=1 --no-parallel '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8 -XX:ActiveProcessorCount=2' '-Pkotlin.compiler.execution.strategy=in-process' :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

Последняя наблюдавшаяся установленная/проверенная на A51 версия — candidate10 из1.2. Candidate11 собран, архив/SHA/cert/core/Lint подтверждены; новая установка и native/A512.x ещё впереди. Перед2.1 проверить, не менялись ли проекты после backup19:45:26МСК; при изменениях снять новую копию, не clear/uninstall.
