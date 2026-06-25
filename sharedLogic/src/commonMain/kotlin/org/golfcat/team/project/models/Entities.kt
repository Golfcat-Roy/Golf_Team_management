package org.golfcat.team.project.models

import kotlinx.serialization.SerialName
// import kotlinx.serialization.Serializable // 💡 暫時移除，避開 Wasm 編譯器 Bug

// @Serializable
data class Team(
    val id: String? = null,
    val name: String,
    @SerialName("join_code") val joinCode: String,
    @SerialName("subscription_status") val subscriptionStatus: String = "active",
    @SerialName("subscription_type") val subscriptionType: String = "free",
    @SerialName("member_limit") val memberLimit: Int = 30,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class User(
    val id: String? = null,
    @SerialName("line_uid") val lineUid: String,
    @SerialName("line_display_name") val lineDisplayName: String? = null,
    @SerialName("real_name") val realName: String = "User",
    @SerialName("initial_handicap") val initialHandicap: Double = 36.0,
    @SerialName("is_super_admin") val isSuperAdmin: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class UserUpdate(
    @SerialName("real_name") val realName: String,
    @SerialName("initial_handicap") val initialHandicap: Double
)

// @Serializable
data class TeamMember(
    val id: String? = null,
    @SerialName("team_id") val teamId: String,
    @SerialName("user_id") val userId: String,
    val role: String = "member",
    @SerialName("current_team_handicap") val currentTeamHandicap: Double = 36.0,
    @SerialName("member_status") val memberStatus: String = "active",
    @SerialName("sponsor_member_id") val sponsorMemberId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class Event(
    val id: String? = null,
    @SerialName("team_id") val teamId: String,
    val title: String,
    val date: String,
    val location: String,
    @SerialName("handicap_rule") val handicapRule: String,
    val fee: Int = 0,
    @SerialName("registration_status") val registrationStatus: String = "open",
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("group_count") val groupCount: Int? = null,
    @SerialName("match_play_sub_mode") val matchPlaySubMode: String? = null,
    val pars: List<Int> = listOf(4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4),
    @SerialName("hidden_holes") val hiddenHoles: List<Int>? = null,
    @SerialName("is_settled") val isSettled: Boolean = false,
    @SerialName("is_archived_in_list") val isArchivedInList: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class EventRegistration(
    val id: String? = null,
    @SerialName("event_id") val eventId: String,
    @SerialName("team_member_id") val teamMemberId: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class EventGroup(
    val id: String? = null,
    @SerialName("event_id") val eventId: String,
    @SerialName("group_number") val groupNumber: Int,
    val side: String? = null, // 'A' or 'B'
    @SerialName("captain_id") val captainId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class EventGroupUpdate(
    @SerialName("group_number") val groupNumber: Int
)

// @Serializable
data class EventGroupMember(
    val id: String? = null,
    @SerialName("group_id") val groupId: String,
    @SerialName("team_member_id") val teamMemberId: String,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class MatchPairing(
    val id: String? = null,
    @SerialName("event_id") val eventId: String,
    @SerialName("group_a_id") val groupAId: String,
    @SerialName("group_b_id") val groupBId: String,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class NewPeoriaVote(
    val id: String? = null,
    @SerialName("event_id") val eventId: String,
    @SerialName("team_member_id") val teamMemberId: String,
    @SerialName("voted_holes") val votedHoles: List<Int>,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class Score(
    val id: String? = null,
    @SerialName("event_id") val eventId: String,
    @SerialName("team_member_id") val teamMemberId: String,
    @SerialName("gross_score") val grossScore: Int,
    @SerialName("net_score") val netScore: Double? = null,
    @SerialName("applied_handicap") val appliedHandicap: Double? = null,
    @SerialName("hole_scores") val holeScores: List<Int>,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
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
    val isArchivedInList: Boolean = false
)

// @Serializable
data class MemberWithUser(
    val id: String,
    @SerialName("team_id") val teamId: String,
    @SerialName("current_team_handicap") val handicap: Double,
    val role: String,
    val users: User,
    @SerialName("sponsor_member_id") val sponsorMemberId: String? = null,
    var groupNumber: Int? = null,
    var groupSide: String? = null,
    var pairedGroupName: String? = null
)

// @Serializable
data class HistoryEntry(
    val eventTitle: String,
    val date: String,
    val grossScore: Int,
    val holeScores: List<Int>,
    val pars: List<Int>,
    val startTime: String? = null,
    val netScore: Double? = null,
    val appliedHandicap: Double? = null
)

// @Serializable
data class PersonalHistory(
    val birdieCount: Int,
    val eagleCount: Int,
    val parCount: Int,
    val entries: List<HistoryEntry>
)

// @Serializable
data class TeamStats(
    val teamId: String,
    val teamName: String,
    val joinCode: String,
    val memberCount: Int,
    val adminName: String?,
    val status: String,
    @SerialName("subscription_type") val subscriptionType: String = "free",
    @SerialName("member_limit") val memberLimit: Int = 30
)

// @Serializable
data class LineGroup(
    val id: String,
    @SerialName("team_id") val teamId: String,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class EventGroupWithMembers(
    val group: EventGroup,
    val members: List<MemberWithUser>
)

// @Serializable
data class CourseMaster(
    val id: String? = null,
    val name: String,
    @SerialName("location_city") val locationCity: String? = null,
    val pars: List<Int>,
    @SerialName("created_by_team_id") val createdByTeamId: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false
)

// @Serializable
data class EventLeaderboardEntry(
    val memberId: String,
    val realName: String,
    val holeScores: List<Int>,
    val grossScore: Int,
    val toPar: Int,
    val holesPlayed: Int,
    val isFinished: Boolean,
    val points: Double = 0.0
)

// @Serializable
data class AppErrorLog(
    val id: String? = null,
    @SerialName("team_id") val teamId: String? = null,
    @SerialName("team_name") val teamName: String? = null,
    val message: String,
    @SerialName("created_at") val createdAt: String? = null
)

// @Serializable
data class ScoreWithEvent(
    @SerialName("event_id") val eventId: String,
    @SerialName("team_member_id") val teamMemberId: String,
    @SerialName("gross_score") val grossScore: Int,
    @SerialName("net_score") val netScore: Double? = null,
    @SerialName("applied_handicap") val appliedHandicap: Double? = null,
    @SerialName("hole_scores") val holeScores: List<Int>,
    val events: Event
)

// @Serializable
data class TeamWithEmbeddedMembers(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("join_code") val joinCode: String,
    @SerialName("subscription_status") val subscriptionStatus: String,
    @SerialName("subscription_type") val subscriptionType: String,
    @SerialName("member_limit") val memberLimit: Int,
    @SerialName("team_members") val members: List<EmbeddedMember>
) {
    val team: Team get() = Team(id, name, joinCode, subscriptionStatus, subscriptionType, memberLimit)
}

// @Serializable
data class EmbeddedMember(
    val role: String,
    val users: EmbeddedUser
)

// @Serializable
data class EmbeddedUser(
    @SerialName("real_name") val realName: String
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
