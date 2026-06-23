plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}

allprojects {
    configurations.all {
        // 使用 0.6.0 版，這在 Kotlin 2.x 中通常比 0.6.1 更穩定
        resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    }
}
