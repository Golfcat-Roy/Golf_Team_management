package org.golfcat.team.project

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TeamRepositoryIntegrationTest {

    private fun createMockSupabaseClient(handler: MockRequestHandler): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://mock.supabase.co",
            supabaseKey = "mock_key"
        ) {
            install(Postgrest)
            httpEngine = MockEngine { request ->
                handler(request)
            }
        }
    }

    @Test
    fun testGetAllTeamsSuccess() = runTest {
        val mockClient = createMockSupabaseClient { request ->
            respond(
                content = """[{"id": "t1", "name": "Team A", "join_code": "A123", "status": "active"}]""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val repository = TeamRepository(mockClient)
        val teams = repository.getAllTeams()
        assertEquals(1, teams.size)
        assertEquals("Team A", teams[0].name)
    }

    @Test
    fun testApiError406Handling() = runTest {
        val mockClient = createMockSupabaseClient { request ->
            respond(
                content = "Not Acceptable",
                status = HttpStatusCode.NotAcceptable
            )
        }
        val repository = TeamRepository(mockClient)
        
        // Ensure the repository throws an exception when 406 is returned
        // In the App.kt, this exception is caught to trigger logout
        assertFailsWith<Exception> {
            repository.getAllTeams()
        }
    }

    @Test
    fun testNetworkTimeoutHandling() = runTest {
        val mockClient = createMockSupabaseClient { request ->
            // Simulating a timeout or network error by throwing an exception in the engine
            throw io.ktor.client.network.sockets.ConnectTimeoutException("Timed out")
        }
        val repository = TeamRepository(mockClient)

        // Supabase SDK wraps Ktor exceptions into HttpRequestException
        assertFailsWith<io.github.jan.supabase.exceptions.HttpRequestException> {
            repository.getAllTeams()
        }
    }
}
