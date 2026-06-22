package org.golfcat.team.project

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import org.golfcat.team.project.models.Event

class LineMessagingService {
    // 暫時不初始化 HttpClient 以確保編譯能過
    private val channelAccessToken = "f5e/TiPoSGFdTPnyTAczbVairsL314Zs0lggxmWb5ESHPJ9aMvyJtgGJ1USdObEzX1o4xhh746q5rIJdIuetRCseGVMkpzM2QHLUDpHoXqK4dstg/Uq0NDpr+4yPkn9lODepvnY1wnb/qGV6fBfrYQdB04t89/1O/w1cDnyilFU="

    suspend fun sendEventFlexMessage(to: String, event: Event): Boolean {
        // 先回傳 false，確保主程式能編譯成功部署到 GitHub Pages
        return false
    }
}
