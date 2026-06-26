package org.golfcat.team.project

import org.golfcat.team.project.models.User

// 💡 暫時移除所有 JS 互操作，測試是否為編譯器 Bug
/*
external interface LiffProfile : JsAny {
    val userId: JsString
    val displayName: JsString
}
*/

actual suspend fun loginWithLine(): User? {
    return User(
        id = "mock-wasm-user",
        lineUid = "WASM123",
        lineDisplayName = "Wasm 測試員",
        realName = "Wasm 測試員",
        initialHandicap = 18.0,
        isSuperAdmin = true,
        createdAt = null
    )
}

// 💡 暫時 Mock 掉初始化
suspend fun initializeLiff(liffId: String) {
    println("initializeLiff Mocked: $liffId")
}
