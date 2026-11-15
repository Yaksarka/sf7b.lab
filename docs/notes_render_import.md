> Исторический журнал/постановка. Действующие разрешения и точка остановки — [AGENTS](../AGENTS.md) и [STATE](../STATE.md). Архив не отменяет паузу разработки.

# Кэш дорогих узлов предпросмотра

13.11.2026, продолжение после checkpoint 11:50. Исходники: `<repository>`. Применены RTK и karpathy-guidelines; `.codegraph` отсутствует. Gradle/JADX/ADB этим агентом не запускались: сборку и телефон контролирует root.

## Изменения

- `Renderer.cacheable`: кроме действующих эффектов, кэшируются текст, генераторы, непустое рисование, трансформированные изображения, реальные blend-проходы, маски слоёв/узлов и узлы с внешней маской. Lookup выполняется до оценки входов узла.
- Обычный image-source, solid, пустой drawing/text, одиночный stack без opacity, слой без маски, выключенные/нулевые и нейтральные adjust/curves не создают дополнительные растры. Математика рендера не изменялась.
- `NodeRasterCache.keys`: версия пространства производных растров v4, прежние namespace/resourceVersion/размер/версия документа/алгоритм/seed/параметры/маски/геометрия сохранены. Добавлены проверки отмены при построении ключей. После загрузки cache-hit Renderer повторно проверяет отмену до возврата результата.
- Неизвестный resourceVersion по-прежнему отключает reuse зависимой ветви. SHA-256, атомарная запись, ограничение 192 MiB и LRU остались прежними. Полноразмерный экспорт и миниатюры не подключались к preview-cache.

## Новые контракты NodeRasterCacheTest

Восемь тестов: text-only reuse с пропуском font-renderer; изменение верхнего blend/маски без повторной растеризации смешанных источников; независимая invalidation текста/штрихов/seed генератора/transform изображения; версия байтов шрифта и unknown font; mask-node lookup до image-source; отсутствие записей для дешёвых aliases; invalidation algorithmVersion; отмена после cache-hit без публикации результата или повреждения прежних записей. Проверяются точные пиксели cached против uncached и реальные вызовы SourceProvider.

Старые семь контрактов сохранены: параметры/seed/mask/resolution/resourceVersion/namespace, усечённый кэш, отмена записи, LRU и посторонние файлы.

## Проверка

Новые изменения ещё не собраны и тесты не исполнены. Root должен выполнить один общий build; целевой быстрый набор из папки исходников:

```powershell
.\gradlew.bat '-Psf7bBuildRoot=<local-private-path>' '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' '-Pkotlin.compiler.execution.strategy=in-process' --no-daemon --max-workers=1 --no-parallel --console=plain :core:test --tests lab.sf7b.core.NodeRasterCacheTest
```

После целевого набора нужен обычный полный core:test (включая сохранённые pixel snapshots) и сборка приложения/тестового APK. Новых замеров latency/RAM здесь пока нет. Native 15-layer persistence делает project_storage, его файл не менялся.

## 12:05 — отдельный native-контракт

По дополнительному поручению root добавлен `MixedPreviewCacheDeviceTest.kt`: собственный PNG 2000×2000, 15 image/text/drawing/generator/solid слоёв, transform/opacity/blend/маски, CPU preview 512×512. Проверяются cold render, изменение верхнего blend, нулевые повторные image/text-provider calls, минимум два cache hits, точное cached/uncached совпадение всех premultiplied pixels, реальное изменение картинки и бюджет 64 MiB. Scratch pool 48 MiB соответствует текущему EditorModel.

CSV `files/test-results/mixed-preview-cache.csv`: один cold graph+decode, один warm upper-blend edit, один uncached graph с уже прогретым decode cache. Это не медианы и не сравнение с оригиналами; PNG fixture creation, UI, autosave, cleanup директорий и прочая работа приложения не входят в интервалы. Тест использует только собственную UUID cache-directory, не ProjectLibrary и не текущую пользовательскую работу. Согласован с project_storage; его `MixedDocumentDeviceTest.kt` не менялся. Новый native-тест ещё не скомпилирован/не исполнен, запуск централизованно делает root.
