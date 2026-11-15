package lab.sf7b.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.*

private const val MAX_MANIFEST_BYTES = 32L * 1024 * 1024
private const val MAX_PACKAGE_BYTES = 1_500_000_000L
private const val MAX_THUMBNAIL_BYTES = 16L * 1024 * 1024
@Serializable private data class PackageMember(val path: String, val size: Long, val sha256: String)
@Serializable private data class PackageIndex(val formatVersion: Int = 1, val files: List<PackageMember>)
private fun readBounded(file: File, limit: Long = MAX_MANIFEST_BYTES): String {
    require(file.isFile && file.length() <= limit) { "Missing or oversized document" }
    return file.readText(Charsets.UTF_8)
}
/** Flush a private temporary file before replacing a live file. */
private fun writeAtomic(target: File, bytes: ByteArray) {
    target.parentFile?.mkdirs()
    val temporary = File(target.parentFile, ".${target.name}-${uid()}.tmp")
    try {
        FileOutputStream(temporary).use { it.write(bytes); it.fd.sync() }
        atomicReplace(temporary, target)
    } finally { temporary.delete() }
}
private fun writeJsonWithBackup(target: File, previous: File, text: String, valid: (String) -> Unit) {
    if (target.isFile) {
        val old = runCatching { readBounded(target).also(valid) }.getOrNull()
        if (old != null) writeAtomic(previous, old.toByteArray(Charsets.UTF_8))
    }
    writeAtomic(target, text.toByteArray(Charsets.UTF_8))
}

