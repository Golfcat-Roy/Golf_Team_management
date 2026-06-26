package org.golfcat.team.project

import org.golfcat.team.project.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TeamRepository {
    private val _events = MutableStateFlow<List<EventWithDetails>>(listOf(
        EventWithDetails(
            id = "e1", 
            title = "2024 Summer Open", 
            date = "2024-06-25", 
            location = "Daxi Golf Course", 
            registrationStatus = "open", 
            handicapRule = "New_New_Peoria", 
            startTime = "08:30", 
            groupCount = 4, 
            participantCount = 12, 
            isUserRegistered = false, 
            isArchivedInList = false
        )
    ))
    
    val events: StateFlow<List<EventWithDetails>> = _events.asStateFlow()

    // 💡 Mock Score Data
    private val mockScores = mutableMapOf<String, List<Int>>(
        "u1" to List(18) { 4 } // Default 18 holes of Par 4
    )

    suspend fun toggleRegistration(eventId: String) {
        val currentList = _events.value
        _events.value = currentList.map { event ->
            if (event.id == eventId) {
                event.copy(
                    isUserRegistered = !event.isUserRegistered,
                    participantCount = if (event.isUserRegistered) event.participantCount - 1 else event.participantCount + 1
                )
            } else event
        }
    }

    suspend fun getEventPars(eventId: String): List<Int> = List(18) { 4 }

    suspend fun getMemberScore(eventId: String, memberId: String): List<Int> {
        return mockScores[memberId] ?: List(18) { 0 }
    }

    suspend fun updateHoleScore(eventId: String, memberId: String, holeIndex: Int, score: Int) {
        val currentHoles = (mockScores[memberId] ?: List(18) { 0 }).toMutableList()
        if (holeIndex in 0 until 18) {
            currentHoles[holeIndex] = score
            mockScores[memberId] = currentHoles
        }
    }
}
