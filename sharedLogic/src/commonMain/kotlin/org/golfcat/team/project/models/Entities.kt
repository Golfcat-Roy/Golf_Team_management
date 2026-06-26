package org.golfcat.team.project.models

// 💡 徹底移除所有外部依賴註解 (SerialName, Serializable)，確保編譯 100% 通過
data class Team(
    val id: String?,
    val name: String,
    val joinCode: String,
    val subscriptionStatus: String,
    val subscriptionType: String,
    val memberLimit: Int,
    val createdAt: String?
)

data class User(
    val id: String?,
    val lineUid: String,
    val lineDisplayName: String?,
    val realName: String,
    val initialHandicap: Double,
    val isSuperAdmin: Boolean,
    val createdAt: String?
)

data class UserUpdate(
    val realName: String,
    val initialHandicap: Double
)

data class TeamMember(
    val id: String?,
    val teamId: String,
    val userId: String,
    val role: String,
    val currentTeamHandicap: Double,
    val memberStatus: String,
    val sponsorMemberId: String?,
    val createdAt: String?
)

data class Event(
    val id: String?,
    val teamId: String,
    val title: String,
    val date: String,
    val location: String,
    val handicapRule: String,
    val fee: Int,
    val registrationStatus: String,
    val startTime: String?,
    val groupCount: Int?,
    val matchPlaySubMode: String?,
    val pars: List<Int>,
    val hiddenHoles: List<Int>?,
    val isSettled: Boolean,
    val isArchivedInList: Boolean,
    val createdAt: String?
)

data class EventRegistration(
    val id: String?,
    val eventId: String,
    val teamMemberId: String,
    val status: String,
    val createdAt: String?
)

data class EventGroup(
    val id: String?,
    val eventId: String,
    val groupNumber: Int,
    val side: String?,
    val captainId: String?,
    val createdAt: String?
)

data class EventGroupUpdate(
    val groupNumber: Int
)

data class EventGroupMember(
    val id: String?,
    val groupId: String,
    val teamMemberId: String,
    val createdAt: String?
)

data class MatchPairing(
    val id: String?,
    val eventId: String,
    val groupAId: String,
    val groupBId: String,
    val createdAt: String?
)

data class NewPeoriaVote(
    val id: String?,
    val eventId: String,
    val teamMemberId: String,
    val votedHoles: List<Int>,
    val createdAt: String?
)

data class Score(
    val id: String?,
    val eventId: String,
    val teamMemberId: String,
    val grossScore: Int,
    val netScore: Double?,
    val appliedHandicap: Double?,
    val holeScores: List<Int>,
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

data class MemberWithUser(
    val id: String,
    val teamId: String,
    val handicap: Double,
    val role: String,
    val users: User,
    val sponsorMemberId: String?,
    val groupNumber: Int?,
    val groupSide: String?,
    val pairedGroupName: String?
)

data class HistoryEntry(
    val eventTitle: String,
    val date: String,
    val grossScore: Int,
    val holeScores: List<Int>,
    val pars: List<Int>,
    val startTime: String?,
    val netScore: Double?,
    val appliedHandicap: Double?
)

data class PersonalHistory(
    val birdieCount: Int,
    val eagleCount: Int,
    val parCount: Int,
    val entries: List<HistoryEntry>
)

data class TeamStats(
    val teamId: String,
    val teamName: String,
    val joinCode: String,
    val memberCount: Int,
    val adminName: String?,
    val status: String,
    val subscriptionType: String,
    val memberLimit: Int
)

data class LineGroup(
    val id: String,
    val teamId: String,
    val createdAt: String?
)

data class EventGroupWithMembers(
    val group: EventGroup,
    val members: List<MemberWithUser>
)

data class CourseMaster(
    val id: String?,
    val name: String,
    val locationCity: String?,
    val pars: List<Int>,
    val createdByTeamId: String?,
    val isVerified: Boolean
)

data class EventLeaderboardEntry(
    val memberId: String,
    val realName: String,
    val holeScores: List<Int>,
    val grossScore: Int,
    val toPar: Int,
    val holesPlayed: Int,
    val isFinished: Boolean,
    val points: Double
)

data class AppErrorLog(
    val id: String?,
    val teamId: String?,
    val teamName: String?,
    val message: String,
    val createdAt: String?
)

data class ScoreWithEvent(
    val eventId: String,
    val teamMemberId: String,
    val grossScore: Int,
    val netScore: Double?,
    val appliedHandicap: Double?,
    val holeScores: List<Int>,
    val events: Event
)

data class TeamWithEmbeddedMembers(
    val id: String?,
    val name: String,
    val joinCode: String,
    val subscriptionStatus: String,
    val subscriptionType: String,
    val memberLimit: Int,
    val members: List<EmbeddedMember>
) {
    val team: Team get() = Team(id, name, joinCode, subscriptionStatus, subscriptionType, memberLimit, null)
}

data class EmbeddedMember(
    val role: String,
    val users: EmbeddedUser
)

data class EmbeddedUser(
    val realName: String
)

object MemberRoles {
    const val PRESIDENT = "president"
    const val VICE_PRESIDENT = "vice_president"
    const val ADMIN = "admin"
    const val VICE_ADMIN = "vice_admin"
    const val MEMBER = "member"
    const val GUEST = "guest"

    fun isAdmin(role: String?): Boolean {
        return role == ADMIN || role == PRESIDENT || role == VICE_PRESIDENT || role == VICE_ADMIN
    }

    fun getDisplayName(role: String?): String {
        return when (role) {
            PRESIDENT -> "會長"
            VICE_PRESIDENT -> "副會長"
            ADMIN -> "總幹事"
            VICE_ADMIN -> "副總幹事"
            GUEST -> "來賓"
            else -> "球員"
        }
    }
}
