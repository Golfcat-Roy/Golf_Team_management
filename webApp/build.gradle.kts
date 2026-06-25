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
                implementation(projects.sharedUI)

                // 💡 必須依賴這些 Compose Canvas 核心庫
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3) // 如果你是用 material2，這裡就改 material
                implementation(compose.ui)        // 👈 CanvasBasedWindow 就在這裡面！
                implementation(compose.components.resources)
            }
        }
    }
}