class ProjectFiles(val directory: File) {
    val assets = File(directory, "assets")
    var recoveredFromBackup: Boolean = false; private set
    init { require(assets.mkdirs() || assets.isDirectory) { "Cannot create project storage" } }
    @Synchronized fun save(doc: Document) {
        val encoded=Codec.encode(doc);val bytes = encoded.toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_MANIFEST_BYTES) { "Document exceeds 32 MB" }
        val current=File(directory,"document.json")
        if(runCatching{readBounded(current)}.getOrNull()==encoded)return
        writeJsonWithBackup(current, File(directory, "last-good.json"), encoded) { Codec.decode(it) }
    }
    @Synchronized fun load(): Document? {
        recoveredFromBackup = false
        val current = File(directory, "document.json"); val previous = File(directory, "last-good.json")
        if (!current.exists() && !previous.exists()) return null
        return try { Codec.decode(readBounded(current)) } catch (first: Exception) {
            try { Codec.decode(readBounded(previous)).also { recoveredFromBackup = true } } catch (_: Exception) { throw IOException("The project and recovery copy could not be opened", first) }
        }
    }
    fun validateResources(doc: Document) {
        resourcePaths(doc).forEach { require(File(directory, it).isFile) { "Missing asset $it" } }
    }
    @Synchronized fun pack(doc: Document, output: OutputStream, thumbnail: ByteArray? = null, check: () -> Unit = {}) {
        val manifest = Codec.encode(doc).toByteArray(Charsets.UTF_8)
        require(manifest.size <= MAX_MANIFEST_BYTES) { "Document exceeds 32 MB" }
        require(thumbnail == null || thumbnail.size <= MAX_THUMBNAIL_BYTES) { "Thumbnail too large" }
        validateResources(doc)
        val members = mutableListOf<PackageMember>(); var total = 0L
        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            fun add(name: String, input: InputStream) {
                val hash = MessageDigest.getInstance("SHA-256"); var size = 0L
                zip.putNextEntry(ZipEntry(name))
                input.use { source ->
                    val buffer = ByteArray(65536)
                    while (true) {
                        check(); val count = source.read(buffer); if (count < 0) break
                        size += count; total += count; require(total <= MAX_PACKAGE_BYTES) { "Project exceeds 1.5 GB" }
                        hash.update(buffer, 0, count); zip.write(buffer, 0, count)
                    }
                }
                zip.closeEntry()
                members += PackageMember(name, size, hash.digest().joinToString("") { "%02x".format(it.toInt() and 255) })
            }
            add("manifest.json", manifest.inputStream())
            resourcePaths(doc).forEach { add(it, File(directory, it).inputStream()) }
            if (thumbnail != null) add("thumbnail.png", thumbnail.inputStream())
            val index = Codec.json.encodeToString(PackageIndex(files = members)).toByteArray(Charsets.UTF_8)
            zip.putNextEntry(ZipEntry("package.json")); zip.write(index); zip.closeEntry()
        }
    }
    /** Only an empty destination is accepted; extraction happens in a private staging directory. */
    @Synchronized fun unpack(input: InputStream, check: () -> Unit = {}): Document {
        require(directory.listFiles().orEmpty().all { it == assets && it.isDirectory && it.listFiles().orEmpty().isEmpty() }) { "Import needs an empty staging directory" }
        val stage = File(directory, ".unpack-${uid()}"); require(stage.mkdir()) { "Cannot create import staging directory" }
        val members = linkedMapOf<String, PackageMember>()
        try {
            var total = 0L; var count = 0
            ZipInputStream(BufferedInputStream(input)).use { zip ->
                while (true) {
                    check(); val entry = zip.nextEntry ?: break
                    require(++count <= 1024 && entry.name !in members) { "Invalid or duplicate archive entry" }
                    require(!entry.isDirectory && (entry.name in listOf("manifest.json", "thumbnail.png", "package.json") || safeAsset(entry.name))) { "Unsafe archive path" }
                    val destination = File(stage, entry.name); destination.parentFile!!.mkdirs()
                    val hash = MessageDigest.getInstance("SHA-256"); var size = 0L
                    FileOutputStream(destination).use { out ->
                        val buffer = ByteArray(65536)
                        while (true) {
                            check(); val n = zip.read(buffer); if (n < 0) break
                            total += n; size += n; require(total <= MAX_PACKAGE_BYTES) { "Project exceeds 1.5 GB" }
                            require(entry.name !in listOf("manifest.json", "package.json") || size <= MAX_MANIFEST_BYTES) { "Manifest too large" }
                            require(entry.name != "thumbnail.png" || size <= MAX_THUMBNAIL_BYTES) { "Thumbnail too large" }
                            hash.update(buffer, 0, n); out.write(buffer, 0, n)
                        }
                        out.fd.sync()
                    }
                    members[entry.name] = PackageMember(entry.name, size, hash.digest().joinToString("") { "%02x".format(it.toInt() and 255) }); zip.closeEntry()
                }
            }
            val doc = Codec.decode(readBounded(File(stage, "manifest.json")))
            val integrity = File(stage, "package.json")
            if (integrity.exists()) {
                val index = Codec.json.decodeFromString<PackageIndex>(readBounded(integrity))
                require(index.formatVersion == 1) { "Unsupported package version: ${index.formatVersion}" }
                require(index.files.map { it.path }.distinct().size == index.files.size) { "Duplicate integrity record" }
                require(index.files.associateBy { it.path } == members.filterKeys { it != "package.json" }) { "Project integrity check failed" }
            }
            val paths = resourcePaths(doc)
            paths.forEach { require(File(stage, it).isFile) { "Missing asset $it" } }
            paths.forEach { path -> check(); atomicReplace(File(stage, path), File(directory, path)) }
            File(stage, "thumbnail.png").takeIf { it.isFile }?.let { atomicReplace(it, File(directory, "thumbnail.png")) }
            save(doc); return doc
        } finally { stage.deleteRecursively() }
    }
    companion object {
        fun resourcePaths(doc: Document): List<String> = doc.layers.flatMap { listOf(it.asset, it.text.font) }.filter { it.isNotBlank() }.distinct()
            .also { paths -> paths.forEach { require(safeAsset(it)) { "Unsafe asset path" } } }
    }
}

@Serializable private data class StoredProject(val id: String, val name: String, val updatedAt: Long, val width: Int, val height: Int, val createdAt: Long = updatedAt, val revision: Long = 1)
@Serializable private data class LibraryIndex(val version: Int = 1, val activeId: String? = null, val projects: List<StoredProject> = emptyList())
data class ProjectEntry(val id: String, val name: String, val updatedAt: Long, val width: Int, val height: Int, val thumbnail: File?, val revision: Long)
data class OpenProject(val entry: ProjectEntry, val directory: File, val document: Document)
enum class RecoveryKind { DOCUMENT_BACKUP, LIBRARY_INDEX, PROJECT_METADATA, LEGACY_WORKSPACE_FAILED, DELETE_CLEANUP }
data class RecoveryNotice(val kind:RecoveryKind,val projectId:String?=null,val projectName:String?=null)

