package org.golfcat.team.project

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import org.golfcat.team.project.models.Event

class LineMessagingService(private val httpClient: HttpClient = supabase.httpClient) {
    private val channelAccessToken = "f5e/TiPoSGFdTPnyTAczbVairsL314Zs0lggxmWb5ESHPJ9aMvyJtgGJ1USdObEzX1o4xhh746q5rIJdIuetRCseGVMkpzM2QHLUDpHoXqK4dstg/Uq0NDpr+4yPkn9lODepvnY1wnb/qGV6fBfrYQdB04t89/1O/w1cDnyilFU="
    private val liffUrl = "https://liff.line.me/2010382913-rCaKoQcE"

    suspend fun sendEventFlexMessage(to: String, event: Event): Boolean {
        val flexMessage = buildEventFlexJson(event)
        
        return try {
            val response: HttpResponse = httpClient.post("https://api.line.me/v2/bot/message/push") {
                header(HttpHeaders.Authorization, "Bearer $channelAccessToken")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("to", to)
                    putJsonArray("messages") {
                        add(buildJsonObject {
                            put("type", "flex")
                            put("altText", "【新球賽通知】${event.title}")
                            put("contents", flexMessage)
                        })
                    }
                })
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            println("Line Messaging Error: $e")
            false
        }
    }

    private fun buildEventFlexJson(event: Event): JsonObject {
        // 這是一個簡化版的 Flex Message JSON 結構
        return buildJsonObject {
            put("type", "bubble")
            putJsonObject("body") {
                put("type", "box")
                put("layout", "vertical")
                putJsonArray("contents") {
                    add(buildJsonObject {
                        put("type", "text")
                        put("text", "⛳ 新球賽報名通知")
                        put("weight", "bold")
                        put("color", "#1DB446")
                        put("size", "sm")
                    })
                    add(buildJsonObject {
                        put("type", "text")
                        put("text", event.title)
                        put("weight", "bold")
                        put("size", "xl")
                        put("margin", "md")
                    })
                    add(buildJsonObject {
                        put("type", "box")
                        put("layout", "vertical")
                        put("margin", "lg")
                        putJsonArray("contents") {
                            add(buildDataRow("日期", event.date))
                            add(buildDataRow("地點", event.location))
                        }
                    })
                }
            }
            putJsonObject("footer") {
                put("type", "box")
                put("layout", "vertical")
                putJsonArray("contents") {
                    add(buildJsonObject {
                        put("type", "button")
                        putJsonObject("action") {
                            put("type", "uri")
                            put("label", "立即開啟 LIFF 報名")
                            put("uri", "$liffUrl?eventId=${event.id}")
                        }
                        put("style", "primary")
                        put("color", "#133B2B")
                    })
                }
            }
        }
    }

    private fun buildDataRow(label: String, value: String): JsonObject {
        return buildJsonObject {
            put("type", "box")
            put("layout", "baseline")
            put("spacing", "sm")
            putJsonArray("contents") {
                add(buildJsonObject {
                    put("type", "text")
                    put("text", label)
                    put("color", "#aaaaaa")
                    put("size", "sm")
                    put("flex", 1)
                })
                add(buildJsonObject {
                    put("type", "text")
                    put("text", value)
                    put("wrap", true)
                    put("color", "#666666")
                    put("size", "sm")
                    put("flex", 5)
                })
            }
        }
    }
}
