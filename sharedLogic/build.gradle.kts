import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    android {
        namespace = "org.golfcat.team.project.sharedLogic"
        compileSdk = 37
        minSdk = 24
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib")) // 💡 強制引入標準庫，解決 Symbol for Any not found
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.ktor.client.core)
        }

        androidMain.dependencies {
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.js)
                implementation(libs.wrappers.browser)
            }
        }
    }
}
