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
        // 強制使用 0.4.1，這是目前 KMP Web 最穩定的日期函式庫版本
        resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    }
}
