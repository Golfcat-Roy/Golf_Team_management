package org.golfcat.team.project

import kotlin.js.Promise

@JsName("liff")
external object liff {
    fun init(config: LiffConfig): Promise<Unit>
    fun login(config: LoginConfig = definedExternally)
    fun logout()
    fun isLoggedIn(): Boolean
    fun getIDToken(): String?
    fun getProfile(): Promise<LiffProfile>
    fun closeWindow()
    fun sendMessages(messages: Array<LiffMessage>): Promise<Unit>
    val os: String
    val language: String
    val version: String
    val lineVersion: String?
    val type: String?
    fun isInClient(): Boolean
}

external interface LiffConfig {
    var liffId: String
}

external interface LoginConfig {
    var redirectUri: String?
}

external interface LiffProfile {
    val userId: String
    val displayName: String
    val pictureUrl: String?
    val statusMessage: String?
}

external interface LiffMessage {
    var type: String
    var text: String?
}
