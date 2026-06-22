package org.golfcat.team.project

import org.golfcat.team.project.models.User

actual suspend fun loginWithLine(): User? {
    // Web 版本的 LINE 登入邏輯 (LIFF 或 OAuth)
    return null
}
