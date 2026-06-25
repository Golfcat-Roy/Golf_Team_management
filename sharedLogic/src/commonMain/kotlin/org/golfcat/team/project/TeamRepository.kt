package org.golfcat.team.project

import org.golfcat.team.project.models.*
import kotlinx.datetime.*

class TeamRepository(private val customClient: Any? = null) {
    
    // 💡 Wasm 專用的 Mock 資料
    suspend fun getAllTeams(): List<Team> = listOf(
        Team(id = "1", name = "高爾夫貓球隊", joinCode = "GC001", subscriptionStatus = "active", subscriptionType = "premium", memberLimit = 100)
    )

    suspend fun getMyTeams(userId: String): List<Team> = getAllTeams()

    suspend fun syncUser(lineUser: User): User = lineUser

    suspend fun updateUserProfile(userId: String, realName: String, handicap: Double): User = 
        User(id = userId, lineUid = "U123", realName = realName, initialHandicap = handicap)

    suspend fun getUserByLineUid(lineUid: String): User? = null

    suspend fun getTeamMatchHistory(teamId: String): List<Event> = listOf(
        Event(id = "e1", teamId = teamId, title = "2024 夏季賽", date = "2024-06-25", location = "大溪球場", handicapRule = "New_New_Peoria", fee = 2500, registrationStatus = "closed")
    )

    suspend fun getEventById(eventId: String): Event? = null

    suspend fun joinTeamByCode(joinCode: String, userId: String, initialHandicap: Double = 36.0): Team = 
        Team(id = "1", name = "高爾夫貓球隊", joinCode = joinCode)

    suspend fun broadcastEventToLine(event: Event): Boolean = true

    suspend fun createEvent(teamId: String, location: String, date: String, handicapRule: String, pars: List<Int>? = null, startTime: String? = null, groupCount: Int? = null, matchPlaySubMode: String? = null) {}

    suspend fun updateEvent(eventId: String, location: String, date: String, handicapRule: String, pars: List<Int>, startTime: String?, groupCount: Int?, matchPlaySubMode: String?, newPeoriaStrategy: String? = null) {}

    suspend fun registerForEvent(eventId: String, teamMemberId: String) {}

    suspend fun cancelRegistration(eventId: String, teamMemberId: String) {}

    suspend fun submitVotes(eventId: String, teamMemberId: String, votedHoles: List<Int>) {}

    suspend fun deleteVotes(eventId: String, teamMemberId: String) {}

    suspend fun getMember(userId: String, teamId: String): TeamMember? = 
        TeamMember(id = "m1", teamId = teamId, userId = userId, role = "admin")

    suspend fun submitScores(eventId: String, memberScores: Map<String, List<Int>>) {}

    suspend fun getScoresByEvent(eventId: String): List<Score> = emptyList()

    suspend fun getEventLeaderboard(eventId: String): List<EventLeaderboardEntry> = emptyList()

    suspend fun getEventPars(eventId: String): List<Int> = List(18) { 4 }

    suspend fun getEventsWithDetails(teamId: String, userId: String): List<EventWithDetails> = listOf(
        EventWithDetails(id = "e1", title = "2024 夏季賽", date = "2024-06-25", location = "大溪球場", registrationStatus = "closed", handicapRule = "New_New_Peoria", startTime = "08:00", groupCount = 4, participantCount = 16, isUserRegistered = true)
    )

    suspend fun archiveEventFromList(eventId: String) {}

    suspend fun getEventParticipants(eventId: String): List<MemberWithUser> = emptyList()

    suspend fun registerGuest(eventId: String, sponsorMemberId: String, guestName: String, handicap: Double) {}

    suspend fun getLeaderboard(teamId: String): List<MemberWithUser> = emptyList()

    suspend fun getUserGroupId(userId: String, eventId: String): String? = null

    suspend fun getGroupMembers(groupId: String): List<MemberWithUser> = emptyList()

    suspend fun deleteTeamMember(memberId: String) {}

    suspend fun updateMemberHandicap(memberId: String, newHandicap: Double) {}

    suspend fun updateMemberRole(memberId: String, newRole: String) {}

    suspend fun updateEventStatus(eventId: String, status: String) {}

    suspend fun startEvent(eventId: String) {}

    suspend fun finishEvent(eventId: String): Any? = null

    suspend fun batchUpdateMemberHandicaps(eventId: String, handicapMap: Map<String, Double>) {}

    suspend fun deleteEvent(eventId: String) {}

    suspend fun getFullPersonalHistory(userId: String, teamId: String): PersonalHistory = 
        PersonalHistory(birdieCount = 1, eagleCount = 0, parCount = 10, entries = emptyList())

    suspend fun getSuperAdminDashboard(): List<TeamStats> = emptyList()

    suspend fun updateTeamSubscription(teamId: String, type: String, limit: Int) {}

    suspend fun createTeamBySuperAdmin(name: String, joinCode: String, type: String = "free", limit: Int = 30): Team = 
        Team(name = name, joinCode = joinCode)

    suspend fun logError(teamId: String?, message: String) {}

    suspend fun getErrorLogs(): List<AppErrorLog> = emptyList()

    suspend fun deleteTeam(teamId: String) {}

    suspend fun updateTeamStatus(teamId: String, status: String) {}
    
    suspend fun createEventGroup(eventId: String, groupNumber: Int, side: String? = null): EventGroup = 
        EventGroup(eventId = eventId, groupNumber = groupNumber, side = side)

    suspend fun deleteEventGroup(groupId: String, eventId: String) {}

    suspend fun assignMemberToGroup(groupId: String, teamMemberId: String) {}

    suspend fun batchUpdateGroupMembers(eventId: String, side: String?, assignments: List<Pair<String, String>>) {}

    suspend fun getGroupsWithMembers(eventId: String, side: String? = null, includeAllSides: Boolean = false): List<EventGroupWithMembers> = emptyList()

    suspend fun removeFromGroup(groupId: String, teamMemberId: String) {}

    suspend fun pairGroupsRandomly(eventId: String) {}

    suspend fun getMatchPairings(eventId: String): List<MatchPairing> = emptyList()

    suspend fun getMatchPlayResults(eventId: String): List<Any> = emptyList()

    suspend fun searchCourses(query: String): List<CourseMaster> = emptyList()

    suspend fun saveCourseMaster(course: CourseMaster) {}
}
