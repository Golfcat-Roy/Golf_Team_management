pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT) // 💡 允許 Kotlin 插件添加 Node.js 倉庫
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Golf_Team_management"
include(":webApp")
