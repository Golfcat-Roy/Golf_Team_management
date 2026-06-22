package org.golfcat.team.project

import org.golfcat.team.project.models.Score
import org.golfcat.team.project.models.User
import org.golfcat.team.project.models.MemberWithUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandicapCalculatorTest {

    // --- Team Handicap (Phase 1.1) ---

    @Test
    fun testCalculateNewTeamHandicapAllTiers() {
        // Tier 1: < 10
        assertEquals(8.0, HandicapCalculator.calculateNewTeamHandicap(9.0, 1)) // Rank 1: -1.0
        assertEquals(9.0, HandicapCalculator.calculateNewTeamHandicap(9.0, 2)) // Rank 2: no deduction

        // Tier 2: 10 - 19.9
        assertEquals(13.0, HandicapCalculator.calculateNewTeamHandicap(15.0, 1)) // Rank 1: -2.0
        assertEquals(14.0, HandicapCalculator.calculateNewTeamHandicap(15.0, 2)) // Rank 2: -1.0
        assertEquals(15.0, HandicapCalculator.calculateNewTeamHandicap(15.0, 3)) // Rank 3: no deduction

        // Tier 3: 20 - 29.9
        assertEquals(22.0, HandicapCalculator.calculateNewTeamHandicap(25.0, 1)) // Rank 1: -3.0
        assertEquals(23.0, HandicapCalculator.calculateNewTeamHandicap(25.0, 2)) // Rank 2: -2.0
        assertEquals(24.0, HandicapCalculator.calculateNewTeamHandicap(25.0, 3)) // Rank 3: -1.0
        assertEquals(25.0, HandicapCalculator.calculateNewTeamHandicap(25.0, 4)) // Rank 4: no deduction

        // Tier 4: >= 30
        assertEquals(31.0, HandicapCalculator.calculateNewTeamHandicap(35.0, 1)) // Rank 1: -4.0
        assertEquals(32.0, HandicapCalculator.calculateNewTeamHandicap(35.0, 2)) // Rank 2: -3.0
        assertEquals(33.0, HandicapCalculator.calculateNewTeamHandicap(35.0, 3)) // Rank 3: -2.0
        assertEquals(35.0, HandicapCalculator.calculateNewTeamHandicap(35.0, 4)) // Rank 4: no deduction

        // Edge case: handicap becomes 0
        assertEquals(0.0, HandicapCalculator.calculateNewTeamHandicap(0.5, 1))
    }

    @Test
    fun testSettleTeamHandicapRankingAndTieBreaking() {
        val members = listOf(
            createMember("m1", "High Hcp", 30.0),
            createMember("m2", "Low Hcp", 10.0)
        )
        val scores = listOf(
            Score(eventId = "e1", teamMemberId = "m1", grossScore = 100, holeScores = emptyList()), // Net: 100 - 30 = 70
            Score(eventId = "e1", teamMemberId = "m2", grossScore = 80, holeScores = emptyList())   // Net: 80 - 10 = 70
        )
        
        // Both net scores are 70.0. Tie-break: Lower handicap wins.
        // m2 (10.0) should be Rank 1, m1 (30.0) should be Rank 2.
        val results = HandicapCalculator.settleTeamHandicap(scores, members, 72)
        
        assertEquals("m2", results[0].memberId)
        assertEquals(1, results[0].rank)
        assertEquals(8.0, results[0].newTeamHandicap) // 10.0 - 2.0 (Rank 1 in Tier 2)

        assertEquals("m1", results[1].memberId)
        assertEquals(2, results[1].rank)
        assertEquals(27.0, results[1].newTeamHandicap) // 30.0 - 3.0 (Rank 2 in Tier 4)
    }

    // --- New New Peoria (Phase 1.1) ---

    @Test
    fun testSettleNewPeoriaCalculation() {
        val members = listOf(createMember("m1", "Player", 20.0))
        val scores = listOf(
            Score(eventId = "e1", teamMemberId = "m1", grossScore = 90, holeScores = List(18) { 5 })
        )
        val hiddenHoles = listOf(1, 2, 3, 4, 5, 6)
        val totalPar = 72

        // Gross = 90
        // HiddenGross = 5 * 6 = 30
        // Formula: ((90 - 30) * 1.5 - 72) * 0.8
        // (60 * 1.5 - 72) * 0.8 = (90 - 72) * 0.8 = 18 * 0.8 = 14.4
        // Net = 90 - 14.4 = 75.6

        val results = HandicapCalculator.settleNewPeoria(scores, members, hiddenHoles, totalPar)
        
        assertEquals(14.4, results[0].appliedHandicap, 0.001)
        assertEquals(75.6, results[0].netScore, 0.001)
    }

    @Test
    fun testDetermineNewNewPeoriaHiddenHolesWithTies() {
        // 10 holes with votes, some ties
        val votes = mapOf(
            1 to 10, 2 to 10, 3 to 10, 4 to 10, 5 to 10, // Top 5
            6 to 5, 7 to 5, 8 to 5 // Tie for 6th spot
        )
        
        val result = HandicapCalculator.determineNewNewPeoriaHiddenHoles(votes)
        
        assertEquals(6, result.hiddenHoles.size)
        assertTrue(result.hiddenHoles.containsAll(listOf(1, 2, 3, 4, 5)))
        // 6th hole should be one of (6, 7, 8)
        val sixthHole = result.hiddenHoles.last { it > 5 }
        assertTrue(listOf(6, 7, 8).contains(sixthHole))
        assertTrue(result.systemMessage?.contains("系統自動隨機抽選") == true)
    }

    // --- Match Play (Phase 1.2) ---

    @Test
    fun testMatchPlayBestBallAndBonuses() {
        val pars = List(18) { 4 }
        
        val teamA = listOf(createMember("a1", "A1", 10.0), createMember("a2", "A2", 10.0))
        val teamB = listOf(createMember("b1", "B1", 10.0), createMember("b2", "B2", 10.0))
        
        // Hole 1: A1=3 (Birdie), A2=5, B1=4, B2=4
        // A wins hole (1.0). A1 gets Birdie bonus (0.5). Total A: 1.5, B: 0.0
        val scoresA = listOf(
            Score(eventId = "e1", teamMemberId = "a1", grossScore = 72, holeScores = listOf(3) + List(17) { 4 }),
            Score(eventId = "e1", teamMemberId = "a2", grossScore = 72, holeScores = listOf(5) + List(17) { 4 })
        )
        val scoresB = listOf(
            Score(eventId = "e1", teamMemberId = "b1", grossScore = 72, holeScores = listOf(4) + List(17) { 4 }),
            Score(eventId = "e1", teamMemberId = "b2", grossScore = 72, holeScores = listOf(4) + List(17) { 4 })
        )
        
        val result = HandicapCalculator.calculateMatchPlayPoints(teamA, teamB, scoresA, scoresB, pars)
        
        assertEquals(1.5, result.holeResults[0].pointsA)
        assertEquals(0.0, result.holeResults[0].pointsB)
        assertEquals("A", result.holeResults[0].winnerSide)
        assertEquals(3, result.bestScoresA[0])
        assertEquals(4, result.bestScoresB[0])

        // Hole 2: A1=6, A2=6, B1=2 (Eagle), B2=6
        // B wins hole (1.0). B1 gets Eagle bonus (1.0). Total B: 2.0
        val scoresA2 = listOf(
            Score(eventId = "e1", teamMemberId = "a1", grossScore = 72, holeScores = listOf(4, 6) + List(16) { 4 }),
            Score(eventId = "e1", teamMemberId = "a2", grossScore = 72, holeScores = listOf(4, 6) + List(16) { 4 })
        )
        val scoresB2 = listOf(
            Score(eventId = "e1", teamMemberId = "b1", grossScore = 72, holeScores = listOf(4, 2) + List(16) { 4 }),
            Score(eventId = "e1", teamMemberId = "b2", grossScore = 72, holeScores = listOf(4, 6) + List(16) { 4 })
        )

        val result2 = HandicapCalculator.calculateMatchPlayPoints(teamA, teamB, scoresA2, scoresB2, pars)
        assertEquals(0.0, result2.holeResults[1].pointsA)
        assertEquals(2.0, result2.holeResults[1].pointsB)
        assertEquals("B", result2.holeResults[1].winnerSide)
    }

    @Test
    fun testMatchPlayHoleInOneBonus() {
        val pars = List(18) { 4 }
        val teamA = listOf(createMember("a1", "Pro", 0.0))
        val teamB = listOf(createMember("b1", "Noob", 36.0))
        
        // Hole 1: A1=1 (HIO) -> Points: 1 (win) + 1 (Eagle/HIO bonus) = 2.0
        val scoresA = listOf(Score(eventId = "e1", teamMemberId = "a1", grossScore = 72, holeScores = listOf(1) + List(17) { 4 }))
        val scoresB = listOf(Score(eventId = "e1", teamMemberId = "b1", grossScore = 72, holeScores = listOf(4) + List(17) { 4 }))

        val result = HandicapCalculator.calculateMatchPlayPoints(teamA, teamB, scoresA, scoresB, pars)
        assertEquals(2.0, result.holeResults[0].pointsA)
    }

    // --- Helper Methods ---
    private fun createMember(id: String, name: String, handicap: Double): MemberWithUser {
        return MemberWithUser(
            id = id,
            teamId = "t1",
            handicap = handicap,
            role = "member",
            users = User(id = "u_$id", lineUid = "l_$id", realName = name)
        )
    }
}
