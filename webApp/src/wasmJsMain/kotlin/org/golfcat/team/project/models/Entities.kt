package org.golfcat.team.project.models

data class User(
    val id: String?,
    val lineUid: String,
    val lineDisplayName: String?,
    val realName: String,
    val initialHandicap: Double,
    val isSuperAdmin: Boolean,
    val createdAt: String?
)

data class EventWithDetails(
    val id: String,
    val title: String,
    val date: String,
    val location: String?,
    val registrationStatus: String,
    val handicapRule: String,
    val startTime: String?,
    val groupCount: Int?,
    val participantCount: Int,
    val isUserRegistered: Boolean,
    val isArchivedInList: Boolean
)

data class Score(
    val id: String? = null,
    val eventId: String,
    val teamMemberId: String,
    val grossScore: Int,
    val netScore: Double? = null,
    val appliedHandicap: Double? = null,
    val holeScores: List<Int>,
    val createdAt: String? = null
)

data class MemberWithUser(
    val id: String,
    val teamId: String,
    val handicap: Double,
    val role: String,
    val users: User,
    val sponsorMemberId: String? = null,
    val groupNumber: Int? = null,
    val groupSide: String? = null,
    val pairedGroupName: String? = null
)
