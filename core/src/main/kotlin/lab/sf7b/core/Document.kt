package lab.sf7b.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.math.*

fun uid(): String = UUID.randomUUID().toString()
@Serializable data class Point(val x: Float, val y: Float)
@Serializable data class Stroke(val points: List<Point>, val color: Int = -1, val size: Float = .025f, val softness: Float = .3f, val opacity: Float = 1f, val erase: Boolean = false)
@Serializable data class Mask(val base: Float = 1f, val strokes: List<Stroke> = emptyList(), val selection: String = "none", val low: Float = .2f, val high: Float = .8f, val color: Int = -1, val tolerance: Float = .2f, val inverted: Boolean = false)
@Serializable data class Transform(val x: Float = 0f, val y: Float = 0f, val scale: Float = 1f, val angle: Float = 0f, val flipX: Boolean = false, val flipY: Boolean = false)
@Serializable data class TextData(val text: String = "sf7b.lab", val font: String = "", val size: Float = .12f, val color: Int = -1, val align: String = "center", val lineSpacing: Float = 1.15f, val letterSpacing: Float = 0f, val outline: Float = 0f, val outlineColor: Int = 0xff000000.toInt(), val shadow: Float = 0f)
@Serializable data class Effect(val id: String, val params: Map<String, Float> = emptyMap(), val seed: Long = 7, val enabled: Boolean = true, val mix: Float = 1f, val palette: List<Int> = listOf(0xff101020.toInt(),0xff246bce.toInt(),0xfff7cd86.toInt()), val curves: List<List<Point>> = List(4) { listOf(Point(0f,0f),Point(1f,1f)) }, val mask: Mask? = null, val algorithmVersion: Int = 1) {
    @kotlinx.serialization.Transient private val resolved = Catalog.definition(id,algorithmVersion).params.associate{it.id to it.default}+params
    fun p(key: String): Float = resolved[key] ?: 0f
    @kotlinx.serialization.Transient val radians:Double=(resolved["angle"]?:0f)*PI/180
    @kotlinx.serialization.Transient val cosine:Float=cos(radians).toFloat()
    @kotlinx.serialization.Transient val sine:Float=sin(radians).toFloat()
    @kotlinx.serialization.Transient val hueCosine:Float=cos((resolved["hue"]?:0f)*PI/180).toFloat()
    @kotlinx.serialization.Transient val hueSine:Float=(sin((resolved["hue"]?:0f)*PI/180)/sqrt(3.0)).toFloat()
    @kotlinx.serialization.Transient val contrastMultiplier:Float=2f.pow((resolved["contrast"]?:0f)*2)
    @kotlinx.serialization.Transient private val exposureCurve:FloatArray?=if(id=="adjust"&&(resolved["exposure"]?:0f)!=0f)FloatArray(4096){i->val encoded=i/4095f;val linear=if(encoded<=.04045f)encoded/12.92f else ((encoded+.055f)/1.055f).pow(2.4f);val exposed=linear*2f.pow(resolved["exposure"]?:0f);if(exposed<=.0031308f)exposed*12.92f else 1.055f*exposed.pow(1f/2.4f)-.055f}else null
    fun exposed(encoded:Float):Float=exposureCurve?.get((encoded.coerceIn(0f,1f)*4095).roundToInt())?:encoded
}
@Serializable data class Layer(val id: String = uid(), val name: String = "Layer", val kind: String = "solid", val asset: String = "", val color: Int = 0, val generator: Effect = Effect("linear_gradient"), val text: TextData = TextData(), val strokes: List<Stroke> = emptyList(), val transform: Transform = Transform(), val opacity: Float = 1f, val visible: Boolean = true, val blend: String = "Normal", val mask: Mask? = null, val head: String = "")
@Serializable data class Node(val id: String = uid(), val kind: String = "effect", val inputs: List<String> = emptyList(), val layer: String = "", val effect: Effect = Effect("rgb_split"), val blend: String = "Normal", val opacity: Float = 1f, val automatic: Boolean = false, val maskInput:String="", val maskData:Mask=Mask())
@Serializable data class Frame(val left: Float = 0f, val top: Float = 0f, val width: Float = 1f, val height: Float = 1f, val turn: Int = 0, val flipX: Boolean = false, val flipY: Boolean = false)
@Serializable data class Document(val version: Int = 1, val name: String = "Untitled", val width: Int = 1080, val height: Int = 1080, val layers: List<Layer> = emptyList(), val nodes: List<Node> = emptyList(), val root: String = "", val frame: Frame = Frame(), val colorSpace: String = "sRGB") {
    val outputWidth: Int get() = max(1, if (frame.turn % 2 == 0) (width * frame.width).roundToInt() else (height * frame.height).roundToInt())
    val outputHeight: Int get() = max(1, if (frame.turn % 2 == 0) (height * frame.height).roundToInt() else (width * frame.width).roundToInt())
    fun layer(id: String): Layer = layers.first { it.id == id }
    fun withLayer(l: Layer): Document = copy(layers = layers.map { if (it.id == l.id) l else it }).stack()
    fun addLayer(l: Layer): Document {
        val source = Node(id="source-${l.id}",kind="source",layer=l.id)
        return copy(layers=layers+l.copy(head=source.id),nodes=nodes+source).stack()
    }
    fun addEffect(layerId: String, effect: Effect, input: String? = null): Document {
        val l = layer(layerId)
        val node = Node(inputs=listOf(input ?: l.head),layer=layerId,effect=effect)
        return copy(nodes=nodes+node,layers=layers.map { if(it.id==layerId) it.copy(head=node.id) else it }).stack()
    }
    fun branch(layerId: String, a: String, b: String, mode: String): Document {
        val n=Node(kind="blend",inputs=listOf(a,b),layer=layerId,blend=mode)
        return copy(nodes=nodes+n,layers=layers.map { if(it.id==layerId) it.copy(head=n.id) else it }).stack().also { it.validate() }
    }
    fun removeNode(id: String): Document {
        val n=nodes.first{it.id==id}; require(n.kind!="source" && !n.automatic)
        val fallback=n.inputs.first()
        return copy(nodes=nodes.filter{it.id!=id}.map{it.copy(inputs=it.inputs.map{v->if(v==id)fallback else v},maskInput=if(it.maskInput==id)""else it.maskInput)}, layers=layers.map{if(it.head==id)it.copy(head=fallback)else it}).stack()
    }
    fun removeLayer(id: String): Document {
        val removed=nodes.filter{it.layer==id}.map{it.id}.toSet()
        require(nodes.none{it.layer!=id && !it.automatic && (it.inputs.any{v->v in removed}||it.maskInput in removed)}) { "Layer is used by another branch" }
        return copy(layers=layers.filter{it.id!=id},nodes=nodes.filter{it.id !in removed}).stack()
    }
    fun duplicateLayer(id: String): Document {
        val original=layer(id); val newId=uid()
        val own=nodes.filter{it.layer==id && !it.automatic}; val ids=own.associate{it.id to if(it.kind=="source")"source-$newId" else uid()}
        val copied=own.map{it.copy(id=ids.getValue(it.id),layer=newId,inputs=it.inputs.map{v->ids[v]?:v},maskInput=ids[it.maskInput]?:it.maskInput)}
        return copy(layers=layers+original.copy(id=newId,name=original.name+" +",head=ids[original.head]?:original.head),nodes=nodes+copied).stack()
    }
    fun stack(): Document {
        val ns=nodes.filter{!it.automatic}.toMutableList(); var previous=""
        for(l in layers) {
            val output=Node(id="output-${l.id}",kind="layer",inputs=listOf(l.head),layer=l.id,automatic=true)
            ns+=output
            val n=Node(id="stack-${l.id}",kind="blend",inputs=listOfNotNull(previous.takeIf{it.isNotEmpty()},output.id),layer=l.id,blend=l.blend,opacity=if(l.visible)l.opacity else 0f,automatic=true)
            ns+=n; previous=n.id
        }
        return copy(nodes=ns,root=previous)
    }
    fun validate() {
        require(version==1) { "Unsupported project version: $version" }
        require(width in 1..30000 && height in 1..30000 && width.toLong()*height<=240_000_000) { "Invalid canvas size" }
        require(colorSpace=="sRGB") { "Only sRGB is supported" }
        require(layers.size<=128 && nodes.size<=1024) { "Project is too complex" }
        val f=frame
        require(listOf(f.left,f.top,f.width,f.height).all{it.isFinite()} && f.left>=0 && f.top>=0 && f.width>0 && f.height>0 && f.left+f.width<=1.00001f && f.top+f.height<=1.00001f && f.turn in 0..3)
        require(nodes.map{it.id}.distinct().size==nodes.size && layers.map{it.id}.distinct().size==layers.size)
        val ids=nodes.associateBy{it.id}; val visiting=hashSetOf<String>(); val done=hashSetOf<String>()
        fun visit(id: String) {
            if(id in done)return
            require(visiting.add(id)) { "Cyclic graph" }; val n=ids[id]?:error("Missing node $id")
            require(n.kind in listOf("source","effect","blend","mask","layer"))
            val secondSource=n.effect.id=="displacement"||(n.effect.id=="low_freq_banding"&&n.effect.algorithmVersion==2)
            require((n.kind=="source" && n.inputs.isEmpty())||(n.kind=="effect"&&(n.inputs.size==1||(secondSource&&n.inputs.size==2)))||(n.kind=="blend"&&n.inputs.size in 1..2)||(n.kind in listOf("mask","layer")&&n.inputs.size==1))
            if(n.kind=="effect") validateEffect(n.effect)
            require(n.opacity.isFinite()&&n.opacity in 0f..1f && n.blend in BLENDS)
            if(n.kind=="mask")validateMask(n.maskData)
            require(layers.any{it.id==n.layer}) { "Missing node owner" }
            n.inputs.forEach{visit(it)};if(n.maskInput.isNotEmpty())visit(n.maskInput); visiting.remove(id);done+=id
        }
        if(root.isNotEmpty())visit(root)
        nodes.forEach{visit(it.id)}
        layers.forEach { l ->
            require(l.kind in listOf("image","solid","generator","text","drawing")); require(l.head in ids)
            require(l.kind!="image" || l.asset.isNotEmpty()) { "Image layer has no asset" }
            require(nodes.any{it.kind=="source"&&it.layer==l.id})
            require(l.opacity.isFinite()&&l.opacity in 0f..1f && l.blend in BLENDS)
            require(listOf(l.transform.x,l.transform.y,l.transform.scale,l.transform.angle).all{it.isFinite()} && l.transform.scale in .01f..20f)
            require(l.asset.isEmpty() || safeAsset(l.asset)); require(l.text.font.isEmpty()||safeAsset(l.text.font))
            require(l.text.text.length<=100000 && l.text.size in .001f..2f && l.text.lineSpacing in .5f..3f && l.text.letterSpacing in -.1f..1f && l.text.outline in 0f..0.05f && l.text.shadow in 0f..0.1f)
            require(l.text.align in listOf("left","center","right")) { "Invalid text alignment" }
            validateEffect(l.generator); validateMask(l.mask); validateStrokes(l.strokes)
        }
        nodes.filter{it.kind=="source"}.forEach{require(layers.any{l->l.id==it.layer})}
    }
    companion object { fun canvas(w: Int,h: Int,color: Int=0): Document = Document(width=w,height=h).addLayer(Layer(name="Background",color=color)).also{it.validate()} }
}
val BLENDS=listOf("Normal","Multiply","Screen","Overlay","Soft Light","Darken","Lighten","Difference","Exclusion","Add")
fun safeAsset(s: String): Boolean = Regex("assets/[a-zA-Z0-9_.-]+").matches(s) && !s.contains("..")
fun validateEffect(e: Effect) {
    val def=Catalog.definition(e)
    require(e.params.all{(k,v)->def.params.any{p->p.id==k&&v.isFinite()&&v>=p.min&&v<=p.max&&(!p.integral||v==v.roundToInt().toFloat())}}) { "Invalid effect parameter" }
    require(e.mix.isFinite()&&e.mix in 0f..1f && e.palette.size in 2..32)
    require(e.curves.size==4 && e.curves.all{c->c.size in 2..32 && c.all{it.x.isFinite()&&it.y.isFinite()&&it.x in 0f..1f&&it.y in 0f..1f} && c.zipWithNext().all{it.first.x<it.second.x}})
    validateMask(e.mask)
}
fun validateMask(m: Mask?) { if(m!=null){require(m.base in 0f..1f&&m.low in 0f..1f&&m.high in 0f..1f&&m.tolerance in .001f..1f&&m.selection in listOf("none","luma","color"));validateStrokes(m.strokes)} }
fun validateStrokes(s: List<Stroke>) { require(s.size<=20000);s.forEach{v->require(v.points.size<=100000&&v.points.all{it.x.isFinite()&&it.y.isFinite()}&&v.size in .0001f..1f&&v.softness in 0f..1f&&v.opacity in 0f..1f)} }
object Codec {
    val json=Json { encodeDefaults=true; ignoreUnknownKeys=false; prettyPrint=false; allowSpecialFloatingPointValues=false }
    fun encode(d: Document): String { d.validate(); return json.encodeToString(d) }
    fun decode(s: String): Document = json.decodeFromString<Document>(s).also{it.validate()}
}
@Serializable data class Preset(val version: Int=1,val name: String,val effects: List<Effect>) {
    fun validate(){require(version==1&&name.isNotBlank()&&name.length<=200&&effects.size in 1..100);effects.forEach{validateEffect(it)}}
}
