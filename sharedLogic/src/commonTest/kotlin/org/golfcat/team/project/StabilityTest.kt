package org.golfcat.team.project

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.golfcat.team.project.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StabilityTest {

    // --- 4.1 高併發 API 壓力測試 (模擬 20 名球員同時儲存成績) ---
    
    @Test
    fun testConcurrentScoreSaving() = runTest {
        val concurrentPlayers = 20
        var requestCount = 0
        
        val mockClient = createSupabaseClient(
            supabaseUrl = "https://mock.supabase.co",
            supabaseKey = "mock_key"
        ) {
            install(Postgrest)
            httpEngine = MockEngine { request ->
                // 模擬網路延遲以增加碰撞機會
                delay(10)
                requestCount++
                respond(
                    content = """{"status": "success"}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }
        
        val repository = TeamRepository(mockClient)
        val eventId = "e1"
        val pars = List(18) { 4 }
        
        // 啟動 20 個協程同時發送請求
        withContext(Dispatchers.Default) {
            val jobs = List(concurrentPlayers) { i ->
                async {
                    val scores = mapOf("m_$i" to List(18) { 4 })
                    repository.submitScores(eventId, scores, pars)
                }
            }
            jobs.awaitAll()
        }
        
        assertEquals(concurrentPlayers, requestCount, "所有的併發請求都應被正確處理")
    }

    // --- 4.2 時區與跨日邏輯測試 ---

    @Test
    fun testEventDateCategorizationInDifferentTimezones() {
        // 假設賽事日期是 2025-05-20
        val eventDate = "2025-05-20"
        
        // 模擬當前時間為 2025-05-20 00:30:00 (台北時間 UTC+8)
        // 這在 UTC 是 2025-05-19 16:30:00
        val nowUtc = LocalDateTime(2025, 5, 19, 16, 30, 0).toInstant(TimeZone.UTC)
        
        // 驗證邏輯：在 UTC+8 時區看，今天應該是 2025-05-20
        val todayInTaipei = nowUtc.toLocalDateTime(FixedOffsetTimeZone(UtcOffset(hours = 8))).date.toString()
        assertEquals(eventDate, todayInTaipei, "在 UTC+8 時區應視為當天賽事")
        
        // 驗證邏輯：在 UTC-4 時區看，當前是 2025-05-19 12:30:00
        val todayInNewYork = nowUtc.toLocalDateTime(FixedOffsetTimeZone(UtcOffset(hours = -4))).date.toString()
        assertTrue(todayInNewYork != eventDate, "在 UTC-4 時區應視為未來的賽事")
    }

    // --- 4.3 蒙地卡羅擴充：極端桿數穩定性 ---

    @Test
    fun testMonteCarloExtremeScores() {
        val participants = List(10) { i ->
            MemberWithUser(
                id = "m_$i", teamId = "t1", handicap = 15.0, role = "member",
                users = User(id = "u_$i", lineUid = "l_$i", realName = "P$i")
            )
        }
        
        // 模擬極端桿數：有人打 1 桿 (HIO)，有人打 15 桿 (爆掉)
        val scores = participants.map { m ->
            Score(
                eventId = "e1", teamMemberId = m.id, grossScore = 0, // will sum
                holeScores = List(18) { if (kotlin.random.Random.nextBoolean()) 1 else 15 }
            ).let { it.copy(grossScore = it.holeScores.sum()) }
        }
        
        val totalPar = 72
        
        // 確保計算過程不會因為桿數過高或過低產生 NaN 或崩潰
        val results = HandicapCalculator.settleTeamHandicap(scores, participants, totalPar)
        assertEquals(10, results.size)
        results.forEach { 
            assertTrue(it.newTeamHandicap >= 0.0, "新差點不應為負數")
            assertTrue(!it.netScore.isNaN(), "淨桿不應為 NaN")
        }
    }
}
