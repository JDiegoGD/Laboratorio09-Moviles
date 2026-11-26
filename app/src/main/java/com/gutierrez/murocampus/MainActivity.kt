package com.gutierrez.murocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.gutierrez.murocampus.data.PostRepositoryRemote
import com.gutierrez.murocampus.data.remote.RetrofitClient
import com.gutierrez.murocampus.ui.BarraMuroCampus
import com.gutierrez.murocampus.ui.PantallaLista
import com.gutierrez.murocampus.ui.theme.MuroCampusTheme
import com.gutierrez.murocampus.viewmodel.PostsViewModel
import com.gutierrez.murocampus.viewmodel.PostsViewModelFactory

class MainActivity : ComponentActivity() {

    private val vm: PostsViewModel by viewModels {
        PostsViewModelFactory(PostRepositoryRemote(RetrofitClient.api))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuroCampusTheme {
                Scaffold(topBar = { BarraMuroCampus() }) { padding ->
                    PantallaLista(vm, onAbrir = {}, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}
