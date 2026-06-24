import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    // 💡 極簡版的 Wasm 設定，移除會報錯的舊語法
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                // 匯入你的 sharedUI 模組
                implementation(projects.sharedUI)

                // 💡 關鍵修正：這裡必須使用 compose. 開頭，不能用 libs.compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui) // 👈 CanvasBasedWindow 就藏在這裡面！
                implementation(compose.components.resources)
            }
        }
    }
}