/** One directory per UUID. The Android caller should dispatch these synchronized methods to IO. */
class ProjectLibrary(private val filesDir: File) {
    private val root = File(filesDir, "projects")
    private val indexFile = File(root, "library.json")
    private val backupIndex = File(root, "library-last-good.json")
    private var index: LibraryIndex
    private val warnings = mutableListOf<String>()
    private val notices = mutableListOf<RecoveryNotice>()
    val recoveryMessages: List<String> get() = synchronized(this) { warnings.toList() }
    val recoveryNotices: List<RecoveryNotice> get() = synchronized(this) { notices.toList() }
    private fun warn(text:String,notice:RecoveryNotice){if(notice !in notices){warnings+=text;notices+=notice}}
    init {
        require(root.mkdirs() || root.isDirectory) { "Cannot create project library" }
        index = loadIndex(); recoverPublishedDirectories(); migrateLegacyWorkspace()
    }
    @Synchronized fun list(): List<ProjectEntry> = index.projects.sortedByDescending { it.updatedAt }.map(::entry)
    @Synchronized fun activeId(): String? = index.activeId
    @Synchronized fun setActive(id: String?) {
        require(id == null || index.projects.any { it.id == id }) { "Unknown project" }
        replaceIndex(index.copy(activeId = id))
    }
    @Synchronized fun defaultName(): String {
        val base = "sf7b.lab_" + SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ROOT).format(Date())
        var name = base; var suffix = 2
        while (index.projects.any { it.name == name }) name = "$base (${suffix++})"
        return name
    }
    @Synchronized fun create(document: Document, resourcesFrom: File? = null, name: String? = null): ProjectEntry {
        val named = document.copy(name = checkedName(name ?: document.name.takeUnless { it.isBlank() || it == "Untitled" } ?: defaultName()))
        return publish(named, resourcesFrom).entry
    }
    @Synchronized fun open(id: String): OpenProject {
        var stored = find(id); val directory = directory(id); val files = ProjectFiles(directory)
        val document = files.load() ?: error("Missing project document"); files.validateResources(document)
        if(files.recoveredFromBackup)warn("Recovered previous project version: ${document.name}",RecoveryNotice(RecoveryKind.DOCUMENT_BACKUP,id,document.name))
        if(stored.name!=document.name||stored.width!=document.width||stored.height!=document.height){
            stored=stored.copy(name=document.name,width=document.width,height=document.height)
            writeMetadata(directory,stored)
            replaceIndex(index.copy(projects=index.projects.map{if(it.id==id)stored else it}))
        }
        return OpenProject(entry(stored), directory, document)
    }
    @Synchronized fun save(id: String, document: Document, thumbnail: ByteArray? = null): ProjectEntry {
        val old = find(id); val directory = directory(id); require(directory.isDirectory) { "Project was deleted" }
        val doc = document.copy(name = checkedName(document.name)); val store = ProjectFiles(directory)
        store.validateResources(doc)
        val saved=try{store.load()}catch(_:IOException){null}
        if(saved==doc&&!store.recoveredFromBackup){if(thumbnail!=null)saveThumbnail(id,thumbnail,doc);return entry(old)}
        store.save(doc)
        val updated = old.copy(name = doc.name, width = doc.width, height = doc.height, updatedAt = maxOf(System.currentTimeMillis(), old.updatedAt + 1), revision = old.revision + 1)
        writeMetadata(directory, updated)
        replaceIndex(index.copy(projects = index.projects.map { if (it.id == id) updated else it }))
        if (thumbnail != null) saveThumbnail(id, thumbnail, doc)
        return entry(updated)
    }
    /** A delayed renderer cannot replace a newer document's thumbnail. */
    @Synchronized fun saveThumbnail(id: String, bytes: ByteArray, expectedDocument: Document? = null): Boolean {
        require(validId(id)) { "Invalid project ID" }
        if (index.projects.none { it.id == id }) return false
        require(bytes.size <= MAX_THUMBNAIL_BYTES) { "Thumbnail too large" }
        val directory = directory(id); require(directory.isDirectory) { "Project was deleted" }
        if (expectedDocument != null && ProjectFiles(directory).load() != expectedDocument) return false
        writeAtomic(File(directory, "thumbnail.png"), bytes); return true
    }
    @Synchronized fun rename(id: String, name: String): ProjectEntry = save(id, open(id).document.copy(name = checkedName(name)))
    @Synchronized fun duplicate(id: String, name: String? = null, check: () -> Unit = {}): ProjectEntry {
        val source = open(id); val previousActive = index.activeId
        val copied = publish(source.document.copy(name = checkedName(name ?: "${source.document.name.take(195)} copy")), source.directory, check = check).entry
        setActive(previousActive)
        return copied
    }
    @Synchronized fun delete(id: String) {
        find(id); val original = directory(id); val retired = File(root, ".deleted-$id-${uid()}")
        atomicReplace(original, retired)
        try {
            val kept = index.projects.filter { it.id != id }
            replaceIndex(index.copy(projects = kept, activeId = if (index.activeId == id) kept.maxByOrNull { it.updatedAt }?.id else index.activeId))
        } catch (e: Exception) { atomicReplace(retired, original); throw e }
        if (!retired.deleteRecursively()) warn("Deleted project cleanup is pending: $id",RecoveryNotice(RecoveryKind.DELETE_CLEANUP,id))
    }
    @Synchronized fun importProject(input: InputStream, check: () -> Unit = {}): OpenProject {
        val stage = File(root, ".import-${uid()}")
        try {
            val imported = ProjectFiles(stage).unpack(input, check)
            val doc = imported.copy(name = checkedName(imported.name.takeUnless { it.isBlank() } ?: defaultName()))
            if(doc!=imported)ProjectFiles(stage).save(doc)
            val id=uid();val now=System.currentTimeMillis();val stored=StoredProject(id,doc.name,now,doc.width,doc.height)
            writeMetadata(stage,stored);check();atomicReplace(stage,directory(id))
            replaceIndex(index.copy(activeId=id,projects=index.projects+stored))
            return OpenProject(entry(stored),directory(id),doc)
        }
        finally { stage.deleteRecursively() }
    }
    @Synchronized fun exportProject(id: String, output: OutputStream, thumbnail: ByteArray? = null, check: () -> Unit = {}) {
        val opened = open(id)
        val thumb = thumbnail ?: opened.entry.thumbnail?.takeIf { it.length() <= MAX_THUMBNAIL_BYTES }?.readBytes()
        ProjectFiles(opened.directory).pack(opened.document, output, thumb, check)
    }
    private fun find(id: String): StoredProject {
        require(validId(id)) { "Invalid project ID" }
        return index.projects.firstOrNull { it.id == id } ?: error("Unknown project: $id")
    }
    private fun directory(id: String): File { require(validId(id)) { "Invalid project ID" }; return File(root, id) }
    private fun entry(info: StoredProject): ProjectEntry = ProjectEntry(info.id, info.name, info.updatedAt, info.width, info.height, File(directory(info.id), "thumbnail.png").takeIf { it.isFile }, info.revision)
    private fun publish(document: Document, source: File?, forcedId: String = uid(), check: () -> Unit = {}): OpenProject {
        document.validate(); require(validId(forcedId) && !directory(forcedId).exists()) { "Duplicate project ID" }
        val stage = File(root, ".create-${uid()}"); val store = ProjectFiles(stage)
        try {
            ProjectFiles.resourcePaths(document).forEach { path ->
                check(); val input = source?.let { File(it, path) } ?: error("Missing resource source"); require(input.isFile) { "Missing asset $path" }
                input.inputStream().use { from -> FileOutputStream(File(stage, path)).use { to ->
                    val buffer = ByteArray(65536)
                    while (true) { check(); val n = from.read(buffer); if (n < 0) break; to.write(buffer, 0, n) }
                    to.fd.sync()
                } }
            }
            source?.let { File(it, "thumbnail.png") }?.takeIf { it.isFile && it.length() <= MAX_THUMBNAIL_BYTES }?.copyTo(File(stage, "thumbnail.png"))
            store.save(document); val now = System.currentTimeMillis(); val stored = StoredProject(forcedId, document.name, now, document.width, document.height)
            writeMetadata(stage, stored); check(); atomicReplace(stage, directory(forcedId))
            // A published directory can be recovered if the subsequent index update is interrupted.
            replaceIndex(index.copy(activeId = forcedId, projects = index.projects + stored))
            return OpenProject(entry(stored), directory(forcedId), document)
        } finally { stage.deleteRecursively() }
    }
    private fun replaceIndex(next: LibraryIndex) {
        writeJsonWithBackup(indexFile, backupIndex, Codec.json.encodeToString(next)) { validateIndex(it) }; index = next
    }
    private fun validateIndex(text: String): LibraryIndex = Codec.json.decodeFromString<LibraryIndex>(text).also {
        require(it.version == 1 && it.projects.map { p -> p.id }.distinct().size == it.projects.size)
        require(it.projects.all { p -> validId(p.id) && p.revision >= 1 && p.name.isNotBlank() })
        require(it.activeId == null || it.projects.any { p -> p.id == it.activeId })
    }
    private fun loadIndex(): LibraryIndex {
        if (!indexFile.exists() && !backupIndex.exists()) return LibraryIndex()
        return runCatching { validateIndex(readBounded(indexFile)) }.getOrElse {
            warn("Project library index was recovered",RecoveryNotice(RecoveryKind.LIBRARY_INDEX))
            runCatching { validateIndex(readBounded(backupIndex)) }.getOrDefault(LibraryIndex())
        }
    }
    private fun writeMetadata(directory: File, info: StoredProject) = writeAtomic(File(directory, "project-info.json"), Codec.json.encodeToString(info).toByteArray(Charsets.UTF_8))
    private fun recoverPublishedDirectories() {
        val recovered = index.projects.associateBy { it.id }.toMutableMap()
        root.listFiles().orEmpty().filter { it.isDirectory && validId(it.name) }.forEach { directory ->
            val metadata = runCatching { Codec.json.decodeFromString<StoredProject>(readBounded(File(directory, "project-info.json"))).also { require(it.id == directory.name && it.revision >= 1 && it.name.isNotBlank()) } }.getOrNull()
            if (metadata != null) recovered[metadata.id] = metadata
            else if (directory.name !in recovered) {
                val doc = runCatching { ProjectFiles(directory).load() }.getOrNull()
                val info = StoredProject(directory.name, doc?.name ?: "Recovery ${directory.name.take(8)}", directory.lastModified(), doc?.width ?: 1, doc?.height ?: 1)
                recovered[info.id] = info;warn("Project metadata was recovered: ${info.id}",RecoveryNotice(RecoveryKind.PROJECT_METADATA,info.id,info.name))
            }
        }
        val found = recovered.values.filter { directory(it.id).isDirectory }
        replaceIndex(LibraryIndex(activeId = index.activeId?.takeIf { id -> found.any { it.id == id } } ?: found.maxByOrNull { it.updatedAt }?.id, projects = found))
    }
    private fun migrateLegacyWorkspace() {
        val marker = File(root, "legacy-workspace-migrated"); if (marker.exists()) return
        val old = File(filesDir, "workspace")
        if (!old.isDirectory || (!File(old, "document.json").exists() && !File(old, "last-good.json").exists())) return
        val migrationId = UUID.nameUUIDFromBytes(old.canonicalPath.toByteArray(Charsets.UTF_8)).toString()
        try {
            if (index.projects.none { it.id == migrationId }) {
                val doc = ProjectFiles(old).load() ?: return; ProjectFiles(old).validateResources(doc)
                publish(doc.copy(name = doc.name.takeUnless { it.isBlank() || it == "Untitled" } ?: "Recovered workspace"), old, migrationId)
            }
            writeAtomic(marker, migrationId.toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {warn("Legacy workspace was retained and could not be migrated: ${e.message}",RecoveryNotice(RecoveryKind.LEGACY_WORKSPACE_FAILED))}
    }
    private fun checkedName(name: String): String = name.trim().also { require(it.isNotEmpty() && it.length <= 200) { "Project name must contain 1–200 characters" } }
    private fun validId(id: String): Boolean = runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false)
}
fun atomicReplace(temp: File, target: File) {
    try { Files.move(temp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING) }
    catch (_: java.nio.file.AtomicMoveNotSupportedException) { Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING) }
}
