package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(navController = navController, startDestination = "screenA") {
            composable("screenA") {
                ScreenA(onUriSelected = { uri ->
                    val encodedUri = Uri.encode(uri.toString()) // IMPORTANT
                    navController.navigate("screenB?imageUri=$encodedUri")
                })
            }
            composable("screenB?imageUri={imageUri}") {
                val uri = it.arguments?.let {
                    Uri.parse(it.getString("imageUri"))
                }
                ScreenB(uri)
            }
        }

    }
}

@Composable
fun ScreenA(onUriSelected: (Uri?) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if(activityResult.resultCode == Activity.RESULT_OK) {
            val uri = activityResult.data?.data!!
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            onUriSelected(uri)
        }
    }
    LaunchedEffect(launcher) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        coroutineScope.launch {
            launcher.launch(intent)
        }
    }
}

@Composable
fun ScreenB(fileUri: Uri?) {
    Box(Modifier.padding(16.dp)) {
        fileUri?.let {
            GlideImage(
                imageModel = fileUri,
                contentScale = ContentScale.Crop,
            )
        }
    }
}