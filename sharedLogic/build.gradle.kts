import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// 💡 1. 這一區 Plugins 是關鍵，必須放在檔案的最頂部！
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinx.serialization)
}

// 💡 2. Kotlin 區塊獨立在外面
kotlin {
    android {
        namespace = "org.golfcat.team.project.sharedLogic"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        sourceSets {
            commonMain.dependencies {
                implementation(libs.kotlinx.datetime)

                // 💡 1. 補回 Kotlin 協程 (Coroutines)
                implementation(libs.kotlinx.coroutines.core)

                // 💡 2. 補回 Supabase 相關模組
                implementation(libs.supabase.postgrest) // 資料庫查詢用
                implementation(libs.supabase.auth)      // 身份驗證用 (有些舊版可能叫 gotrue)

                // 💡 3. 如果你的 Supabase 還有用到其他模組 (例如 Ktor client 或 Serialization)，也要一併加回來
                implementation(libs.ktor.client.core)
            }
            // ...
        }
        }
    }
}