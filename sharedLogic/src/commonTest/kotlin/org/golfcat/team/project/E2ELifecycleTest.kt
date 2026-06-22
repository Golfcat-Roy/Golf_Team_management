package org.golfcat.team.project

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.golfcat.team.project.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class E2ELifecycleTest {

    @Test
    fun testFullEventLifecycle() = runTest {
        val teamId = "t1"
        val adminId = "u_admin"
        val memberId = "m_1"
        val eventId = "e_999"

        // 模擬 Supabase 回傳序列，使用 state 追蹤狀態
        var currentStatus = "open"
        
        val mockClient = createSupabaseClient(
            supabaseUrl = "https://mock.supabase.co",
            supabaseKey = "mock_key"
        ) {
            install(Postgrest)
            httpEngine = MockEngine { request ->
                val path = request.url.encodedPath
                val method = request.method
                
                when {
                    // 1. 建立賽事
                    path.contains("events") && method == HttpMethod.Post -> {
                        respond(content = "{}", status = HttpStatusCode.Created)
                    }
                    // 2. 獲取賽事詳情 (動態反映 currentStatus)
                    path.contains("events") && method == HttpMethod.Get -> {
                        respond(
                            content = """[{"id": "$eventId", "team_id": "$teamId", "title": "E2E Test Event", "registration_status": "$currentStatus", "date": "2025-12-25", "location": "E2E Course", "handicap_rule": "Team_Handicap", "pars": [4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4]}]""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                    // 3. 更新賽事狀態 (PATCH)
                    path.contains("events") && method == HttpMethod.Patch -> {
                        // 簡單解析狀態更新 (通常會檢查 request body，這裡簡化處理)
                        if (request.body.toString().contains("closed")) {
                            currentStatus = "closed"
                        } else if (request.body.toString().contains("started")) {
                            currentStatus = "started"
                        }
                        respond(content = "{}", status = HttpStatusCode.OK)
                    }
                    // 4. 報名賽事
                    path.contains("event_participants") && method == HttpMethod.Post -> {
                        respond(content = "{}", status = HttpStatusCode.Created)
                    }
                    // 5. 提交成績
                    path.contains("scores") && method == HttpMethod.Post -> {
                        respond(content = "{}", status = HttpStatusCode.OK)
                    }
                    else -> respond(content = "[]", status = HttpStatusCode.OK)
                }
            }
        }

        val repository = TeamRepository(mockClient)

        // --- Step 1: Admin Creates Event ---
        repository.createEvent(
            teamId = teamId,
            location = "E2E Course",
            date = "2025-12-25",
            handicapRule = "Team_Handicap",
            pars = List(18) { 4 }
        )

        // --- Step 2: Member Registers ---
        repository.registerForEvent(eventId, memberId)

        // --- Step 3: Start the Event ---
        repository.updateEventStatus(eventId, "started")

        // --- Step 4: Submit Scores ---
        val scores = mapOf(memberId to List(18) { 4 }) // All Pars
        val achievements = repository.submitScores(eventId, scores, List(18) { 4 })
        // Since they are all pars, no "Birdie/Eagle" achievements should be returned by current logic 
        // (but we check if the call finishes without crash)
        assertTrue(achievements.isEmpty() || achievements.any { it.contains("洞") })

        // --- Step 5: Finish and Close ---
        repository.updateEventStatus(eventId, "closed")
        
        // --- Step 6: Verify Final State ---
        val event = repository.getEventById(eventId)
        assertEquals("closed", event?.registrationStatus)
        assertEquals(eventId, event?.id)
    }
}
