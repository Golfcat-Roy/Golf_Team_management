package org.golfcat.team.project

import org.golfcat.team.project.models.*
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.random.Random

class HandicapMonteCarloTest {

    @Test
    fun testMonteCarloHandicapLogic() {
        val iterations = 10000
        var totalFailures = 0
        
        println("Starting Monte Carlo Simulation: $iterations iterations...")

        repeat(iterations) { i ->
            try {
                runSingleSimulation()
            } catch (e: Exception) {
                println("Iteration $i failed: ${e.message}")
                totalFailures++
            }
        }

        println("Simulation completed. Total Failures: $totalFailures")
        assertTrue(totalFailures == 0, "Handicap logic should never crash")
    }

    private fun runSingleSimulation() {
        val playerCount = Random.nextInt(4, 40)
        val pars = List(18) { 4 }
        val totalPar = pars.sum()
        
        val participants = mutableListOf<MemberWithUser>()
        val scores = mutableListOf<Score>()

        repeat(playerCount) { id ->
            val memberId = "m_$id"
            val initialHcp = Random.nextDouble(0.0, 36.0)
            
            participants.add(
                MemberWithUser(
                    id = memberId,
                    teamId = "t1",
                    handicap = initialHcp,
                    role = "member",
                    users = User(id = "u_$id", lineUid = "l_$id", realName = "Player $id")
                )
            )

            val holeScores = List(18) { Random.nextInt(1, 10) }
            scores.add(
                Score(
                    eventId = "e1",
                    teamMemberId = memberId,
                    grossScore = holeScores.sum(),
                    holeScores = holeScores
                )
            )
        }

        // 1. 測試球隊差點結算
        val teamResults = HandicapCalculator.settleTeamHandicap(scores, participants, totalPar)
        
        // 驗證排名
        for (j in 0 until teamResults.size - 1) {
            assertTrue(teamResults[j].netScore <= teamResults[j+1].netScore, "Rank ${j+1} net score should be <= Rank ${j+2}")
        }

        // 2. 測試新新貝利亞
        val hiddenHoles = (1..18).shuffled().take(6)
        val peoriaResults = HandicapCalculator.settleNewPeoria(scores, participants, hiddenHoles, totalPar)
        
        // 驗證新新貝利亞淨桿合理性 (不應低於標準桿過多，設定 -40 為極限，因為 6 洞隱藏洞全拿 1 桿時差點會非常大)
        peoriaResults.forEach { 
            assertTrue(it.netScore > (totalPar - 40), "Peoria net score too low: ${it.netScore}")
        }
    }
}
