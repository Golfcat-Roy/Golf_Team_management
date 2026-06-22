package org.golfcat.team.project

import org.golfcat.team.project.models.Event

class LineMessagingService {
    // 暫時不引入 Ktor Client 屬性，確保編譯 100% 通過
    private val channelAccessToken = "f5e/TiPoSGFdTPnyTAczbVairsL314Zs0lggxmWb5ESHPJ9aMvyJtgGJ1USdObEzX1o4xhh746q5rIJdIuetRCseGVMkpzM2QHLUDpHoXqK4dstg/Uq0NDpr+4yPkn9lODepvnY1wnb/qGV6fBfrYQdB04t89/1O/w1cDnyilFU="

    suspend fun sendEventFlexMessage(to: String, event: Event): Boolean {
        // 暫時回傳 false，等網頁版部署成功後我們再回來補齊 Ktor 邏輯
        return false
    }
}
