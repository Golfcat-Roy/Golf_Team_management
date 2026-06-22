package org.golfcat.team.project

import org.golfcat.team.project.models.User

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.IDToken

actual suspend fun loginWithLine(): User? {
    val activity = ActivityProvider.currentActivity ?: return null
    val result = LineLoginHelper.login(activity) ?: return null
    
    println("LINE 登入成功，正在嘗試 Supabase Auth... ID Token 是否為空: ${result.idToken == null}")

    // 使用 LINE 的 ID Token 登入 Supabase Auth
    result.idToken?.let { token ->
        try {
            supabase.auth.signInWith(IDToken) {
                idToken = token
            }
            println("Supabase Auth 登入成功！UID: ${supabase.auth.currentUserOrNull()?.id}")
        } catch (e: Exception) {
            println("Supabase Auth 登入失敗: ${e.message}")
            e.printStackTrace()
        }
    }
    
    return result.user
}
