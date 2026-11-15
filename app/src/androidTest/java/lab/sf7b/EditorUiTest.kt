package lab.sf7b

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import lab.sf7b.core.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert.*
import java.io.File

class EditorUiTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private val model get()=ViewModelProvider(rule.activity)[EditorModel::class.java]
    private var existingIds=emptySet<String>()
    private var previousProject=""
    private var previousLanguage="ru"
    private var previousTheme="system"
    private var previousQuality=1024
    private var previousHome=true
    private var retained=false
    private val verificationIds=linkedSetOf<String>()
    @Before fun retainExistingWork() {
        rule.waitUntil(60000){!model.loading&&!model.working}
        rule.runOnIdle{existingIds=model.projectEntries.map{it.id}.toSet();previousProject=model.activeProjectId;previousLanguage=model.language;previousTheme=model.theme;previousQuality=model.quality;previousHome=model.home;retained=true}
    }
    @After fun removeOnlyVerificationProjectsAndRestorePreferences() {
        if(!retained)return
        rule.runOnIdle{model.cancelWork()};rule.waitUntil(60000){!model.working}
        runBlocking{model.flushSaves()}
        val owned=verificationIds.filter{it !in existingIds&&model.projectEntries.any{entry->entry.id==it}}
        if(previousProject.isNotEmpty()&&model.projectEntries.any{it.id==previousProject}){
            rule.runOnIdle{model.openLibraryProject(previousProject)}
            rule.waitUntil(60000){!model.working&&model.activeProjectId==previousProject}
        }
        for(id in owned){rule.runOnIdle{model.deleteProject(id)};rule.waitUntil(60000){!model.working&&model.projectEntries.none{it.id==id}}}
        rule.runOnIdle{model.settings(previousLanguage,previousTheme,previousQuality);model.home=previousHome}
        runBlocking{model.flushSaves()}
    }
    private fun ready(){rule.waitUntil(30000){!model.loading};var previous="";rule.runOnIdle{previous=model.activeProjectId;model.settings(lang="ru",mode="dark",preview=512);model.newCanvas(320,256,0)};rule.waitUntil(60000){!model.working&&model.activeProjectId.isNotEmpty()&&model.activeProjectId!=previous};rule.runOnIdle{verificationIds+=model.activeProjectId;val layer=Layer(kind="generator",name="Linear gradient",generator=Effect("linear_gradient"));model.commit(Document(name="UI verification",width=320,height=256).addLayer(layer));model.home=false};rule.waitUntil(60000){model.renderedDocument==model.document&&!model.rendering}}
    private fun screenshot(name:String){
        rule.waitForIdle()
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        // Semantics can be idle before the separate dialog window reaches SurfaceFlinger.
        android.os.SystemClock.sleep(350)
        val bmp=instrumentation.uiAutomation.takeScreenshot()
        val dir=File(rule.activity.filesDir,"test-results");dir.mkdirs()
        File(dir,"$name.png").outputStream().use{bmp.compress(Bitmap.CompressFormat.PNG,100,it)}
        bmp.recycle()
    }
    @Test fun appliesEffectFromSearchAndUndoRestoresGraph(){ready();val before=model.document!!.nodes.count{it.kind=="effect"};rule.onNodeWithText("Эффекты").performClick();rule.onNode(hasSetTextAction()).performTextInput("RGB split");rule.onNodeWithTag("effect:rgb_split").performClick();rule.onNodeWithContentDescription("Применить эффект").assertExists().performClick();rule.waitUntil(45000){!model.rendering&&model.renderedDocument==model.document};assertEquals(before+1,model.document!!.nodes.count{it.kind=="effect"});screenshot("editor-ru-dark");rule.onNodeWithContentDescription("Отменить последнее действие").performClick();rule.runOnIdle{assertEquals(before,model.document!!.nodes.count{it.kind=="effect"})}}
    @Test fun numericKeyboardAndCancelDoNotCommitADraftOrChangeUndo() {
        ready();val before=model.document!!;val undo=model.undoCount
        rule.onNodeWithText("Эффекты").performClick();rule.onNode(hasSetTextAction()).performTextInput("RGB split");rule.onNodeWithTag("effect:rgb_split").performClick()
        rule.onNodeWithText("0.040").performClick();rule.onNode(hasSetTextAction()).performClick().performTextClearance();rule.onNode(hasSetTextAction()).performTextInput("0,070")
        screenshot("numeric-keyboard-ru");rule.onNodeWithText("OK").performClick()
        rule.waitUntil(45000){model.renderedDocument!=before&&!model.rendering}
        rule.onNodeWithContentDescription("Отменить изменения").performClick();rule.waitUntil(45000){model.renderedDocument==before&&!model.rendering}
        rule.runOnIdle{assertEquals(before,model.document);assertEquals(undo,model.undoCount)}
    }
    @Test fun editsTextAndPersistsLayersAcrossActivityRecreation(){ready();rule.onNodeWithText("Текст").performClick();rule.onNode(hasSetTextAction()).performTextClearance();rule.onNode(hasSetTextAction()).performTextInput("Привет sf7b");rule.onNodeWithText("Применить",useUnmergedTree=true).performScrollTo().performClick();rule.waitUntil(45000){!model.rendering};assertTrue(model.document!!.layers.any{it.kind=="text"&&it.text.text=="Привет sf7b"});rule.activityRule.scenario.recreate();rule.waitUntil(45000){!model.loading};rule.runOnIdle{assertTrue(model.document!!.layers.any{it.kind=="text"&&it.text.text=="Привет sf7b"})}}
    @Test fun independentProjectCopyRetainsEarlyEffectAndEditableStateAcrossRecreation() {
        ready();var sourceId="";var ids=emptySet<String>();var sourceSeed=0L
        rule.runOnIdle{model.addEffect("random_pixelate");sourceId=model.activeProjectId;sourceSeed=model.document!!.nodes.first{it.kind=="effect"}.effect.seed;ids=model.projectEntries.map{it.id}.toSet()}
        runBlocking{model.flushSaves()}
        rule.runOnIdle{model.duplicateProject(sourceId)}
        rule.waitUntil(60000){!model.working&&model.projectEntries.any{it.id !in ids}}
        val copiedId=model.projectEntries.single{it.id !in ids}.id;verificationIds+=copiedId
        rule.runOnIdle{model.openLibraryProject(copiedId)}
        rule.waitUntil(60000){!model.working&&model.activeProjectId==copiedId}
        rule.runOnIdle{val d=model.document!!;model.commit(d.copy(nodes=d.nodes.map{if(it.kind=="effect")it.copy(effect=it.effect.copy(seed=876543))else it}))}
        runBlocking{model.flushSaves()}
        assertEquals(876543L,ProjectFiles(model.projectDirectory).load()!!.nodes.first{it.kind=="effect"}.effect.seed)
        rule.activityRule.scenario.recreate();rule.waitUntil(60000){!model.loading}
        rule.runOnIdle{assertEquals(copiedId,model.activeProjectId);assertEquals(876543L,model.document!!.nodes.first{it.kind=="effect"}.effect.seed);model.openLibraryProject(sourceId)}
        rule.waitUntil(60000){!model.working&&model.activeProjectId==sourceId}
        rule.runOnIdle{assertEquals(sourceSeed,model.document!!.nodes.first{it.kind=="effect"}.effect.seed)}
    }
    @Test fun changingAnImagePreservesItsGraphSeedOriginalResourceAndUndo() {
        ready();val directory=File(rule.activity.cacheDir,"source-ui-${uid()}").also{it.mkdirs()}
        try {
            fun image(name:String,width:Int,height:Int,color:Int):android.net.Uri{val file=File(directory,name);val bmp=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);bmp.eraseColor(color);file.outputStream().use{bmp.compress(Bitmap.CompressFormat.PNG,100,it)};bmp.recycle();return android.net.Uri.fromFile(file)}
            val first=image("first.png",48,32,0x804070a0.toInt());val second=image("second.png",96,64,0xff80c030.toInt())
            rule.runOnIdle{model.importImage(first,true)};rule.waitUntil(60000){!model.working&&model.document!!.layers.any{it.kind=="image"}}
            val id=model.document!!.layers.first{it.kind=="image"}.id
            rule.runOnIdle{model.selectLayer(id);model.addEffect("random_pixelate")}
            val before=model.document!!;val oldAsset=before.layer(id).asset
            rule.runOnIdle{model.replaceImage(second,id)};rule.waitUntil(60000){!model.working&&model.document!!.layer(id).asset!=oldAsset}
            val after=model.document!!;assertEquals(before.nodes,after.nodes);assertEquals(before.width,after.width);assertEquals(before.height,after.height)
            assertTrue(File(model.projectDirectory,oldAsset).exists());assertTrue(File(model.projectDirectory,after.layer(id).asset).exists())
            rule.runOnIdle{model.undo();assertEquals(oldAsset,model.document!!.layer(id).asset);model.redo();assertEquals(after.layer(id).asset,model.document!!.layer(id).asset)}
            runBlocking{model.flushSaves()};rule.activityRule.scenario.recreate();rule.waitUntil(60000){!model.loading}
            rule.runOnIdle{assertEquals(after,model.document)}
        }finally{directory.deleteRecursively()}
    }
    @Test fun fullResolutionExportKeepsAlphaAndTheEditableDocument() {
        ready();val directory=File(rule.activity.cacheDir,"export-ui-${uid()}").also{it.mkdirs()}
        try {
            val input=File(directory,"input.png");val bitmap=Bitmap.createBitmap(2000,2000,Bitmap.Config.ARGB_8888);bitmap.eraseColor(0x8070458f.toInt());input.outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
            rule.runOnIdle{model.importImage(android.net.Uri.fromFile(input),true)};rule.waitUntil(60000){!model.working&&model.document!!.layers.any{it.kind=="image"}}
            val layer=model.document!!.layers.first{it.kind=="image"}
            val doc=listOf(Effect("adjust",mapOf("brightness" to .08f,"contrast" to .15f,"saturation" to .2f)),Effect("invert"),Effect("rgb_split",mapOf("amount" to .01f)),Effect("curves"),Effect("posterize")).fold(Document(name="Export verification",width=2000,height=2000).addLayer(layer)){d,e->d.addEffect(layer.id,e)}
            assertTrue(GpuRenderer.canAccelerate(doc));rule.runOnIdle{model.commit(doc)};rule.waitUntil(60000){model.renderedDocument==doc&&!model.rendering}
            val output=File(directory,"full.png");val start=android.os.SystemClock.elapsedRealtime()
            rule.runOnIdle{model.export(android.net.Uri.fromFile(output),2000,2000,0)};rule.waitUntil(60000){!model.working&&output.length()>0}
            val elapsed=android.os.SystemClock.elapsedRealtime()-start
            assertEquals("GPU",model.lastExportBackend);val decoded=android.graphics.BitmapFactory.decodeFile(output.path);assertNotNull(decoded)
            assertEquals(2000,decoded.width);assertEquals(2000,decoded.height);assertEquals(128,decoded.getPixel(1000,1000) ushr 24);decoded.recycle()
            rule.runOnIdle{assertEquals(doc,model.document)}
            val reports=File(rule.activity.filesDir,"test-results").also{it.mkdirs()};File(reports,"model-full-export-performance.txt").writeText("backend=${model.lastExportBackend}\nsize=2000x2000\nalpha=128\nelapsed_ms=$elapsed\nModel export including queue wait, original load, rendering, PNG and file URI delivery; excludes system picker; one run, not median.\n")
        }finally{directory.deleteRecursively()}
    }
    @Test fun bothThemesAndLanguagesRenderEditorAndSettings(){ready();for(lang in listOf("ru","en"))for(theme in listOf("dark","light")){rule.runOnIdle{model.settings(lang,theme);model.home=false};rule.waitForIdle();screenshot("editor-$lang-$theme");rule.onNodeWithContentDescription(if(lang=="ru")"Меню проекта"else "Project menu").performClick();rule.onNodeWithText(if(lang=="ru")"Настройки"else "Settings").performClick();screenshot("settings-$lang-$theme");rule.onNodeWithText(if(lang=="ru")"Готово"else "Done").performScrollTo().performClick()}}
}
