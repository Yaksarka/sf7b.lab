# Производительность sf7b.lab

Актуально13.11.2026, candidate10. Полный SPEC ещё не выполнен; показатели ниже относятся к перечисленным графам, не всему каталогу. Native Android Galaxy A51 SM-A515F, Android13/API33, debug0.2.0. Свой sRGB PNG2000×2000; preview1024×1024. Три последовательных прогона на вариант, новый SurfacePool/AndroidSources, общий decode-cache. Это не три холодных запуска приложения. Питание/сети/драйверы не менялись.

## Измеренные медианы

| Сценарий | CPU8, мс | CPU9, мс | GPU9, мс | GPU10, мс |
|---|---:|---:|---:|---:|
| Identity |19|18|849|187|
| Adjust |3537|1609|879|223|
| Adjust→Invert→RGB split→Curves→Posterize |13771|7438|841|238|
| Bloom v2 |2641|2602|1101|465|
| Multi Distort v2 |19155|18789|не поддержан|не поддержан|
| Full PNG2000×2000, пять цветовых |38521*|14581*|4124|1764|

*CPU полный PNG — один прогон, остальные показанные значения — медиана3. Candidate10 изменил только host GPU upload/readback через bulk IntBuffer/ARGB↔ABGR; CPU9 и математика шейдеров не менялись. Candidate9 использует построчные9color kernels/lazy alpha LUT. Лишний8MiB lattice cache MultiDistort удалён без наблюдаемого выигрыша; candidates-per-cell pruning сохранён. Самая ранняя measured MultiDistort4=28525мс,5=18767мс. Для тяжёлого нового MultiDistort цель preview пока не достигнута.

CSV и условия: 8 (материал отсутствует в этой копии),9 (материал отсутствует в этой копии),10 GPU (материал отсутствует в этой копии),10 summary (материал отсутствует в этой копии). GPU-only10 test OK(1),12.956с; все8 correctness cases candidate10 прошли39.756с. Native22/22 candidate9, UI7/7 candidate8. CPU9 SHA snapshots72/72 (4718592pixels), distortion coordinates12/12 (43884) совпали точно с прежней собственной математикой. Это не доказательство паритета оригиналам.

## Границы измерения

Render включает рабочий decode, создание pool/EGL и вычисление графа. Bitmap-конверсия отдельно: GPU10 медианы57/57/58/121мс для identity/adjust/five/Bloom. Picker, Compose, autosave, cleanup после закрытия pool и пользовательская реакция исключены. `heap_bytes` — выборка Java+native в конце итерации, не peak/PSS/GPU memory. Full-PNG включает оригинальный full-size render+PNG+закрытие pool. Предпросмотр источником экспорта не является.

Реальный EditorModel fullGPU export2000×2000/alpha128, документ неизменен:4159мс candidate10,6124мс candidate8, по1прогону. Это иной интервал: queue wait,original load,render,PNG,fileURI; не контролируемая медиана. Model10 (материал отсутствует в этой копии).

Кэш CPU dependency nodes192MiB preview-only, decode-cache384MiB на диске, preview scratch RAM48MiB. В candidate6b lastThreshold после MultiDistort:cached1316мс/cold20092мс, first22244мс, по1прогону; exact pixels и0image reads при hit. Отчёт (материал отсутствует в этой копии). Пока кэшируются только active effects, расширение на источники/композитинг ещё не реализовано.

GPU export включён после full2000 parity: candidate6b maxchannel64 провалился, candidate7 после исправления квантования/интерполяции max0. Candidate10 bulk transfer сохранил все8 cases, tolerance не ослаблялась. GPU только простые image chains/basiccolors/Bloom при лимите4MP/texture/radius; остальные графы CPU. Identity bypass избегает лишнего EGL в приложении.

## Что ещё проверить

15смешанных слоёв на телефоне, source/blend cache и изменения раннего узла, настоящий end-to-end slider/import/project latency/panzoomFPS, peak memory; сопоставимые старый APK и оригиналы OneLab/Glitch на тех же операциях. Оригиналы по этим данным быстрее/медленнее не объявлять. История первых итераций — [архив](PERFORMANCE_HISTORY_20261113.md); Windows отдельные [RAM/tiled результаты](PERFORMANCE_RENDER.md).
