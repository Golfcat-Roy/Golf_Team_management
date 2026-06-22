package org.golfcat.team.project

import android.app.Activity
import android.content.Intent
import com.linecorp.linesdk.auth.LineLoginApi
import com.linecorp.linesdk.auth.LineLoginResult
import org.golfcat.team.project.models.User
import kotlinx.coroutines.CompletableDeferred

object LineLoginHelper {
    private const val CHANNEL_ID = "2010382913"
    private const val REQUEST_CODE = 1001

    private var loginDeferred: CompletableDeferred<IdTokenResult?>? = null

    suspend fun login(activity: Activity): IdTokenResult? {
        val deferred = CompletableDeferred<IdTokenResult?>()
        loginDeferred = deferred

        val loginIntent = LineLoginApi.getLoginIntent(
            activity,
            CHANNEL_ID,
            com.linecorp.linesdk.auth.LineAuthenticationParams.Builder()
                .scopes(listOf(com.linecorp.linesdk.Scope.PROFILE, com.linecorp.linesdk.Scope.OPENID_CONNECT))
                .build()
        )
        activity.startActivityForResult(loginIntent, REQUEST_CODE)

        return deferred.await()
    }

    data class IdTokenResult(val user: User, val idToken: String?)

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            val result: LineLoginResult = LineLoginApi.getLoginResultFromIntent(data)
            if (result.isSuccess) {
                val profile = result.lineProfile
                val idToken = result.lineIdToken?.rawString
                val user = User(
                    lineUid = profile?.userId ?: "",
                    lineDisplayName = profile?.displayName,
                    realName = "User" // 預設為 User，觸發 App 進入綁定流程
                )
                loginDeferred?.complete(IdTokenResult(user, idToken))
            } else {
                loginDeferred?.complete(null)
            }
            loginDeferred = null
        }
    }
}
