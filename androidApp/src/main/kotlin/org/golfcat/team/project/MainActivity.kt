package org.golfcat.team.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 設定 Android 端的登入啟動器
        ActivityProvider.currentActivity = this
        
        enableEdgeToEdge()

        setContent {
            App()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LineLoginHelper.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityProvider.currentActivity == this) {
            ActivityProvider.currentActivity = null
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}