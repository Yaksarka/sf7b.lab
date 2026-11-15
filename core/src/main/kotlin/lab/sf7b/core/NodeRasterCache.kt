package lab.sf7b.core

import kotlinx.serialization.encodeToString
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/** Disposable preview results. No project data or export source is stored here. */
class NodeRasterCache(private val directory: File, private val namespace: String, private val maxBytes: Long = 192L * 1024 * 1024) {
    var hits = 0; private set
    var misses = 0; private set
    init { require(maxBytes >= 0); directory.mkdirs() }

    internal fun keys(doc: Document, width: Int, height: Int, sources: SourceProvider, check: () -> Unit = {}): Map<String, String?> {
        val nodes = doc.nodes.associateBy { it.id }
        val result = hashMapOf<String, String?>()
        val prefix = listOf("sf7b-preview-raster-v4", namespace, "$width,$height", "${doc.version},${doc.width},${doc.height},${doc.colorSpace}")
        fun key(id: String): String? {
            check()
            if (result.containsKey(id)) return result[id]
            val node = nodes.getValue(id)
            val parts = prefix.toMutableList()
            parts += Codec.json.encodeToString(node.copy(id = "", inputs = emptyList(), layer = "", maskInput = ""))
            if (node.kind == "source") {
                val layer = doc.layer(node.layer)
                parts += Codec.json.encodeToString(layer.copy(id = "", name = "", head = "", mask = null, opacity = 1f, visible = true, blend = "Normal"))
                val asset = when (layer.kind) { "image" -> layer.asset; "text" -> layer.text.font; else -> null }
                if (asset != null) {
                    val version = sources.resourceVersion(asset)
                    if (version == null) { result[id] = null; return null }
                    parts += version
                }
            } else if (node.kind == "layer") {
                parts += Codec.json.encodeToString(doc.layer(node.layer).mask)
            }
            for (input in node.inputs + listOfNotNull(node.maskInput.takeIf { it.isNotEmpty() })) {
                val child = key(input)
                if (child == null) { result[id] = null; return null }
                parts += child
            }
            check()
            return digest(parts).also { result[id] = it }
        }
        if (doc.root.isNotEmpty()) key(doc.root)
        return result
    }

    internal fun open(pool: SurfacePool, key: String, width: Int, height: Int): Surface? = synchronized(lock) {
        val file = File(directory, "$key.rgba")
        if (!file.isFile || file.length() != width.toLong() * height * 4) { misses++; return@synchronized null }
        try {
            pool.open(file, width, height).also { file.setLastModified(System.currentTimeMillis()); hits++ }
        } catch (_: IOException) { misses++; null }
          catch (_: IllegalArgumentException) { misses++; null }
    }

    internal fun store(key: String, surface: Surface, check: () -> Unit) {
        val size = surface.width.toLong() * surface.height * 4
        if (size > maxBytes) return
        val temporary = File(directory, ".${uid()}.tmp")
        val target = File(directory, "$key.rgba")
        try {
            directory.mkdirs()
            FileOutputStream(temporary).use { file ->
                BufferedOutputStream(file, 65536).use { output ->
                    val buffer = ByteBuffer.allocate(surface.width * 4).order(ByteOrder.LITTLE_ENDIAN)
                    for (y in 0 until surface.height) {
                        check(); buffer.clear(); buffer.asIntBuffer().put(surface.readRow(y)); output.write(buffer.array())
                    }
                }
            }
            check()
            synchronized(lock) {
                atomicReplace(temporary, target)
                target.setLastModified(System.currentTimeMillis())
                trim(target)
            }
        } catch (_: IOException) {
            // A full/unavailable derived cache must not prevent rendering or saving a project.
        } finally { temporary.delete() }
    }

    fun trim() = synchronized(lock) { trim(null) }
    private fun trim(protected: File?) {
        val files = directory.listFiles()?.filter { it.isFile && owned.matches(it.name) } ?: return
        var bytes = files.sumOf { it.length() }
        for (file in files.sortedBy { it.lastModified() }) {
            if (bytes <= maxBytes) break
            if (file == protected) continue
            val size = file.length()
            if (file.delete()) bytes -= size
        }
    }

    companion object {
        private val lock = Any()
        private val owned = Regex("[0-9a-f]{64}\\.rgba")
        private fun digest(parts: List<String>): String {
            val hash = MessageDigest.getInstance("SHA-256")
            for (part in parts) {
                val bytes = part.toByteArray(Charsets.UTF_8)
                hash.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); hash.update(bytes)
            }
            return hash.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
