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
