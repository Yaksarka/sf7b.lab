package lab.sf7b.core

import org.junit.Test
import org.junit.Assert.*
import java.io.*
import java.nio.file.Files
import java.util.zip.*
import javax.imageio.ImageIO
import kotlin.math.*

class EditorContractTest {
    private fun directory()=Files.createTempDirectory("sf7b-test").toFile()
    private fun fixture(w:Int=96,h:Int=80,alpha:Boolean=true):Document=Document(width=w,height=h).addLayer(Layer(name="Fixture",kind="image",asset="assets/test.image",mask=if(alpha)Mask(strokes=listOf(Stroke(listOf(Point(.2f,.1f),Point(.5f,.7f)),size=.25f,erase=true)))else null))
    private fun source(pool:SurfacePool):SourceProvider=object:SourceProvider{
        override fun image(asset:String):Surface {val s=pool.create(96,80);for(y in 0 until 80)s.writeRow(y,IntArray(96){x->val alpha=if(x<8)0f else if(x<24).45f else 1f;Pixels.rgba(x/95f,y/79f,if((x/13+y/11)%2==0).8f else .15f,alpha)});s.seal();return s}
        override fun text(layer:Layer,width:Int,height:Int)=error("Android text is tested on device")
    }
    private fun render(d:Document,w:Int=d.width,h:Int=d.height,tile:Int=32):IntArray {val dir=directory();try{return SurfacePool(dir,tile,memoryBytes=256*1024).use{pool->val surface=Renderer(pool,source(pool)).render(d,w,h);IntArray(w*h).also{all->for(y in 0 until h)surface.readRow(y).copyInto(all,y*w)}}}finally{dir.deleteRecursively()}}
    @Test fun unchangedSaveKeepsActualRecoveryBytesAndModificationMetadata() {
        val dir=directory()
        try {
            val library=ProjectLibrary(dir);val entry=library.create(Document.canvas(24,18),name="Original")
            val first=library.open(entry.id);val edited=first.document.copy(name="Edited")
            val committed=library.save(entry.id,edited)
            val current=File(first.directory,"document.json");val recovery=File(first.directory,"last-good.json")
            val metadata=File(first.directory,"project-info.json");val index=File(dir,"projects/library.json")
            val currentBytes=current.readBytes();val recoveryBytes=recovery.readBytes();val metadataBytes=metadata.readBytes();val indexBytes=index.readBytes()
            val modifiedTimes=listOf(current.lastModified(),recovery.lastModified(),metadata.lastModified(),index.lastModified())
            repeat(3){library.save(entry.id,edited);ProjectFiles(first.directory).save(edited)}
            assertArrayEquals(currentBytes,current.readBytes());assertArrayEquals(recoveryBytes,recovery.readBytes())
            assertArrayEquals(metadataBytes,metadata.readBytes());assertArrayEquals(indexBytes,index.readBytes())
            assertEquals(modifiedTimes,listOf(current.lastModified(),recovery.lastModified(),metadata.lastModified(),index.lastModified()))
            assertEquals(committed.updatedAt,library.list().single().updatedAt);assertEquals(committed.revision,library.list().single().revision)
            assertEquals(first.document,Codec.decode(recovery.readText()))
        }finally{dir.deleteRecursively()}
    }
    @Test fun saveOfRecoveredDocumentRepairsTheDamagedCurrentFileEvenWhenItsModelIsUnchanged() {
        val dir=directory()
        try {
            val library=ProjectLibrary(dir);val entry=library.create(Document.canvas(24,18),name="Safe")
            val original=library.open(entry.id);library.save(entry.id,original.document.copy(name="Later"))
            val current=File(original.directory,"document.json");current.writeText("{truncated")
            val recovered=library.open(entry.id)
            assertEquals(original.document,recovered.document)
            assertTrue(library.recoveryNotices.any{it.kind==RecoveryKind.DOCUMENT_BACKUP&&it.projectId==entry.id})
            library.save(entry.id,recovered.document)
            assertEquals(recovered.document,Codec.decode(current.readText()))
            assertArrayEquals(Codec.encode(recovered.document).toByteArray(Charsets.UTF_8),current.readBytes())
            val reopened=ProjectFiles(original.directory);assertEquals(recovered.document,reopened.load());assertFalse(reopened.recoveredFromBackup)
        }finally{dir.deleteRecursively()}
    }
    @Test fun projectLibraryKeepsMoreThanFifteenIndependentDocumentsAndResources() {
        val dir=directory();val input=directory()
        try {
            File(input,"assets").mkdirs();File(input,"assets/test.image").writeBytes(byteArrayOf(1,2,3))
            val lib=ProjectLibrary(dir)
            val base=fixture();val doc=base.addEffect(base.layers.first().id,Effect("random_pixelate",seed=87654321L))
            val original=lib.create(doc,input,name="Cover")
            val copy=lib.duplicate(original.id,"Independent cover")
            assertNotEquals(original.id,copy.id)
            assertEquals(lib.open(original.id).document.nodes,lib.open(copy.id).document.nodes)
            for(i in 1..16)lib.create(Document.canvas(20+i,32),name="Work $i")
            assertEquals(18,lib.list().size)
            input.deleteRecursively()
            val edited=lib.open(copy.id).document.copy(nodes=lib.open(copy.id).document.nodes.map{if(it.kind=="effect")it.copy(effect=it.effect.copy(seed=19))else it})
            lib.save(copy.id,edited)
            assertEquals(87654321L,lib.open(original.id).document.nodes.first{it.kind=="effect"}.effect.seed)
            lib.delete(original.id)
            val reopened=ProjectLibrary(dir)
            assertEquals(17,reopened.list().size)
            assertEquals(19L,reopened.open(copy.id).document.nodes.first{it.kind=="effect"}.effect.seed)
            assertArrayEquals(byteArrayOf(1,2,3),File(reopened.open(copy.id).directory,"assets/test.image").readBytes())
            assertThrows(IllegalStateException::class.java){reopened.save(original.id,doc)}
        } finally {dir.deleteRecursively();input.deleteRecursively()}
    }
    @Test fun projectLibraryMigratesLegacyWorkspaceWithoutDeletingItAndOnlyOnce() {
        val dir=directory()
        try {
            val workspace=File(dir,"workspace");val files=ProjectFiles(workspace)
            File(workspace,"assets/test.image").writeBytes(byteArrayOf(7,8,9));val doc=fixture();files.save(doc)
            val first=ProjectLibrary(dir);assertEquals(1,first.list().size)
            assertEquals(doc.copy(name="Recovered workspace"),first.open(first.list().single().id).document)
            assertEquals(doc,files.load());assertArrayEquals(byteArrayOf(7,8,9),File(workspace,"assets/test.image").readBytes())
            assertEquals(1,ProjectLibrary(dir).list().size)
        } finally {dir.deleteRecursively()}
    }
    @Test fun projectLibraryRecoversCorruptIndexAndKeepsNewestProjectMetadata() {
        val dir=directory()
        try {
            val lib=ProjectLibrary(dir);val a=lib.create(Document.canvas(32,48),name="A");val b=lib.create(Document.canvas(48,32),name="B")
            lib.rename(a.id,"A renamed");assertEquals(a.id,lib.list().first().id)
            File(dir,"projects/library.json").writeText("{broken")
            File(dir,"projects/library-last-good.json").writeText("{also broken")
            val recovered=ProjectLibrary(dir)
            assertEquals(setOf(a.id,b.id),recovered.list().map{it.id}.toSet())
            assertEquals("A renamed",recovered.open(a.id).document.name)
            assertEquals("A renamed",recovered.list().first{it.id==a.id}.name)
        } finally {dir.deleteRecursively()}
    }
    @Test fun projectLibraryRejectsStaleThumbnailsAndDeletedProjectWrites() {
        val dir=directory()
        try {
            val lib=ProjectLibrary(dir);val entry=lib.create(Document.canvas(16,16));val before=lib.open(entry.id).document
            val after=before.copy(name="New revision");lib.save(entry.id,after)
            assertFalse(lib.saveThumbnail(entry.id,byteArrayOf(1),before))
            assertNull(lib.list().single().thumbnail)
            assertTrue(lib.saveThumbnail(entry.id,byteArrayOf(2),after))
            assertArrayEquals(byteArrayOf(2),lib.list().single().thumbnail!!.readBytes())
            lib.delete(entry.id)
            assertThrows(IllegalStateException::class.java){lib.save(entry.id,after)}
            assertTrue(ProjectLibrary(dir).list().isEmpty())
        } finally {dir.deleteRecursively()}
    }
    @Test fun cancelledProjectImportDoesNotPublishOrDamageExistingProject() {
        val dir=directory()
        try {
            val lib=ProjectLibrary(dir);val original=lib.create(Document.canvas(32,32),name="Keep")
            val bytes=ByteArrayOutputStream();lib.exportProject(original.id,bytes)
            var calls=0
            assertThrows(InterruptedException::class.java){lib.importProject(bytes.toByteArray().inputStream()){if(++calls>1)throw InterruptedException()}}
            assertEquals(listOf(original.id),lib.list().map{it.id})
            assertEquals("Keep",lib.open(original.id).document.name)
            assertTrue(File(dir,"projects").listFiles().orEmpty().none{it.name.startsWith(".import-")||it.name.startsWith(".create-")})
        } finally {dir.deleteRecursively()}
    }
    @Test fun projectPackageIntegrityRejectsChangedAssetAndAcceptsLegacyPackage() {
        val dir=directory();val target=directory();val legacyTarget=directory()
        try {
            val store=ProjectFiles(dir);val doc=fixture();File(dir,"assets/test.image").writeBytes(byteArrayOf(1,2,3))
            val bytes=ByteArrayOutputStream();store.pack(doc,bytes)
            val members=linkedMapOf<String,ByteArray>()
            ZipInputStream(bytes.toByteArray().inputStream()).use{zip->while(true){val e=zip.nextEntry?:break;members[e.name]=zip.readBytes();zip.closeEntry()}}
            fun zipMembers(data:Map<String,ByteArray>):ByteArray=ByteArrayOutputStream().also{output->ZipOutputStream(output).use{zip->data.forEach{(name,content)->zip.putNextEntry(ZipEntry(name));zip.write(content);zip.closeEntry()}}}.toByteArray()
            members["assets/test.image"]=byteArrayOf(3,2,1)
            assertThrows(IllegalArgumentException::class.java){ProjectFiles(target).unpack(zipMembers(members).inputStream())}
            assertNull(ProjectFiles(target).load())
            members.remove("package.json")
            assertEquals(doc,ProjectFiles(legacyTarget).unpack(zipMembers(members).inputStream()))
        } finally {dir.deleteRecursively();target.deleteRecursively();legacyTarget.deleteRecursively()}
    }
    @Test fun importCannotOverwriteNonemptyDestinationAndPortableCopiesHaveNewIds() {
        val dir=directory();val destination=directory()
        try {
            val lib=ProjectLibrary(dir);val entry=lib.create(Document.canvas(16,16),name="Same name")
            val bytes=ByteArrayOutputStream();lib.exportProject(entry.id,bytes)
            val imported=lib.importProject(bytes.toByteArray().inputStream())
            assertNotEquals(entry.id,imported.entry.id);assertEquals("Same name",imported.document.name)
            val sentinel=File(destination,"keep.txt").apply{writeText("Keep")}
            assertThrows(IllegalArgumentException::class.java){ProjectFiles(destination).unpack(bytes.toByteArray().inputStream())}
            assertEquals("Keep",sentinel.readText())
        } finally {dir.deleteRecursively();destination.deleteRecursively()}
    }
    @Test fun legacyEffectDefaultsHaveVersionOneAndFutureAlgorithmsAreRejected() {
        val base=fixture();val doc=base.addEffect(base.layers.first().id,Effect("bloom",seed=89767))
        val oldJson=Codec.encode(doc).replace(",\"algorithmVersion\":1","")
        assertEquals(doc,Codec.decode(oldJson))
        val n=doc.nodes.first{it.kind=="effect"}
        assertThrows(IllegalArgumentException::class.java){doc.copy(nodes=doc.nodes.map{if(it.id==n.id)it.copy(effect=it.effect.copy(algorithmVersion=999))else it}).validate()}
    }
    @Test fun damagedCurrentManifestCannotOverwriteTheLastGoodRecoveryFile() {
        val dir=directory()
        try {
            val store=ProjectFiles(dir);val first=Document.canvas(16,16).copy(name="First")
            store.save(first);store.save(first.copy(name="Second"));File(dir,"document.json").writeText("{broken")
            store.save(first.copy(name="Third"));File(dir,"document.json").writeText("{broken again")
            assertEquals(first,store.load())
        } finally {dir.deleteRecursively()}
    }
    @Test fun projectRoundTripKeepsGraphResourcesMasksAndEarlyParameters(){
        val dir=directory();val imported=directory();try{val files=ProjectFiles(dir);File(dir,"assets/test.image").writeBytes(byteArrayOf(1,2,3));File(dir,"assets/test.ttf").writeBytes(byteArrayOf(9,8,7));var d=fixture();val layer=d.layers[0].id;for(e in listOf("patchwork_break","low_freq_banding","multi_distort","random_pixelate","bloom"))d=d.addEffect(layer,Effect(e,seed=723451))
            d=d.addLayer(Layer(kind="text",text=TextData(text="Кириллица / Latin",font="assets/test.ttf"),transform=Transform(angle=27f),mask=Mask(inverted=true)))
            d=d.addLayer(Layer(kind="drawing",strokes=listOf(Stroke(listOf(Point(.1f,.2f),Point(.8f,.6f)),color=0x80ff0033.toInt()))))
            val archive=ByteArrayOutputStream();files.pack(d,archive);val reopened=ProjectFiles(imported).unpack(ByteArrayInputStream(archive.toByteArray()))
            assertEquals(d,reopened);assertArrayEquals(byteArrayOf(9,8,7),File(imported,"assets/test.ttf").readBytes())
            val first=reopened.nodes.first{it.kind=="effect"};val edited=reopened.copy(nodes=reopened.nodes.map{if(it.id==first.id)it.copy(effect=it.effect.copy(mix=.1f))else it});assertNotEquals(Codec.encode(reopened),Codec.encode(edited));assertEquals(5,edited.nodes.count{it.kind=="effect"})
        }finally{dir.deleteRecursively();imported.deleteRecursively()}
    }
    @Test fun lastGoodRecoversAfterTruncatedManifest(){val dir=directory();try{val store=ProjectFiles(dir);val d=fixture();store.save(d);store.save(d.copy(name="second"));File(dir,"document.json").writeText("{broken");assertEquals(d,store.load())}finally{dir.deleteRecursively()}}
    @Test fun cyclesAndUnsafeResourcesAreRejected(){val base=fixture();val d=base.addEffect(base.layers[0].id,Effect("invert"));val n=d.nodes.first{it.kind=="effect"};assertThrows(IllegalArgumentException::class.java){d.copy(nodes=d.nodes.map{if(it.id==n.id)it.copy(inputs=listOf(n.id))else it}).validate()};assertThrows(IllegalArgumentException::class.java){base.copy(layers=base.layers.map{it.copy(asset="../outside")}).validate()}}
    @Test fun seedAndTilesAreDeterministicForAllEffects(){val d=fixture();for(def in Catalog.effects){val doc=d.addEffect(d.layers[0].id,Effect(def.id,seed=86753));val a=render(doc,tile=16);val b=render(Codec.decode(Codec.encode(doc)),tile=64);assertArrayEquals("Tile or seed mismatch: ${def.id}",a,b);for(c in a){assertTrue(Pixels.r(c)<=Pixels.a(c));assertTrue(Pixels.g(c)<=Pixels.a(c));assertTrue(Pixels.b(c)<=Pixels.a(c))}}}
    @Test fun allCatalogueEntriesActuallyProcessPixels(){val d=fixture();val baseline=render(d);for(def in Catalog.effects){var e=Effect(def.id);if(def.id=="curves")e=e.copy(curves=listOf(listOf(Point(0f,.1f),Point(.5f,.9f),Point(1f,1f)))+e.curves.drop(1));if(def.id=="adjust")e=e.copy(params=mapOf("brightness" to .2f));if(def.id=="channel_multiplier")e=e.copy(params=mapOf("red" to 0f));if(def.id=="color_swap")e=e.copy(params=mapOf("tolerance" to 1f,"hardness" to 1f));if(def.id=="lum_select")e=e.copy(params=mapOf("target" to 1f,"tolerance" to .001f,"hardness" to 1f));val output=render(d.addEffect(d.layers[0].id,e));assertFalse("No visible operation: ${def.id}",baseline.contentEquals(output))}}
    @Test fun disablingEffectKeepsSettingsAndRestoresInput(){val d=fixture();val e=Effect("patchwork_break",seed=42,params=mapOf("amount" to .7f),enabled=false);val doc=d.addEffect(d.layers[0].id,e);assertArrayEquals(render(d),render(doc));assertEquals(42L,doc.nodes.first{it.kind=="effect"}.effect.seed)}
    @Test fun premultipliedInterpolationAvoidsBlackHalos(){val dir=directory();try{SurfacePool(dir,16).use{p->val s=p.create(2,1);s.writeRow(0,intArrayOf(Pixels.premul(0xffff0000.toInt()),Pixels.premul(0x000000ff)));s.seal();val edge=s.sample(.5f,0f);val c=Pixels.straight(edge);assertEquals(255,Pixels.r(c));assertEquals(0,Pixels.g(c));assertEquals(0,Pixels.b(c));assertTrue(Pixels.a(c) in 127..128)}}finally{dir.deleteRecursively()}}
    @Test fun blendsUseCorrectSourceOverAlpha(){for(mode in BLENDS){val c=Pixels.over(Pixels.premul(0x8000ff00.toInt()),Pixels.premul(0x80ff0000.toInt()),mode);assertTrue(Pixels.a(c) in 191..192)};assertEquals(0,Pixels.over(0,0,"Screen"));assertEquals(0xffff0000.toInt(),Pixels.over(0,Pixels.premul(0xffff0000.toInt()),"Multiply"))}
    @Test fun streamingPngHasExactCropDimensionsAndAlpha(){val d=fixture().copy(frame=Frame(.25f,.1f,.5f,.8f,turn=1));val dir=directory();try{SurfacePool(dir,32).use{p->val s=Renderer(p,source(p)).render(d,96,80);val bytes=ByteArrayOutputStream();PngWriter.write(bytes,39,31,{y->outputRow(d,s,y,39,31)});val decoded=ImageIO.read(ByteArrayInputStream(bytes.toByteArray()));assertEquals(39,decoded.width);assertEquals(31,decoded.height);assertTrue(decoded.colorModel.hasAlpha());assertTrue((0 until 31).any{y->(0 until 39).any{x->(decoded.getRGB(x,y) ushr 24)<250}})}}finally{dir.deleteRecursively()}}
    @Test fun packageRejectsTraversalAndDoesNotReplaceExistingProject(){val dir=directory();try{val bytes=ByteArrayOutputStream();ZipOutputStream(bytes).use{it.putNextEntry(ZipEntry("../outside"));it.write(1);it.closeEntry()};assertThrows(IllegalArgumentException::class.java){ProjectFiles(dir).unpack(ByteArrayInputStream(bytes.toByteArray()))};assertFalse(File(dir.parentFile,"outside").exists())}finally{dir.deleteRecursively()}}
    @Test fun graphBranchRemainsEditable(){val d=fixture();val layer=d.layers[0].id;val a=d.addEffect(layer,Effect("rgb_split"));val b=a.addEffect(layer,Effect("waves"),d.layer(layer).head);val branch=b.branch(layer,a.layer(layer).head,b.layer(layer).head,"Screen");val result=render(branch);val early=a.layer(layer).head;val changed=branch.copy(nodes=branch.nodes.map{if(it.id==early)it.copy(effect=it.effect.copy(params=mapOf("amount" to .2f)))else it});assertFalse(result.contentEquals(render(changed)))}
    @Test fun randomVariantChangesOnlyWithSeed(){val d=fixture();val a=d.addEffect(d.layers[0].id,Effect("random_pixelate",seed=2));val b=d.addEffect(d.layers[0].id,Effect("random_pixelate",seed=3));assertFalse(render(a).contentEquals(render(b)))}
    @Test fun previewAndExportUseSamePatternCoordinates(){val layer=Layer(kind="generator",generator=Effect("checker",mapOf("size" to .25f)));val d=Document(width=256,height=256).addLayer(layer).addEffect(layer.id,Effect("patchwork_break",mapOf("size" to .25f),seed=42));val a=render(d,256,256);val b=render(d,128,128);var different=0;var total=0;for(y in 4 until 128 step 8)for(x in 4 until 128 step 8){total++;if(a[(y*2)*256+x*2]!=b[y*128+x])different++};assertTrue("Composition mismatch $different/$total",different<total/5)}
    @Test fun cancelledPngNeverReportsCompletion(){val bytes=ByteArrayOutputStream();var checks=0;var finished=false;assertThrows(InterruptedException::class.java){PngWriter.write(bytes,30,30,{IntArray(30)},check={if(++checks>10)throw InterruptedException()},progress={if(it==1f)finished=true})};assertFalse(finished)}
    @Test fun graphMaskAndDisplacementInputsSurviveRestoreAndRejectCycles(){val base=fixture();val layer=base.layers[0].id;val source=base.layers[0].head;val mask=Node(kind="mask",inputs=listOf(source),layer=layer,maskData=Mask(selection="luma",low=.5f,high=1f));val masked=base.copy(nodes=base.nodes+mask).addEffect(layer,Effect("invert"));val effect=masked.nodes.first{it.kind=="effect"};val d=masked.copy(nodes=masked.nodes.map{if(it.id==effect.id)it.copy(maskInput=mask.id)else it});d.validate();assertArrayEquals(render(d),render(Codec.decode(Codec.encode(d))));assertFalse(render(d).contentEquals(render(base)));val cyclic=d.copy(nodes=d.nodes.map{if(it.id==mask.id)it.copy(inputs=listOf(effect.id))else it});assertThrows(IllegalArgumentException::class.java){cyclic.validate()};val disp=base.addEffect(layer,Effect("displacement"));val withMap=disp.copy(nodes=disp.nodes.map{if(it.kind=="effect")it.copy(inputs=listOf(source,source))else it});withMap.validate();assertFalse(render(disp).contentEquals(render(withMap)))}
    @Test fun layerMaskIsAppliedAfterGeneratorsAndBloom(){val base=fixture(alpha=false);val layer=base.layers[0].id;val d=base.addEffect(layer,Effect("linear_gradient")).addEffect(layer,Effect("bloom"));val hidden=d.withLayer(d.layer(layer).copy(mask=Mask(base=0f)));assertTrue(render(hidden).all{it==0});val partial=d.withLayer(d.layer(layer).copy(mask=Mask(base=.5f)));assertTrue(render(partial).all{Pixels.a(it)in 127..128});assertFalse(render(d).all{Pixels.a(it)<200})}
    @Test fun oneExposureStopDoublesLinearLight(){val layer=Layer(kind="solid",color=0xff767676.toInt());val d=Document(width=8,height=8).addLayer(layer).addEffect(layer.id,Effect("adjust",mapOf("exposure" to 1f)));assertTrue(render(d).all{abs(Pixels.r(it)-162)<=1});assertEquals(1f,Effect("adjust").exposed(1f),0f)}
    @Test fun fineRandomPixelateKeepsDocumentScaleInPreview(){val layer=Layer(kind="generator",generator=Effect("linear_gradient",palette=List(32){if(it%2==0)0xff000000.toInt()else -1}));val d=Document(width=2048,height=32).addLayer(layer).addEffect(layer.id,Effect("random_pixelate",mapOf("size" to .01f,"variation" to 1f)));val full=render(d);val preview=render(d,256,4);var error=0L;for(y in 0 until 4)for(x in 0 until 256){val xx=x*8+3;val yy=y*8+3;val expected=Pixels.mix(Pixels.mix(full[yy*2048+xx],full[yy*2048+xx+1],.5f),Pixels.mix(full[(yy+1)*2048+xx],full[(yy+1)*2048+xx+1],.5f),.5f);error+=abs(Pixels.r(expected)-Pixels.r(preview[y*256+x]))};assertTrue("Preview pattern error ${error/1024f}",error/1024f<5f)}
}
