package lab.sf7b

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lab.sf7b.core.ProjectEntry

@Composable fun ProjectGallery(model:EditorModel,actions:UiActions,onNew:()->Unit,onSettings:()->Unit){
    var all by rememberSaveable{mutableStateOf(false)}
    var rename by remember{mutableStateOf<ProjectEntry?>(null)}
    var deleting by remember{mutableStateOf<ProjectEntry?>(null)}
    val entries=model.projectEntries
    Column(Modifier.fillMaxSize().padding(horizontal=12.dp)){
        Row(Modifier.fillMaxWidth().heightIn(min=56.dp),verticalAlignment=Alignment.CenterVertically){
            Text(if(all)model.tr("Все проекты","All projects")else "sf7b.lab",Modifier.weight(1f),fontSize=22.sp)
            if(all)TextButton(onClick={all=false}){Text(model.tr("Недавние","Recent"))}
            IconButton(onClick=onSettings){Icon(Icons.Default.Settings,model.tr("Настройки","Settings"))}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            Button(onClick={actions.openImage(false)},Modifier.weight(1f)){Text(model.tr("Изображение","Image"),maxLines=1)}
            OutlinedButton(onClick=onNew,Modifier.weight(1f)){Text(model.tr("Полотно","Canvas"),maxLines=1)}
        }
        TextButton(onClick=actions.openProject,modifier=Modifier.fillMaxWidth()){Text(model.tr("Импортировать проект .sf7blab","Import .sf7blab project"))}
        if(entries.isEmpty())Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){Text(model.tr("Создайте первую работу. Все проекты сохраняются отдельно.","Create your first work. Each project is saved separately."),Modifier.padding(24.dp))}
        else LazyVerticalGrid(columns=GridCells.Fixed(4),modifier=Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=8.dp)){
            items(if(all)entries else entries.take(15),key={it.id}){entry->
                var menu by remember{mutableStateOf(false)}
                val thumbnail by produceState<Bitmap?>(null,entry.id,entry.revision,entry.updatedAt){value=withContext(Dispatchers.IO){entry.thumbnail?.let{file->
                    try{val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true};BitmapFactory.decodeFile(file.path,bounds)
                        if(bounds.outWidth<=0||bounds.outHeight<=0)null else{var sample=1;while(maxOf(bounds.outWidth,bounds.outHeight)/sample>256)sample*=2;BitmapFactory.decodeFile(file.path,BitmapFactory.Options().apply{inSampleSize=sample;inPreferredConfig=Bitmap.Config.ARGB_8888})}
                    }catch(_:Exception){null}catch(_:OutOfMemoryError){null}
                }}}
                Column{
                    Box(Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceContainer).clickable(enabled=!model.working){model.openLibraryProject(entry.id)}){
                        thumbnail?.let{Image(it.asImageBitmap(),entry.name,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)}
                        if(thumbnail==null)Text("${entry.width}×${entry.height}",Modifier.align(Alignment.Center).padding(3.dp),fontSize=10.sp)
                        Box(Modifier.align(Alignment.TopEnd)){
                            IconButton(onClick={menu=true},modifier=Modifier.size(48.dp)){Icon(Icons.Default.MoreVert,model.tr("Меню проекта","Project menu"))}
                            DropdownMenu(menu,{menu=false}){
                                DropdownMenuItem(text={Text(model.tr("Переименовать","Rename"))},onClick={menu=false;rename=entry})
                                DropdownMenuItem(text={Text(model.tr("Создать копию","Duplicate"))},onClick={menu=false;model.duplicateProject(entry.id)})
                                DropdownMenuItem(text={Text(model.tr("Экспортировать проект","Export project"))},onClick={menu=false;actions.exportLibraryProject(entry.id)})
                                DropdownMenuItem(text={Text(model.tr("Удалить","Delete"))},onClick={menu=false;deleting=entry})
                            }
                        }
                    }
                    Text(entry.name,Modifier.fillMaxWidth().padding(top=4.dp),fontSize=12.sp,maxLines=2,overflow=TextOverflow.Ellipsis)
                }
            }
            if(!all&&entries.size>=15)item("all-projects"){OutlinedButton(onClick={all=true},modifier=Modifier.fillMaxWidth().aspectRatio(1f),contentPadding=PaddingValues(4.dp)){Text(model.tr("Все проекты","All projects"),fontSize=12.sp)}}
        }
    }
    rename?.let{entry->TextPrompt(model.tr("Имя проекта","Project name"),entry.name,model,{rename=null}){model.renameProject(entry.id,it)}}
    deleting?.let{entry->AlertDialog(onDismissRequest={deleting=null},title={Text(model.tr("Удалить проект?","Delete project?"))},text={Text(entry.name)},confirmButton={TextButton(onClick={deleting=null;model.deleteProject(entry.id)}){Text(model.tr("Удалить","Delete"))}},dismissButton={TextButton(onClick={deleting=null}){Text(model.tr("Отмена","Cancel"))}})}
}
