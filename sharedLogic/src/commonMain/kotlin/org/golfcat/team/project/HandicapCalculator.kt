package org.golfcat.team.project

import org.golfcat.team.project.models.Score
import org.golfcat.team.project.models.MemberWithUser
import kotlin.math.max

/**
 * 差點與成績計算邏輯
 */
object HandicapCalculator {

    /**
     * 1. 球隊內部差點調整公式 (Team Handicap)
     * 計算調整後的新差點
     * @param currentHandicap 賽前差點
     * @param rank 淨桿排名
     * @returns 調整後的新差點 (最低為 0)
     */
    fun calculateNewTeamHandicap(currentHandicap: Double, rank: Int): Double {
        var deduction = 0.0

        if (currentHandicap < 10.0) {
            // Tier 1: < 10
            if (rank == 1) deduction = 1.0
        } else if (currentHandicap >= 10.0 && currentHandicap < 20.0) {
            // Tier 2: 10 - 19.9
            if (rank == 1) deduction = 2.0
            else if (rank == 2) deduction = 1.0
        } else if (currentHandicap >= 20.0 && currentHandicap < 30.0) {
            // Tier 3: 20 - 29.9
            if (rank == 1) deduction = 3.0
            else if (rank == 2) deduction = 2.0
            else if (rank == 3) deduction = 1.0
        } else if (currentHandicap >= 30.0) {
            // Tier 4: >= 30
            if (rank == 1) deduction = 4.0
            else if (rank == 2) deduction = 3.0
            else if (rank == 3) deduction = 2.0
        }

        return max(currentHandicap - deduction, 0.0)
    }

    /**
     * 結算球賽成績與排名 (Team Handicap 模式)
     */
    fun settleTeamHandicap(
        scores: List<Score>, 
        members: List<MemberWithUser>,
        totalPar: Int = 72
    ): List<SettledScore> {
        val memberMap = members.associateBy { it.id }
        
        // 1. 計算淨桿
        val scoredPlayers = scores.mapNotNull { s ->
            val member = memberMap[s.teamMemberId] ?: return@mapNotNull null
            SettledScore(
                memberId = s.teamMemberId,
                realName = member.users.realName,
                grossScore = s.grossScore,
                holeScores = s.holeScores,
                toPar = s.grossScore - totalPar,
                currentTeamHandicap = member.handicap,
                netScore = s.grossScore - member.handicap
            )
        }.toMutableList()

        // 2. 依淨桿由低到高排序。若平手，由賽前差點較低者勝出。
        scoredPlayers.sortWith(compareBy({ it.netScore }, { it.currentTeamHandicap }))

        // 3. 分配排名並計算下次新差點
        return scoredPlayers.mapIndexed { index, sp ->
            val rank = index + 1
            sp.copy(
                rank = rank,
                newTeamHandicap = calculateNewTeamHandicap(sp.currentTeamHandicap, rank)
            )
        }
    }

    /**
     * 2. 新新貝利亞隱藏洞抽選 (New New Peoria Selection)
     * @param holeVotes Map<洞數(1-18), 得票數>
     * @returns 最終選出的 6 個隱藏洞與提示訊息
     */
    fun determineNewNewPeoriaHiddenHoles(holeVotes: Map<Int, Int>): HiddenHolesResult {
        // 將 Map 轉換為 { hole, votes } 的列表
        val voteList = holeVotes.entries.map { VoteItem(it.key, it.value) }
            .sortedByDescending { it.votes }

        val selectedHoles = mutableListOf<Int>()
        var systemMessage: String? = null

        var currentIdx = 0
        while (selectedHoles.size < 6 && currentIdx < voteList.size) {
            val currentVoteCount = voteList[currentIdx].votes
            
            // 找出所有與當前同票數的洞
            val tiedHoles = voteList.filter { it.votes == currentVoteCount }.map { it.hole }
            val neededHoles = 6 - selectedHoles.size

            if (tiedHoles.size <= neededHoles) {
                // 缺額足夠，全部納入
                selectedHoles.addAll(tiedHoles)
                currentIdx += tiedHoles.size
            } else {
                // 缺額不足，進入平手隨機抽選階段
                val randomlyChosen = tiedHoles.shuffled().take(neededHoles)
                selectedHoles.addAll(randomlyChosen)
                
                systemMessage = "因第 ${tiedHoles.joinToString(", ")} 洞得票數相同，第 ${randomlyChosen.joinToString(", ")} 洞為系統自動隨機抽選結果"
                break
            }
        }

        return HiddenHolesResult(selectedHoles.sorted(), systemMessage)
    }

