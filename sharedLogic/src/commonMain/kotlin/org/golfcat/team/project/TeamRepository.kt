package org.golfcat.team.project

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.datetime.*
import org.golfcat.team.project.models.*
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.Clock

class TeamRepository(private val customClient: io.github.jan.supabase.SupabaseClient? = null) {
    // Use a getter to ensure 'supabase' (which is lazy) is not initialized until needed.
    private val client get() = customClient ?: supabase

    suspend fun getAllTeams(): List<Team> {
        return client.postgrest["teams"].select().decodeList<Team>()
    }

    suspend fun getMyTeams(userId: String): List<Team> {
        val memberTeamIds = client.postgrest["team_members"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<TeamMember>()
            .map { it.teamId }

        if (memberTeamIds.isEmpty()) return emptyList()

        return client.postgrest["teams"]
            .select { filter { isIn("id", memberTeamIds) } }
            .decodeList<Team>()
    }

    suspend fun syncUser(lineUser: User): User {
        val existingUser = getUserByLineUid(lineUser.lineUid)
        return if (existingUser == null) {
            client.postgrest["users"].insert(lineUser) { select() }.decodeSingle<User>()
        } else {
            val updatedUser = existingUser.copy(
                lineDisplayName = lineUser.lineDisplayName,
                realName = if (existingUser.realName == "User") lineUser.realName else existingUser.realName
            )
            // 確保有 ID 才能更新
            if (existingUser.id == null) {
                client.postgrest["users"].insert(updatedUser) { select() }.decodeSingle<User>()
            } else {
                client.postgrest["users"].update(updatedUser) {
                    filter { eq("id", existingUser.id!!) }
                    select()
                }.decodeSingle<User>()
            }
        }
    }

    suspend fun updateUserProfile(userId: String, realName: String, handicap: Double): User {
        // 現在 RLS 已設為 true，我們可以安全地使用 select() 回傳完整資料
        return client.postgrest["users"].update(
            UserUpdate(realName = realName, initialHandicap = handicap)
        ) {
            filter { eq("id", userId) }
            select()
        }.decodeSingle<User>()
    }

    suspend fun getUserByLineUid(lineUid: String): User? {
        return client.postgrest["users"]
            .select { filter { eq("line_uid", lineUid) } }
            .decodeSingleOrNull<User>()
    }

    suspend fun getTeamMatchHistory(teamId: String): List<Event> {
        val team = client.postgrest["teams"].select { filter { eq("id", teamId) } }.decodeSingleOrNull<Team>()
        
        return client.postgrest["events"].select {
            filter { 
                eq("team_id", teamId)
                eq("registration_status", "closed")
            }
            order("date", Order.DESCENDING)
            order("start_time", Order.DESCENDING)
            if (team?.subscriptionType == "free") {
                limit(2)
            }
        }.decodeList<Event>()
    }

    suspend fun getEventById(eventId: String): Event? {
        return client.postgrest["events"]
            .select { filter { eq("id", eventId) } }
            .decodeSingleOrNull<Event>()
    }

    suspend fun joinTeamByCode(joinCode: String, userId: String, initialHandicap: Double = 36.0): Team {
        val team = client.postgrest["teams"]
            .select { filter { eq("join_code", joinCode) } }
            .decodeSingleOrNull<Team>() ?: throw Exception("Invalid Join Code")

        val existingMember = getMember(userId, team.id!!)
        if (existingMember != null) return team

        // 檢查人數上限
        val currentMemberCount = client.postgrest["team_members"]
            .select {
                filter { eq("team_id", team.id!!) }
            }.decodeList<TeamMember>().size

        if (currentMemberCount >= team.memberLimit) {
            throw Exception("該球隊人數已達上限 (${team.memberLimit}人)，請聯絡管理員。")
        }

        val anyExistingMembers = client.postgrest["team_members"]
            .select {
                filter { eq("team_id", team.id!!) }
                limit(1)
            }
            .decodeList<TeamMember>()
        
        val role = if (anyExistingMembers.isEmpty()) "admin" else "member"
        val member = TeamMember(
            teamId = team.id!!,
            userId = userId,
            role = role,
            currentTeamHandicap = initialHandicap
        )
        client.postgrest["team_members"].insert(member)
        return team
    }

    suspend fun createEvent(
        teamId: String,
        location: String,
        date: String,
        handicapRule: String,
        pars: List<Int>? = null,
        startTime: String? = null,
        groupCount: Int? = null,
        matchPlaySubMode: String? = null
    ) {
        val eventPars = if (pars != null && pars.size == 18) pars else List(18) { 4 }
        val event = Event(
            teamId = teamId,
            title = "$location 例行賽",
            date = date,
            location = location,
            handicapRule = handicapRule,
            pars = eventPars,
            registrationStatus = "open",
            startTime = startTime,
            groupCount = groupCount,
            matchPlaySubMode = matchPlaySubMode
        )
        try {
            client.postgrest["events"].insert(event)
        } catch (e: Exception) {
            println("Postgrest insert error: ${e.message}")
            throw e
        }
    }

    suspend fun updateEvent(
        eventId: String,
        location: String,
        date: String,
        handicapRule: String,
        pars: List<Int>,
        startTime: String?,
        groupCount: Int?,
        matchPlaySubMode: String?,
        newPeoriaStrategy: String? = null // "RANDOM" or "VOTE"
    ) {
        val oldEvent = getEventById(eventId) ?: throw Exception("找不到該賽事")
        
        // 規則 3: 球隊差點或新新貝利亞不可轉成分組對抗
        if (handicapRule == "Match_Play" && oldEvent.handicapRule != "Match_Play") {
            throw Exception("不允許將一般賽制更改為分組對抗賽")
        }

        var finalHiddenHoles = oldEvent.hiddenHoles

        // 策略處理：若是切換到新新貝利亞，或原本就是新新貝利亞但要求重設策略
        if (handicapRule == "New_New_Peoria") {
            when (newPeoriaStrategy) {
                "RANDOM" -> {
                    // 選項一：系統隨機抽選 6 洞
                    finalHiddenHoles = (1..18).shuffled().take(6).sorted()
                }
                "VOTE" -> {
                    // 選項二：清空隱藏洞，讓球員投票
                    finalHiddenHoles = null
                }
            }
        }

        // 規則 2: 若從新新貝利亞轉出，刪除先前投票紀錄
        if (oldEvent.handicapRule == "New_New_Peoria" && handicapRule != "New_New_Peoria") {
            client.postgrest["new_peoria_votes"].delete {
                filter { eq("event_id", eventId) }
            }
            finalHiddenHoles = null
        }

        val updateData = Event(
            id = eventId,
            teamId = oldEvent.teamId,
            title = "$location 例行賽",
            date = date,
            location = location,
            handicapRule = handicapRule,
            pars = pars,
            startTime = startTime,
            groupCount = groupCount,
            matchPlaySubMode = matchPlaySubMode,
            hiddenHoles = finalHiddenHoles
        )

        client.postgrest["events"].update(updateData) {
            filter { eq("id", eventId) }
        }

        // 預留功能: 在 LINE 群組發送通知
        sendLineNotification("賽事資訊已更新：${updateData.title} (${updateData.date})")
    }

    private fun sendLineNotification(message: String) {
        // 未來實作 LINE Messaging API
        println("LINE Notification Placeholder: $message")
    }

    suspend fun registerForEvent(eventId: String, teamMemberId: String) {
        val registration = EventRegistration(eventId = eventId, teamMemberId = teamMemberId, status = "registered")
        try {
            client.postgrest["event_registrations"].insert(registration)
        } catch (e: Exception) {
            if (e.message?.contains("23505") == true) throw Exception("您已經報名過了。")
            throw e
        }
    }

    suspend fun cancelRegistration(eventId: String, teamMemberId: String) {
        client.postgrest["event_registrations"].delete {
            filter {
                eq("event_id", eventId)
                eq("team_member_id", teamMemberId)
            }
        }
        // 同步刪除該成員在該賽事的投票
        deleteVotes(eventId, teamMemberId)
    }

    suspend fun submitVotes(eventId: String, teamMemberId: String, votedHoles: List<Int>) {
        val vote = NewPeoriaVote(eventId = eventId, teamMemberId = teamMemberId, votedHoles = votedHoles)
        client.postgrest["new_peoria_votes"].upsert(vote) { onConflict = "event_id, team_member_id" }
    }

    suspend fun deleteVotes(eventId: String, teamMemberId: String) {
        client.postgrest["new_peoria_votes"].delete {
            filter {
                eq("event_id", eventId)
                eq("team_member_id", teamMemberId)
            }
        }
    }

    suspend fun getMember(userId: String, teamId: String): TeamMember? {
        return client.postgrest["team_members"]
            .select {
                filter {
                    eq("user_id", userId)
                    eq("team_id", teamId)
                }
            }
            .decodeSingleOrNull<TeamMember>()
    }

    suspend fun submitScores(eventId: String, memberScores: Map<String, List<Int>>) {
        val scoreEntries = mutableListOf<Score>()
        for ((memberId, holeScores) in memberScores) {
            scoreEntries.add(Score(eventId = eventId, teamMemberId = memberId, grossScore = holeScores.sum(), holeScores = holeScores))
        }
        client.postgrest["scores"].upsert(scoreEntries) { onConflict = "event_id, team_member_id" }
    }

    suspend fun getScoresByEvent(eventId: String): List<Score> {
        return client.postgrest["scores"].select { filter { eq("event_id", eventId) } }.decodeList<Score>()
    }

    suspend fun getEventLeaderboard(eventId: String): List<EventLeaderboardEntry> {
        val event = getEventById(eventId) ?: return emptyList()
        val participants = getEventParticipants(eventId)
        val scores = getScoresByEvent(eventId).associateBy { it.teamMemberId }
        
        val entries = participants.map { m ->
            val s = scores[m.id]
            val holeScores = s?.holeScores ?: List(18) { 0 }
            val holesPlayed = holeScores.count { it > 0 }
            val isFinished = holesPlayed == 18
            
            // 計算相對於標準桿的分數 (To Par)
            var toPar = 0
            holeScores.forEachIndexed { index, score ->
                if (score > 0) {
                    toPar += (score - event.pars[index])
                }
            }
            
            EventLeaderboardEntry(
                memberId = m.id,
                realName = m.users.realName,
                holeScores = holeScores,
                grossScore = holeScores.sum(),
                toPar = toPar,
                holesPlayed = holesPlayed,
                isFinished = isFinished
            )
        }.toMutableList()

        // 如果是對抗賽，計算積分並附加到 Entry
        if (event.handicapRule == "Match_Play") {
            val memberPointsMap = mutableMapOf<String, Double>()
            val pairings = getMatchPairings(eventId)
            val groupMap = getGroupsWithMembers(eventId, includeAllSides = true).associateBy { it.group.id }
            
            pairings.forEach { p ->
                val gA = groupMap[p.groupAId]
                val gB = groupMap[p.groupBId]
                
                val membersA = gA?.members ?: emptyList()
                val membersB = gB?.members ?: emptyList()
                
                val scoresA = membersA.map { scores[it.id] }
                val scoresB = membersB.map { scores[it.id] }
                
                val res = HandicapCalculator.calculateMatchPlayPoints(
                    membersA, membersB, scoresA, scoresB, event.pars
                )
                
                membersA.forEach { memberPointsMap[it.id] = res.totalPointsA }
                membersB.forEach { memberPointsMap[it.id] = res.totalPointsB }
            }

            return entries.map { it.copy(points = memberPointsMap[it.memberId] ?: 0.0) }
                .sortedByDescending { it.points }
        }

        // 排序邏輯 (一般賽制)：
        // 1. 已完成 18 洞的排在前面，依總桿數 (grossScore) 由小到大
        // 2. 未完成 18 洞的排在後面，依相對於標準桿 (toPar) 由小到大
        return entries.sortedWith { a, b ->
            when {
                a.isFinished && b.isFinished -> a.grossScore.compareTo(b.grossScore)
                a.isFinished && !b.isFinished -> -1
                !a.isFinished && b.isFinished -> 1
                else -> a.toPar.compareTo(b.toPar)
            }
        }
    }

    suspend fun getEventPars(eventId: String): List<Int> {
        val event = client.postgrest["events"].select { filter { eq("id", eventId) } }.decodeSingleOrNull<Event>()
        return event?.pars ?: List(18) { 4 }
    }

    suspend fun getEventsWithDetails(teamId: String, userId: String): List<EventWithDetails> {
        val member = getMember(userId, teamId)
        val events = client.postgrest["events"].select {
            filter { eq("team_id", teamId); eq("is_archived_in_list", false) }
        }.decodeList<Event>()
        
        if (events.isEmpty()) return emptyList()

        // 排序邏輯：
        // 1. 已結束 (closed) 排前面，日期由新到舊 (DESC)
        // 2. 未結束 (open/started) 排後面，日期由舊到新 (ASC - 即將到來的在前面)
        val sortedEvents = events.sortedWith { a, b ->
            val statusA = a.registrationStatus == "closed"
            val statusB = b.registrationStatus == "closed"
            
            if (statusA != statusB) {
                // 不同狀態時，closed (true) 排在前面
                if (statusA) -1 else 1
            } else {
                // 相同狀態時
                if (statusA) {
                    // 都是 closed，日期由新到舊 (DESC)
                    b.date.compareTo(a.date)
                } else {
                    // 都是 open/started，日期由舊到新 (ASC)
                    a.date.compareTo(b.date)
                }
            }
        }

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val filteredEvents = sortedEvents.filter { e ->
            if (e.registrationStatus == "closed") {
                try {
                    val eventDate = LocalDate.parse(e.date)
                    val daysUntil = eventDate.daysUntil(now)
                    daysUntil <= 30 // 稍微放寬預設顯示時間，由管理員手動移除
                } catch (ex: Exception) {
                    true
                }
            } else {
                true
            }
        }
        
        if (filteredEvents.isEmpty()) return emptyList()
        
        val eventIds = filteredEvents.map { it.id!! }
        val allRegs = try {
            client.postgrest["event_registrations"].select {
                filter { isIn("event_id", eventIds); eq("status", "registered") }
            }.decodeList<EventRegistration>()
        } catch (e: Exception) {
            emptyList()
        }
        
        val counts = allRegs.groupBy { it.eventId }.mapValues { it.value.size }
        val userRegEventIds = if (member != null) {
            allRegs.filter { it.teamMemberId == member.id }.map { it.eventId }.toSet()
        } else {
            emptySet()
        }

        return filteredEvents.map { e ->
            EventWithDetails(
                id = e.id!!,
                title = e.title,
                date = e.date,
                location = e.location,
                registrationStatus = e.registrationStatus,
                handicapRule = e.handicapRule,
                startTime = e.startTime,
                groupCount = e.groupCount,
                participantCount = counts[e.id] ?: 0,
                isUserRegistered = userRegEventIds.contains(e.id),
                isArchivedInList = e.isArchivedInList
            )
        }
    }

    suspend fun archiveEventFromList(eventId: String) {
        client.postgrest["events"].update({ set("is_archived_in_list", true) }) {
            filter { eq("id", eventId) }
        }
    }

    suspend fun getEventParticipants(eventId: String): List<MemberWithUser> {
        val registrations = client.postgrest["event_registrations"].select {
            filter { eq("event_id", eventId); eq("status", "registered") }
        }.decodeList<EventRegistration>()
        val memberIds = registrations.map { it.teamMemberId }
        if (memberIds.isEmpty()) return emptyList()

        val members = client.postgrest["team_members"].select(Columns.raw("*, users(*)")) {
            filter { isIn("id", memberIds) }
        }.decodeList<MemberWithUser>()

        // 獲取該賽事的所有分組與成員關聯
        val groups = client.postgrest["event_groups"].select {
            filter { eq("event_id", eventId) }
        }.decodeList<EventGroup>()
        val groupIds = groups.map { it.id!! }
        
        if (groupIds.isNotEmpty()) {
            val groupMembers = client.postgrest["event_group_members"].select {
                filter { isIn("group_id", groupIds) }
            }.decodeList<EventGroupMember>()

            val groupMap = groups.associateBy { it.id }
            val memberToGroupMap = groupMembers.associate { it.teamMemberId to groupMap[it.groupId] }
            val pairings = getMatchPairings(eventId)

            members.forEach { m ->
                val group = memberToGroupMap[m.id]
                m.groupNumber = group?.groupNumber
                m.groupSide = group?.side
                
                // 處理配對資訊
                if (group != null) {
                    val pairing = pairings.find { it.groupAId == group.id!! || it.groupBId == group.id!! }
                    if (pairing != null) {
                        val otherGroupId = if (group.side == "A") pairing.groupBId else pairing.groupAId
                        val otherGroup = groups.find { it.id == otherGroupId }
                        if (otherGroup != null) {
                            m.pairedGroupName = "${otherGroup.side}隊-第 ${otherGroup.groupNumber} 組"
                        }
                    }
                }
            }
        }

        // 排序：依陣營 (A->B->null)，再依組號
        return members.sortedWith(compareBy({ it.groupSide ?: "Z" }, { it.groupNumber ?: Int.MAX_VALUE }, { it.users.realName }))
    }

    suspend fun registerGuest(eventId: String, sponsorMemberId: String, guestName: String, handicap: Double) {
        val event = client.postgrest["events"].select { filter { eq("id", eventId) } }.decodeSingle<Event>()
        val teamId = event.teamId
        val team = client.postgrest["teams"].select { filter { eq("id", teamId) } }.decodeSingle<Team>()

        // 1. 檢查該球隊是否已經有同名的來賓
        val existingGuestMembers = client.postgrest["team_members"].select(Columns.raw("*, users(*)")) {
            filter { 
                eq("team_id", teamId)
                eq("role", "guest")
            }
        }.decodeList<MemberWithUser>()
        
        val foundGuest = existingGuestMembers.find { it.users.realName == guestName }
        
        val finalMemberId = if (foundGuest != null) {
            // 已存在同名來賓，直接使用
            foundGuest.id
        } else {
            // 不存在，檢查人數上限後建立新資料
            val currentMemberCount = client.postgrest["team_members"]
                .select {
                    filter { eq("team_id", teamId) }
                }.decodeList<TeamMember>().size

            if (currentMemberCount >= team.memberLimit) {
                throw Exception("該球隊人數已達上限 (${team.memberLimit}人)，無法新增來賓。")
            }

            // 建立來賓 User
            val guestUser = User(
                lineUid = "GUEST_${Clock.System.now().toEpochMilliseconds()}", 
                realName = guestName, 
                initialHandicap = handicap
            )
            val dbGuestUser = client.postgrest["users"].insert(guestUser) { select() }.decodeSingle<User>()
            
            // 建立來賓 TeamMember
            val teamMember = TeamMember(
                teamId = teamId, 
                userId = dbGuestUser.id!!, 
                role = "guest", 
                currentTeamHandicap = handicap, 
                sponsorMemberId = sponsorMemberId
            )
            val dbGuestMember = client.postgrest["team_members"].insert(teamMember) { select() }.decodeSingle<TeamMember>()
            dbGuestMember.id!!
        }
        
        // 2. 執行報名
        registerForEvent(eventId, finalMemberId)
    }

    suspend fun getLeaderboard(teamId: String): List<MemberWithUser> {
        val members = client.postgrest["team_members"].select(Columns.raw("*, users(*)")) {
            filter { eq("team_id", teamId) }
        }.decodeList<MemberWithUser>()

        // 排序邏輯：
        // 1. 正式成員 (admin, member) 在前，來賓 (guest) 在後
        // 2. 各群組內依差點由小到大排序
        return members.sortedWith(
            compareBy<MemberWithUser> { it.role == "guest" }
                .thenBy { it.handicap }
        )
    }

    suspend fun getUserGroupId(userId: String, eventId: String): String? {
        val event = client.postgrest["events"].select { filter { eq("id", eventId) } }.decodeSingleOrNull<Event>() ?: return null
        val member = getMember(userId, event.teamId) ?: return null
        val groupMemberships = client.postgrest["event_group_members"].select { filter { eq("team_member_id", member.id!!) } }.decodeList<EventGroupMember>()
        if (groupMemberships.isEmpty()) return null
        val groupIds = groupMemberships.map { it.groupId }
        val group = client.postgrest["event_groups"].select { filter { eq("event_id", eventId); isIn("id", groupIds) } }.decodeSingleOrNull<EventGroup>()
        return group?.id
    }

    suspend fun getGroupMembers(groupId: String): List<MemberWithUser> {
        val groupMembers = client.postgrest["event_group_members"].select { filter { eq("group_id", groupId) } }.decodeList<EventGroupMember>()
        val memberIds = groupMembers.map { it.teamMemberId }
        if (memberIds.isEmpty()) return emptyList()
        return client.postgrest["team_members"].select(Columns.raw("*, users(*)")) { filter { isIn("id", memberIds) } }.decodeList<MemberWithUser>()
    }

    suspend fun deleteTeamMember(memberId: String) {
        client.postgrest["team_members"].delete { filter { eq("id", memberId) } }
    }

    suspend fun updateMemberHandicap(memberId: String, newHandicap: Double) {
        client.postgrest["team_members"].update({ set("current_team_handicap", newHandicap) }) { filter { eq("id", memberId) } }
    }

    suspend fun updateMemberRole(memberId: String, newRole: String) {
        client.postgrest["team_members"].update({ set("role", newRole) }) { filter { eq("id", memberId) } }
    }

    suspend fun updateEventStatus(eventId: String, status: String) {
        client.postgrest["events"].update({ set("registration_status", status) }) { 
            filter { eq("id", eventId) } 
        }
    }

    suspend fun startEvent(eventId: String) {
        val event = getEventById(eventId) ?: throw Exception("找不到賽事資料")
        val teamId = event.teamId
        
        try {
            // 1. 檢查是否所有報名人員都已分組
            val participants = getEventParticipants(eventId)
            val unassigned = participants.filter { it.groupNumber == null }
            if (unassigned.isNotEmpty()) {
                val names = unassigned.joinToString(", ") { it.users.realName }
                val errorMsg = "參賽人員未完成分組 ($names)，請先完成分組後再開始球賽"
                logError(teamId, errorMsg)
                throw Exception(errorMsg)
            }

            // 2. 對抗賽額外處理：確保配對存在
            if (event.handicapRule == "Match_Play") {
                val count = event.groupCount ?: 0
                ensureGroupsExist(eventId, count, "A")
                ensureGroupsExist(eventId, count, "B")
                
                val existing = getMatchPairings(eventId)
                if (existing.isEmpty()) {
                    pairGroupsSequentially(eventId)
                }
            }
            
            updateEventStatus(eventId, "started")
        } catch (e: Exception) {
            logError(teamId, "開始球賽失敗: ${e.message}")
            throw e
        }
    }

    private suspend fun ensureGroupsExist(eventId: String, count: Int, side: String?) {
        val existing = client.postgrest["event_groups"].select {
            filter { eq("event_id", eventId); if (side != null) eq("side", side) else eq("side", "null") }
        }.decodeList<EventGroup>()
        
        val existingNums = existing.map { it.groupNumber }.toSet()
        (1..count).forEach { num ->
            if (num !in existingNums) {
                createEventGroup(eventId, num, side)
            }
        }
    }

    private suspend fun pairGroupsSequentially(eventId: String) {
        val allGroups = client.postgrest["event_groups"].select {
            filter { eq("event_id", eventId) }
        }.decodeList<EventGroup>()

        val groupIds = allGroups.mapNotNull { it.id }
        if (groupIds.isEmpty()) return

        val allGroupMembers = client.postgrest["event_group_members"].select {
            filter { isIn("group_id", groupIds) }
        }.decodeList<EventGroupMember>()
        val activeGroupIds = allGroupMembers.map { it.groupId }.toSet()

        val sideA = allGroups.filter { it.side == "A" && it.id in activeGroupIds }.sortedBy { it.groupNumber }
        val sideB = allGroups.filter { it.side == "B" && it.id in activeGroupIds }.sortedBy { it.groupNumber }

        val pairCount = kotlin.math.min(sideA.size, sideB.size)
        val pairings = mutableListOf<MatchPairing>()
        for (i in 0 until pairCount) {
            pairings.add(
                MatchPairing(
                    eventId = eventId,
                    groupAId = sideA[i].id!!,
                    groupBId = sideB[i].id!!
                )
            )
        }

        if (pairings.isNotEmpty()) {
            client.postgrest["match_pairings"].insert(pairings)
        }
    }

    suspend fun finishEvent(eventId: String): HandicapCalculator.HiddenHolesResult? {
        val event = getEventById(eventId) ?: throw Exception("Event not found")
        
        var result: HandicapCalculator.HiddenHolesResult? = null
        
        // 如果是新新貝利亞賽制，結算隱藏洞
        if (event.handicapRule == "New_New_Peoria") {
            val allVotes = client.postgrest["new_peoria_votes"].select {
                filter { eq("event_id", eventId) }
            }.decodeList<NewPeoriaVote>()
            
            val holeVotes = mutableMapOf<Int, Int>()
            // 初始化 1-18 洞
            (1..18).forEach { holeVotes[it] = 0 }
            
            allVotes.forEach { vote ->
                vote.votedHoles.forEach { hole ->
                    holeVotes[hole] = (holeVotes[hole] ?: 0) + 1
                }
            }
            
            result = HandicapCalculator.determineNewNewPeoriaHiddenHoles(holeVotes)
            
            // 將選出的隱藏洞寫回 Event 紀錄中
            client.postgrest["events"].update({ set("hidden_holes", result.hiddenHoles) }) {
                filter { eq("id", eventId) }
            }
        }
        
        // 將狀態改為已結束 (closed)
        updateEventStatus(eventId, "closed")
        return result
    }

    suspend fun batchUpdateMemberHandicaps(eventId: String, handicapMap: Map<String, Double>) {
        for ((memberId, newHandicap) in handicapMap) {
            updateMemberHandicap(memberId, newHandicap)
        }
        // 標記賽事為已結算
        client.postgrest["events"].update({ set("is_settled", true) }) {
            filter { eq("id", eventId) }
        }
    }

    suspend fun deleteEvent(eventId: String) {
        client.postgrest["events"].delete { filter { eq("id", eventId) } }
    }

    suspend fun getFullPersonalHistory(userId: String, teamId: String): PersonalHistory {
        val member = getMember(userId, teamId) ?: throw Exception("Not a member")
        val scoresWithEvents = client.postgrest["scores"].select(Columns.raw("*, events(*)")) { 
            filter { eq("team_member_id", member.id!!) }
        }.decodeList<ScoreWithEvent>()
        var b = 0; var e = 0; var p = 0
        val entries = scoresWithEvents.map { s ->
            val ev = s.events
            s.holeScores.forEachIndexed { i, sc -> if (sc > 0) { val pr = ev.pars[i]; when { sc == pr - 1 -> b++; sc <= pr - 2 -> e++; sc == pr -> p++ } } }
            HistoryEntry(
                eventTitle = ev.title, 
                date = ev.date, 
                grossScore = s.grossScore, 
                holeScores = s.holeScores, 
                pars = ev.pars, 
                startTime = ev.startTime,
                netScore = s.netScore,
                appliedHandicap = s.appliedHandicap
            )
        }
        return PersonalHistory(b, e, p, entries.sortedWith(compareByDescending<HistoryEntry> { it.date }.thenByDescending { it.startTime }))
    }

    suspend fun getSuperAdminDashboard(): List<TeamStats> {
        // 使用資源嵌套一次拉取球隊與其成員資訊 (只抓角色與姓名)
        val teamsWithMembers = client.postgrest["teams"]
            .select(Columns.raw("*, team_members(role, users(real_name))"))
            .decodeList<TeamWithEmbeddedMembers>()

        return teamsWithMembers.map { item ->
            val team = item.team
            val members = item.members
            val admin = members.find { MemberRoles.isAdmin(it.role) }
            
            TeamStats(
                teamId = team.id!!,
                teamName = team.name,
                joinCode = team.joinCode,
                memberCount = members.size,
                adminName = admin?.users?.realName ?: "尚未綁定",
                status = team.subscriptionStatus,
                subscriptionType = team.subscriptionType,
                memberLimit = team.memberLimit
            )
        }
    }

    suspend fun updateTeamSubscription(teamId: String, type: String, limit: Int) {
        client.postgrest["teams"].update({
            set("subscription_type", type)
            set("member_limit", limit)
        }) {
            filter { eq("id", teamId) }
        }
    }

    suspend fun createTeamBySuperAdmin(name: String, joinCode: String, type: String = "free", limit: Int = 30): Team {
        return client.postgrest["teams"].insert(
            Team(name = name, joinCode = joinCode, subscriptionStatus = "active", subscriptionType = type, memberLimit = limit)
        ) { select() }.decodeSingle<Team>()
    }

    suspend fun logError(teamId: String?, message: String) {
        try {
            // 優化：直接存入 ID，不再進行名稱 select 查詢以節省效能
            client.postgrest["error_logs"].insert(AppErrorLog(teamId = teamId, message = message))
        } catch (e: Exception) {
            println("Failed to log error: ${e.message}")
        }
    }

    suspend fun getErrorLogs(): List<AppErrorLog> {
        return client.postgrest["error_logs"].select {
            order("created_at", Order.DESCENDING)
            limit(100)
        }.decodeList<AppErrorLog>()
    }

    suspend fun deleteTeam(teamId: String) {
        try {
            // 1. 取得該球隊所有的賽事 ID，以便清理關聯資料
            val events = client.postgrest["events"].select {
                filter { eq("team_id", teamId) }
            }.decodeList<Event>()
            val eventIds = events.mapNotNull { it.id }

            if (eventIds.isNotEmpty()) {
                // 2. 清理與賽事關聯的資料
                // 刪除成績
                client.postgrest["scores"].delete { filter { isIn("event_id", eventIds) } }
                // 刪除投票
                client.postgrest["new_peoria_votes"].delete { filter { isIn("event_id", eventIds) } }
                // 刪除報名紀錄
                client.postgrest["event_registrations"].delete { filter { isIn("event_id", eventIds) } }
                // 刪除對抗賽配對
                client.postgrest["match_pairings"].delete { filter { isIn("event_id", eventIds) } }

                // 3. 清理組別相關
                val groups = client.postgrest["event_groups"].select {
                    filter { isIn("event_id", eventIds) }
                }.decodeList<EventGroup>()
                val groupIds = groups.mapNotNull { it.id }
                
                if (groupIds.isNotEmpty()) {
                    client.postgrest["event_group_members"].delete { filter { isIn("group_id", groupIds) } }
                    client.postgrest["event_groups"].delete { filter { isIn("event_id", eventIds) } }
                }

                // 4. 刪除賽事本身
                client.postgrest["events"].delete { filter { eq("team_id", teamId) } }
            }

            // 5. 清理球隊成員與 LINE 群組關聯
            client.postgrest["team_members"].delete { filter { eq("team_id", teamId) } }
            client.postgrest["line_groups"].delete { filter { eq("team_id", teamId) } }
            
            // 6. 清理球場大師中由該球隊建立的資料 (可選擇刪除或保留，這裡採刪除以符合全刪需求)
            client.postgrest["course_master"].delete { filter { eq("created_by_team_id", teamId) } }

            // 7. 最後刪除球隊主體
            client.postgrest["teams"].delete {
                filter { eq("id", teamId) }
            }
            println("Team $teamId and all associated data deleted successfully.")
        } catch (e: Exception) {
            logError(teamId, "刪除球隊失敗: ${e.message}")
            println("Error deleting team $teamId: ${e.message}")
            throw e
        }
    }

    suspend fun updateTeamStatus(teamId: String, status: String) {
        client.postgrest["teams"].update({
            set("subscription_status", status)
        }) {
            filter {
                eq("id", teamId)
            }
        }
    }
    
    suspend fun createEventGroup(eventId: String, groupNumber: Int, side: String? = null): EventGroup {
        return client.postgrest["event_groups"].insert(
            EventGroup(eventId = eventId, groupNumber = groupNumber, side = side)
        ) { select() }.decodeSingle<EventGroup>()
    }

    suspend fun deleteEventGroup(groupId: String, eventId: String) {
        // 先獲取要刪除的組別資訊，以得知其 side
        val groupToDelete = client.postgrest["event_groups"].select {
            filter { eq("id", groupId) }
        }.decodeSingleOrNull<EventGroup>()

        client.postgrest["event_groups"].delete {
            filter { eq("id", groupId) }
        }
        
        // 刪除後自動重新編號：獲取該賽事所有組別，並過濾出相同 side 的進行重新編號
        val allGroups = client.postgrest["event_groups"].select {
            filter { eq("event_id", eventId) }
        }.decodeList<EventGroup>()
        
        val remainingGroups = allGroups
            .filter { it.side == groupToDelete?.side }
            .sortedBy { it.groupNumber }
        
        remainingGroups.forEachIndexed { index, group ->
            val newNum = index + 1
            if (group.groupNumber != newNum) {
                client.postgrest["event_groups"].update(EventGroupUpdate(groupNumber = newNum)) {
                    filter { eq("id", group.id!!) }
                }
            }
        }
    }

    suspend fun assignMemberToGroup(groupId: String, teamMemberId: String) {
        client.postgrest["event_group_members"].insert(EventGroupMember(groupId = groupId, teamMemberId = teamMemberId))
    }

    suspend fun batchUpdateGroupMembers(eventId: String, side: String?, assignments: List<Pair<String, String>>) {
        // assignments is a list of (groupId, teamMemberId)
        
        // 1. Get all group IDs for this event and side
        val allGroupsForEvent = client.postgrest["event_groups"].select { 
            filter { eq("event_id", eventId) } 
        }.decodeList<EventGroup>()
        val groups = allGroupsForEvent.filter { it.side == side }
        val groupIds = groups.map { it.id!! }

        if (groupIds.isEmpty()) return

        // 2. Delete all existing members in these groups
        client.postgrest["event_group_members"].delete {
            filter { isIn("group_id", groupIds) }
        }

        // 3. Insert new assignments
        if (assignments.isNotEmpty()) {
            val newMembers = assignments.map { (groupId, teamMemberId) ->
                EventGroupMember(groupId = groupId, teamMemberId = teamMemberId)
            }
            client.postgrest["event_group_members"].insert(newMembers)
        }
    }

    suspend fun getGroupsWithMembers(eventId: String, side: String? = null, includeAllSides: Boolean = false): List<EventGroupWithMembers> {
        // 先抓取該賽事的所有組別，避免 NULL 過濾語法在不同 SDK 版本間的相容性問題
        val allGroupsForEvent = client.postgrest["event_groups"].select { 
            filter { eq("event_id", eventId) } 
        }.decodeList<EventGroup>()

        val groups = if (includeAllSides) allGroupsForEvent else allGroupsForEvent.filter { it.side == side }

        if (groups.isEmpty()) return emptyList()
        
        val groupIds = groups.map { it.id!! }
        val groupMembers = client.postgrest["event_group_members"].select {
            filter { isIn("group_id", groupIds) }
        }.decodeList<EventGroupMember>()
        
        val members = getEventParticipants(eventId)
        
        return groups.map { group ->
            val memberIdsInGroup = groupMembers.filter { it.groupId == group.id }.map { it.teamMemberId }
            EventGroupWithMembers(
                group = group,
                members = members.filter { it.id in memberIdsInGroup }
            )
        }.sortedBy { it.group.groupNumber }
    }

    suspend fun removeFromGroup(groupId: String, teamMemberId: String) {
        client.postgrest["event_group_members"].delete {
            filter {
                eq("group_id", groupId)
                eq("team_member_id", teamMemberId)
            }
        }
    }

    suspend fun pairGroupsRandomly(eventId: String) {
        val event = getEventById(eventId)
        val teamId = event?.teamId
        try {
            // 1. 安全性檢查：若該賽事已有成績輸入，則禁止重新自動配對
            val existingScores = client.postgrest["scores"].select {
                filter { eq("event_id", eventId) }
                limit(1)
            }.decodeList<Score>()
            
            if (existingScores.isNotEmpty()) {
                throw Exception("配對失敗：賽事已有成績填寫，為避免數據混亂禁止重新配對。")
            }

            // 2. 刪除現有配對
            client.postgrest["match_pairings"].delete {
                filter { eq("event_id", eventId) }
            }

            // 2. 抓取該賽事的所有組別
            val allGroups = client.postgrest["event_groups"].select {
                filter { eq("event_id", eventId) }
            }.decodeList<EventGroup>()
            
            val groupIds = allGroups.mapNotNull { it.id }
            if (groupIds.isEmpty()) return

            // 3. 找出有成員的組別 (Active Groups)
            val allGroupMembers = client.postgrest["event_group_members"].select {
                filter { isIn("group_id", groupIds) }
            }.decodeList<EventGroupMember>()
            val activeGroupIds = allGroupMembers.map { it.groupId }.toSet()
            
            // 4. 分開 A 隊與 B 隊的有員組別，並隨機打亂
            val sideA = allGroups.filter { it.side == "A" && it.id in activeGroupIds }.shuffled()
            val sideB = allGroups.filter { it.side == "B" && it.id in activeGroupIds }.shuffled()

            println("Pairing Match Play: Side A active groups = ${sideA.size}, Side B active groups = ${sideB.size}")

            // 5. 進行配對 (以數量少的隊伍為準)
            val pairCount = kotlin.math.min(sideA.size, sideB.size)
            val pairings = mutableListOf<MatchPairing>()
            for (i in 0 until pairCount) {
                pairings.add(
                    MatchPairing(
                        eventId = eventId,
                        groupAId = sideA[i].id!!,
                        groupBId = sideB[i].id!!
                    )
                )
            }

            // 6. 寫入資料庫
            if (pairings.isNotEmpty()) {
                client.postgrest["match_pairings"].insert(pairings)
                println("Successfully created $pairCount match pairings for event $eventId")
            } else {
                val errorMsg = "配對失敗：請先在 A 隊與 B 隊的分組設定中指派球員進組。"
                logError(teamId, errorMsg)
                println("No pairings created: one or both sides have no groups with members assigned.")
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            println("Error in pairGroupsRandomly: ${e.message}")
            if (e.message?.contains("配對失敗") != true) {
                logError(teamId, "自動配對發生系統異常: ${e.message}")
            }
            throw e
        }
    }

    suspend fun getMatchPairings(eventId: String): List<MatchPairing> {
        return client.postgrest["match_pairings"].select {
            filter { eq("event_id", eventId) }
        }.decodeList<MatchPairing>()
    }

    suspend fun getMatchPlayResults(eventId: String): List<HandicapCalculator.MatchPlayPairingResult> {
        val pairings = getMatchPairings(eventId)
        val allGroupsWithMembers = getGroupsWithMembers(eventId, includeAllSides = true)
        val groupMap = allGroupsWithMembers.associateBy { it.group.id }
        val scores = getScoresByEvent(eventId).associateBy { it.teamMemberId }
        val eventPars = getEventPars(eventId)
        
        return pairings.map { pairing ->
            val groupA = groupMap[pairing.groupAId]
            val groupB = groupMap[pairing.groupBId]
            
            val membersA = groupA?.members ?: emptyList()
            val membersB = groupB?.members ?: emptyList()
            
            val scoresA = membersA.map { scores[it.id] }
            val scoresB = membersB.map { scores[it.id] }
            
            HandicapCalculator.calculateMatchPlayPoints(membersA, membersB, scoresA, scoresB, eventPars).copy(
                membersA = membersA.map { it.users.realName },
                membersB = membersB.map { it.users.realName },
                groupANumber = groupA?.group?.groupNumber ?: 0,
                groupBNumber = groupB?.group?.groupNumber ?: 0
            )
        }
    }

    // --- 球場大師資料庫相關 ---
    
    suspend fun searchCourses(query: String): List<CourseMaster> {
        if (query.isBlank()) return emptyList()
        return client.postgrest["course_master"].select {
            filter { ilike("name", "%$query%") }
            limit(5)
        }.decodeList<CourseMaster>()
    }

    suspend fun saveCourseMaster(course: CourseMaster) {
        val existing = client.postgrest["course_master"].select {
            filter { eq("name", course.name) }
        }.decodeSingleOrNull<CourseMaster>()
        
        if (existing == null) {
            client.postgrest["course_master"].insert(course)
        }
    }
}

@Serializable
internal data class ScoreWithEvent(
    @SerialName("event_id") val eventId: String,
    @SerialName("team_member_id") val teamMemberId: String,
    @SerialName("gross_score") val grossScore: Int,
    @SerialName("net_score") val netScore: Double? = null,
    @SerialName("applied_handicap") val appliedHandicap: Double? = null,
    @SerialName("hole_scores") val holeScores: List<Int>,
    val events: Event
)

@Serializable
internal data class TeamWithEmbeddedMembers(
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

@Serializable
internal data class EmbeddedMember(
    val role: String,
    val users: EmbeddedUser
)

@Serializable
internal data class EmbeddedUser(
    @SerialName("real_name") val realName: String
)
