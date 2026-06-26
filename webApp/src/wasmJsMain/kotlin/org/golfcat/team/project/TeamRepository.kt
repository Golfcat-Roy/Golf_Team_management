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
        ),
        EventWithDetails(
            id = "e2", 
            title = "Monthly Match - July", 
            date = "2024-07-15", 
            location = "Sunrise Golf Club", 
            registrationStatus = "open", 
            handicapRule = "Fixed_HCP", 
            startTime = "07:00", 
            groupCount = 0, 
            participantCount = 5, 
            isUserRegistered = true, 
            isArchivedInList = false
        )
    ))
    
    val events: StateFlow<List<EventWithDetails>> = _events.asStateFlow()

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
}