    /**
     * 3. 結算新新貝利亞成績與排名
     */
    fun settleNewPeoria(
        scores: List<Score>,
        members: List<MemberWithUser>,
        hiddenHoles: List<Int>, // 1-based indices
        totalPar: Int
    ): List<SettledScore> {
        val memberMap = members.associateBy { it.id }
        
        val scoredPlayers = scores.mapNotNull { s ->
            val member = memberMap[s.teamMemberId] ?: return@mapNotNull null
            
            // 計算隱藏洞總桿數
            val hiddenGross = hiddenHoles.sumOf { h -> s.holeScores.getOrNull(h - 1) ?: 0 }
            
            // 新新貝利亞公式：((總桿 - 6洞隱藏洞總和) * 1.5 - 標準桿) * 0.8
            val calculatedHandicap = ((s.grossScore - hiddenGross) * 1.5 - totalPar) * 0.8
            
            SettledScore(
                memberId = s.teamMemberId,
                realName = member.users.realName,
                grossScore = s.grossScore,
                holeScores = s.holeScores,
                toPar = s.grossScore - totalPar,
                currentTeamHandicap = member.handicap,
                appliedHandicap = calculatedHandicap,
                netScore = s.grossScore - calculatedHandicap
            )
        }.toMutableList()

        // 依淨桿由低到高排序
        scoredPlayers.sortWith(compareBy({ it.netScore }, { it.currentTeamHandicap }))

        return scoredPlayers.mapIndexed { index, sp ->
            sp.copy(rank = index + 1)
        }
    }

    data class SettledScore(
        val memberId: String,
        val realName: String,
        val grossScore: Int,
        val holeScores: List<Int> = emptyList(),
        val toPar: Int = 0,
        val currentTeamHandicap: Double,
        val appliedHandicap: Double = 0.0, // 新新貝利亞算出的臨時差點
        val netScore: Double,
        val rank: Int = 0,
        val newTeamHandicap: Double = 0.0 // 下次球隊差點
    )

    /**
     * 4. 結算分組對抗賽 (Match Play) 積分
     */
    fun calculateMatchPlayPoints(
        membersA: List<MemberWithUser>,
        membersB: List<MemberWithUser>,
        scoresA: List<Score?>,
        scoresB: List<Score?>,
        eventPars: List<Int>
    ): MatchPlayPairingResult {
        var groupAPoints = 0.0
        var groupBPoints = 0.0
        
        val holeDetails = mutableListOf<MatchPlayHoleResult>()
        val bestScoresA = mutableListOf<Int>()
        val bestScoresB = mutableListOf<Int>()

        for (i in 0 until 18) {
            val par = eventPars[i]
            
            // 找出 A 組與 B 組在該洞的最佳成績 (排除 0)
            val bestA = scoresA.mapNotNull { it?.holeScores?.getOrNull(i) }.filter { it > 0 }.let { if (it.isEmpty()) 0 else it.min() }
            val bestB = scoresB.mapNotNull { it?.holeScores?.getOrNull(i) }.filter { it > 0 }.let { if (it.isEmpty()) 0 else it.min() }
            
            bestScoresA.add(bestA)
            bestScoresB.add(bestB)

            var holeWinSide: String? = null
            var pointsA = 0.0
            var pointsB = 0.0

            if (bestA > 0 && bestB > 0) {
                if (bestA < bestB) {
                    pointsA = 1.0
                    holeWinSide = "A"
                } else if (bestB < bestA) {
                    pointsB = 1.0
                    holeWinSide = "B"
                }
            } else if (bestA > 0) {
                pointsA = 1.0
                holeWinSide = "A"
            } else if (bestB > 0) {
                pointsB = 1.0
                holeWinSide = "B"
            }

            // 計算個人成就加分
            val bonuses = mutableListOf<Pair<String, Double>>() // Member Name to Points
            
            fun checkBonus(member: MemberWithUser, score: Int?) {
                if (score == null || score <= 0) return
                if (score == 1) { // Hole in one is technically better than eagle
                    bonuses.add(member.users.realName to 1.0)
                } else if (score <= par - 2) {
                    bonuses.add(member.users.realName to 1.0)
                } else if (score == par - 1) {
                    bonuses.add(member.users.realName to 0.5)
                }
            }

            membersA.forEachIndexed { idx, m -> 
                val score = scoresA.getOrNull(idx)?.holeScores?.getOrNull(i)
                checkBonus(m, score)
            }
            membersB.forEachIndexed { idx, m -> 
                val score = scoresB.getOrNull(idx)?.holeScores?.getOrNull(i)
                checkBonus(m, score)
            }

            // 加總成就獎金到各組
            bonuses.forEach { (name, pts) ->
                if (membersA.any { it.users.realName == name }) pointsA = pointsA + pts
                else pointsB = pointsB + pts
            }

            groupAPoints += pointsA
            groupBPoints += pointsB

            holeDetails.add(MatchPlayHoleResult(i + 1, holeWinSide, bonuses, pointsA, pointsB))
        }

        return MatchPlayPairingResult(groupAPoints, groupBPoints, holeDetails, bestScoresA = bestScoresA, bestScoresB = bestScoresB)
    }

    data class MatchPlayPairingResult(
        val totalPointsA: Double,
        val totalPointsB: Double,
        val holeResults: List<MatchPlayHoleResult>,
        val membersA: List<String> = emptyList(), // Real names
        val membersB: List<String> = emptyList(),
        val groupANumber: Int = 0,
        val groupBNumber: Int = 0,
        val bestScoresA: List<Int> = emptyList(),
        val bestScoresB: List<Int> = emptyList()
    )

    data class MatchPlayHoleResult(
        val holeNumber: Int,
        val winnerSide: String?, // "A", "B" or null
        val individualBonuses: List<Pair<String, Double>>,
        val pointsA: Double = 0.0,
        val pointsB: Double = 0.0
    )

    data class VoteItem(val hole: Int, val votes: Int)
    data class HiddenHolesResult(val hiddenHoles: List<Int>, val systemMessage: String?)
}
