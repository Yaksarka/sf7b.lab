package lab.sf7b

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lab.sf7b.core.*
import java.util.Locale
import kotlin.math.*

@Composable fun PanelTitle(title:String,subtitle:String?=null){Column(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=8.dp)){Text(title,fontSize=20.sp);if(subtitle!=null)Text(subtitle,fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
@Composable fun NumberControl(label:String,value:Float,min:Float,max:Float,onChange:(Float)->Unit,integer:Boolean=false,unit:String="",default:Float?=null,model:EditorModel){
    var dialog by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxWidth().padding(horizontal=16.dp)){
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f),fontSize=14.sp,maxLines=2,overflow=TextOverflow.Ellipsis);TextButton(onClick={dialog=true}){Text((if(integer)value.roundToInt().toString()else String.format(Locale.ROOT,"%.3f",value))+unit,fontSize=13.sp)};if(default!=null)TextButton(onClick={onChange(default)},Modifier.widthIn(min=48.dp)){Text("↺")}}
        Slider(value.coerceIn(min,max),{onChange(if(integer)it.roundToInt().toFloat()else it)},valueRange=min..max,modifier=Modifier.fillMaxWidth())
    }
    if(dialog){var text by remember{mutableStateOf(if(integer)value.roundToInt().toString()else value.toString())};val parsed=parseNumberInput(text,min,max,integer);val keyboard=LocalSoftwareKeyboardController.current
        fun apply(){parsed?.let{onChange(it);keyboard?.hide();dialog=false}}
        AlertDialog(onDismissRequest={dialog=false},title={Text(label)},text={Column{OutlinedTextField(text,{text=it},singleLine=true,label={Text("$min … $max $unit")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal,imeAction=ImeAction.Done),keyboardActions=KeyboardActions(onDone={apply()}),isError=parsed==null&&text.isNotBlank()&&text!="-");if(parsed==null&&text.isNotBlank()&&text!="-")Text(model.tr("Введите число в допустимом диапазоне","Enter a number within range"),color=MaterialTheme.colorScheme.error,fontSize=13.sp)}},confirmButton={TextButton(onClick={apply()},enabled=parsed!=null){Text("OK")}},dismissButton={TextButton(onClick={dialog=false}){Text(model.tr("Отмена","Cancel"))}})
    }
}
fun parseNumberInput(text:String,min:Float,max:Float,integer:Boolean=false):Float? =
    text.trim().replace(',','.').toFloatOrNull()?.takeIf{it.isFinite()&&it in min..max&&(!integer||it==round(it))}
fun parseColor(text:String):Int?=runCatching{val s=text.trim().removePrefix("#");require((s.length==6||s.length==8)&&s.all{it in '0'..'9'||it in 'a'..'f'||it in 'A'..'F'});val n=s.toLong(16);if(s.length==6)(n or 0xff000000L).toInt()else n.toInt()}.getOrNull()
@Composable fun ColorControl(label:String,color:Int,onChange:(Int)->Unit,model:EditorModel){
    var open by remember{mutableStateOf(false)}
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f),fontSize=14.sp);TextButton(onClick={open=true}){Box(Modifier.size(24.dp).background(Color(color),RoundedCornerShape(5.dp)).border(1.dp,MaterialTheme.colorScheme.outline,RoundedCornerShape(5.dp)));Spacer(Modifier.width(8.dp));Text(String.format("#%08X",color),fontSize=13.sp)}}
    if(open){var text by remember{mutableStateOf(String.format("%08X",color))};val parsed=parseColor(text)
        AlertDialog(onDismissRequest={open=false},title={Text(label)},text={Column(Modifier.verticalScroll(rememberScrollState())){OutlinedTextField(text,{text=it},label={Text("#RRGGBB / #AARRGGBB")},singleLine=true,isError=parsed==null)
            val selected=parsed?:color
            listOf("R" to 16,"G" to 8,"B" to 0,model.tr("Непрозрачность","Opacity") to 24).forEach{(channel,shift)->
                Row(verticalAlignment=Alignment.CenterVertically){Text(channel,Modifier.widthIn(min=28.dp),fontSize=12.sp);Slider(((selected ushr shift)and 255).toFloat(),{v->val mask=255 shl shift;val next=(selected and mask.inv()) or(v.roundToInt() shl shift);text=String.format("%08X",next)},valueRange=0f..255f,modifier=Modifier.weight(1f));Text(((selected ushr shift)and 255).toString(),fontSize=12.sp)}
            }
            Row(Modifier.horizontalScroll(rememberScrollState())){listOf(0x00000000,0xff000000.toInt(),0xffffffff.toInt(),0xffef4444.toInt(),0xff0a84ff.toInt(),0xfff4cc72.toInt(),0xff31c48d.toInt()).forEach{c->Box(Modifier.padding(4.dp).size(48.dp).background(Color(c),RoundedCornerShape(8.dp)).border(1.dp,MaterialTheme.colorScheme.outline,RoundedCornerShape(8.dp)).clickable{text=String.format("%08X",c)})}}}},confirmButton={TextButton(enabled=parsed!=null,onClick={onChange(parsed!!);open=false}){Text("OK")}},dismissButton={TextButton(onClick={open=false}){Text(model.tr("Отмена","Cancel"))}})
    }
}
@Composable fun ToggleRow(label:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f),fontSize=14.sp);Switch(checked,onChange)}}
@Composable fun ChoiceRow(label:String,value:String,choices:List<String>,onChange:(String)->Unit){var expanded by remember{mutableStateOf(false)};Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){Text(label,Modifier.weight(1f),fontSize=14.sp);Box{TextButton(onClick={expanded=true}){Text(value)};DropdownMenu(expanded,{expanded=false}){choices.forEach{choice->DropdownMenuItem(text={Text(choice)},onClick={onChange(choice);expanded=false})}}}}}
@Composable fun ApplyRow(model:EditorModel,onCancel:()->Unit,onApply:()->Unit,enabled:Boolean=true){Row(Modifier.fillMaxWidth().padding(16.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){OutlinedButton(onClick=onCancel,modifier=Modifier.weight(1f)){Text(model.tr("Отмена","Cancel"))};Button(onClick=onApply,enabled=enabled,modifier=Modifier.weight(1f)){Text(model.tr("Применить","Apply"))}}}
@Composable fun TextPrompt(title:String,initial:String,model:EditorModel,onDismiss:()->Unit,onApply:(String)->Unit){var value by remember(title,initial){mutableStateOf(initial)};AlertDialog(onDismissRequest=onDismiss,title={Text(title)},text={OutlinedTextField(value,{value=it},singleLine=true,isError=value.length>200)},confirmButton={TextButton(enabled=value.isNotBlank()&&value.length<=200,onClick={onApply(value.trim());onDismiss()}){Text("OK")}},dismissButton={TextButton(onClick=onDismiss){Text(model.tr("Отмена","Cancel"))}})}
