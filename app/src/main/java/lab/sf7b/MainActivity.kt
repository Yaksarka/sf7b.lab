package lab.sf7b
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
class MainActivity:ComponentActivity(){
    private lateinit var editorModel:EditorModel
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);enableEdgeToEdge();editorModel=ViewModelProvider(this)[EditorModel::class.java];setContent{EditorApp(editorModel)}}
    override fun onStop(){if(::editorModel.isInitialized)editorModel.checkpoint();super.onStop()}
}
