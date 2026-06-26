package org.golfcat.team.project.models

import kotlin.math.roundToInt

object HandicapCalculator {
    data class SettledScore(
        val memberId: String,
        val grossScore: Int,
        val netScore: Double,
        val appliedHandicap: Double,
        val holeScores: List<Int>
    )

    fun calculateNewNewPeoria(
        holeScores: List<Int>,
        pars: List<Int>,
        hiddenHoles: List<Int>
    ): Double {
        if (holeScores.size != 18 || pars.size != 18) return 0.0
        
        // 💡 Calculate sum of hidden holes
        var hiddenSum = 0
        hiddenHoles.forEach { index ->
            if (index in 0 until 18) {
                // Limit score to Par + 3 for calculation stability (common rule)
                val score = holeScores[index]
                val par = pars[index]
                hiddenSum += if (score > par + 3) par + 3 else score
            }
        }
        
        // 💡 Formula: (HiddenSum * 1.5 - TotalPar) * 0.8
        // Using common variant: (Sum of 12 holes * 1.5 - 72) * 0.8
        val handicap = (hiddenSum * 1.5 - 72) * 0.8
        return (handicap * 10).roundToInt() / 10.0
    }

    fun getGrossScore(holeScores: List<Int>): Int = holeScores.sum()

    fun getToPar(holeScores: List<Int>, pars: List<Int>): Int {
        var diff = 0
        holeScores.forEachIndexed { index, score ->
            if (score > 0 && index < pars.size) {
                diff += (score - pars[index])
            }
        }
        return diff
    }
}
