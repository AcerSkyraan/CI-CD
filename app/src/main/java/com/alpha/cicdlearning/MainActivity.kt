package com.alpha.cicdlearning

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alpha.cicdlearning.KidsColoring.Basic.ColoringScreen
import com.alpha.cicdlearning.MemoryCardMatchGame.ui.MemoryGameScreen
import com.alpha.cicdlearning.ObjectDetection.ObjectDetectionScreen
import com.alpha.cicdlearning.SnakeGame.ui.SnakeGame
import com.alpha.cicdlearning.flappybird.FlappyBirdGame
import com.alpha.cicdlearning.objectLabeling.ImageLabelScreen
import com.alpha.cicdlearning.objectLabeling.LiveLabelScreen
import com.alpha.cicdlearning.objectLabeling.MlImageLabelingScreen
import com.alpha.cicdlearning.pong.PongGame
import com.alpha.cicdlearning.ui.theme.CICDLearningTheme


fun setSystemUIVisibility(hide: Boolean, mainActivity: MainActivity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val window = mainActivity.window.insetsController!!
        val windows = WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
        if (hide) window.hide(windows) else window.show(windows)
        // needed for hide, doesn't do anything in show
        window.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        val view = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        mainActivity.window.decorView.systemUiVisibility = if (hide) view else view.inv()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestCameraPermission()

        setContent {
            setSystemUIVisibility(true ,this)
            CICDLearningTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    PillDragAnimation()
//                    SimpleTwoBoxExample()
//                    SimpleTwoBoxExample()
//                    MixedLayoutExample()
//                    DemoScreen()
                   // DemoWaveScreen()
//                    LiquidWaveDemo()
//                    MediaPickerScreen(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(innerPadding)
//                    )
//                    VideoFeedScreen()

//                    ColoringScreen()

//                    ImageLabelScreen()
//                    MlImageLabelingScreen()



//                    LiveLabelScreen()
//                    ObjectDetectionScreen()

//                    SnakeGame()
                    // MemoryGameScreen()

                    //PongGame()
                    FlappyBirdGame()
                }


            }
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }
}

