package io.github.linxiks.pindex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.linxiks.pindex.core.theme.PindexTheme
import io.github.linxiks.pindex.ui.PindexNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PindexTheme { PindexNavHost() } }
    }
}
