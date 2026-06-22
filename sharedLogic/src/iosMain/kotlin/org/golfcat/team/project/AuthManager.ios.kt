package org.golfcat.team.project

import org.golfcat.team.project.models.User

actual suspend fun loginWithLine(): User? {
    // iOS 版本的 LINE 登入邏輯
    return null
}
